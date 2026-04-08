package com.example.backend.modules.projects.acc.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class BusinessRuleResponseDTO {

    private UUID id;
    private String code;
    private String description;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}