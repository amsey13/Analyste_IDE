package com.example.backend.modules.projects.acc.api;

import com.example.backend.modules.projects.acc.dto.*;
import com.example.backend.modules.projects.acc.entity.SupportProject;
import com.example.backend.modules.projects.acc.service.SupportFeatureService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<Map<String, Double>> saveBpmn(
            @PathVariable UUID projectId,
            @RequestBody String bpmnXml) {
        SupportProject updatedProject = supportService.saveBpmnDiagram(projectId, bpmnXml);
        Map<String, Double> response = new HashMap<>();
        response.put("coverageScore", updatedProject.getCoverageScore());

        return ResponseEntity.ok(response);
    }



    @PostMapping("/projects/{projectId}/dictionary-entries")
    public ResponseEntity<DictionaryEntryResponseDTO> addDictionaryEntry(
            @PathVariable UUID projectId,
            @RequestBody DictionaryEntryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supportService.addDictionaryEntry(projectId, dto));
    }

    @DeleteMapping("/dictionary-entries/{entryId}")
    public ResponseEntity<Void> deleteDictionaryEntry(@PathVariable UUID entryId) {
        supportService.deleteDictionaryEntry(entryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/dictionary-entries/{entryId}/attributes")
    public ResponseEntity<DictionaryAttributeResponseDTO> addDictionaryAttribute(
            @PathVariable UUID entryId,
            @RequestBody DictionaryAttributeRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supportService.addDictionaryAttribute(entryId, dto));
    }

    @DeleteMapping("/dictionary-attributes/{attrId}")
    public ResponseEntity<Void> deleteDictionaryAttribute(@PathVariable UUID attrId) {
        supportService.deleteDictionaryAttribute(attrId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/dictionary-entries/{entryId}")
    public ResponseEntity<DictionaryEntryResponseDTO> updateDictionaryEntry(
            @PathVariable UUID entryId,
            @RequestBody DictionaryEntryRequestDTO dto) {
        return ResponseEntity.ok(supportService.updateDictionaryEntry(entryId, dto));
    }

    @PutMapping("/dictionary-attributes/{attrId}")
    public ResponseEntity<DictionaryAttributeResponseDTO> updateDictionaryAttribute(
            @PathVariable UUID attrId,
            @RequestBody DictionaryAttributeRequestDTO dto) {
        return ResponseEntity.ok(supportService.updateDictionaryAttribute(attrId, dto));
    }

    @GetMapping("/projects/{projectId}/dictionary-suggestions")
    public ResponseEntity<List<DictionaryEntryRequestDTO>> getDictionarySuggestions(@PathVariable UUID projectId) {
        try {
            List<DictionaryEntryRequestDTO> suggestions = supportService.getDictionarySuggestions(projectId);
            return ResponseEntity.ok(suggestions);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/projects/{projectId}/associations")
    public ResponseEntity<List<DictionaryAssociationResponseDTO>> getAssociations(@PathVariable UUID projectId) {
        return ResponseEntity.ok(supportService.getAssociationsByProject(projectId));
    }

    @PostMapping("/projects/{projectId}/associations")
    public ResponseEntity<DictionaryAssociationResponseDTO> addAssociation(
            @PathVariable UUID projectId,
            @RequestBody @Valid DictionaryAssociationRequestDTO request) {
        return ResponseEntity.ok(supportService.addAssociation(projectId, request));
    }

    @PutMapping("/associations/{associationId}")
    public ResponseEntity<DictionaryAssociationResponseDTO> updateAssociation(
            @PathVariable UUID associationId,
            @RequestBody @Valid DictionaryAssociationRequestDTO request) {
        return ResponseEntity.ok(supportService.updateAssociation(associationId, request));
    }

    @DeleteMapping("/associations/{associationId}")
    public ResponseEntity<Void> deleteAssociation(@PathVariable UUID associationId) {
        supportService.deleteAssociation(associationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/projects/{projectId}/business-rules")
    public ResponseEntity<List<BusinessRuleResponseDTO>> getBusinessRules(@PathVariable UUID projectId) {
        return ResponseEntity.ok(supportService.getBusinessRules(projectId));
    }

    @PostMapping("/projects/{projectId}/business-rules")
    public ResponseEntity<BusinessRuleResponseDTO> addBusinessRule(
            @PathVariable UUID projectId,
            @Valid @RequestBody BusinessRuleRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supportService.addBusinessRule(projectId, request));
    }

    @DeleteMapping("/business-rules/{ruleId}")
    public ResponseEntity<Void> deleteBusinessRule(@PathVariable UUID ruleId) {
        supportService.deleteBusinessRule(ruleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/projects/{projectId}/generate-mcd")
    public ResponseEntity<Void> generateMcdFromRules(@PathVariable UUID projectId) {
        try {
            supportService.generateMcdFromBusinessRules(projectId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/projects/{projectId}/audit")
    public ResponseEntity<ProjectAuditResponseDTO> generateAudit(@PathVariable UUID projectId) {
        try {
            return ResponseEntity.ok(supportService.auditProject(projectId));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/projects/{projectId}/audit")
    public ResponseEntity<ProjectAuditResponseDTO> getAudit(@PathVariable UUID projectId) {
        try {
            ProjectAuditResponseDTO report = supportService.getLastAuditReport(projectId);
            if (report != null) {
                return ResponseEntity.ok(report);
            }
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/projects/{projectId}/export/mcd")
    public ResponseEntity<byte[]> exportProjectToMcd(@PathVariable UUID projectId) {
        byte[] mcdFileBytes = supportService.generateMcdFile(projectId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "Modele_JMerise.mcd");

        return ResponseEntity.ok()
                .headers(headers)
                .body(mcdFileBytes);
    }
}