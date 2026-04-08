package com.example.backend.modules.projects.acc.dto;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DictionaryEntryRequestDTO {
    private UUID id;
    private String name;
    private String description;
    private List<DictionaryAttributeRequestDTO> attributes = new ArrayList<>();

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

    public List<DictionaryAttributeRequestDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<DictionaryAttributeRequestDTO> attributes) {
        this.attributes = attributes;
    }
}