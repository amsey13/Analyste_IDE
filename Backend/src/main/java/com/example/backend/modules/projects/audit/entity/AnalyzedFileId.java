package com.example.backend.modules.projects.audit.entity;

import java.io.Serializable;
import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AnalyzedFileId that = (AnalyzedFileId) o;
        return Objects.equals(id, that.id) && Objects.equals(project, that.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, project);
    }

    public void setProjectId(UUID projectId) {
        this.project = projectId;
    }


}
