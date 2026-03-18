package com.example.backend.modules.projects.acc.service;

import com.example.backend.modules.analysis.parser.BpmnParserStrategy;
import com.example.backend.modules.projects.acc.dao.ActorRepository;
import com.example.backend.modules.projects.acc.dao.DictionaryAttributeRepository;
import com.example.backend.modules.projects.acc.dao.DictionaryEntryRepository;
import com.example.backend.modules.projects.acc.dao.UserStoryRepository;
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

    public SupportFeatureService(ActorRepository actorRepository,
                                 UserStoryRepository userStoryRepository,
                                 ProjectRepository projectRepository,
                                 SupportProjectMapper supportProjectMapper,
                                DictionaryEntryRepository dictionaryEntryRepository,
                                 DictionaryAttributeRepository dictionaryAttributeRepository) {
        this.actorRepository = actorRepository;
        this.userStoryRepository = userStoryRepository;
        this.projectRepository = projectRepository;
        this.supportProjectMapper = supportProjectMapper;
        this.dictionaryEntryRepository = dictionaryEntryRepository;
        this.dictionaryAttributeRepository = dictionaryAttributeRepository;
    }
    private UserStoryResponseDTO mapToDTO(UserStory us) {
        UserStoryResponseDTO dto = new UserStoryResponseDTO();
        dto.setId(us.getId());
        dto.setIdentifier(us.getIdentifier());
        dto.setDescription(us.getDescription());
        dto.setBenefit(us.getBenefit());
        dto.setAcceptanceCriteria(us.getAcceptanceCriteria());
        if (us.getActor() != null) {
            dto.setActorId(us.getActor().getId());
            dto.setActorName(us.getActor().getName());
        }
        return dto;
    }

    private ActorResponseDTO mapToActorDTO(Actor actor) {
        ActorResponseDTO dto = new ActorResponseDTO();
        dto.setId(actor.getId());
        dto.setName(actor.getName());
        return dto;
    }
    /**
     * Méthode de sécurité interne pour vérifier que l'utilisateur connecté
     * est bien le propriétaire du projet qu'il tente de modifier.
     */
    private SupportProject getProjectAndCheckOwnership(UUID projectId) {
        String currentExternalId = SecurityContextHolder.getContext().getAuthentication().getName();

        SupportProject project = (SupportProject) projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projet introuvable avec l'ID : " + projectId));

        if (!project.getUser().getExternalId().equals(currentExternalId)) {
            throw new UnauthorizedAccessException("Accès refusé : vous n'êtes pas le propriétaire de ce projet.");
        }

        return project;
    }

    // --- Gestion des Acteurs ---

    @Transactional
    public ActorResponseDTO addActor(UUID projectId, String name) {
        SupportProject project = getProjectAndCheckOwnership(projectId);

        Actor actor = new Actor();
        actor.setName(name);
        actor.setProject(project); //
        return mapToActorDTO(actorRepository.save(actor));
    }

    @Transactional
    public ActorResponseDTO updateActor(UUID actorId, String newName) {
        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new ProjectNotFoundException("Acteur introuvable"));

        // Vérification de sécurité via le projet rattaché à l'acteur
        getProjectAndCheckOwnership(actor.getProject().getIdProject());

        actor.setName(newName);
        return mapToActorDTO(actorRepository.save(actor));
    }

    @Transactional
    public void deleteActor(UUID actorId) {
        userStoryRepository.deleteByActorId(actorId);
        actorRepository.deleteById(actorId);
    }

    // --- Gestion des User Stories ---

    @Transactional
    public UserStoryResponseDTO addUserStory(UUID projectId, UUID actorId, String description, String benefit, String acceptanceCriteria) {
        SupportProject project = getProjectAndCheckOwnership(projectId);

        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new ProjectNotFoundException("Acteur introuvable"));

        // Sécurité supplémentaire : on vérifie que l'acteur appartient bien au bon projet
        if (!actor.getProject().getIdProject().equals(projectId)) {
            throw new UnauthorizedAccessException("L'acteur spécifié n'appartient pas à ce projet.");
        }

        UserStory us = new UserStory();
        us.setDescription(description);
        us.setAcceptanceCriteria(acceptanceCriteria);
        us.setBenefit(benefit);
        us.setActor(actor); //
        us.setProject(project); //
        UserStory saved = userStoryRepository.save(us);
        return mapToDTO(saved);
    }

    @Transactional
    public UserStoryResponseDTO updateUserStory(UUID usId, String description , String benefit, String acceptanceCriteria, UUID actorId) {
        UserStory us = userStoryRepository.findById(usId)
                .orElseThrow(() -> new ProjectNotFoundException("User Story introuvable"));
        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("Acteur non trouvé"));

        getProjectAndCheckOwnership(us.getProject().getIdProject());

        us.setAcceptanceCriteria(acceptanceCriteria);
        us.setBenefit(benefit);
        us.setDescription(description);
        us.setActor(actor);
        UserStory saved = userStoryRepository.save(us);
        return mapToDTO(saved);
    }

    @Transactional
    public void deleteUserStory(UUID usId) {
        UserStory us = userStoryRepository.findById(usId)
                .orElseThrow(() -> new ProjectNotFoundException("User Story introuvable"));

        getProjectAndCheckOwnership(us.getProject().getIdProject());

        userStoryRepository.delete(us);
    }

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

        // On appelle la méthode de manière STATIQUE (voir étape 2)
        Set<String> linkedUsIdsInBpmn = BpmnParserStrategy.extractLinkedUserStories(project.getBpmnXml());

        long coveredCount = allStories.stream()
                .filter(us -> linkedUsIdsInBpmn.contains(us.getId().toString()))
                .count();

        double percentage = (double) coveredCount / allStories.size() * 100;
        double roundedPercentage = Math.round(percentage * 100.0) / 100.0;

        project.setCoverageScore(roundedPercentage);
    }

    @Transactional(readOnly = true)
    public SupportProjectResponseDTO getProjectDetails(UUID projectId) {
        SupportProject project = getProjectAndCheckOwnership(projectId);
        return (SupportProjectResponseDTO) supportProjectMapper.map(project);
    }

    // --- Gestion du Dictionnaire (Entités) ---

    @Transactional
    public DictionaryEntryResponseDTO addDictionaryEntry(UUID projectId, DictionaryEntryRequestDTO dto) {
        SupportProject project = getProjectAndCheckOwnership(projectId);
        DictionaryEntry entry = new DictionaryEntry();
        entry.setName(dto.getName());
        entry.setDescription(dto.getDescription());
        entry.setProject(project);
        return mapToDictionaryEntryDTO(dictionaryEntryRepository.save(entry));
    }

    @Transactional
    public DictionaryEntryResponseDTO updateDictionaryEntry(UUID entryId, DictionaryEntryRequestDTO dto) {
        DictionaryEntry entry = dictionaryEntryRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("Entité introuvable"));

        // Sécurité : on vérifie que l'utilisateur possède bien le projet lié à cette entité
        getProjectAndCheckOwnership(entry.getProject().getIdProject());

        entry.setName(dto.getName());
        entry.setDescription(dto.getDescription());

        return mapToDictionaryEntryDTO(dictionaryEntryRepository.save(entry));
    }

    @Transactional
    public void deleteDictionaryEntry(UUID entryId) {
        DictionaryEntry entry = dictionaryEntryRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("Entité introuvable"));
        getProjectAndCheckOwnership(entry.getProject().getIdProject());
        dictionaryEntryRepository.delete(entry);
    }

