package com.example.backend.acc.entity;

public enum StatusProject {

    INITIALISE("Initialisé"),
    EN_SPECIFICATION("En Specification"),
    EN_MODELISATION("En modelisation"),
    PRET_A_ANALYSER("Pret a analyser"),
    LIVRE("Livré");

    private final String label;

    StatusProject(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
