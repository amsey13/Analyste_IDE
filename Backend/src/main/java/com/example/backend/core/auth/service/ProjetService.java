package com.example.backend.core.auth.service;

import com.example.backend.core.auth.dao.ProjetRepository;
import com.example.backend.core.auth.dto.ProjetDTO;
import com.example.backend.core.entity.Projet;
import com.example.backend.core.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjetService {

    @Autowired
    private ProjetRepository projetRepository;


    private ProjetDTO mapDTO(Projet projet){
        ProjetDTO dto = new ProjetDTO();
        dto.setIdProjet(projet.getIdProjet());
        dto.setNom(projet.getNom());
        dto.setDescription(projet.getDescription());
        dto.setDateCreation(projet.getDateCreation());
        dto.setDateModification(projet.getDateModifiction());
        return dto;
    }


    public List<ProjetDTO> getProjetsByUserId(UUID userId) {

        return projetRepository.findByUserId(userId).stream()
                .map(this::mapDTO)
                .collect(Collectors.toList());

    }

    public ProjetDTO createProjet(ProjetDTO dto, User user) {
        Projet projet = new Projet();
        projet.setNom(dto.getNom());
        projet.setDescription(dto.getDescription());
        projet.setDateCreation(LocalDateTime.now());
        projet.setUser(user);

        Projet savedProjet = projetRepository.save(projet);
        return mapDTO(savedProjet);
    }




}
