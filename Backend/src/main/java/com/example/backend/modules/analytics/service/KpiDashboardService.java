package com.example.backend.modules.analytics.service;

import com.example.backend.modules.analytics.dto.ProjectKpiDTO;
import com.example.backend.modules.analytics.entity.LogExecution;
import com.example.backend.modules.analytics.dao.LogExecutionRepository;
import com.example.backend.modules.projects.acc.entity.StatusProject;
import com.example.backend.modules.projects.audit.dao.AuditProjectRepository;
import com.example.backend.modules.projects.core.dao.ProjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class KpiDashboardService {

    private final LogExecutionRepository logExecutionRepository;
    private final ProjectRepository projectRepository;
    private final AuditProjectRepository auditProjectRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    public KpiDashboardService(LogExecutionRepository logExecutionRepository,
                                ProjectRepository projectRepository,
                               AuditProjectRepository auditProjectRepository) {
        this.logExecutionRepository = logExecutionRepository;
        this.projectRepository = projectRepository;
        this.auditProjectRepository = auditProjectRepository;
    }

    /**
     * The function calculates various key performance indicators (KPIs) for a project based on log
     * data and audit information.
     * 
     * @param projectId The `calculateProjectEvolution` method calculates various key performance
     * indicators (KPIs) for a given project based on the provided `projectId`. It retrieves audit logs
     * for the project, failure rates for different operations, and average duration for a specific
     * operation.
     * @return The method `calculateProjectEvolution` returns a `ProjectKpiDTO` object that contains
     * various key performance indicators (KPIs) related to a specific project. These KPIs include:
     * - Score evolution
     * - Correction rate
     * - Failure rates for deserialization, Taiga export, and PDF generation
     * - Average duration for PDF generation
     * - Number of successful audit operations
     */
    public ProjectKpiDTO calculateProjectEvolution(UUID projectId) {
        List<LogExecution> audits = logExecutionRepository.findByProjectIdAndOperationAndStatusOrderByStartTimeAsc(projectId,"AUDIT_IA","SUCCESS");


        double failDeserial = logExecutionRepository.getFailureRateByOperation("DESERIALIZATION", projectId);
        double failTaiga = logExecutionRepository.getFailureRateByOperation("TAIGA_EXPORT", projectId);
        double failPdf = logExecutionRepository.getFailureRateByOperation("PDF_GENERATION", projectId);
        double avgTime = logExecutionRepository.getAverageDurationByOperation("PDF_GENERATION", projectId);

        if (audits.size() < 2) {
            return new ProjectKpiDTO(0, 0.0, failDeserial, failTaiga, failPdf, avgTime,audits.size());
        }

        try {

            int firstScore = extractIntFromDetails(audits.get(0).getDetails(), "score");
            int firstAnoms = extractIntFromDetails(audits.get(0).getDetails(), "anomalies");

            int lastScore = extractIntFromDetails(audits.get(audits.size() - 1).getDetails(), "score");
            int lastAnoms = extractIntFromDetails(audits.get(audits.size() - 1).getDetails(), "anomalies");

            int scoreEvolution = lastScore - firstScore;
            double correctionRate = (firstAnoms > 0) ?
                    Math.max(0, ((double) (firstAnoms - lastAnoms) / firstAnoms) * 100.0) : 0.0;


            return new ProjectKpiDTO(
                    scoreEvolution,
                    Math.round(correctionRate * 100.0) / 100.0,
                    failDeserial,
                    failTaiga,
                    failPdf,
                    avgTime,
                    audits.size()
            );

        } catch (Exception e) {
            System.err.println("Erreur technique lors du calcul KPI : " + e.getMessage());
            return new ProjectKpiDTO(0, 0.0, failDeserial, failTaiga, failPdf, avgTime,audits.size());
        }

    }
    
    /**
     * The function `extractIntFromDetails` takes a JSON string `details` and extracts an integer value
     * corresponding to the specified `field`.
     * 
     * @param details Details is a JSON string that contains information about a specific object or
     * entity. It could be something like:
     * @param field The `field` parameter in the `extractIntFromDetails` method is used to specify the
     * key or field name for which you want to extract an integer value from the `details` string.
     * @return The method `extractIntFromDetails` is returning an integer value extracted from the
     * specified field in the details string.
     */
    private int extractIntFromDetails(String details, String field) throws Exception {
        return objectMapper.readTree(details).get(field).asInt();
    }



    public Double getGlobalAccompagnementRate() {
        return projectRepository.getAccompagnementAdoptionRate();
    }

    public Double getAverageIaTime() {
        return logExecutionRepository.getAverageIaDuration();
    }

    public Double getGlobalCompletionRate() {
        long totalProjects = projectRepository.count();

        if (totalProjects == 0) {
            return 0.0;
        }
        long completedSupport = projectRepository.countSupportProjectsByStatus(StatusProject.LIVRE);
        long completedAudit = auditProjectRepository.countCompletedAudits();

        double rate = ((double) (completedSupport + completedAudit) / totalProjects) * 100.0;
        return Math.round(rate * 100.0) / 100.0;
    }
}