package com.example.backend.modules.projects.audit.entity;

import java.io.Serializable;
import java.util.UUID;

public class AnalyzedFileId implements Serializable {

    private UUID id;
    private UUID project;


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProjectId() {
        return project;
    }

    public void setProjectId(UUID projectId) {
        this.project = projectId;
    }
}
