package com.example.backend.modules.projects.audit.entity;

public enum SeverityLevel {
    LOW(5),
    MEDUIM(15),
    HIGH(30),
    CRITICAL(50)

    ;
    private final int malus;
    SeverityLevel(int malus) { this.malus = malus; }
    public int getMalus() { return malus; }
}
