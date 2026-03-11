package com.example.backend.modules.projects.core.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.backend.core.auth.entity.User;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "projects")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name="project_type")
@Data
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;


    public LocalDateTime getDateCreation() {
        return createdAt;
    }

    public void setDateCreation(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdateAt(LocalDateTime updatedAt) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public void setBaseInfo(String name, String description, User user) {
        this.name = name;
        this.description = description;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

}
