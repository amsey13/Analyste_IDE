package com.example.backend.modules.projects.audit.entity;

public enum SeverityLevel {
    LOW(1),
    MEDIUM(3),
    HIGH(5),
    CRITICAL(10)

    ;
    private final int malus;
    SeverityLevel(int malus) { this.malus = malus; }
    public int getMalus() { return malus; }
}
