package com.example.backend.acc.entity;

import com.example.backend.core.modules.projects.entity.Project;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ACCOMPAGNEMENT")
public class SupportProjects extends Project {

    // Maybe there will be something later....
}
