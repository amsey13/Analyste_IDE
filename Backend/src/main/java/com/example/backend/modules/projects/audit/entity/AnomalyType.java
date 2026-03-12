package com.example.backend.modules.projects.audit.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name="type_anomalie")
public class AnomalyType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String wording;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getWording() {
        return wording;
    }

    public void setWording(String wording) {
        this.wording = wording;
    }
}
