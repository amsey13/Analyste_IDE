package com.example.backend.modules.projects.acc.mapper;

import com.example.backend.modules.projects.acc.dto.SupportProjectResponseDTO;
import com.example.backend.modules.projects.acc.dto.UserStoryResponseDTO;
import com.example.backend.modules.projects.acc.entity.SupportProject;
import com.example.backend.modules.projects.acc.entity.UserStory;
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

        // MAPPING DES USER STORIES (La partie cruciale)
        if (support.getUserStories() != null) {
            dto.setUserStories(support.getUserStories().stream()
                    .map(this::mapToUserStoryDTO)
                    .collect(java.util.stream.Collectors.toList()));
        }
        dto.setCoverageScore(support.getCoverageScore());
        return dto;
    }
    private UserStoryResponseDTO mapToUserStoryDTO(UserStory us) {
        UserStoryResponseDTO dto = new UserStoryResponseDTO();
        dto.setId(us.getId());
        dto.setIdentifier(us.getIdentifier());
        dto.setDescription(us.getDescription());
        dto.setBenefit(us.getBenefit());
        dto.setAcceptanceCriteria(us.getAcceptanceCriteria());

        if (us.getActor() != null) {
            dto.setActorId(us.getActor().getId());
            dto.setActorName(us.getActor().getName());
        }
        return dto;
    }
}