package com.example.backend.modules.projects.acc.service;

import IhmMCD2.*;
import Merise2.Entite2;
import Merise2.Attribut2;
import Merise2.Relation2;
import Outil.ConfigSave;
import Output.SQLSave;
import com.example.backend.modules.analysis.exporter.MistralService;
import com.example.backend.modules.analysis.parser.BpmnParserStrategy;
import com.example.backend.modules.analytics.dao.LogExecutionRepository;
import com.example.backend.modules.analytics.entity.LogExecution;
import com.example.backend.modules.projects.acc.dao.*;
import com.example.backend.modules.projects.acc.dto.*;
import com.example.backend.modules.projects.acc.entity.*;
import com.example.backend.modules.projects.acc.mapper.SupportProjectMapper;
import com.example.backend.modules.projects.core.dao.ProjectRepository;
import com.example.backend.modules.projects.core.exception.ProjectNotFoundException;
import com.example.backend.modules.projects.core.exception.UnauthorizedAccessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
public class SupportFeatureService {

    private final ActorRepository actorRepository;
    private final UserStoryRepository userStoryRepository;
    private final ProjectRepository projectRepository;
    private final SupportProjectMapper supportProjectMapper;
    private final DictionaryEntryRepository dictionaryEntryRepository;
    private final DictionaryAttributeRepository dictionaryAttributeRepository;
    private final MistralService mistralService;
    private final DictionaryAssociationRepository associationRepository;
    private final BusinessRuleRepository businessRuleRepository;
    private final LogExecutionRepository logExecutionRepository;

    public SupportFeatureService(ActorRepository actorRepository,
                                 UserStoryRepository userStoryRepository,
                                 ProjectRepository projectRepository,
                                 SupportProjectMapper supportProjectMapper,
                                 DictionaryEntryRepository dictionaryEntryRepository,
                                 DictionaryAttributeRepository dictionaryAttributeRepository,
                                 MistralService mistralService,
                                 DictionaryAssociationRepository associationRepository,
                                 BusinessRuleRepository businessRuleRepository,
                                 LogExecutionRepository logExecutionRepository) {
        this.actorRepository = actorRepository;
        this.userStoryRepository = userStoryRepository;
        this.projectRepository = projectRepository;
        this.supportProjectMapper = supportProjectMapper;
        this.dictionaryEntryRepository = dictionaryEntryRepository;
        this.dictionaryAttributeRepository = dictionaryAttributeRepository;
        this.mistralService = mistralService;
        this.associationRepository = associationRepository;
        this.businessRuleRepository = businessRuleRepository;
        this.logExecutionRepository = logExecutionRepository;
    }

    private SupportProject getProjectAndCheckOwnership(UUID projectId) {
        String currentExternalId = SecurityContextHolder.getContext().getAuthentication().getName();
        SupportProject project = (SupportProject) projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projet introuvable"));

        if (!project.getUser().getExternalId().equals(currentExternalId)) {
            throw new UnauthorizedAccessException("Accès refusé");
        }
        return project;
    }

    // --- Acteurs ---
    @Transactional
    public ActorResponseDTO addActor(UUID projectId, String name) {
        SupportProject project = getProjectAndCheckOwnership(projectId);
        Actor actor = new Actor();
        actor.setName(name);
        actor.setProject(project);
        Actor savedActor = actorRepository.save(actor);
        updateProjectActivity(project);
        return supportProjectMapper.toActorDTO(savedActor);
    }

    @Transactional
    public ActorResponseDTO updateActor(UUID actorId, String newName) {
        Actor actor = actorRepository.findById(actorId).orElseThrow(() -> new EntityNotFoundException());
        SupportProject project = getProjectAndCheckOwnership(actor.getProject().getIdProject());
        actor.setName(newName);
        Actor savedActor = actorRepository.save(actor);
        updateProjectActivity(project);
        return supportProjectMapper.toActorDTO(savedActor);
    }

    @Transactional
    public void deleteActor(UUID actorId) {
        Actor actor = actorRepository.findById(actorId).orElseThrow(() -> new EntityNotFoundException());
        SupportProject project = getProjectAndCheckOwnership(actor.getProject().getIdProject());
        userStoryRepository.deleteByActorId(actorId);
        actorRepository.deleteById(actorId);
        updateProjectActivity(project);
    }

