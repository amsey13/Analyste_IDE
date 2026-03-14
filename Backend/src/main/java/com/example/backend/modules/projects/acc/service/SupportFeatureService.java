package com.example.backend.modules.projects.acc.service;

import com.example.backend.modules.analysis.parser.BpmnParserStrategy;
import com.example.backend.modules.projects.acc.dao.ActorRepository;
import com.example.backend.modules.projects.acc.dao.UserStoryRepository;
import com.example.backend.modules.projects.acc.dto.UserStoryResponseDTO;
import com.example.backend.modules.projects.acc.entity.Actor;
import com.example.backend.modules.projects.acc.entity.SupportProject;
import com.example.backend.modules.projects.acc.entity.UserStory;
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

    public SupportFeatureService(ActorRepository actorRepository,
                                 UserStoryRepository userStoryRepository,
                                 ProjectRepository projectRepository) {
        this.actorRepository = actorRepository;
        this.userStoryRepository = userStoryRepository;
        this.projectRepository = projectRepository;
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
    public Actor addActor(UUID projectId, String name) {
        SupportProject project = getProjectAndCheckOwnership(projectId);

        Actor actor = new Actor();
        actor.setName(name);
        actor.setProject(project); //
        return actorRepository.save(actor);
    }

    @Transactional
    public Actor updateActor(UUID actorId, String newName) {
        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new ProjectNotFoundException("Acteur introuvable"));

        // Vérification de sécurité via le projet rattaché à l'acteur
        getProjectAndCheckOwnership(actor.getProject().getIdProject());

        actor.setName(newName);
        return actorRepository.save(actor);
    }

    @Transactional
    public void deleteActor(UUID actorId) {
        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new ProjectNotFoundException("Acteur introuvable"));

        getProjectAndCheckOwnership(actor.getProject().getIdProject());

        actorRepository.delete(actor);
    }

    // --- Gestion des User Stories ---

    @Transactional
    public UserStoryResponseDTO addUserStory(UUID projectId, UUID actorId, String identifier, String description, String benefit, String acceptanceCriteria) {
        SupportProject project = getProjectAndCheckOwnership(projectId);

        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new ProjectNotFoundException("Acteur introuvable"));

        // Sécurité supplémentaire : on vérifie que l'acteur appartient bien au bon projet
        if (!actor.getProject().getIdProject().equals(projectId)) {
            throw new UnauthorizedAccessException("L'acteur spécifié n'appartient pas à ce projet.");
        }

        UserStory us = new UserStory();
        us.setIdentifier(identifier);
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
}