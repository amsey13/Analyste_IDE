package com.example.backend.core.modules.projects.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.backend.core.auth.entity.User;

import com.example.backend.core.modules.projects.dto.AttributeEncryptor;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "projects")
@Data
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "taiga_token", columnDefinition = "TEXT")
    @Convert(converter = AttributeEncryptor.class)
    private String taigaToken;

    @Column(name = "project_slug")
    private String projectSlug;


    public LocalDateTime getDateCreation() {
        return createdAt;
    }

    public void setDateCreation(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDateModifiction() {
        return updatedAt;
    }

    public void setDateModifiction(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UUID getIdProjet() {
        return id;
    }

    public void setIdProjet(UUID idProject) {
        this.id = idProject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNom() {
        return name;
    }

    public void setNom(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTaigaToken() {
        return taigaToken;
    }

    public void setTaigaToken(String taigaToken) {
        this.taigaToken = taigaToken;
    }

    public String getSlugProject() {
        return projectSlug;
    }

    public void setSlugProject(String slugProject) {
        this.projectSlug = slugProject;
    }
}
