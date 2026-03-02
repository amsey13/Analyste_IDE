package com.example.backend.core.modules.projects.dto;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProjetDTO {
    private UUID idProjet;

    private String nom;

    private String description;
    private LocalDateTime creationDate;
    private LocalDateTime modificationDate;

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setDateCreation(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

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

    public LocalDateTime getDateModification() {
        return modificationDate;
    }

    public void setDateModification(LocalDateTime modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }


}
