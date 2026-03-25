package com.example.backend.modules.projects.acc.service;

import com.example.backend.modules.analysis.exporter.MistralService;
import com.example.backend.modules.analysis.parser.BpmnParserStrategy;
import com.example.backend.modules.projects.acc.dao.*;
import com.example.backend.modules.projects.acc.dto.*;
import com.example.backend.modules.projects.acc.entity.*;
import com.example.backend.modules.projects.acc.mapper.SupportProjectMapper;
import com.example.backend.modules.projects.core.dao.ProjectRepository;
import com.example.backend.modules.projects.core.exception.ProjectNotFoundException;
import com.example.backend.modules.projects.core.exception.UnauthorizedAccessException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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

    public SupportFeatureService(ActorRepository actorRepository,
                                 UserStoryRepository userStoryRepository,
                                 ProjectRepository projectRepository,
                                 SupportProjectMapper supportProjectMapper,
                                 DictionaryEntryRepository dictionaryEntryRepository,
                                 DictionaryAttributeRepository dictionaryAttributeRepository,
                                 MistralService mistralService,
                                 DictionaryAssociationRepository associationRepository,
                                 BusinessRuleRepository businessRuleRepository) {
        this.actorRepository = actorRepository;
        this.userStoryRepository = userStoryRepository;
        this.projectRepository = projectRepository;
        this.supportProjectMapper = supportProjectMapper;
        this.dictionaryEntryRepository = dictionaryEntryRepository;
        this.dictionaryAttributeRepository = dictionaryAttributeRepository;
        this.mistralService = mistralService;
        this.associationRepository = associationRepository;
        this.businessRuleRepository = businessRuleRepository;
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
        return supportProjectMapper.toActorDTO(actorRepository.save(actor));
    }

    @Transactional
    public ActorResponseDTO updateActor(UUID actorId, String newName) {
        Actor actor = actorRepository.findById(actorId).orElseThrow(() -> new EntityNotFoundException());
        getProjectAndCheckOwnership(actor.getProject().getIdProject());
        actor.setName(newName);
        return supportProjectMapper.toActorDTO(actorRepository.save(actor));
    }

    @Transactional
    public void deleteActor(UUID actorId) {
        userStoryRepository.deleteByActorId(actorId);
        actorRepository.deleteById(actorId);
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
        return supportProjectMapper.toUserStoryDTO(userStoryRepository.save(us));
    }

    @Transactional
    public UserStoryResponseDTO updateUserStory(UUID usId, String desc, String ben, String crit, UUID actorId) {
        UserStory us = userStoryRepository.findById(usId).orElseThrow(() -> new EntityNotFoundException());
        Actor actor = actorRepository.findById(actorId).orElseThrow(() -> new EntityNotFoundException());
        getProjectAndCheckOwnership(us.getProject().getIdProject());

        us.setDescription(desc);
        us.setBenefit(ben);
        us.setAcceptanceCriteria(crit);
        us.setActor(actor);
        return supportProjectMapper.toUserStoryDTO(userStoryRepository.save(us));
    }

    @Transactional
    public void deleteUserStory(UUID usId) {
        UserStory us = userStoryRepository.findById(usId).orElseThrow(() -> new EntityNotFoundException());
        getProjectAndCheckOwnership(us.getProject().getIdProject());
        userStoryRepository.delete(us);
    }

    // --- BPMN ---
    @Transactional
    public SupportProject saveBpmnDiagram(UUID projectId, String bpmnXml) {
        SupportProject project = getProjectAndCheckOwnership(projectId);
        project.setBpmnXml(bpmnXml);
        updateCoverageScoreInternal(project);
        return projectRepository.save(project);
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
        return supportProjectMapper.toDictionaryEntryDTO(dictionaryEntryRepository.save(entry));
    }

    @Transactional
    public DictionaryEntryResponseDTO updateDictionaryEntry(UUID entryId, DictionaryEntryRequestDTO dto) {
        DictionaryEntry entry = dictionaryEntryRepository.findById(entryId).orElseThrow(() -> new EntityNotFoundException());
        getProjectAndCheckOwnership(entry.getProject().getIdProject());
        entry.setName(dto.getName());
        entry.setDescription(dto.getDescription());
        return supportProjectMapper.toDictionaryEntryDTO(dictionaryEntryRepository.save(entry));
    }

    @Transactional
    public void deleteDictionaryEntry(UUID entryId) {
        DictionaryEntry entry = dictionaryEntryRepository.findById(entryId).orElseThrow(() -> new EntityNotFoundException());
        getProjectAndCheckOwnership(entry.getProject().getIdProject());
        dictionaryEntryRepository.delete(entry);
    }

    // --- Dictionnaire (Attributs) ---
    @Transactional
    public DictionaryAttributeResponseDTO addDictionaryAttribute(UUID entryId, DictionaryAttributeRequestDTO dto) {
        DictionaryEntry entry = dictionaryEntryRepository.findById(entryId).orElseThrow(() -> new EntityNotFoundException());
        getProjectAndCheckOwnership(entry.getProject().getIdProject());

        DictionaryAttribute attr = new DictionaryAttribute();
        attr.setName(dto.getName());
        attr.setDataType(dto.getDataType());
        attr.setSize(dto.getSize());
        attr.setPrimaryKey(dto.getPrimaryKey());
        attr.setNotNull(dto.getNotNull());
        attr.setDescription(dto.getDescription());
        attr.setDictionaryEntry(entry);
        return supportProjectMapper.toDictionaryAttributeDTO(dictionaryAttributeRepository.save(attr));
    }

    @Transactional
    public DictionaryAttributeResponseDTO updateDictionaryAttribute(UUID attrId, DictionaryAttributeRequestDTO dto) {
        DictionaryAttribute attr = dictionaryAttributeRepository.findById(attrId).orElseThrow(() -> new EntityNotFoundException());
        getProjectAndCheckOwnership(attr.getDictionaryEntry().getProject().getIdProject());

        attr.setName(dto.getName());
        attr.setDataType(dto.getDataType());
        attr.setSize(dto.getSize());
        attr.setPrimaryKey(dto.getPrimaryKey());
        attr.setNotNull(dto.getNotNull());
        attr.setDescription(dto.getDescription());
        return supportProjectMapper.toDictionaryAttributeDTO(dictionaryAttributeRepository.save(attr));
    }

    @Transactional
    public void deleteDictionaryAttribute(UUID attrId) {
        DictionaryAttribute attr = dictionaryAttributeRepository.findById(attrId).orElseThrow(() -> new EntityNotFoundException());
        getProjectAndCheckOwnership(attr.getDictionaryEntry().getProject().getIdProject());
        dictionaryAttributeRepository.delete(attr);
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
        getProjectAndCheckOwnership(projectId);
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

        return supportProjectMapper.toDictionaryAssociationDTO(associationRepository.save(assoc));
    }

    @Transactional
    public DictionaryAssociationResponseDTO updateAssociation(UUID associationId, DictionaryAssociationRequestDTO request) {
        DictionaryAssociation assoc = associationRepository.findById(associationId)
                .orElseThrow(() -> new EntityNotFoundException("Association introuvable"));

        // Sécurité : on vérifie que l'utilisateur a les droits sur le projet
        getProjectAndCheckOwnership(assoc.getSource().getProject().getIdProject());

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

        return supportProjectMapper.toDictionaryAssociationDTO(associationRepository.save(assoc));
    }

    @Transactional
    public void deleteAssociation(UUID associationId) {
        DictionaryAssociation assoc = associationRepository.findById(associationId).orElseThrow(() -> new IllegalArgumentException());
        getProjectAndCheckOwnership(assoc.getSource().getProject().getIdProject());
        associationRepository.delete(assoc);
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

        return supportProjectMapper.toBusinessRuleResponseDTO(rule);
    }

    @Transactional
    public void deleteBusinessRule(UUID ruleId) {
        BusinessRule rule = businessRuleRepository.findById(ruleId)
                .orElseThrow(() -> new EntityNotFoundException("Règle introuvable"));
        getProjectAndCheckOwnership(rule.getProject().getIdProject());
        businessRuleRepository.delete(rule);
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
    }
}