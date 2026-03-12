package com.example.backend.modules.projects.audit.entity;

import jakarta.persistence.*;

@Entity
@Table(name="type_anomalie")
public class AnomalyType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private int id;

    @Column
    private String wording;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWording() {
        return wording;
    }

    public void setWording(String wording) {
        this.wording = wording;
    }
}
