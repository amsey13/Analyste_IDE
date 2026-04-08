package com.example.backend.modules.projects.acc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dictionary_entry")
public class DictionaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private SupportProject project;


    @OneToMany(mappedBy = "dictionaryEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DictionaryAttribute> attributes = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SupportProject getProject() {
        return project;
    }

    public void setProject(SupportProject project) {
        this.project = project;
    }

    public List<DictionaryAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<DictionaryAttribute> attributes) {
        this.attributes = attributes;
    }
}