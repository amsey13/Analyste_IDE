package com.example.backend.modules.analytics.dto;

public class ProjectKpiDTO {
    private int scoreEvolution;
    private double correctionRate;

    private double failureDeserialisationRate;
    private double failureTaigaExportRate;
    private double failurePdfGenerationRate;
    private double avgGenerationTime;
    private String systemHealth;
    private int totalAuditRun;



    public ProjectKpiDTO(int scoreEvolution, double correctionRate,
                         double failureDeserialisationRate, double failureTaigaExportRate,
                         double failurePdfGenerationRate, double avgGenerationTime,int totalAuditRun) {
        this.scoreEvolution = scoreEvolution;
        this.correctionRate = correctionRate;
        this.failureDeserialisationRate = failureDeserialisationRate;
        this.failureTaigaExportRate = failureTaigaExportRate;
        this.failurePdfGenerationRate = failurePdfGenerationRate;
        this.avgGenerationTime = avgGenerationTime;
        this.totalAuditRun = totalAuditRun;
        this.systemHealth = determineHealth();
    }

    private String determineHealth() {
        if (failureDeserialisationRate > 15 || failureTaigaExportRate > 30) return "CRITICAL";
        if (failureDeserialisationRate > 5 || failurePdfGenerationRate > 5 || avgGenerationTime > 5000) return "DEGRADED";
        return "EXCELLENT";
    }

    public double getAvgGenerationTime() {
        return avgGenerationTime;
    }

    public void setAvgGenerationTime(double avgGenerationTime) {
        this.avgGenerationTime = avgGenerationTime;
    }

    public void setCorrectionRate(double correctionRate) {
        this.correctionRate = correctionRate;
    }

    public double getFailureDeserialisationRate() {
        return failureDeserialisationRate;
    }

    public void setFailureDeserialisationRate(double failureDeserialisationRate) {
        this.failureDeserialisationRate = failureDeserialisationRate;
    }

    public double getFailurePdfGenerationRate() {
        return failurePdfGenerationRate;
    }

    public void setFailurePdfGenerationRate(double failurePdfGenerationRate) {
        this.failurePdfGenerationRate = failurePdfGenerationRate;
    }

    public double getFailureTaigaExportRate() {
        return failureTaigaExportRate;
    }

    public void setFailureTaigaExportRate(double failureTaigaExportRate) {
        this.failureTaigaExportRate = failureTaigaExportRate;
    }

    public void setScoreEvolution(int scoreEvolution) {
        this.scoreEvolution = scoreEvolution;
    }

    public String getSystemHealth() {
        return systemHealth;
    }

    public void setSystemHealth(String systemHealth) {
        this.systemHealth = systemHealth;
    }

    public int getTotalAuditRun() {
        return totalAuditRun;
    }

    public void setTotalAuditRun(int totalAuditRun) {
        this.totalAuditRun = totalAuditRun;
    }

    public int getScoreEvolution() {
        return scoreEvolution;
    }
    public double getCorrectionRate() {
        return correctionRate;
    }
}