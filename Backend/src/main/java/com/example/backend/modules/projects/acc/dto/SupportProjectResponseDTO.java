package com.example.backend.modules.projects.acc.dto;

import com.example.backend.modules.projects.acc.entity.Actor;
import com.example.backend.modules.projects.acc.entity.UserStory;
import com.example.backend.modules.projects.core.dto.ProjectResponseDTO;

import java.util.List;

public class SupportProjectResponseDTO extends ProjectResponseDTO {
    private String status;
    private String bpmnXml;
    private String dataDictionary;
    private List<Actor> actors;
    private List<UserStory> userStories;

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

    public List<Actor> getActors() {
        return actors;
    }

    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }

    public List<UserStory> getUserStories() {
        return userStories;
    }

    public void setUserStories(List<UserStory> userStories) {
        this.userStories = userStories;
    }
}