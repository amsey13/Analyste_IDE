package com.example.backend.modules.projects.acc.entity;


import com.example.backend.modules.projects.core.entity.Project;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("ACCOMPAGNEMENT")
public class SupportProject extends Project {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProject status;
}
