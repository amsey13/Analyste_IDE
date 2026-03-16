package com.example.backend.modules.projects.audit.entity;

import com.example.backend.core.util.AttributeEncryptor;
import com.example.backend.modules.projects.core.entity.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("AUDIT")
public class AuditProject extends Project {

    @Column(name = "taiga_token", columnDefinition = "TEXT")
    @Convert(converter = AttributeEncryptor.class)
    private String taigaToken;


    @Column(name="project_slug")
    private String projectSlug;


    public String getProjectSlug() {
        return projectSlug;
    }

    public void setProjectSlug(String projectSlug) {
        this.projectSlug = projectSlug;
    }

    public String getTaigaToken() {
        return taigaToken;
    }

    public void setTaigaToken(String taigaToken) {
        this.taigaToken = taigaToken;
    }
}
