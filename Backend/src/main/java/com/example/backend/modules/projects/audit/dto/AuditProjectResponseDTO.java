package com.example.backend.modules.projects.audit.dto;

import com.example.backend.modules.projects.core.dto.ProjectResponseDTO;

public class AuditProjectResponseDTO extends ProjectResponseDTO {
    private String projectSlug;
    private boolean taigaLinked;

    public String getProjectSlug() {
        return projectSlug;
    }

    public void setProjectSlug(String projectSlug) {
        this.projectSlug = projectSlug;
    }

    public boolean isTaigaLinked() {
        return taigaLinked;
    }

    public void setTaigaLinked(boolean taigaLinked) {
        this.taigaLinked = taigaLinked;
    }
}