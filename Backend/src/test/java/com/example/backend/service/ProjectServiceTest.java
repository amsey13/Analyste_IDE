package com.example.backend.service;

import com.example.backend.modules.projects.audit.dto.AuditProjectRequestDTO;
import com.example.backend.core.auth.dao.UserRepository;
import com.example.backend.core.auth.entity.User;
import com.example.backend.core.auth.exeption.UserNotFoundException;
import com.example.backend.modules.projects.core.dao.ProjectRepository;
import com.example.backend.modules.projects.core.dto.BaseProjectRequestDTO;
import com.example.backend.modules.projects.core.dto.ProjectResponseDTO;
import com.example.backend.modules.projects.core.entity.Project;
import com.example.backend.modules.projects.core.service.ProjectService;

import com.example.backend.modules.projects.audit.taiga.service.TaigaService;
import com.example.backend.modules.projects.audit.taiga.exception.IncorrectIdentifiersException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaigaService taigaService;

    @InjectMocks
    private ProjectService projectService;


    @Test
    public void testCreateProjectSuccessWithoutTaiga() throws UserNotFoundException, IncorrectIdentifiersException {

        BaseProjectRequestDTO request = new BaseProjectRequestDTO();
        request.setName("Test creation Projet");
        UUID id = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(id);

        //We mock the security

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {

            Authentication auth = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);

            //simulates the output of Spring's security elements
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn("use-external-id");
            when(userRepository.findByExternalId("use-external-id")).thenReturn(Optional.of(mockUser));
            when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArguments()[0]);

            ProjectResponseDTO projetResult = projectService.createProject(request);

            assertNotNull(projetResult);
            assertEquals("Test creation Projet", projetResult.getName());
            verify(projectRepository, times(1)).save(any(Project.class));

            verify(taigaService, never()).authenticate(anyString(), anyString());


        }
    }

    @Test
    public void testCreateProjectSuccessWithTaiga() throws UserNotFoundException, IncorrectIdentifiersException {
        AuditProjectRequestDTO request = new AuditProjectRequestDTO();
        request.setName("Projet Taiga");
        request.setTaigaUserName("user-taiga");
        request.setTaigaPassword("pass-taiga");
        request.setTaigaProjectUrl("https://tree.taiga.io/project/mon-super-projet");

        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            Authentication auth = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);

            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn("user-id");
            when(userRepository.findByExternalId("user-id")).thenReturn(Optional.of(mockUser));

            // Correction : anyString() garantit que le mock s'active, peu importe la valeur exacte
            when(taigaService.authenticate(anyString(), anyString())).thenReturn("fake-token");

            // Correction : any() au lieu de any(Project.class) pour bien capter l'instance AuditProject
            when(projectRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            ProjectResponseDTO result = projectService.createAuditProject(request);

            assertNotNull(result);
            verify(taigaService, times(1)).authenticate(anyString(), anyString());
            verify(projectRepository, times(1)).save(any());
        }
    }

    @Test
    public void testCreateProjectWithInvalidTaigaIdentifiersShouldThrownAnException() {
        AuditProjectRequestDTO request = new AuditProjectRequestDTO();
        request.setName("Test creation Projet");
        request.setTaigaUserName("mauvais-user");
        request.setTaigaPassword("mauvais-pass");
        request.setTaigaProjectUrl("https://tree.taiga.io/project/slug");

        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());

        try(MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)){
            Authentication auth = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);

            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn("user-id");

            when(userRepository.findByExternalId("user-id")).thenReturn(Optional.of(mockUser));

            // Correction : On renvoie null pour forcer TON code métier (if token == null) à jeter l'exception
            when(taigaService.authenticate(anyString(), anyString())).thenReturn(null);

            assertThrows(IncorrectIdentifiersException.class, () -> {
                projectService.createAuditProject(request);
            });
        } catch (IncorrectIdentifiersException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreateProjectWithUserNotFoundException() {

        BaseProjectRequestDTO request = new BaseProjectRequestDTO();
        request.setName("Projet Test");

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {

            Authentication auth = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);

            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn("unknown-id");

            when(userRepository.findByExternalId("unknown-id")).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> projectService.createProject(request));


        }
    }


    @Test
    public void testGetProjetsFromUserSucces() {

        User mockUser = new User();
        UUID id = UUID.randomUUID();
        mockUser.setId(id);

        List<Project> mockProjets = new ArrayList<>();
        Project mockProjet1 = new Project();
        mockProjet1.setName("Projet 1");
        Project mockProjet2 = new Project();
        mockProjet2.setName("Projet 2");
        mockProjets.add(mockProjet1);
        mockProjets.add(mockProjet2);

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {

            Authentication auth = mock(Authentication.class);
            SecurityContext context = mock(SecurityContext.class);

            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn("user-external-id");

            when(userRepository.findByExternalId("user-external-id")).thenReturn(Optional.of(mockUser));
            when(projectRepository.findByUser(mockUser)).thenReturn(mockProjets);

            List<ProjectResponseDTO> results = projectService.getProjectsFromUser();

            assertFalse(results.isEmpty());
            assertEquals(2, results.size());
        }


    }

    @Test
    public void testGetProjetsForEmptyList() {

        User mockUser = new User();

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn("user-external-id");

            when(userRepository.findByExternalId("user-external-id")).thenReturn(Optional.of(mockUser));

            when(projectRepository.findByUser(mockUser)).thenReturn(new ArrayList<>());

            List<ProjectResponseDTO> result = projectService.getProjectsFromUser();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }


    @Test
    void deleteProjetShouldDeleteWhenOwner() throws UserNotFoundException {
        // 1. Arrange
        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        String unique_Id = "id-pout-test";

        Project projet = new Project();
        projet.setIdProjet(projectId);

        User mockUser = new User();
        mockUser.setId(ownerId);
        projet.setUser(mockUser);
        mockUser.setExternalId(unique_Id);


        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projet));


        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);


            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(auth);


            when(auth.getName()).thenReturn(unique_Id);


            projectService.deleteProject(projectId);


            verify(projectRepository, times(1)).delete(projet);
        }
    }

    @Test
    void deleteProjetShouldThrowExceptionWhenNotOwner() {
        UUID projectId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Project projet = new Project();
        projet.setIdProjet(projectId);

        User mockUser = new User();
        mockUser.setId(ownerId);
        projet.setUser(mockUser);
        mockUser.setExternalId("owner-id-dans-db");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projet));

        // We mock the security so that it returns a different ID
        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            Authentication auth = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);

            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn("intruder-id");


            assertThrows(AccessDeniedException.class, () -> {
                projectService.deleteProject(projectId);
            });


            verify(projectRepository, never()).delete(any());
        }
    }
}
