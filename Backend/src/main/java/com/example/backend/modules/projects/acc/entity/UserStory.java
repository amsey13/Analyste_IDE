package com.example.backend.modules.projects.acc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "user_story")

public class UserStory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String identifier;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(length = 500)
    private String benefit;

    public String getBenefit() {
        return benefit;
    }

    public void setBenefit(String benefit) {
        this.benefit = benefit;
    }

    public String getAcceptanceCriteria() {
        return acceptanceCriteria;
    }

    public void setAcceptanceCriteria(String acceptanceCriteria) {
        this.acceptanceCriteria = acceptanceCriteria;
    }

    @Column(columnDefinition = "TEXT")
    private String acceptanceCriteria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private Actor actor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_project_id", nullable = false)
    private SupportProject project;

    @PrePersist
    public void generateIdentifier() {
        if (this.identifier == null || this.identifier.isEmpty()) {
            String shortId = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            this.identifier = "US-" + shortId;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public SupportProject getProject() {
        return project;
    }

    public void setProject(SupportProject project) {
        this.project = project;
    }
}