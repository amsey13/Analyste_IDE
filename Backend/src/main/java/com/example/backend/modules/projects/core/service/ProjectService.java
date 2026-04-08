package com.example.backend.modules.projects.core.service;


import com.example.backend.modules.projects.audit.dto.AuditProjectRequestDTO;
import com.example.backend.modules.projects.audit.entity.AuditProject;
import com.example.backend.core.auth.dao.UserRepository;
import com.example.backend.core.auth.entity.User;
import com.example.backend.core.auth.exception.UserNotFoundException;
import com.example.backend.modules.projects.core.dao.ProjectRepository;
import com.example.backend.modules.projects.core.dto.BaseProjectRequestDTO;
import com.example.backend.modules.projects.core.dto.ProjectResponseDTO;
import com.example.backend.modules.projects.core.entity.Project;
import com.example.backend.modules.projects.acc.dto.SupportProjectRequestDTO;
import com.example.backend.modules.projects.acc.entity.SupportProject;
import com.example.backend.modules.projects.acc.entity.StatusProject;

import com.example.backend.modules.projects.audit.taiga.service.TaigaService;
import com.example.backend.modules.projects.audit.taiga.exception.IncorrectIdentifiersException;
import com.example.backend.modules.projects.core.mapper.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;

import java.util.List;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaigaService taigaService;
    private final List<ProjectMapper> mappers;
    public ProjectService(ProjectRepository projectRepository,
                          TaigaService taigaService,
                          UserRepository userRepository,
                          List<ProjectMapper> mappers) {
        this.projectRepository = projectRepository;
        this.taigaService = taigaService;
        this.userRepository = userRepository;
        this.mappers = mappers;
    }

    /**
     * The `mapDTO` function takes a `Projet` object and maps its attributes to a `ProjetDTO` object.
     *
     * @param projet The `mapDTO` method takes a `Projet` object as a parameter and maps its attributes
     * to a `ProjetDTO` object. The `Projet` class seems to have the following attributes:
     * @return The method `mapDTO` is returning a `ProjetDTO` object after mapping the properties from
     * a `Projet` object.
     */

    private ProjectResponseDTO mapDTO(Project projet) {
        return mappers.stream()
                .filter(mapper -> mapper.supports(projet))
                .findFirst()
                .map(mapper -> mapper.map(projet))
                .orElseThrow(() -> new RuntimeException("Erreur critique : Aucun mapper trouvé pour " + projet.getClass().getName()));
    }


  
    /**
     * This Java function retrieves projects associated with a user based on their external ID.
     * 
     * @return A list of `ProjetDTO` objects is being returned.
     */
    public List<ProjectResponseDTO> getProjectsFromUser(){
        String externalId = SecurityContextHolder.getContext().getAuthentication().getName();

        
        User user = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        
        List<Project> projets = projectRepository.findByUser(user);

        
        return projets.stream()
                .map(this::mapDTO)
                .collect(Collectors.toList());
    }

    private Boolean hasTaigaInformation(AuditProjectRequestDTO dto) {

        return dto.getTaigaUserName() != null && !dto.getTaigaUserName().isEmpty()
                && dto.getTaigaPassword() != null && !dto.getTaigaPassword().isEmpty()
                && dto.getTaigaProjectUrl() != null && !dto.getTaigaProjectUrl().isEmpty();
    }

    /**
     * The function `getAuthenticatedUser()` retrieves the authenticated user based on their external
     * ID or throws a `UserNotFoundException` if the user is not found.
     *
     * @return The method `getAuthenticatedUser()` is returning a `User` object.
     */
    private User getAuthenticatedUser() throws UserNotFoundException {
        String externalId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

   
    private <T extends Project> T prepareProject(T project, BaseProjectRequestDTO dto) throws UserNotFoundException {
        User user = getAuthenticatedUser();
        project.setBaseInfo(dto.getName(), dto.getDescription(), user);
        project.setDateCreation(LocalDateTime.now());
        return project;
    }



    private String extractSlug(String url) {
        if (url == null || !url.contains("project/")) {
            throw new IllegalArgumentException("Cannot extract slug from null or invalid URL");
        }
        return url.split("project/")[1].split("/")[0];

    }


    private void takeTaigaInformation(AuditProjectRequestDTO dto, AuditProject audit) throws IncorrectIdentifiersException {
        String slug = extractSlug(dto.getTaigaProjectUrl());
        String token = taigaService.authenticate(dto.getTaigaUserName(), dto.getTaigaPassword());

        if(token == null){
            throw new IncorrectIdentifiersException("Invalid Taiga Identifiers");
        }
        audit.setProjectSlug(slug);
        audit.setTaigaToken(token);
    }

    public ProjectResponseDTO createAuditProject(AuditProjectRequestDTO dto) throws UserNotFoundException, IncorrectIdentifiersException {

        AuditProject audit = this.prepareProject(new AuditProject(), dto);


        if(this.hasTaigaInformation(dto)){
            this.takeTaigaInformation(dto, audit);
        }

        return mapDTO(projectRepository.save(audit));
    }




    public ProjectResponseDTO createSupportProject(SupportProjectRequestDTO dto) throws UserNotFoundException {
       SupportProject support = this.prepareProject(new SupportProject(), dto);
        support.setStatus(StatusProject.INITIALISE);

        return mapDTO(projectRepository.save(support));
    }

    /**
     * This Java function deletes a project by its UUID after checking if the current user has
     * permission to delete it.
     * 
     * @param id The `id` parameter in the `deleteProject` method is of type `UUID` and represents the
     * unique identifier of the project that is to be deleted.
     */
    public void deleteProject(UUID id) throws UserNotFoundException {

        Project projet = projectRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Project not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = auth.getName();
        String userKey = projet.getUser().getExternalId();
        if (!userKey.equals(currentUserId)) {
            throw new AccessDeniedException("You're not allowed to delete this project.");
        }
        projectRepository.delete(projet);

    }

    public ProjectResponseDTO getProjectById(UUID id) throws UserNotFoundException {

        Project projet = projectRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Project not found with ID: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = auth.getName();
        String projectOwnerId = projet.getUser().getExternalId();

        if (!projectOwnerId.equals(currentUserId)) {
            throw new AccessDeniedException("You're not allowed to access this project.");
        }
        return mapDTO(projet);
    }






}
