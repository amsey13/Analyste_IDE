package com.example.backend.core.modules.projects.dto;

public class ProjetRequestDTO {

    private String nom;
    private String description;
    private String taigaUserName;
    private String taigaPassword;
    private String taigaProjectUrl;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTaigaPassword() {
        return taigaPassword;
    }

    public void setTaigaPassword(String taigaPassword) {
        this.taigaPassword = taigaPassword;
    }

    public String getTaigaUserName() {
        return taigaUserName;
    }

    public void setTaigaUserName(String taigaUserName) {
        this.taigaUserName = taigaUserName;
    }

    public String getTaigaProjectUrl() {
        return taigaProjectUrl;
    }

    public void setTaigaProjectUrl(String taigaProjectUrl) {
        this.taigaProjectUrl = taigaProjectUrl;
    }
}
