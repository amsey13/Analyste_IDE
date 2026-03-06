package com.example.backend.modules.projects.acc.entity;

import com.example.backend.modules.projects.core.entity.Project;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ACCOMPAGNEMENT")
public class SupportProject extends Project {

    // Maybe there will be something later....
}
