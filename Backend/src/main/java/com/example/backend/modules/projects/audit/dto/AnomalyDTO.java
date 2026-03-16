package com.example.backend.modules.projects.audit.dto;

public record AnomalyDTO(
        String description,
        String type,
        String severity
) {
}
