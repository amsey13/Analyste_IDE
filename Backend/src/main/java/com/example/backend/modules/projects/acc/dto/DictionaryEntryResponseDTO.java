package com.example.backend.modules.projects.acc.dto;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DictionaryEntryResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private List<DictionaryAttributeResponseDTO> attributes = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DictionaryAttributeResponseDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<DictionaryAttributeResponseDTO> attributes) {
        this.attributes = attributes;
    }
}