package com.example.backend.modules.projects.acc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class DictionaryAssociationRequestDTO {

    @NotNull(message = "L'entité source est obligatoire")
    private UUID sourceId;

    @NotNull(message = "L'entité cible est obligatoire")
    private UUID targetId;

    @NotBlank(message = "Le nom de l'association est obligatoire")
    private String name;

    @NotBlank(message = "La cardinalité source est obligatoire")
    private String sourceMultiplicity;

    @NotBlank(message = "La cardinalité cible est obligatoire")
    private String targetMultiplicity;

    private Boolean isRelative;

    private Boolean isCif = false;

    private Boolean isInheritance = false;

    private UUID ruleId;





    public UUID getSourceId() {
        return sourceId;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
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
    public UUID getRuleId() {
        return ruleId;
    }
    public void setRuleId(UUID ruleId) {
        this.ruleId = ruleId;
    }
}