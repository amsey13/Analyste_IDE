package com.example.backend.service;

import com.example.backend.modules.projects.audit.dto.AuditProjectRequestDTO;
import com.example.backend.core.auth.dao.UserRepository;
import com.example.backend.core.auth.entity.User;
import com.example.backend.core.auth.exception.UserNotFoundException;
import com.example.backend.modules.projects.core.dao.ProjectRepository;
import com.example.backend.modules.projects.core.dto.BaseProjectRequestDTO;
import com.example.backend.modules.projects.core.dto.ProjectResponseDTO;
import com.example.backend.modules.projects.core.entity.Project;
import com.example.backend.modules.projects.core.service.ProjectService;
import com.example.backend.modules.projects.core.mapper.ProjectMapper;
import com.example.backend.modules.projects.acc.dto.SupportProjectRequestDTO;
import com.example.backend.modules.projects.audit.taiga.service.TaigaService;
import com.example.backend.modules.projects.audit.taiga.exception.IncorrectIdentifiersException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaigaService taigaService;
    @Mock private ProjectMapper projectMapper;

    private ProjectService projectService;
    private List<ProjectMapper> mappers;

    @BeforeEach
    void setUp() {
        mappers = new ArrayList<>();
        mappers.add(projectMapper);
        projectService = new ProjectService(projectRepository, taigaService, userRepository, mappers);
    }

    private void setupMapper(String name) {
        when(projectMapper.supports(any())).thenReturn(true);
        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setName(name);
        when(projectMapper.map(any())).thenReturn(dto);
    }

    // --- CRÉATION ---

    @Test
    void testCreateProjectSuccess() throws Exception {
        BaseProjectRequestDTO req = new BaseProjectRequestDTO();
        req.setName("Base");
        try (MockedStatic<SecurityContextHolder> ms = mockStatic(SecurityContextHolder.class)) {
            mockAuth(ms, "u1");
            when(userRepository.findByExternalId("u1")).thenReturn(Optional.of(new User()));
            when(projectRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            setupMapper("Base");
            assertNotNull(projectService.createProject(req));
        }
    }

    @Test
    void testCreateAuditProjectSuccess() throws Exception {
        AuditProjectRequestDTO req = new AuditProjectRequestDTO();
        req.setName("Audit");
        req.setTaigaUserName("u"); req.setTaigaPassword("p"); req.setTaigaProjectUrl("https://taiga.io/project/s");
        try (MockedStatic<SecurityContextHolder> ms = mockStatic(SecurityContextHolder.class)) {
            mockAuth(ms, "u1");
            when(userRepository.findByExternalId("u1")).thenReturn(Optional.of(new User()));
            when(taigaService.authenticate(anyString(), anyString())).thenReturn("token");
            when(projectRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            setupMapper("Audit");
            assertNotNull(projectService.createAuditProject(req));
        }
    }

    @Test
    void testCreateSupportProjectSuccess() throws Exception {
        SupportProjectRequestDTO req = new SupportProjectRequestDTO();
        req.setName("Accompagnement");
        try (MockedStatic<SecurityContextHolder> ms = mockStatic(SecurityContextHolder.class)) {
            mockAuth(ms, "u1");
            when(userRepository.findByExternalId("u1")).thenReturn(Optional.of(new User()));
            when(projectRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            setupMapper("Accompagnement");
            ProjectResponseDTO res = projectService.createSupportProject(req);
            assertEquals("Accompagnement", res.getName());
        }
    }

    @Test
    void testCreateAuditInvalidIdentifiers() {
        AuditProjectRequestDTO req = new AuditProjectRequestDTO();
        req.setTaigaUserName("b"); req.setTaigaPassword("b"); req.setTaigaProjectUrl("https://taiga.io/project/s");
        try (MockedStatic<SecurityContextHolder> ms = mockStatic(SecurityContextHolder.class)) {
            mockAuth(ms, "u1");
            when(userRepository.findByExternalId("u1")).thenReturn(Optional.of(new User()));
            when(taigaService.authenticate(anyString(), anyString())).thenReturn(null);
            assertThrows(IncorrectIdentifiersException.class, () -> projectService.createAuditProject(req));
        } catch (IncorrectIdentifiersException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testCreateProjectUserNotFound() {
        try (MockedStatic<SecurityContextHolder> ms = mockStatic(SecurityContextHolder.class)) {
            mockAuth(ms, "unknown");
            when(userRepository.findByExternalId("unknown")).thenReturn(Optional.empty());
            assertThrows(UserNotFoundException.class, () -> projectService.createProject(new BaseProjectRequestDTO()));
        }
    }

    // --- RÉCUPÉRATION ---

    @Test
    void testGetProjectsSuccess() {
        try (MockedStatic<SecurityContextHolder> ms = mockStatic(SecurityContextHolder.class)) {
            mockAuth(ms, "u1");
            User u = new User();
            when(userRepository.findByExternalId("u1")).thenReturn(Optional.of(u));
            when(projectRepository.findByUser(u)).thenReturn(List.of(new Project()));
            setupMapper("P1");
            assertEquals(1, projectService.getProjectsFromUser().size());
        }
    }

    @Test
    void testGetProjectsEmpty() {
        try (MockedStatic<SecurityContextHolder> ms = mockStatic(SecurityContextHolder.class)) {
            mockAuth(ms, "u1");
            User u = new User();
            when(userRepository.findByExternalId("u1")).thenReturn(Optional.of(u));
            when(projectRepository.findByUser(u)).thenReturn(new ArrayList<>());
            assertTrue(projectService.getProjectsFromUser().isEmpty());
        }
    }

    // --- SUPPRESSION ---

    @Test
    void testDeleteSuccess() throws Exception {
        UUID id = UUID.randomUUID();
        Project p = new Project();
        User u = new User(); u.setExternalId("o"); p.setUser(u);
        when(projectRepository.findById(id)).thenReturn(Optional.of(p));
        try (MockedStatic<SecurityContextHolder> ms = mockStatic(SecurityContextHolder.class)) {
            mockAuth(ms, "o");
            projectService.deleteProject(id);
            verify(projectRepository).delete(p);
        }
    }

    @Test
    void testDeleteForbidden() {
        UUID id = UUID.randomUUID();
        Project p = new Project();
        User u = new User(); u.setExternalId("o"); p.setUser(u);
        when(projectRepository.findById(id)).thenReturn(Optional.of(p));
        try (MockedStatic<SecurityContextHolder> ms = mockStatic(SecurityContextHolder.class)) {
            mockAuth(ms, "hacker");
            assertThrows(AccessDeniedException.class, () -> projectService.deleteProject(id));
        }
    }

    private void mockAuth(MockedStatic<SecurityContextHolder> ms, String id) {
        Authentication a = mock(Authentication.class);
        SecurityContext c = mock(SecurityContext.class);
        ms.when(SecurityContextHolder::getContext).thenReturn(c);
        when(c.getAuthentication()).thenReturn(a);
        when(a.getName()).thenReturn(id);
    }
}