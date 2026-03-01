package com.example.backend.core.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Attention, c'est UUID ici, pas IDENTITY
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name") // Fait le lien avec full_name en base
    private String fullName;

    @Column(name = "external_id", unique = true) // Fait le lien avec external_id en base
    private String externalId;

    @Column(name = "is_active")
    private boolean isActive = true;

    @OneToMany(mappedBy = "user")
    private List<Projet> projets;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<Projet> getProjets() {
        return projets;
    }

    public void setProjets(List<Projet> projets) {
        this.projets = projets;
    }
}