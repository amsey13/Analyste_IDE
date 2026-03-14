package com.example.backend.modules.projects.acc.api;

import com.example.backend.modules.projects.acc.dto.ActorDTO;
import com.example.backend.modules.projects.acc.dto.ActorResponseDTO;
import com.example.backend.modules.projects.acc.dto.UserStoryDTO;
import com.example.backend.modules.projects.acc.dto.UserStoryResponseDTO;
import com.example.backend.modules.projects.acc.entity.Actor;
import com.example.backend.modules.projects.acc.entity.UserStory;
import com.example.backend.modules.projects.acc.service.SupportFeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Contrôleur pour la gestion manuelle du mode Accompagnement.
 * Les exceptions (404, 403) sont gérées par le GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/support")
public class SupportFeatureController {

    @Autowired
    private SupportFeatureService supportService;

    // --- Gestion des Acteurs ---

    /**
     * Ajoute un acteur à un projet.
     * Vérifie la propriété du projet dans le service.
     */
    @PostMapping("/projects/{projectId}/actors")
    public ResponseEntity<ActorResponseDTO> addActor(
            @PathVariable UUID projectId,
            @RequestBody ActorDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supportService.addActor(projectId, dto.getName()));
    }

    /**
     * Modifie un acteur.
     */
    @PutMapping("/actors/{actorId}")
    public ResponseEntity<ActorResponseDTO> updateActor(
            @PathVariable UUID actorId,
            @RequestBody ActorDTO dto) {
        return ResponseEntity.ok(supportService.updateActor(actorId, dto.getName()));
    }

    /**
     * Supprime un acteur.
     */
    @DeleteMapping("/actors/{actorId}")
    public ResponseEntity<Void> deleteActor(@PathVariable UUID actorId) {
        supportService.deleteActor(actorId);
        return ResponseEntity.noContent().build();
    }

    // --- Gestion des User Stories ---

    /**
     * Ajoute une User Story liée à un acteur spécifique.
     * Vérifie la propriété du projet et le lien acteur-projet dans le service.
     */
    @PostMapping("/projects/{projectId}/actors/{actorId}/user-stories")
    public ResponseEntity<UserStoryResponseDTO> addUserStory(
            @PathVariable UUID projectId,
            @PathVariable UUID actorId,
            @RequestBody UserStoryDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supportService.addUserStory(projectId, actorId, dto.getDescription(), dto.getBenefit(), dto.getAcceptanceCriteria()));
    }

    /**
     * Met à jour une User Story.
     */
    @PutMapping("/user-stories/{usId}")
    public ResponseEntity<UserStoryResponseDTO> updateUserStory(
            @PathVariable UUID usId,
            @RequestBody UserStoryDTO dto) {
        return ResponseEntity.ok(supportService.updateUserStory(usId, dto.getDescription(), dto.getBenefit(), dto.getAcceptanceCriteria(), dto.getActorId()));
    }

    /**
     * Supprime une User Story.
     */
    @DeleteMapping("/user-stories/{usId}")
    public ResponseEntity<Void> deleteUserStory(@PathVariable UUID usId) {
        supportService.deleteUserStory(usId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/projects/{projectId}/bpmn")
    public ResponseEntity<Void> saveBpmn(
            @PathVariable UUID projectId,
            @RequestBody String bpmnXml) {
        supportService.saveBpmnDiagram(projectId, bpmnXml);
        return ResponseEntity.ok().build();
    }

}