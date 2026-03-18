package com.example.backend.modules.projects.core.mapper;

import com.example.backend.modules.projects.core.dto.ProjectResponseDTO;
import com.example.backend.modules.projects.core.entity.Project;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.LOWEST_PRECEDENCE) // Il s'exécute en dernier si aucun autre mapper ne "supporte" le projet
public class DefaultProjectMapper implements ProjectMapper {

    @Override
    public boolean supports(Project project) {
        // Il accepte tout ce qui est au moins un "Project"
        return true;
    }

    @Override
    public ProjectResponseDTO map(Project project) {
        ProjectResponseDTO dto = new ProjectResponseDTO();
        mapBaseFields(project, dto);
        return dto;
    }
}
