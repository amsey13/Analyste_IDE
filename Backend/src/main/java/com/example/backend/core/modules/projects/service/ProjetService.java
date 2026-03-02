package com.example.backend.core.modules.projects.service;

import com.example.backend.core.auth.dao.UserRepository;
import com.example.backend.core.auth.entity.User;
import com.example.backend.core.auth.exeption.UserNotFoundException;
import com.example.backend.core.modules.projects.dao.ProjetRepository;
import com.example.backend.core.modules.projects.dto.ProjetDTO;
import com.example.backend.core.modules.projects.entity.Projet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;

import java.util.List;

import java.util.stream.Collectors;

@Service
public class ProjetService {

    @Autowired
    private ProjetRepository projetRepository;

    @Autowired
    private UserRepository userRepository;


    
    /**
     * The `mapDTO` function takes a `Projet` object and maps its attributes to a `ProjetDTO` object.
     * 
     * @param projet The `mapDTO` method takes a `Projet` object as a parameter and maps its attributes
     * to a `ProjetDTO` object. The `Projet` class seems to have the following attributes:
     * @return The method `mapDTO` is returning a `ProjetDTO` object after mapping the properties from
     * a `Projet` object.
     */
    private ProjetDTO mapDTO(Projet projet){
        ProjetDTO dto = new ProjetDTO();
        dto.setIdProjet(projet.getIdProjet());
        dto.setNom(projet.getNom());
        dto.setDescription(projet.getDescription());
        dto.setDateCreation(projet.getDateCreation());
        dto.setDateModification(projet.getDateModifiction());
        return dto;
    }


  
    /**
     * This Java function retrieves projects associated with a user based on their external ID.
     * 
     * @return A list of `ProjetDTO` objects is being returned.
     */
    public List<ProjetDTO> getProjetsFromUser(){
        String externalId = SecurityContextHolder.getContext().getAuthentication().getName();

        
        User user = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        
        List<Projet> projets = projetRepository.findByUser(user);

        
        return projets.stream()
                .map(this::mapDTO)
                .collect(Collectors.toList());
    }

   
    /**
     * The function creates a new project using the provided data transfer object and associates it
     * with the authenticated user, then saves and returns the mapped project DTO.
     * 
     * @param dto The `dto` parameter in the `createProjet` method is of type `ProjetDTO`, which is a
     * Data Transfer Object representing project data. It contains information such as the project
     * name, description, and possibly other details related to a project. This method is responsible
     * for creating a new project
     * @return The `createProjet` method returns a `ProjetDTO` object after creating a new `Projet`
     * entity, saving it to the database, and mapping it to a DTO object.
     */
    public ProjetDTO createProjet(ProjetDTO dto) throws UserNotFoundException {

        String externalId = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Projet projet = new Projet();
        projet.setNom(dto.getNom());
        projet.setDescription(dto.getDescription());
        projet.setDateCreation(LocalDateTime.now());
        projet.setUser(user);

        Projet savedProjet = projetRepository.save(projet);
        return mapDTO(savedProjet);
    }




}
