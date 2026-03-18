package com.example.backend.modules.projects.audit.entity;

public enum SeverityLevel {
    LOW(2),
    MEDIUM(5),
    HIGH(10),
    CRITICAL(20)

    ;
    private final int malus;
    SeverityLevel(int malus) { this.malus = malus; }
    public int getMalus() { return malus; }
}
