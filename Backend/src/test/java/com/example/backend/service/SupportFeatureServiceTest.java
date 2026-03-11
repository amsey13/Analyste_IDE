package com.example.backend.service;

import com.example.backend.core.auth.entity.User;
import com.example.backend.modules.projects.acc.dao.ActorRepository;
import com.example.backend.modules.projects.acc.dao.UserStoryRepository;
import com.example.backend.modules.projects.acc.entity.Actor;
import com.example.backend.modules.projects.acc.entity.SupportProject;
import com.example.backend.modules.projects.acc.entity.UserStory;
import com.example.backend.modules.projects.acc.service.SupportFeatureService;
import com.example.backend.modules.projects.core.dao.ProjectRepository;
import com.example.backend.modules.projects.core.exception.UnauthorizedAccessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SupportFeatureServiceTest {

    @Mock private ActorRepository actorRepository;
    @Mock private UserStoryRepository userStoryRepository;
    @Mock private ProjectRepository projectRepository;

    @InjectMocks
    private SupportFeatureService supportService;

    // Helper pour créer un projet cohérent
    private SupportProject createMockProject(String ownerId) {
        SupportProject project = new SupportProject();
        project.setIdProjet(UUID.randomUUID()); // Génère un ID
        User owner = new User();
        owner.setExternalId(ownerId);
        project.setUser(owner);
        return project;
    }

    @Test
    void testAddActorSuccess() {
        SupportProject project = createMockProject("user1");
        UUID pid = project.getIdProjet(); // On utilise l'ID réel du projet mocké

        when(projectRepository.findById(pid)).thenReturn(Optional.of(project));

        try (MockedStatic<SecurityContextHolder> ms = mockStatic(SecurityContextHolder.class)) {
            mockAuth(ms, "user1");
            supportService.addActor(pid, "Client");
            verify(actorRepository).save(any(Actor.class));
        }
    }

    @Test
    void testAddUserStorySuccess() {
        SupportProject project = createMockProject("user1");
        UUID pid = project.getIdProjet(); // L'ID doit matcher
        UUID aid = UUID.randomUUID();

        Actor actor = new Actor();
        actor.setProject(project); // L'acteur appartient bien à ce projet

        when(projectRepository.findById(pid)).thenReturn(Optional.of(project));
        when(actorRepository.findById(aid)).thenReturn(Optional.of(actor));

        try (MockedStatic<SecurityContextHolder> ms = mockStatic(SecurityContextHolder.class)) {
            mockAuth(ms, "user1");
            supportService.addUserStory(pid, aid, "US1", "Desc");
            verify(userStoryRepository).save(any(UserStory.class));
        }
    }

    @Test
    void testAddUserStoryActorProjectMismatch() {
        SupportProject p1 = createMockProject("user1");
        UUID p1Id = p1.getIdProjet();

        SupportProject p2 = createMockProject("user1"); // Un autre projet
        UUID aid = UUID.randomUUID();

        Actor actor = new Actor();
        actor.setProject(p2); // L'acteur appartient à P2, pas à P1

        when(projectRepository.findById(p1Id)).thenReturn(Optional.of(p1));
        when(actorRepository.findById(aid)).thenReturn(Optional.of(actor));

        try (MockedStatic<SecurityContextHolder> ms = mockStatic(SecurityContextHolder.class)) {
            mockAuth(ms, "user1");
            // Doit lever l'exception car aid appartient à p2 et on essaie d'ajouter dans p1
            assertThrows(UnauthorizedAccessException.class, () ->
                    supportService.addUserStory(p1Id, aid, "US1", "X")
            );
        }
    }

    @Test
    void testSaveBpmnDiagram() {
        SupportProject project = createMockProject("user1");
        UUID pid = project.getIdProjet();

        when(projectRepository.findById(pid)).thenReturn(Optional.of(project));

        try (MockedStatic<SecurityContextHolder> ms = mockStatic(SecurityContextHolder.class)) {
            mockAuth(ms, "user1");
            supportService.saveBpmnDiagram(pid, "<xml/>");
            assertEquals("<xml/>", project.getBpmnXml());
            verify(projectRepository).save(project);
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