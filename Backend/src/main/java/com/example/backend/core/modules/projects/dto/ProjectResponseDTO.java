package com.example.backend.core.modules.projects.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProjectResponseDTO {
    private UUID idProjet;

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
        return idProjet;
    }

    public void setIdProjet(UUID id) {
        this.idProjet = id;
    }


    public String getNom() {
        return name;
    }

    public void setNom(String name) {
        this.name = name;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getModificationDate() {
        return updateDate;
    }

    public void setModificationDate(LocalDateTime modificationDate) {
        this.updateDate = modificationDate;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }
}
