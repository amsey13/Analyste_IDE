package com.example.backend.modules.projects.acc.dto;

import lombok.Data;

@Data
public class ActorDTO {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}