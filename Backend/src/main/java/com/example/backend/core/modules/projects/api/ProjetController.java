package com.example.backend.core.modules.projects.api;

import com.example.backend.core.auth.exeption.UserNotFoundException;
import com.example.backend.core.modules.projects.dao.ProjetRepository;
import com.example.backend.core.modules.projects.dto.ProjetDTO;
import com.example.backend.core.modules.projects.service.ProjetService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/projects")
public class ProjetController {


    @Autowired
    private ProjetService projetService;


    @GetMapping
    public ResponseEntity<List<ProjetDTO>> getAllProjets() {

        return ResponseEntity.ok(projetService.getProjetsFromUser());

    }


    @PostMapping
    public ResponseEntity<ProjetDTO> createProjet(@RequestBody ProjetDTO projetDTO) throws UserNotFoundException {
        return ResponseEntity.ok(projetService.createProjet(projetDTO));
    }



}
