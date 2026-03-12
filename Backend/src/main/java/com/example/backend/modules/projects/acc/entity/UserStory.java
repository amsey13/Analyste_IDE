package com.example.backend.modules.projects.acc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "user_story")
@Getter
@Setter
public class UserStory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String identifier;

    @Column(nullable = false, length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private Actor actor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_project_id", nullable = false)
    private SupportProject project;

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