package com.example.backend.modules.projects.audit.entity;

import jakarta.persistence.*;

@Entity
@Table(name="format_fichier")
public class FormatFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private int id;

    @Column(unique=true,nullable=false)
    private String libelle;

    private String extension;

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

}
