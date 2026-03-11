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
    public ProjectResponseDTO createProject(BaseProjectRequestDTO dto) throws UserNotFoundException, IncorrectIdentifiersException {

       User user = this.getAuthenticatedUser();
        Project projet = new Project();
        projet.setBaseInfo(dto.getName(), dto.getDescription(),user);
        projet.setDateCreation(LocalDateTime.now());
        return mapDTO(projectRepository.save(projet));
    }

    public ProjectResponseDTO createAuditProject(AuditProjectRequestDTO dto) throws UserNotFoundException, IncorrectIdentifiersException {
        User user = this.getAuthenticatedUser();
        AuditProject audit = new AuditProject();
        audit.setBaseInfo(dto.getName(), dto.getDescription(), user);
        audit.setDateCreation(LocalDateTime.now());

        boolean hasTaigaInformation = dto.getTaigaUserName() != null && !dto.getTaigaUserName().isEmpty()
                && dto.getTaigaPassword() != null && !dto.getTaigaPassword().isEmpty()
                && dto.getTaigaProjectUrl() != null && !dto.getTaigaProjectUrl().isEmpty();

        if(hasTaigaInformation){
            String slug = extractSlug(dto.getTaigaProjectUrl());
            String token = taigaService.authenticate(dto.getTaigaUserName(), dto.getTaigaPassword());

            if(token == null){
                throw new IncorrectIdentifiersException("Invalid Taiga Identifiers");
            }
            audit.setProjectSlug(slug);
            audit.setTaigaToken(token);
        }

        return mapDTO(projectRepository.save(audit));
    }

    private String extractSlug(String url) {
        if (url == null || !url.contains("project/")) {
            throw new IllegalArgumentException("Cannot extract slug from null or invalid URL");
        }
        return url.split("project/")[1].split("/")[0];

    }


    public ProjectResponseDTO createSupportProject(SupportProjectRequestDTO dto) throws UserNotFoundException {
        User user = this.getAuthenticatedUser();
        SupportProject supportProject = new SupportProject();

        supportProject.setBaseInfo(dto.getName(), dto.getDescription(), user);
        supportProject.setDateCreation(LocalDateTime.now());
        supportProject.setStatus(StatusProject.INITIALISE);

        return mapDTO(projectRepository.save(supportProject));
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


    /**
     * The function creates an audit project using the provided data and saves it to the repository
     * after setting necessary attributes.
     * 
     * @param dto AuditProjectRequestDTO dto
     * @param user The `user` parameter in the `createAuditProject` method is an instance of the `User`
     * class. It is used to set the user-related information for the `AuditProject` being created. This
     * information might include details like the user's name, email, role, or any other relevant
     * @return The method `createAuditProject` is returning a `ProjectResponseDTO` object.
     */
    private ProjectResponseDTO createAuditProject(AuditProjectRequestDTO dto, User user) throws UserNotFoundException, IncorrectIdentifiersException {

        AuditProject audit = new AuditProject();
        audit.setBaseInfo(dto.getName(), dto.getDescription(), user);
        audit.setDateCreation(LocalDateTime.now());

        boolean hasTaigaInformation = dto.getTaigaUserName() != null && !dto.getTaigaUserName().isEmpty()
                && dto.getTaigaPassword() != null && !dto.getTaigaPassword().isEmpty()
                && dto.getTaigaProjectUrl() != null && !dto.getTaigaProjectUrl().isEmpty();

        if(hasTaigaInformation){
            String slug = extractSlug(dto.getTaigaProjectUrl());
            String token = taigaService.authenticate(dto.getTaigaUserName(), dto.getTaigaPassword());

            if(token == null){
                throw new IncorrectIdentifiersException("Invalid Taiga Identifiers");
            }
            audit.setProjectSlug(slug);
            audit.setTaigaToken(token);
        }

        return mapDTO(projectRepository.save(audit));
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



}
