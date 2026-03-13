package com.example.backend.modules.projects.audit.entity;

import jakarta.persistence.*;

import javax.print.attribute.standard.Severity;
import java.util.UUID;

@Entity
public class Anomaly {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeverityLevel severity;

    @ManyToOne
    @JoinColumn(name="type_anomalie_id", nullable = false)
    private AnomalyType anomalyType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rapport_id", nullable = false)
    private Report rapport;

    public UUID getId() {
        return id;
    }

    public AnomalyType getAnomalyType() {
        return anomalyType;
    }

    public void setAnomalyType(AnomalyType anomalyType) {
        this.anomalyType = anomalyType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Report getRapport() {
        return rapport;
    }

    public void setRapport(Report rapport) {
        this.rapport = rapport;
    }

    public SeverityLevel getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityLevel severity) {
        this.severity = severity;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
