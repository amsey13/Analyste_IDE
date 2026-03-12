package com.example.backend.modules.projects.audit.mapper;

import com.example.backend.modules.projects.audit.dto.AuditProjectResponseDTO;
import com.example.backend.modules.projects.audit.entity.AuditProject;
import com.example.backend.modules.projects.core.dto.ProjectResponseDTO;
import com.example.backend.modules.projects.core.entity.Project;
import com.example.backend.modules.projects.core.mapper.ProjectMapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuditProjectMapper implements ProjectMapper {

    @Override
    public boolean supports(Project project) {
        return project instanceof AuditProject;
    }

    @Override
    public ProjectResponseDTO map(Project project) {
        AuditProject audit = (AuditProject) project;
        AuditProjectResponseDTO dto = new AuditProjectResponseDTO();

        // Mapping des champs communs (ID, Name, Desc, Dates)
        mapBaseFields(audit, dto);

        // Mapping spécifique à l'Audit
        dto.setProjectSlug(audit.getProjectSlug());

        // On indique si Taiga est configuré sans exposer le token
        dto.setTaigaLinked(audit.getTaigaToken() != null && !audit.getTaigaToken().isEmpty());

        return dto;
    }
}