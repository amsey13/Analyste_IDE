package com.example.backend.modules.projects.api;

import com.example.backend.core.auth.exception.UserNotFoundException;
import com.example.backend.modules.projects.audit.dto.AuditProjectRequestDTO;
import com.example.backend.modules.projects.core.dto.BaseProjectRequestDTO;
import com.example.backend.modules.projects.core.dto.ProjectResponseDTO;
import com.example.backend.modules.projects.core.service.ProjectService;
import com.example.backend.modules.projects.acc.dto.SupportProjectRequestDTO;
import com.example.backend.modules.projects.audit.taiga.exception.IncorrectIdentifiersException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {


    @Autowired
    private ProjectService projectService;



    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects() {

        return ResponseEntity.ok(projectService.getProjectsFromUser());

    }




    @PostMapping("/audit")
    public ResponseEntity<ProjectResponseDTO> createAuditProject(@RequestBody AuditProjectRequestDTO auditDTO) throws UserNotFoundException, IncorrectIdentifiersException {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createAuditProject(auditDTO));
    }

    @PostMapping("/support")
    public ResponseEntity<ProjectResponseDTO> createSupportProject(@RequestBody SupportProjectRequestDTO dto) throws UserNotFoundException {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createSupportProject(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> deleteProject(@PathVariable UUID id) throws UserNotFoundException {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();

    }
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable UUID id) throws UserNotFoundException {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }





}
