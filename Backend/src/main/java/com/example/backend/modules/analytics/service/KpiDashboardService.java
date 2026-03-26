package com.example.backend.modules.analytics.service;

import com.example.backend.modules.analytics.dto.ProjectKpiDTO;
import com.example.backend.modules.analytics.entity.LogExecution;
import com.example.backend.modules.analytics.dao.LogExecutionRepository;
import com.example.backend.modules.projects.core.dao.ProjectRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class KpiDashboardService {

    private final LogExecutionRepository logExecutionRepository;
    private final ProjectRepository projectRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    public KpiDashboardService(LogExecutionRepository logExecutionRepository,
                                ProjectRepository projectRepository) {
        this.logExecutionRepository = logExecutionRepository;
        this.projectRepository = projectRepository;
    }

    public ProjectKpiDTO calculateProjectEvolution(UUID projectId) {
        List<LogExecution> audits = logExecutionRepository
                .findByProjectIdAndOperationAndStatusOrderByStartTimeAsc(projectId, "AUDIT_IA", "SUCCESS");
        if (audits.size() < 2) {
            return new ProjectKpiDTO(0, 0.0);
        }

        try {

            JsonNode firstAuditDetails = objectMapper.readTree(audits.get(0).getDetails());
            int firstScore = firstAuditDetails.get("score").asInt();
            int firstAnomalies = firstAuditDetails.get("anomalies").asInt();

            JsonNode lastAuditDetails = objectMapper.readTree(audits.get(audits.size() - 1).getDetails());
            int lastScore = lastAuditDetails.get("score").asInt();
            int lastAnomalies = lastAuditDetails.get("anomalies").asInt();

            // --- CALCULS DES KPIS ---

            // KPI A07 : Évolution des performances
            int scoreEvolution = lastScore - firstScore;

            // KPI A03 : Taux de correction moyen
            double correctionRate = 0.0;
            if (firstAnomalies > 0) {
                int correctedAnomalies = firstAnomalies - lastAnomalies;
                correctionRate = ((double) correctedAnomalies / firstAnomalies) * 100.0;
                if (correctionRate < 0) correctionRate = 0.0;
            }
            return new ProjectKpiDTO(scoreEvolution, Math.round(correctionRate * 100.0) / 100.0);

        } catch (Exception e) {
            return new ProjectKpiDTO(0, 0.0);
        }
    }

    public Double getGlobalAccompagnementRate() {
        return projectRepository.getAccompagnementAdoptionRate();
    }

    public Double getAverageIaTime() {
        return logExecutionRepository.getAverageIaDuration();
    }
}