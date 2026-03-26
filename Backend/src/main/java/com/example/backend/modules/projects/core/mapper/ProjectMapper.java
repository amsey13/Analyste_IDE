package com.example.backend.modules.projects.core.mapper;

import com.example.backend.modules.projects.core.dto.ProjectResponseDTO;
import com.example.backend.modules.projects.core.entity.Project;

public interface ProjectMapper {
    // Vérifie si ce mapper est capable de gérer ce type de projet
    boolean supports(Project project);

    // Transforme l'entité en DTO
    ProjectResponseDTO map(Project project);

    // Méthode par défaut pour éviter de réécrire le mapping des champs communs (DRY)
    default void mapBaseFields(Project source, ProjectResponseDTO target) {
        target.setIdProject(source.getIdProject());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setCreationDate(source.getDateCreation());
        target.setUpdateDate(source.getUpdatedAt());
    }
}