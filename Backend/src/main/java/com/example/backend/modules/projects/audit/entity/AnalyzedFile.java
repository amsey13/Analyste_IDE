package com.example.backend.modules.projects.audit.entity;

import com.example.backend.modules.projects.core.entity.Project;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@IdClass(AnalyzedFileId.class)
public class AnalyzedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,name="nom_fichier")
    private String fileName;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_project")
    private Project project;

    @Column(nullable = false)
    private LocalDateTime uploadDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name="Format_id")
    private FormatFile formatFile;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FormatFile getFormatFile() {
        return formatFile;
    }

    public void setFormatFile(FormatFile formatFile) {
        this.formatFile = formatFile;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}
