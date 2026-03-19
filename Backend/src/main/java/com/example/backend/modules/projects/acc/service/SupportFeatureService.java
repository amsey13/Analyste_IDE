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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    public SupportFeatureService(ActorRepository actorRepository,
                                 UserStoryRepository userStoryRepository,
                                 ProjectRepository projectRepository,
                                 SupportProjectMapper supportProjectMapper,
                                 DictionaryEntryRepository dictionaryEntryRepository,
                                 DictionaryAttributeRepository dictionaryAttributeRepository,
                                 MistralService mistralService,
                                 DictionaryAssociationRepository associationRepository) {
        this.actorRepository = actorRepository;
        this.userStoryRepository = userStoryRepository;
        this.projectRepository = projectRepository;
        this.supportProjectMapper = supportProjectMapper;
        this.dictionaryEntryRepository = dictionaryEntryRepository;
        this.dictionaryAttributeRepository = dictionaryAttributeRepository;
        this.mistralService = mistralService;
        this.associationRepository = associationRepository;
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

        return supportProjectMapper.toDictionaryAssociationDTO(associationRepository.save(assoc));
    }

    @Transactional
    public void deleteAssociation(UUID associationId) {
        DictionaryAssociation assoc = associationRepository.findById(associationId).orElseThrow(() -> new IllegalArgumentException());
        getProjectAndCheckOwnership(assoc.getSource().getProject().getIdProject());
        associationRepository.delete(assoc);
    }
}