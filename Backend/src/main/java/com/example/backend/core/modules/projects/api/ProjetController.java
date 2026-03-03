package com.example.backend.core.modules.projects.api;

import com.example.backend.core.auth.exeption.UserNotFoundException;
import com.example.backend.core.modules.projects.dto.ProjetRequestDTO;
import com.example.backend.core.modules.projects.dto.ProjetResponseDTO;
import com.example.backend.core.modules.projects.service.ProjetService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/projects")
public class ProjetController {


    @Autowired
    private ProjetService projetService;


    @GetMapping
    public ResponseEntity<List<ProjetResponseDTO>> getAllProjets() {

        return ResponseEntity.ok(projetService.getProjetsFromUser());

    }


    @PostMapping
    public ResponseEntity<ProjetResponseDTO> createProjet(@RequestBody ProjetRequestDTO projetDTO) throws UserNotFoundException {
        return ResponseEntity.ok(projetService.createProjet(projetDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProjetResponseDTO> deleteProjet(@PathVariable UUID id) throws UserNotFoundException {
        projetService.deleteProject(id);
        return ResponseEntity.ok(new ProjetResponseDTO());

    }





}
