package com.example.backend.core.modules.projects.entity;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.backend.core.auth.entity.User;

import jakarta.persistence.*;

@Entity
public class Projet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idProjet;


    @Column(name="Date_Creation")
    private LocalDateTime dateCreation;


    @Column(name="Date_derniere_Modification")
    private LocalDateTime dateModifiction;

    @Column
    private String description;

    @Column(nullable=false)
    private String nom;


    @ManyToOne(optional=false)
    @JoinColumn(name="user_id")
    private User user;


    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateModifiction() {
        return dateModifiction;
    }

    public void setDateModifiction(LocalDateTime dateModifiction) {
        this.dateModifiction = dateModifiction;
    }

    public UUID getIdProjet() {
        return idProjet;
    }

    public void setIdProjet(UUID idProjet) {
        this.idProjet = idProjet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
