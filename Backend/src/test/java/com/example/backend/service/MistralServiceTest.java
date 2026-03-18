package com.example.backend.service;


import com.example.backend.modules.analysis.exporter.ClientHttp;
import com.example.backend.modules.analysis.exporter.HttpResponse;
import com.example.backend.modules.analysis.exporter.MistralService;
import com.example.backend.modules.projects.audit.dto.AnomalyDTO;
import com.example.backend.modules.projects.audit.entity.Anomaly;
import com.example.backend.modules.projects.audit.entity.Report;
import com.example.backend.modules.projects.audit.entity.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.databind.ObjectMapper;
@ExtendWith(MockitoExtension.class)
public class MistralServiceTest {

    private MistralService mistralService;
    @Mock private RestTemplate restTemplate;
    @Mock private ClientHttp client;
    ObjectMapper mapper = new ObjectMapper();


    @BeforeEach
    public void setUp() {
        mistralService = new MistralService("fake_key_for_test",client);
    }

    @Test
    public void testExecuteAuditAnalysisShouldReturnDtoList() throws IOException {


        String innerJson = "{\"anomalies\": [{\"description\": \"Tache orpheline\", \"type\": \"TACHE_SANS_US\", \"severity\": \"HIGH\"}]}";


        String mockJsonResponse = """
        {
              "outputs": [
                {
                  "content": "%s"
                }
              ]
            }
        """.formatted(innerJson.replace("\"", "\\\""));


        HttpResponse fakeResponse = new HttpResponse(200,null, mockJsonResponse);

        when(client.execute(anyString())).thenReturn(fakeResponse);

        List<AnomalyDTO> result = mistralService.executeAuditAnalysis(
                "Contenu BPMN", "Contenu MCD", "Contenu MFC", "Contenu US"
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Tache orpheline", result.get(0).description());
        assertEquals("HIGH", result.get(0).severity());

    }

    @Test
    public void testCalculCoherenceScoreWhenNoAnamolies() {
        Report report = new Report();
        report.setAnomalies(new ArrayList<>());
        double scorePerfect = mistralService.calculScore(report);
        assertEquals(100.0, scorePerfect, "Un audit sans anomalie doit valoir 100");
    }

    @Test
    public void testCalculCoherenceScoreWhenSomeAnomalies() {
        Report report = new Report();


        Anomaly criticalAnomaly = new Anomaly();
        criticalAnomaly.setDescription("Erreur grave");
        criticalAnomaly.setSeverity(SeverityLevel.CRITICAL);


        Anomaly mediumAnomaly = new Anomaly();
        mediumAnomaly.setDescription("Erreur moyenne");
        mediumAnomaly.setSeverity(SeverityLevel.MEDIUM);

        report.setAnomalies(new ArrayList<>(List.of(criticalAnomaly, mediumAnomaly)));


        double scoreMixed = mistralService.calculScore(report);

        assertEquals(75.0, scoreMixed, "Le score devrait être de 75 (100 - 20 - 5)");
    }

    @Test
    public void testCalculCoherenceScoreWhenTooMuchAnomaliesWithSeverity() {

        Report report = new Report();
        List<Anomaly> anomalies = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Anomaly catastrophe = new Anomaly();
            catastrophe.setDescription("Catastrophe numéro " + i);
            catastrophe.setSeverity(SeverityLevel.CRITICAL);
            anomalies.add(catastrophe);
        }
        report.setAnomalies(anomalies);


        report.setAnomalies(anomalies);
        double scoreDisaster = mistralService.calculScore(report);
        assertTrue(scoreDisaster >= 0, "Le score ne doit jamais être négatif");
        assertEquals(0.0, scoreDisaster, "Le score doit plafonner à 0");

    }




}