    // --- User Stories ---
    @Transactional
    public UserStoryResponseDTO addUserStory(UUID projectId, UUID actorId, String desc, String benefit, String crit) {
        SupportProject project = getProjectAndCheckOwnership(projectId);
        Actor actor = actorRepository.findById(actorId).orElseThrow(() -> new EntityNotFoundException());
        if (actor.getProject() == null || !actor.getProject().getIdProject().equals(projectId)) {
            throw new UnauthorizedAccessException("L'acteur spécifié n'appartient pas à ce projet.");
        }
        UserStory us = new UserStory();
        us.setDescription(desc);
        us.setBenefit(benefit);
        us.setAcceptanceCriteria(crit);
        us.setActor(actor);
        us.setProject(project);
        UserStory savedUs = userStoryRepository.save(us);
        updateProjectActivity(project);
        return supportProjectMapper.toUserStoryDTO(savedUs);
    }

    @Transactional
    public UserStoryResponseDTO updateUserStory(UUID usId, String desc, String ben, String crit, UUID actorId) {
        UserStory us = userStoryRepository.findById(usId).orElseThrow(() -> new EntityNotFoundException());
        Actor actor = actorRepository.findById(actorId).orElseThrow(() -> new EntityNotFoundException());
        SupportProject project = getProjectAndCheckOwnership(us.getProject().getIdProject());

        us.setDescription(desc);
        us.setBenefit(ben);
        us.setAcceptanceCriteria(crit);
        us.setActor(actor);
        UserStory savedUs = userStoryRepository.save(us);
        updateProjectActivity(project);
        return supportProjectMapper.toUserStoryDTO(savedUs);
    }

    @Transactional
    public void deleteUserStory(UUID usId) {
        UserStory us = userStoryRepository.findById(usId).orElseThrow(() -> new EntityNotFoundException());
        SupportProject project = getProjectAndCheckOwnership(us.getProject().getIdProject());
        userStoryRepository.delete(us);
        updateProjectActivity(project);
    }

    // --- BPMN ---
    @Transactional
    public SupportProject saveBpmnDiagram(UUID projectId, String bpmnXml) {
        SupportProject project = getProjectAndCheckOwnership(projectId);
        project.setBpmnXml(bpmnXml);
        updateCoverageScoreInternal(project);
        updateProjectActivity(project);
        return project;
    }

    private void updateCoverageScoreInternal(SupportProject project) {
        List<UserStory> allStories = project.getUserStories();
        if (allStories == null || allStories.isEmpty()) {
            project.setCoverageScore(0.0);
            return;
        }
        Set<String> linkedUsIds = BpmnParserStrategy.extractLinkedUserStories(project.getBpmnXml());
        long covered = allStories.stream().filter(us -> linkedUsIds.contains(us.getId().toString())).count();
        project.setCoverageScore(Math.round(((double) covered / allStories.size() * 100) * 100.0) / 100.0);
    }

    @Transactional(readOnly = true)
    public SupportProjectResponseDTO getProjectDetails(UUID projectId) {
        SupportProject project = getProjectAndCheckOwnership(projectId);
        return (SupportProjectResponseDTO) supportProjectMapper.map(project);
    }

    // --- Dictionnaire (Entités) ---
    @Transactional
    public DictionaryEntryResponseDTO addDictionaryEntry(UUID projectId, DictionaryEntryRequestDTO dto) {
        SupportProject project = getProjectAndCheckOwnership(projectId);
        DictionaryEntry entry = new DictionaryEntry();
        entry.setName(dto.getName());
        entry.setDescription(dto.getDescription());
        entry.setProject(project);
        DictionaryEntry savedEntry = dictionaryEntryRepository.save(entry);
        updateProjectActivity(project);
        return supportProjectMapper.toDictionaryEntryDTO(savedEntry);
    }

    @Transactional
    public DictionaryEntryResponseDTO updateDictionaryEntry(UUID entryId, DictionaryEntryRequestDTO dto) {
        DictionaryEntry entry = dictionaryEntryRepository.findById(entryId).orElseThrow(() -> new EntityNotFoundException());
        SupportProject project = getProjectAndCheckOwnership(entry.getProject().getIdProject());
        entry.setName(dto.getName());
        entry.setDescription(dto.getDescription());
        DictionaryEntry savedEntry = dictionaryEntryRepository.save(entry);
        updateProjectActivity(project);
        return supportProjectMapper.toDictionaryEntryDTO(savedEntry);
    }

    @Transactional
    public void deleteDictionaryEntry(UUID entryId) {
        DictionaryEntry entry = dictionaryEntryRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("Entité introuvable"));
        SupportProject project = getProjectAndCheckOwnership(entry.getProject().getIdProject());

        // 1. Trouver les associations du projet
        List<DictionaryAssociation> projectAssociations = associationRepository.findByProjectId(project.getIdProject());

