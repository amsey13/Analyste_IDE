package com.example.backend.modules.projects.acc.mapper;

import com.example.backend.modules.projects.acc.dto.SupportProjectResponseDTO;
import com.example.backend.modules.projects.acc.entity.SupportProject;
import com.example.backend.modules.projects.core.dto.ProjectResponseDTO;
import com.example.backend.modules.projects.core.entity.Project;
import com.example.backend.modules.projects.core.mapper.ProjectMapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // On veut qu'il soit testé AVANT le mapper par défaut
public class SupportProjectMapper implements ProjectMapper {

    @Override
    public boolean supports(Project project) {
        return project instanceof SupportProject;
    }

    @Override
    public ProjectResponseDTO map(Project project) {
        SupportProject support = (SupportProject) project;
        SupportProjectResponseDTO dto = new SupportProjectResponseDTO();

        // On remplit les champs communs via l'interface
        mapBaseFields(support, dto);

        // On remplit les champs spécifiques
        dto.setStatus(support.getStatus() != null ? support.getStatus().name() : null);
        dto.setBpmnXml(support.getBpmnXml());
        dto.setDataDictionary(support.getDataDictionary());
        dto.setActors(support.getActors());
        dto.setUserStories(support.getUserStories());
        dto.setCoverageScore(support.getCoverageScore());
        return dto;
    }
}