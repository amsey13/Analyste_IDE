package com.example.backend.modules.projects.acc.service;

import com.example.backend.core.auth.dao.UserRepository;
import com.example.backend.modules.projects.acc.dao.ActorRepository;
import com.example.backend.modules.projects.acc.dao.UserStoryRepository;
import com.example.backend.modules.projects.acc.entity.Actor;
import com.example.backend.modules.projects.acc.entity.SupportProject;
import com.example.backend.modules.projects.acc.entity.UserStory;
import com.example.backend.modules.projects.core.dao.ProjectRepository;
import com.example.backend.modules.projects.core.exception.ProjectNotFoundException;
import com.example.backend.modules.projects.core.exception.UnauthorizedAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SupportFeatureService {

    @Autowired
    private ActorRepository actorRepository;

    @Autowired
    private UserStoryRepository userStoryRepository;

    @Autowired
    private ProjectRepository projectRepository;

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
        getProjectAndCheckOwnership(actor.getProject().getIdProjet());

        actor.setName(newName);
        return actorRepository.save(actor);
    }

    @Transactional
    public void deleteActor(UUID actorId) {
        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new ProjectNotFoundException("Acteur introuvable"));

        getProjectAndCheckOwnership(actor.getProject().getIdProjet());

        actorRepository.delete(actor);
    }

    // --- Gestion des User Stories ---

    @Transactional
    public UserStory addUserStory(UUID projectId, UUID actorId, String identifier, String description) {
        SupportProject project = getProjectAndCheckOwnership(projectId);

        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new ProjectNotFoundException("Acteur introuvable"));

        // Sécurité supplémentaire : on vérifie que l'acteur appartient bien au bon projet
        if (!actor.getProject().getIdProjet().equals(projectId)) {
            throw new UnauthorizedAccessException("L'acteur spécifié n'appartient pas à ce projet.");
        }

        UserStory us = new UserStory();
        us.setIdentifier(identifier);
        us.setDescription(description);
        us.setActor(actor); //
        us.setProject(project); //
        return userStoryRepository.save(us);
    }

    @Transactional
    public UserStory updateUserStory(UUID usId, String identifier, String description) {
        UserStory us = userStoryRepository.findById(usId)
                .orElseThrow(() -> new ProjectNotFoundException("User Story introuvable"));

        getProjectAndCheckOwnership(us.getProject().getIdProjet());

        us.setIdentifier(identifier);
        us.setDescription(description);
        return userStoryRepository.save(us);
    }

    @Transactional
    public void deleteUserStory(UUID usId) {
        UserStory us = userStoryRepository.findById(usId)
                .orElseThrow(() -> new ProjectNotFoundException("User Story introuvable"));

        getProjectAndCheckOwnership(us.getProject().getIdProjet());

        userStoryRepository.delete(us);
    }

    @Transactional
    public SupportProject saveBpmnDiagram(UUID projectId, String bpmnXml) {
        SupportProject project = getProjectAndCheckOwnership(projectId);

        project.setBpmnXml(bpmnXml);
        return projectRepository.save(project);
    }
}