        List<DictionaryAssociation> associationsToDelete = projectAssociations.stream()
                .filter(assoc -> assoc.getSource().getId().equals(entryId) || assoc.getTarget().getId().equals(entryId))
                .toList();
        associationRepository.deleteAll(associationsToDelete);
        associationRepository.flush();
        dictionaryEntryRepository.delete(entry);
        dictionaryEntryRepository.flush();
        updateProjectActivity(project);
    }

    // --- Dictionnaire (Attributs) ---
    @Transactional
    public DictionaryAttributeResponseDTO addDictionaryAttribute(UUID entryId, DictionaryAttributeRequestDTO dto) {
        DictionaryEntry entry = dictionaryEntryRepository.findById(entryId).orElseThrow(() -> new EntityNotFoundException());
        SupportProject project = getProjectAndCheckOwnership(entry.getProject().getIdProject());

        DictionaryAttribute attr = new DictionaryAttribute();
        attr.setName(dto.getName());
        attr.setDataType(dto.getDataType());
        attr.setSize(dto.getSize());
        attr.setPrimaryKey(dto.getPrimaryKey());
        attr.setNotNull(dto.getNotNull());
        attr.setDescription(dto.getDescription());
        attr.setDictionaryEntry(entry);
        DictionaryAttribute savedAttr = dictionaryAttributeRepository.save(attr);
        updateProjectActivity(project);
        return supportProjectMapper.toDictionaryAttributeDTO(savedAttr);
    }

    @Transactional
    public DictionaryAttributeResponseDTO updateDictionaryAttribute(UUID attrId, DictionaryAttributeRequestDTO dto) {
        DictionaryAttribute attr = dictionaryAttributeRepository.findById(attrId).orElseThrow(() -> new EntityNotFoundException());
        SupportProject project = getProjectAndCheckOwnership(attr.getDictionaryEntry().getProject().getIdProject());

        attr.setName(dto.getName());
        attr.setDataType(dto.getDataType());
        attr.setSize(dto.getSize());
        attr.setPrimaryKey(dto.getPrimaryKey());
        attr.setNotNull(dto.getNotNull());
        attr.setDescription(dto.getDescription());
        DictionaryAttribute savedAttr = dictionaryAttributeRepository.save(attr);
        updateProjectActivity(project);
        return supportProjectMapper.toDictionaryAttributeDTO(savedAttr);
    }

    @Transactional
    public void deleteDictionaryAttribute(UUID attrId) {
        DictionaryAttribute attr = dictionaryAttributeRepository.findById(attrId).orElseThrow(() -> new EntityNotFoundException());
        SupportProject project = getProjectAndCheckOwnership(attr.getDictionaryEntry().getProject().getIdProject());
        dictionaryAttributeRepository.delete(attr);
        updateProjectActivity(project);
    }

    // --- IA ---
    @Transactional(readOnly = true)
    public List<DictionaryEntryRequestDTO> getDictionarySuggestions(UUID projectId) throws IOException {
        SupportProject project = getProjectAndCheckOwnership(projectId);
        if (project.getUserStories() == null || project.getUserStories().isEmpty()) return Collections.emptyList();

        StringBuilder usContent = new StringBuilder();
        for (UserStory us : project.getUserStories()) {
            usContent.append("- ").append(us.getIdentifier()).append(" : ").append(us.getDescription()).append("\n");
        }
        return mistralService.suggestDictionaryFromUserStories(usContent.toString());
    }

    // --- Associations (MCD) ---
    @Transactional(readOnly = true)
    public List<DictionaryAssociationResponseDTO> getAssociationsByProject(UUID projectId) {
        getProjectAndCheckOwnership(projectId);
        return associationRepository.findByProjectId(projectId).stream()
                .map(supportProjectMapper::toDictionaryAssociationDTO)
                .toList();
    }

    @Transactional
    public DictionaryAssociationResponseDTO addAssociation(UUID projectId, DictionaryAssociationRequestDTO request) {
        SupportProject project = getProjectAndCheckOwnership(projectId);
        DictionaryEntry src = dictionaryEntryRepository.findById(request.getSourceId()).orElseThrow(() -> new IllegalArgumentException());
        DictionaryEntry tgt = dictionaryEntryRepository.findById(request.getTargetId()).orElseThrow(() -> new IllegalArgumentException());

        DictionaryAssociation assoc = new DictionaryAssociation();
        assoc.setSource(src);
        assoc.setTarget(tgt);
        assoc.setName(request.getName());
        assoc.setSourceMultiplicity(request.getSourceMultiplicity());
        assoc.setTargetMultiplicity(request.getTargetMultiplicity());
        assoc.setRelative(request.getIsRelative() != null ? request.getIsRelative() : false);
        assoc.setCif(request.getIsCif() != null ? request.getIsCif() : false);
        assoc.setIsInheritance(request.getIsInheritance() != null ? request.getIsInheritance() : false);
        if (request.getRuleId() != null) {
            BusinessRule rule = businessRuleRepository.findById(request.getRuleId())
                    .orElseThrow(() -> new EntityNotFoundException("Règle de gestion introuvable"));
            assoc.setBusinessRule(rule);
        }

        DictionaryAssociation savedAssoc = associationRepository.save(assoc);
        updateProjectActivity(project);
        return supportProjectMapper.toDictionaryAssociationDTO(savedAssoc);
    }

    @Transactional
    public DictionaryAssociationResponseDTO updateAssociation(UUID associationId, DictionaryAssociationRequestDTO request) {
        DictionaryAssociation assoc = associationRepository.findById(associationId)
                .orElseThrow(() -> new EntityNotFoundException("Association introuvable"));

        // Sécurité : on vérifie que l'utilisateur a les droits sur le projet
        SupportProject project = getProjectAndCheckOwnership(assoc.getSource().getProject().getIdProject());

        // On récupère les entités (au cas où l'utilisateur change les flèches)
        DictionaryEntry src = dictionaryEntryRepository.findById(request.getSourceId())
                .orElseThrow(() -> new EntityNotFoundException("Source introuvable"));
        DictionaryEntry tgt = dictionaryEntryRepository.findById(request.getTargetId())
                .orElseThrow(() -> new EntityNotFoundException("Cible introuvable"));

        // Mise à jour des champs
        assoc.setSource(src);
        assoc.setTarget(tgt);
        assoc.setName(request.getName());
        assoc.setSourceMultiplicity(request.getSourceMultiplicity());
        assoc.setTargetMultiplicity(request.getTargetMultiplicity());
        assoc.setRelative(request.getIsRelative() != null ? request.getIsRelative() : false);
        assoc.setCif(request.getIsCif() != null ? request.getIsCif() : false);
        assoc.setIsInheritance(request.getIsInheritance() != null ? request.getIsInheritance() : false);
        if (request.getRuleId() != null) {
            BusinessRule rule = businessRuleRepository.findById(request.getRuleId())
                    .orElseThrow(() -> new EntityNotFoundException("Règle de gestion introuvable"));
            assoc.setBusinessRule(rule);
        } else {
            assoc.setBusinessRule(null); // On permet de retirer la règle
        }

        DictionaryAssociation savedAssoc = associationRepository.save(assoc);
        updateProjectActivity(project);
        return supportProjectMapper.toDictionaryAssociationDTO(savedAssoc);
    }

    @Transactional
    public void deleteAssociation(UUID associationId) {
        DictionaryAssociation assoc = associationRepository.findById(associationId).orElseThrow(() -> new IllegalArgumentException());
        SupportProject project = getProjectAndCheckOwnership(assoc.getSource().getProject().getIdProject());
        associationRepository.delete(assoc);
        updateProjectActivity(project);
    }
    // --- GESTION DES RÈGLES DE GESTION ---

    @Transactional(readOnly = true)
    public List<BusinessRuleResponseDTO> getBusinessRules(UUID projectId) {
        getProjectAndCheckOwnership(projectId);
        return businessRuleRepository.findByProject_Id(projectId).stream()
                .map(supportProjectMapper::toBusinessRuleResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BusinessRuleResponseDTO addBusinessRule(UUID projectId, BusinessRuleRequestDTO request) {
        SupportProject project = getProjectAndCheckOwnership(projectId);

        BusinessRule rule = new BusinessRule();
        rule.setProject(project);
        rule.setCode(request.getCode());
        rule.setDescription(request.getDescription());

        rule = businessRuleRepository.save(rule);
        updateProjectActivity(project);

        return supportProjectMapper.toBusinessRuleResponseDTO(rule);
    }

    @Transactional
    public void deleteBusinessRule(UUID ruleId) {
        BusinessRule rule = businessRuleRepository.findById(ruleId)
                .orElseThrow(() -> new EntityNotFoundException("Règle introuvable"));
        SupportProject project = getProjectAndCheckOwnership(rule.getProject().getIdProject());

        // --- ANTICIPATION : Détacher la règle des associations MCD avant suppression ---
        List<DictionaryAssociation> projectAssociations = associationRepository.findByProjectId(project.getIdProject());
        for (DictionaryAssociation assoc : projectAssociations) {
            if (assoc.getBusinessRule() != null && assoc.getBusinessRule().getId().equals(ruleId)) {
                assoc.setBusinessRule(null); // On enlève le lien sans casser le MCD
                associationRepository.save(assoc);
            }
        }
        // ---------------------------------------------------------------------------------

        businessRuleRepository.delete(rule);
        updateProjectActivity(project);
    }

    // --- GÉNÉRATION IA DU MCD ---
    @Transactional
    public void generateMcdFromBusinessRules(UUID projectId) throws IOException {
        SupportProject project = getProjectAndCheckOwnership(projectId);

        // 1. Récupérer les règles
        List<BusinessRule> rules = businessRuleRepository.findByProject_Id(projectId);
        if (rules.isEmpty()) {
            throw new IllegalArgumentException("Aucune règle de gestion trouvée pour générer le MCD.");
        }

        // 2. Construire le texte pour l'IA (version Stream élégante)
        String rulesContent = rules.stream()
                .map(r -> r.getCode() + " : " + r.getDescription())
                .collect(Collectors.joining("\n"));

        // 3. Appeler Mistral
        McdSuggestionDTO suggestion = mistralService.suggestMcdFromBusinessRules(rulesContent);

        // 4. Sauvegarder les Entités et Attributs
        Map<String, DictionaryEntry> savedEntities = new HashMap<>();

        // Utilisation d'Optional pour éviter le "if (suggestion.getEntries() != null)"
        ofNullable(suggestion.getEntries())
                .orElse(java.util.Collections.emptyList())
                .forEach(entryDto -> {

                    DictionaryEntry entry = new DictionaryEntry();
                    entry.setName(entryDto.getName());
                    entry.setDescription(entryDto.getDescription());
                    entry.setProject(project);
                    final DictionaryEntry savedEntry = dictionaryEntryRepository.save(entry);

                    savedEntities.put(savedEntry.getName().toLowerCase(), savedEntry);

                    // Boucle sur les attributs sans le "if != null"
                    ofNullable(entryDto.getAttributes())
                            .orElse(java.util.Collections.emptyList())
                            .forEach(attrDto -> {
                                DictionaryAttribute attr = new DictionaryAttribute();
                                attr.setName(attrDto.getName());
                                attr.setDataType(attrDto.getDataType());
                                attr.setSize(attrDto.getSize());
                                // Astuce Clean Code : Boolean.TRUE.equals() gère les nulls tout seul
                                attr.setPrimaryKey(Boolean.TRUE.equals(attrDto.getPrimaryKey()));
                                attr.setNotNull(Boolean.TRUE.equals(attrDto.getNotNull()));
                                attr.setDescription(attrDto.getDescription());
                                attr.setDictionaryEntry(savedEntry);
                                dictionaryAttributeRepository.save(attr);
                            });
                });

        // 5. Sauvegarder les Associations
        ofNullable(suggestion.getAssociations())
                .orElse(java.util.Collections.emptyList())
                .forEach(assocDto -> {

                    if (assocDto.getSourceName() == null || assocDto.getTargetName() == null) return;

                    DictionaryEntry source = savedEntities.get(assocDto.getSourceName().toLowerCase());
                    DictionaryEntry target = savedEntities.get(assocDto.getTargetName().toLowerCase());

                    if (source != null && target != null) {
                        DictionaryAssociation assoc = new DictionaryAssociation();
                        assoc.setSource(source);
                        assoc.setTarget(target);
                        assoc.setName(assocDto.getName());
                        assoc.setSourceMultiplicity(assocDto.getSourceMultiplicity());
                        assoc.setTargetMultiplicity(assocDto.getTargetMultiplicity());
                        assoc.setRelative(false);
                        assoc.setCif(false);
                        assoc.setIsInheritance(false);

                        // On lie l'association à la règle de gestion de façon concise
                        if (assocDto.getRuleCode() != null) {
                            rules.stream()
                                    .filter(r -> r.getCode().equalsIgnoreCase(assocDto.getRuleCode()))
                                    .findFirst()
                                    .ifPresent(assoc::setBusinessRule);
                        }

                        // ⚠️ MODIFICATION ICI : On récupère l'association sauvegardée
                        final DictionaryAssociation savedAssoc = associationRepository.save(assoc);

                        // NOUVEAU : On boucle pour sauvegarder les attributs portés par la relation !
                        ofNullable(assocDto.getAttributes())
                                .orElse(java.util.Collections.emptyList())
                                .forEach(attrDto -> {
                                    DictionaryAttribute attr = new DictionaryAttribute();
                                    attr.setName(attrDto.getName());
                                    attr.setDataType(attrDto.getDataType());
                                    attr.setSize(attrDto.getSize());
                                    attr.setPrimaryKey(Boolean.TRUE.equals(attrDto.getPrimaryKey()));
                                    attr.setNotNull(Boolean.TRUE.equals(attrDto.getNotNull()));
                                    attr.setDescription(attrDto.getDescription());

                                    // On l'attache à l'association, pas à l'entité !
                                    attr.setDictionaryAssociation(savedAssoc);

                                    dictionaryAttributeRepository.save(attr);
                                });
                    }
                });

        updateProjectActivity(project);
    }

    // --- AUDIT GLOBAL DU PROJET ---
    @Transactional
    public ProjectAuditResponseDTO auditProject(UUID projectId) throws IOException {
        SupportProject project = getProjectAndCheckOwnership(projectId);
        long startMillis = System.currentTimeMillis();
        LocalDateTime startTime = LocalDateTime.now();

        // 1. On rassemble toutes les pièces du puzzle
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("Nom du projet : ").append(project.getName()).append("\n\n");

        // Acteurs
        contextBuilder.append("--- ACTEURS ---\n");
        project.getActors().forEach(a ->
                contextBuilder.append("- ").append(a.getName()).append("\n")
        );

        // User Stories
        contextBuilder.append("\n--- USER STORIES ---\n");
        project.getUserStories().forEach(us -> {
            String actorName = (us.getActor() != null) ? us.getActor().getName() : "Inconnu";
            contextBuilder.append("- ").append(us.getIdentifier()).append(" : En tant que ")
                    .append(actorName).append(", je veux ")
                    .append(us.getDescription()).append(" afin de ")
                    .append(us.getBenefit()).append("\n");
        });

        // Règles de Gestion
        contextBuilder.append("\n--- RÈGLES DE GESTION ---\n");
        businessRuleRepository.findByProject_Id(projectId).forEach(r ->
                contextBuilder.append("- ").append(r.getCode()).append(" : ").append(r.getDescription()).append("\n")
        );

        // Dictionnaire de Données
        contextBuilder.append("\n--- DICTIONNAIRE DE DONNÉES ---\n");
        project.getDictionaryEntries().forEach(entry -> {
            contextBuilder.append("Entité : ").append(entry.getName()).append("\n");
            if (entry.getAttributes() != null) {
                entry.getAttributes().forEach(attr ->
                        contextBuilder.append("  - Attribut : ").append(attr.getName()).append(" (").append(attr.getDataType()).append(")\n")
                );
            }
        });

        // MCD (Associations)
        contextBuilder.append("\n--- MODÈLE CONCEPTUEL DE DONNÉES (ASSOCIATIONS) ---\n");
        associationRepository.findByProjectId(projectId).forEach(assoc -> {
            contextBuilder.append("- Relation '").append(assoc.getName()).append("' entre ")
                    .append(assoc.getSource().getName()).append(" (").append(assoc.getSourceMultiplicity()).append(") et ")
                    .append(assoc.getTarget().getName()).append(" (").append(assoc.getTargetMultiplicity()).append(")");

            if (assoc.getAttributes() != null && !assoc.getAttributes().isEmpty()) {
                contextBuilder.append(" | Données portées : ");
                assoc.getAttributes().forEach(attr ->
                        contextBuilder.append(attr.getName()).append(" (").append(attr.getDataType()).append("), ")
                );
            }
            contextBuilder.append("\n");
        });

        contextBuilder.append("\n--- DIAGRAMME BPMN (Format XML) ---\n");
        contextBuilder.append(project.getBpmnXml()).append("\n");
        try {
            ProjectAuditResponseDTO report = mistralService.auditProjectComplete(contextBuilder.toString());

            long duration = System.currentTimeMillis() - startMillis;
            int score = report.getScore();
            int nbAnomalies = (report.getInconsistencies() != null) ? report.getInconsistencies().size() : 0;

            LogExecution log = new LogExecution();
            log.setOperation("AUDIT_IA");
            log.setStartTime(startTime);
            log.setEndTime(LocalDateTime.now());
            log.setDurationMs(duration);
            log.setStatus("SUCCESS");
            log.setProjectId(projectId);
            String detailsJson = String.format("{\"score\": %d, \"anomalies\": %d}", score, nbAnomalies);
            log.setDetails(detailsJson);

            logExecutionRepository.save(log);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new ObjectMapper();
            project.setLastAuditReport(mapper.writeValueAsString(report));
            updateProjectActivity(project);

            return report;

        } catch (Exception e) {
            // LOG D'ÉCHEC (Pour surveiller si Mistral plante)
            LogExecution log = new LogExecution();
            log.setOperation("AUDIT_IA");
            log.setStartTime(startTime);
            log.setEndTime(LocalDateTime.now());
            log.setDurationMs(System.currentTimeMillis() - startMillis);
            log.setStatus("ERROR");
            log.setProjectId(projectId);
            log.setDetails("{\"error\": \"Erreur de l'API IA\"}");
            logExecutionRepository.save(log);

            throw e;
        }

    }

    @Transactional(readOnly = true)
    public ProjectAuditResponseDTO getLastAuditReport(UUID projectId) throws IOException {
        SupportProject project = getProjectAndCheckOwnership(projectId);

        if (project.getLastAuditReport() == null || project.getLastAuditReport().isEmpty()) {
            return null;
        }
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        return mapper.readValue(project.getLastAuditReport(), ProjectAuditResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public byte[] generateMcdFile(UUID projectId) {
        SupportProject project = getProjectAndCheckOwnership(projectId);

        ArrayList<Object> listeEntiteRelation = new ArrayList<>();
        ArrayList<Object> listeLien = new ArrayList<>();
        ArrayList<Object> listeCIF = new ArrayList<>();
        ArrayList<Object> listelienCIF = new ArrayList<>();
        ArrayList<Object> listePostIt = new ArrayList<>();
        ArrayList<Object> listeHeritage = new ArrayList<>();
        ArrayList<Object> listeLienContrainteHeritage = new ArrayList<>();

        Map<UUID, IhmEntite2> entiteMap = new HashMap<>();
        Set<UUID> rulesSeen = new HashSet<>();
        StringBuilder allRulesContent = new StringBuilder("RÈGLES DE GESTION :\n\n");
        boolean projectHasRules = false;

        int x = 100, y = 100;

        if (project.getDictionaryEntries() != null) {
            for (DictionaryEntry entry : project.getDictionaryEntries()) {
                Entite2 meriseEntite = new Entite2(entry.getName());

                if (entry.getAttributes() != null) {
                    for (DictionaryAttribute attr : entry.getAttributes()) {
                        String type = (attr.getDataType() != null) ? attr.getDataType() : "VARCHAR";
                        int taille = 50;
                        try {
                            if (attr.getSize() != null) taille = Integer.parseInt(attr.getSize().split(",")[0].trim());
                        } catch (Exception ignored) {}

                        Attribut2 meriseAttr = new Attribut2(attr.getName(), type, taille, 0,
                                Boolean.TRUE.equals(attr.getPrimaryKey()) ? "PRIMARY KEY" : "",
                                !Boolean.TRUE.equals(attr.getNotNull()),
                                (attr.getDescription() != null) ? attr.getDescription() : "", meriseEntite);
                        meriseEntite.getListeAttributs().add(meriseAttr);
                    }
                }

                IhmEntite2 ihmEntite = new IhmEntite2(meriseEntite, x, y, true);
                try { ihmEntite.setAligne("GAUCHE"); ihmEntite.setAligneTitre("GAUCHE"); } catch (Throwable ignored) {}
                listeEntiteRelation.add(ihmEntite);
                entiteMap.put(entry.getId(), ihmEntite);

                x += 250;
                if (x > 800) { x = 100; y += 200; }
            }
        }

        List<DictionaryAssociation> assocs = associationRepository.findByProjectId(projectId);
        if (assocs != null) {
            for (DictionaryAssociation assoc : assocs) {
                IhmEntite2 src = entiteMap.get(assoc.getSource().getId());
                IhmEntite2 tgt = entiteMap.get(assoc.getTarget().getId());

                if (src == null || tgt == null) continue;

                if (assoc.getBusinessRule() != null && !rulesSeen.contains(assoc.getBusinessRule().getId())) {
                    BusinessRule rule = assoc.getBusinessRule();
                    allRulesContent.append("• ").append(rule.getCode().toUpperCase()).append(" :\n");
                    allRulesContent.append(formatDescription(rule.getDescription(), 45)).append("\n\n");
                    rulesSeen.add(rule.getId());
                    projectHasRules = true;
                }

                if (Boolean.TRUE.equals(assoc.getIsInheritance())) {
                    IhmHeritage2 heritage = new IhmHeritage2(x, y, tgt, 0);
                    try { heritage.setNom(""); heritage.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12)); } catch (Throwable ignored) {}
                    listeHeritage.add(heritage);

                    try {
                        listeLienContrainteHeritage.add(new IhmLienContrainteHeritage2(heritage, tgt));
                        listeLienContrainteHeritage.add(new IhmLienContrainteHeritage2(heritage, src));
                    } catch (Throwable ignored) {}

                } else {
                    // 🔵 CAS 2 : RELATION NORMALE
                    Relation2 logicRel = new Relation2(assoc.getName());
                    if (assoc.getAttributes() != null) {
                        for (DictionaryAttribute attr : assoc.getAttributes()) {
                            Attribut2 meriseAttr = new Attribut2(attr.getName(), (attr.getDataType() != null ? attr.getDataType() : "VARCHAR"), 50, 0, "",
                                    !Boolean.TRUE.equals(attr.getNotNull()), (attr.getDescription() != null ? attr.getDescription() : ""), logicRel);
                            logicRel.getListeAttributs().add(meriseAttr);
                        }
                    }

                    IhmRelation2 ihmRel = new IhmRelation2(logicRel, x, y, true);
                    listeEntiteRelation.add(ihmRel);

                    IhmLien2 lSrc = new IhmLien2(src, ihmRel);
                    String cSrc = (assoc.getSourceMultiplicity() != null) ? assoc.getSourceMultiplicity().replace("..", ",") : "0,n";
                    if (Boolean.TRUE.equals(assoc.getRelative()) && cSrc.contains("1") && !cSrc.toLowerCase().contains("n")) {
                        cSrc = "(" + cSrc + ")"; lSrc.setRelatif(true);
                    }
                    lSrc.setCardinalite(cSrc);
                    listeLien.add(lSrc);

                    IhmLien2 lTgt = new IhmLien2(tgt, ihmRel);
                    String cTgt = (assoc.getTargetMultiplicity() != null) ? assoc.getTargetMultiplicity().replace("..", ",") : "0,n";
                    if (Boolean.TRUE.equals(assoc.getRelative()) && cTgt.contains("1") && !cTgt.toLowerCase().contains("n")) {
                        cTgt = "(" + cTgt + ")"; lTgt.setRelatif(true);
                    }
                    lTgt.setCardinalite(cTgt);

                    if (Boolean.TRUE.equals(assoc.getCif())) {
                        try { lTgt.setFleche(true); } catch (Throwable ignored) {}
                        IhmCIF2 cif = new IhmCIF2(x + 50, y - 50, 30, 30);
                        try { cif.setNom("CIF"); cif.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 10)); } catch (Throwable ignored) {}
                        listeCIF.add(cif);
                        try {
                            listelienCIF.add(new IhmLienCIF2(ihmRel, cif, ""));
                        } catch (Throwable ignored) {}
                    }
                    listeLien.add(lTgt);
                }
                x += 280; if (x > 900) { x = 100; y += 180; }
            }
        }

        if (projectHasRules) {
            IhmPostIt2 postIt = new IhmPostIt2(allRulesContent.toString(), 1150, 50, 300, 400);
            postIt.setCommentaire(allRulesContent.toString());
            try { postIt.setClTexte(java.awt.Color.BLACK); } catch (Throwable ignored) {}
            listePostIt.add(postIt);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            org.springframework.core.io.ClassPathResource resource = new org.springframework.core.io.ClassPathResource("blank.mcd");
            try (ObjectInputStream ois = new ObjectInputStream(resource.getInputStream())) {
                oos.writeObject(ois.readObject());
                ois.readObject(); oos.writeObject(listeEntiteRelation);
                ois.readObject(); oos.writeObject(listeLien);
                ois.readObject(); oos.writeObject(listeCIF);
                ois.readObject(); oos.writeObject(listelienCIF);

                ois.readObject(); oos.writeObject(listePostIt);
                ois.readObject(); oos.writeObject(new ArrayList<>());

                oos.writeObject(ois.readObject());
                ois.readObject(); oos.writeObject(listeHeritage);
                ois.readObject(); oos.writeObject(listeLienContrainteHeritage);

                while (true) {
                    try { oos.writeObject(ois.readObject()); } catch (EOFException e) { break; }
                }
            }
            oos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur de sérialisation MCD", e);
        }
    }

    /**
     * Empêche le Post-it d'être trop large en découpant le texte
     */
    private String formatDescription(String text, int limit) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String word : text.split(" ")) {
            if (count + word.length() > limit) {
                sb.append("\n");
                count = 0;
            }
            sb.append(word).append(" ");
            count += word.length() + 1;
        }
        return sb.toString().trim();
    }

    /**
     * Met à jour le statut du projet en fonction de son avancement
     * et force l'actualisation de la date de modification.
     */
    private void updateProjectActivity(SupportProject project) {
        project.setUpdateAt(LocalDateTime.now());

        boolean hasRules = businessRuleRepository.findByProject_Id(project.getIdProject()).size() > 0;
        boolean hasSpec = (project.getActors() != null && !project.getActors().isEmpty())
                || (project.getUserStories() != null && !project.getUserStories().isEmpty())
                || hasRules;
        boolean hasModel = (project.getDictionaryEntries() != null && !project.getDictionaryEntries().isEmpty())
                || (project.getBpmnXml() != null && !project.getBpmnXml().trim().isEmpty());
        boolean isAudited = project.getLastAuditReport() != null && !project.getLastAuditReport().trim().isEmpty();

        if (isAudited) {
            project.setStatus(StatusProject.LIVRE);
        } else if (hasModel) {
            project.setStatus(StatusProject.EN_MODELISATION);
        } else if (hasSpec) {
            project.setStatus(StatusProject.EN_SPECIFICATION);
        } else {
            project.setStatus(StatusProject.INITIALISE);
        }

        projectRepository.save(project);
    }
}