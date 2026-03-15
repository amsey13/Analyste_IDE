package com.example.backend.integration;

import com.example.backend.core.auth.dao.UserRepository;
import com.example.backend.core.auth.entity.User;
import com.example.backend.modules.analysis.exporter.ClientHttp;
import com.example.backend.modules.analysis.exporter.HttpResponse;
import com.example.backend.modules.projects.audit.dao.AuditProjectRepository;
import com.example.backend.modules.projects.audit.dao.ReportRepository;
import com.example.backend.modules.projects.audit.entity.AuditProject;
import com.example.backend.modules.projects.audit.entity.Report;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;


import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class AuditIntegratonTest {


    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockitoBean
    private ClientHttp client;
    @Autowired
    private UserRepository userRepository;


    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private AuditProjectRepository auditProjectRepository;

    @BeforeEach
    public void setup() {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

    }


    @Test
    @Transactional
    public void testFullAuditFlow() throws Exception {

        User testUser = new User();
        testUser.setExternalId("testeur");
        testUser.setEmail("MamadyCorporation@org");
        userRepository.save(testUser);

        AuditProject project = new AuditProject();
        project.setName("Projet de Test");
        project.setUser(testUser);
        auditProjectRepository.saveAndFlush(project);

        UUID projectId = project.getIdProject();


        String aiJson = "{\"anomalies\": [{\"description\": \"Incohérence BPMN\", \"type\": \"TACHE_SANS_US\", \"severity\": \"HIGH\"}]}";
        String mockAiResponse = "{\"outputs\": [{\"content\": " + new ObjectMapper().writeValueAsString(aiJson) + "}]}";

        when(client.execute(anyString())).thenReturn(new HttpResponse(200, null, mockAiResponse));

        byte[] fakeXml = "<?xml version='1.0' encoding='UTF-8'?><root/>".getBytes();

        // Contenu pour tromper la désérialisation Java
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject("DUMMY_OBJECT");
        byte[] fakeObject = bos.toByteArray();

        mockMvc.perform(multipart("/api/audit/{projectId}/analyze", projectId)
                .file("bpmn", fakeXml)    // Pour BpmnParserStrategy
                .file("mcd", fakeObject)  // Pour tes objets sérialisés
                .file("mfc", fakeObject)  // Pour tes objets sérialisés
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )



                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(90.0)) //
                .andExpect(jsonPath("$.anomalies[0].description").value("Incohérence BPMN"));


        List<Report> savedReports = reportRepository.findAll();
        assertEquals(1, savedReports.size());

        Report mainReport = savedReports.get(0);
        // On vérifie que le rapport est bien lié au bon projet
        assertEquals(projectId, mainReport.getProject().getIdProject());
        assertEquals(90.0, mainReport.getScore());
        assertFalse(mainReport.getAnomalies().isEmpty());
    }

}
