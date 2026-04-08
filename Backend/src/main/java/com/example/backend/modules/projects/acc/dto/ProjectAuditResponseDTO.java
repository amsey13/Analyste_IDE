package com.example.backend.modules.projects.acc.dto;

import java.util.List;

public class ProjectAuditResponseDTO {
    private int score;
    private List<String> inconsistencies;
    private List<String> corrections;

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public List<String> getInconsistencies() { return inconsistencies; }
    public void setInconsistencies(List<String> inconsistencies) { this.inconsistencies = inconsistencies; }

    public List<String> getCorrections() { return corrections; }
    public void setCorrections(List<String> corrections) { this.corrections = corrections; }
}