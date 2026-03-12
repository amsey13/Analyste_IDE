package com.example.backend.modules.projects.audit.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Anomaly {

    @Id
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
