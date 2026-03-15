package com.example.backend.modules.projects.acc.dto;

import com.example.backend.modules.projects.acc.entity.Actor;
import com.example.backend.modules.projects.acc.entity.UserStory;
import com.example.backend.modules.projects.core.dto.ProjectResponseDTO;

import java.util.List;

public class SupportProjectResponseDTO extends ProjectResponseDTO {
    private String status;
    private String bpmnXml;
    private String dataDictionary;
    private Double coverageScore;

    public Double getCoverageScore() {
        return coverageScore;
    }

    public void setCoverageScore(Double coverageScore) {
        this.coverageScore = coverageScore;
    }

    private List<ActorResponseDTO> actors;
    private List<UserStoryResponseDTO> userStories;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBpmnXml() {
        return bpmnXml;
    }

    public void setBpmnXml(String bpmnXml) {
        this.bpmnXml = bpmnXml;
    }

    public String getDataDictionary() {
        return dataDictionary;
    }

    public void setDataDictionary(String dataDictionary) {
        this.dataDictionary = dataDictionary;
    }

    public List<ActorResponseDTO> getActors() {
        return actors;
    }

    public void setActors(List<ActorResponseDTO> actors) {
        this.actors = actors;
    }

    public List<UserStoryResponseDTO> getUserStories() {
        return userStories;
    }

    public void setUserStories(List<UserStoryResponseDTO> userStories) {
        this.userStories = userStories;
    }
}