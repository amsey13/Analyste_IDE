package com.example.backend.modules.projects.acc.dto;

import java.util.UUID;

public class DictionaryAssociationResponseDTO {

    private UUID id;

    private UUID sourceId;
    private String sourceName;

    private UUID targetId;
    private String targetName;

    private String name;
    private String sourceMultiplicity;
    private String targetMultiplicity;
    private Boolean isRelative;
    private Boolean isCif ;
    private Boolean isInheritance;



    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceMultiplicity() {
        return sourceMultiplicity;
    }

    public void setSourceMultiplicity(String sourceMultiplicity) {
        this.sourceMultiplicity = sourceMultiplicity;
    }

    public String getTargetMultiplicity() {
        return targetMultiplicity;
    }

    public void setTargetMultiplicity(String targetMultiplicity) {
        this.targetMultiplicity = targetMultiplicity;
    }
    public Boolean getIsRelative() {
        return isRelative;
    }
    public void setIsRelative(Boolean isRelative) {
        this.isRelative = isRelative;
    }

    public Boolean getIsCif() {
        return isCif;
    }

    public void setIsCif(Boolean cif) {
        isCif = cif;
    }
    public Boolean getIsInheritance() {
        return isInheritance;
    }
    public void setIsInheritance(Boolean isInheritance) {
        this.isInheritance = isInheritance;
    }
}