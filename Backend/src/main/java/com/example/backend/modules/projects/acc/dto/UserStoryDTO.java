package com.example.backend.modules.projects.acc.dto;

import java.util.UUID;

public class UserStoryDTO {
    private String identifier;
    private String description;
    private String benefit;
    private String acceptanceCriteria;
    private UUID actorId;

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getBenefit() {
        return benefit;
    }
    public void setBenefit(String benefit) {this.benefit = benefit;}

    public String getAcceptanceCriteria() {
        return acceptanceCriteria;
    }
    public void setAcceptanceCriteria(String acceptanceCriteria) {this.acceptanceCriteria = acceptanceCriteria;}
}
