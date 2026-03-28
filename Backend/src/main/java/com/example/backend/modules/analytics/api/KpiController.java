package com.example.backend.modules.analytics.api;

import com.example.backend.modules.analytics.dto.ProjectKpiDTO;
import com.example.backend.modules.analytics.service.KpiDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/kpi")
public class KpiController {



    private final KpiDashboardService kpiService;

    public KpiController(KpiDashboardService kpiService) {
        this.kpiService = kpiService;
    }

    /**
     * [KPI A02 & Technique] Statistiques globales de l'application
     * GET /api/kpi/global
     */
    @GetMapping("/global")
    public ResponseEntity<Map<String, Object>> getGlobalStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("accompagnementRate", kpiService.getGlobalAccompagnementRate());
        stats.put("averageIaTimeSeconds", kpiService.getAverageIaTime() / 1000.0);

        return ResponseEntity.ok(stats);
    }

    /**
     * [KPI A03 & A07] Statistiques d'évolution d'un projet spécifique
     * GET /api/kpi/project/{projectId}
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ProjectKpiDTO> getProjectEvolution(@PathVariable UUID projectId) {
        ProjectKpiDTO evolution = kpiService.calculateProjectEvolution(projectId);
        return ResponseEntity.ok(evolution);
    }
}