package com.example.backend.modules.projects.acc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BusinessRuleRequestDTO {

    @NotBlank(message = "Le code est obligatoire")
    private String code;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}