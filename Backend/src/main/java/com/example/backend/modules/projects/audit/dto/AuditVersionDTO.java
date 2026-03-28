package com.example.backend.modules.projects.audit.dto;

import java.util.List;

public record AuditVersionDTO(
        int oldScore,
        int newScore,
        int scoreGap,
        List<AnomalyDTO> fixedAnomalies,
        List<AnomalyDTO> newAnomalies,
        List<AnomalyDTO> persistentAnomalies
) {
}
