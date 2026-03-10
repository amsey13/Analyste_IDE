package com.example.backend.modules.projects.core.dto;

import com.example.backend.modules.projects.acc.dto.SupportProjectRequestDTO;
import com.example.backend.modules.projects.audit.dto.AuditProjectRequestDTO;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "project_type" // Le nom du champ dans ton JSON
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AuditProjectRequestDTO.class, name = "AUDIT"),
        @JsonSubTypes.Type(value = SupportProjectRequestDTO.class, name = "ACCOMPAGNEMENT")
})
public class BaseProjectRequestDTO {

    private String name;
    private String description;


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
