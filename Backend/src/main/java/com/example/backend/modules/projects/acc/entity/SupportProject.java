package com.example.backend.modules.projects.acc.entity;


import com.example.backend.modules.projects.core.entity.Project;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "support_project")
@Getter
@Setter
@DiscriminatorValue("ACCOMPAGNEMENT")
public class SupportProject extends Project {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProject status = StatusProject.INITIALISE;

    @Column(name = "bpmn_xml", columnDefinition = "TEXT")
    private String bpmnXml;

    @Column(name = "coverage_score")
    private Double coverageScore = 0.0;

    @Column(name = "last_audit_report", columnDefinition = "TEXT")
    private String lastAuditReport;



    public Double getCoverageScore() {
        return coverageScore;
    }

    public void setCoverageScore(Double coverageScore) {
        this.coverageScore = coverageScore;
    }

    // Design Pattern "Composition" : Le projet gère le cycle de vie de ses dépendances (CascadeType.ALL)
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Actor> actors = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserStory> userStories = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DictionaryEntry> dictionaryEntries = new ArrayList<>();

    public List<DictionaryEntry> getDictionaryEntries() {
        return dictionaryEntries;
    }

    public void setDictionaryEntries(List<DictionaryEntry> dictionaryEntries) {
        this.dictionaryEntries = dictionaryEntries;
    }

    public StatusProject getStatus() {
        return status;
    }

    public void setStatus(StatusProject status) {
        this.status = status;
    }

    public String getBpmnXml() {
        return bpmnXml;
    }

    public void setBpmnXml(String bpmnXml) {
        this.bpmnXml = bpmnXml;
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

    public String getLastAuditReport() {
        return lastAuditReport;
    }

    public void setLastAuditReport(String lastAuditReport) {
        this.lastAuditReport = lastAuditReport;
    }
}
