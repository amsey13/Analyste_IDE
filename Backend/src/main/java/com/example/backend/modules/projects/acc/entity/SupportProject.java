package com.example.backend.modules.projects.acc.entity;


import com.example.backend.modules.projects.core.entity.Project;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "data_dictionary", columnDefinition = "TEXT")
    private String dataDictionary;

    // Design Pattern "Composition" : Le projet gère le cycle de vie de ses dépendances (CascadeType.ALL)
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Actor> actors = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserStory> userStories = new ArrayList<>();
}
