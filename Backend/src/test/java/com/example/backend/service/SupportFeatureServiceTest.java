package com.example.backend.service;

import com.example.backend.core.auth.entity.User;
import com.example.backend.modules.analysis.parser.BpmnParserStrategy;
import com.example.backend.modules.projects.acc.dao.ActorRepository;
import com.example.backend.modules.projects.acc.dao.UserStoryRepository;
import com.example.backend.modules.projects.acc.dto.ActorResponseDTO;
import com.example.backend.modules.projects.acc.dto.UserStoryResponseDTO;
import com.example.backend.modules.projects.acc.entity.Actor;
import com.example.backend.modules.projects.acc.entity.SupportProject;
import com.example.backend.modules.projects.acc.entity.UserStory;
import com.example.backend.modules.projects.acc.service.SupportFeatureService;
import com.example.backend.modules.projects.core.dao.ProjectRepository;
import com.example.backend.modules.projects.core.exception.UnauthorizedAccessException;
import com.example.backend.modules.projects.acc.mapper.SupportProjectMapper;

// Remplace ces imports par les vrais chemins de ton projet si nécessaire
import com.example.backend.modules.projects.acc.dao.DictionaryEntryRepository;
import com.example.backend.modules.projects.acc.dao.DictionaryAttributeRepository;
import com.example.backend.modules.projects.acc.dao.DictionaryAssociationRepository;
import com.example.backend.modules.analysis.exporter.MistralService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SupportFeatureServiceTest {

    // 1. Déclaration de TOUS les mocks nécessaires pour le constructeur du service
    @Mock private ActorRepository actorRepository;
    @Mock private UserStoryRepository userStoryRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private DictionaryEntryRepository dictionaryEntryRepository;
    @Mock private DictionaryAttributeRepository dictionaryAttributeRepository;
    @Mock private DictionaryAssociationRepository associationRepository;
    @Mock private MistralService mistralService;

    // 2. Le service à tester (SANS @InjectMocks !)
    private SupportFeatureService supportService;

    // 3. Initialisation manuelle pour garantir que le Mapper n'est pas null
    @BeforeEach
    void setUp() {
        SupportProjectMapper realMapper = new SupportProjectMapper();

        supportService = new SupportFeatureService(
                actorRepository,
                userStoryRepository,
                projectRepository,
                realMapper, // <-- C'est ça qui corrige tes NullPointerException !
                dictionaryEntryRepository,
                dictionaryAttributeRepository,
                mistralService,
                associationRepository
        );
    }

    // Helper pour créer un projet cohérent (qui génère bien un UUID unique à chaque fois)
    private SupportProject createMockProject(String ownerId) {
        SupportProject project = new SupportProject();
        project.setIdProject(UUID.randomUUID()); // Génère un ID unique
        User owner = new User();
        owner.setExternalId(ownerId);
        project.setUser(owner);
        return project;
    }

    @Test
    void testAddActorSuccess() {
        SupportProject project = createMockProject("user1");
        UUID pid = project.getIdProject();
        String actorName = "Client";
        when(projectRepository.findById(pid)).thenReturn(Optional.of(project));

        when(actorRepository.save(any(Actor.class))).thenAnswer(invocation -> {
            Actor actorToSave = invocation.getArgument(0);
            if (actorToSave.getId() == null) {
                actorToSave.setId(UUID.randomUUID());
            }
            return actorToSave;
        });

        try (MockedStatic<SecurityContextHolder> ms = mockStatic(SecurityContextHolder.class)) {
            mockAuth(ms, "user1");
            ActorResponseDTO result = supportService.addActor(pid, actorName);

            assertNotNull(result);
            assertEquals(actorName, result.getName());
            assertNotNull(result.getId());
            verify(actorRepository).save(any(Actor.class));
        }
    }

    @Test
    void testAddUserStorySuccess() {
        SupportProject project = createMockProject("user1");
        UUID pid = project.getIdProject();
        UUID aid = UUID.randomUUID();

        Actor actor = new Actor();
        actor.setProject(project);
        actor.setId(aid);
        when(projectRepository.findById(pid)).thenReturn(Optional.of(project));
        when(actorRepository.findById(aid)).thenReturn(Optional.of(actor));

        when(userStoryRepository.save(any(UserStory.class))).thenAnswer(invocation -> {
            UserStory usToSave = invocation.getArgument(0);
            if (usToSave.getId() == null) {
                usToSave.setId(UUID.randomUUID());
            }
            return usToSave;
        });

        try (MockedStatic<SecurityContextHolder> ms = mockStatic(SecurityContextHolder.class)) {
            mockAuth(ms, "user1");
            UserStoryResponseDTO result = supportService.addUserStory(pid, aid,"Desc", "Benefit", "Acceptance");

            assertNotNull(result);
            assertNotNull(result.getId());
            verify(userStoryRepository).save(any(UserStory.class));
        }
    }

    @Test
    void testAddUserStoryActorProjectMismatch() {
        // Grâce au helper, p1 et p2 auront automatiquement des UUID différents !
        SupportProject p1 = createMockProject("user1");
        UUID p1Id = p1.getIdProject();

        SupportProject p2 = createMockProject("user1");
        UUID aid = UUID.randomUUID();

        Actor actor = new Actor();
        actor.setId(aid);
        actor.setProject(p2); // L'acteur appartient à P2, pas à P1

        when(projectRepository.findById(p1Id)).thenReturn(Optional.of(p1));
        when(actorRepository.findById(aid)).thenReturn(Optional.of(actor));

        try (MockedStatic<SecurityContextHolder> ms = mockStatic(SecurityContextHolder.class)) {
            mockAuth(ms, "user1");
            // Doit lever l'exception car aid appartient à p2 et on essaie d'ajouter dans p1
            assertThrows(UnauthorizedAccessException.class, () ->
                    supportService.addUserStory(p1Id, aid, "X","Y","Z")
            );
        }
    }

    @Test
    void testSaveBpmnDiagram() {
        SupportProject project = createMockProject("user1");
        UUID pid = project.getIdProject();

        when(projectRepository.findById(pid)).thenReturn(Optional.of(project));

        try (MockedStatic<SecurityContextHolder> ms = mockStatic(SecurityContextHolder.class)) {
            mockAuth(ms, "user1");
            supportService.saveBpmnDiagram(pid, "<xml/>");

            assertEquals("<xml/>", project.getBpmnXml());
            verify(projectRepository).save(project);
        }
    }

    @Test
    void testSaveBpmnDiagramWithCoverageCalculation() {
        SupportProject project = createMockProject("user1");
        UUID pid = project.getIdProject();

        UserStory us1 = new UserStory(); us1.setId(UUID.randomUUID());
        UserStory us2 = new UserStory(); us2.setId(UUID.randomUUID());
        project.setUserStories(List.of(us1, us2));

        when(projectRepository.findById(pid)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(SupportProject.class))).thenAnswer(i -> i.getArgument(0));

        try (MockedStatic<SecurityContextHolder> msAuth = mockStatic(SecurityContextHolder.class);
             MockedStatic<BpmnParserStrategy> msBpmn = mockStatic(BpmnParserStrategy.class)) {

            mockAuth(msAuth, "user1");

            msBpmn.when(() -> BpmnParserStrategy.extractLinkedUserStories(anyString()))
                    .thenReturn(Set.of(us1.getId().toString()));

            // Act
            supportService.saveBpmnDiagram(pid, "<bpmn>test</bpmn>");

            // Assert
            assertEquals("<bpmn>test</bpmn>", project.getBpmnXml(), "Le XML doit être sauvegardé");
            assertEquals(50.0, project.getCoverageScore(), "Le score doit être de 50.0% (1 US sur 2)");
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