package com.example.backend.modules.analytics.dto;

public class ProjectKpiDTO {
    private int scoreEvolution;
    private double correctionRate;

    public ProjectKpiDTO(int scoreEvolution, double correctionRate) {
        this.scoreEvolution = scoreEvolution;
        this.correctionRate = correctionRate;
    }

    public int getScoreEvolution() {
        return scoreEvolution;
    }
    public double getCorrectionRate() {
        return correctionRate;
    }
}