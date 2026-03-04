package com.example.backend.core.modules.projects.api;

import com.example.backend.core.auth.exeption.UserNotFoundException;
import com.example.backend.core.modules.projects.dto.ProjectRequestDTO;
import com.example.backend.core.modules.projects.dto.ProjectResponseDTO;
import com.example.backend.core.modules.projects.service.ProjectService;

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


    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(@RequestBody ProjectRequestDTO projetDTO) throws UserNotFoundException {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProjet(projetDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> deleteProject(@PathVariable UUID id) throws UserNotFoundException {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();

    }





}
