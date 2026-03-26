package com.example.backend.modules.projects.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProjectResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;

    @JsonProperty("projectType")
    private String projectType;

    // --- Identification ---
    public UUID getIdProject() {
        return id;
    }

    public void setIdProject(UUID id) {
        this.id = id;
    }

    // --- Infos de base ---
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // --- Dates (Noms alignés sur le Mapper) ---
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    // --- Type (Pour le Front) ---
    @JsonProperty("projectType")
    public String getProjectType() {
        return projectType;
    }

    @JsonProperty("projectType")
    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }
}