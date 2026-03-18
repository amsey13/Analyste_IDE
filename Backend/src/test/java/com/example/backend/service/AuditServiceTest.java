package com.example.backend.service;

import com.example.backend.modules.analysis.exporter.MistralService;
import com.example.backend.modules.projects.audit.dao.AnomalyTypeRepository;
import com.example.backend.modules.projects.audit.dao.AuditProjectRepository;
import com.example.backend.modules.projects.audit.dao.ReportRepository;
import com.example.backend.modules.projects.audit.dto.AnomalyDTO;
import com.example.backend.modules.projects.audit.entity.AuditProject;
import com.example.backend.modules.projects.audit.entity.Report;
import com.example.backend.modules.projects.audit.service.AuditService;
import com.example.backend.modules.projects.audit.taiga.service.TaigaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @Mock private AuditProjectRepository auditProjectRepository;
    @Mock private MistralService mistralService;
    @Mock private ReportRepository reportRepository;
    @Mock private AnomalyTypeRepository anomalyTypeRepository;
    @Mock private TaigaService taigaService;

    @InjectMocks
    private AuditService auditService;

    private UUID projectId;
    private AuditProject mockProject;

    @BeforeEach
    public void testSetupConfiguration() {
        projectId = UUID.randomUUID();
        mockProject = new AuditProject();
        mockProject.setIdProject(projectId);
        mockProject.setTaigaToken(null);
    }

    @Test
    public void testStartAuditShouldCreateReportWithAnomalies() throws Exception {

        when(auditProjectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));

        // Simulation of the AI's response
        List<AnomalyDTO> mockAnomalies = List.of(
                new AnomalyDTO("Description test", "INCOHERENCE", "CRITICAL", null)
        );
        when(mistralService.executeAuditAnalysis(any(), any(), any(), any())).thenReturn(mockAnomalies);


        when(anomalyTypeRepository.findByWording(anyString())).thenReturn(Optional.empty());
        when(reportRepository.save(any(Report.class))).thenAnswer(i -> i.getArguments()[0]);

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);


        Report result = auditService.startAudit(projectId, mockFile, null, null);


        assertNotNull(result);
        assertEquals(1, result.getAnomalies().size());
        assertEquals("Description test", result.getAnomalies().get(0).getDescription());
        verify(mistralService, times(1)).executeAuditAnalysis(any(), any(), any(), any());
    }

    @Test
    public void testStartAuditShouldCallTaigaWhenConfigured() throws Exception {

        mockProject.setTaigaToken("valid-token");
        mockProject.setProjectSlug("test-slug");
        when(auditProjectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));


        when(taigaService.getProjectIdBySlug(anyString(), anyString())).thenReturn(123);
        when(taigaService.getUserStories(anyInt(), anyString())).thenReturn(List.of());

        when(mistralService.executeAuditAnalysis(any(), any(), any(), any())).thenReturn(List.of());
        when(reportRepository.save(any(Report.class))).thenAnswer(i -> i.getArguments()[0]);


        auditService.startAudit(projectId, null, null, null);


        verify(taigaService, times(1)).getProjectIdBySlug(eq("test-slug"), eq("valid-token"));
    }

    @Test
    public void testStartAuditShouldThrowExceptionWhenProjectNotFound() {

        when(auditProjectRepository.findById(projectId)).thenReturn(Optional.empty());


        assertThrows(RuntimeException.class, () -> {
            auditService.startAudit(projectId, null, null, null);
        });
    }

    @Test
    public void testStartAuditShouldIncludeSuggestionsWhenProvidedByIA() throws Exception {

        when(auditProjectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));
        String sugg = "Voici comment corriger : liez la tâche à l'US #12.";


        AnomalyDTO mockDto = new AnomalyDTO("Anomalie avec aide", "TACHE_SANS_US", "MEDIUM",sugg);


        when(mistralService.executeAuditAnalysis(any(), any(), any(), any()))
                .thenReturn(List.of(mockDto));

        when(reportRepository.save(any(Report.class))).thenAnswer(i -> i.getArguments()[0]);
        when(anomalyTypeRepository.findByWording(anyString())).thenReturn(Optional.empty());

        Report result = auditService.startAudit(projectId, null, null, null);




        var savedAnomaly = result.getAnomalies().get(0);
        assertNotNull(savedAnomaly.getSuggestion(), "L'anomalie devrait avoir une suggestion liée");
        //assertEquals("Voici comment corriger : liez la tâche à l'US #12.", savedAnomaly.getSuggestion().getContent());
        // assertEquals(savedAnomaly, savedAnomaly.getSuggestion().getAnomaly());
    }









}
