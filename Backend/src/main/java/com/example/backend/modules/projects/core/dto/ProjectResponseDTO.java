package com.example.backend.modules.projects.core.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProjectResponseDTO {
    private UUID id;

    private String name;

    private String description;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;



    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getIdProjet() {
        return id;
    }

    public void setIdProjet(UUID id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getModificationDate() {
        return updateDate;
    }

    public void setUpdateDateDate(LocalDateTime modificationDate) {
        this.updateDate = modificationDate;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }
}
