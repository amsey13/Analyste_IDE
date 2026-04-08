package com.example.backend.modules.projects.core.mapper;

import com.example.backend.modules.projects.core.dto.ProjectResponseDTO;
import com.example.backend.modules.projects.core.entity.Project;

public interface ProjectMapper {

    boolean supports(Project project);


    ProjectResponseDTO map(Project project);


    default void mapBaseFields(Project source, ProjectResponseDTO target) {
        target.setIdProject(source.getIdProject());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setCreationDate(source.getDateCreation());
        target.setUpdateDate(source.getUpdatedAt());
    }
}