package com.example.backend.core.modules.projects.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProjetResponseDTO {
    private UUID idProjet;

    private String nom;

    private String description;
    private LocalDateTime creationDate;
    private LocalDateTime modificationDate;



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
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(LocalDateTime modificationDate) {
        this.modificationDate = modificationDate;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }
}