// --- Gestion du Dictionnaire (Attributs) ---

    @Transactional
    public DictionaryAttributeResponseDTO addDictionaryAttribute(UUID entryId, DictionaryAttributeRequestDTO dto) {
        DictionaryEntry entry = dictionaryEntryRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("Entité introuvable"));
        getProjectAndCheckOwnership(entry.getProject().getIdProject());

        DictionaryAttribute attr = new DictionaryAttribute();
        attr.setName(dto.getName());
        attr.setDataType(dto.getDataType());
        attr.setSize(dto.getSize());
        attr.setPrimaryKey(dto.getPrimaryKey());
        attr.setNotNull(dto.getNotNull());
        attr.setDescription(dto.getDescription());
        attr.setDictionaryEntry(entry);

        return mapToDictionaryAttributeDTO(dictionaryAttributeRepository.save(attr));
    }



    // --- Mise à jour du Dictionnaire (Attributs) ---

    @Transactional
    public DictionaryAttributeResponseDTO updateDictionaryAttribute(UUID attrId, DictionaryAttributeRequestDTO dto) {
        DictionaryAttribute attr = dictionaryAttributeRepository.findById(attrId)
                .orElseThrow(() -> new EntityNotFoundException("Attribut introuvable"));

        // Sécurité : on remonte jusqu'au projet pour vérifier les droits
        getProjectAndCheckOwnership(attr.getDictionaryEntry().getProject().getIdProject());

        attr.setName(dto.getName());
        attr.setDataType(dto.getDataType());
        attr.setSize(dto.getSize());
        attr.setPrimaryKey(dto.getPrimaryKey());
        attr.setNotNull(dto.getNotNull());
        attr.setDescription(dto.getDescription());

        return mapToDictionaryAttributeDTO(dictionaryAttributeRepository.save(attr));
    }

    @Transactional
    public void deleteDictionaryAttribute(UUID attrId) {
        DictionaryAttribute attr = dictionaryAttributeRepository.findById(attrId)
                .orElseThrow(() -> new EntityNotFoundException("Attribut introuvable"));
        getProjectAndCheckOwnership(attr.getDictionaryEntry().getProject().getIdProject());
        dictionaryAttributeRepository.delete(attr);
    }

    // Petits mappers locaux pour renvoyer des DTO propres :
    private DictionaryEntryResponseDTO mapToDictionaryEntryDTO(DictionaryEntry entry) {
        DictionaryEntryResponseDTO dto = new DictionaryEntryResponseDTO();
        dto.setId(entry.getId());
        dto.setName(entry.getName());
        dto.setDescription(entry.getDescription());
        return dto;
    }

    private DictionaryAttributeResponseDTO mapToDictionaryAttributeDTO(DictionaryAttribute attr) {
        DictionaryAttributeResponseDTO dto = new DictionaryAttributeResponseDTO();
        dto.setId(attr.getId());
        dto.setName(attr.getName());
        dto.setDataType(attr.getDataType());
        dto.setSize(attr.getSize());
        dto.setPrimaryKey(attr.getPrimaryKey());
        dto.setNotNull(attr.getNotNull());
        dto.setDescription(attr.getDescription());
        return dto;
    }
}