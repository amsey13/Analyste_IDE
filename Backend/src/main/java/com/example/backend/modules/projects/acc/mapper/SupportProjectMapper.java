package com.example.backend.modules.projects.acc.mapper;

import com.example.backend.modules.projects.acc.dto.*;
import com.example.backend.modules.projects.acc.entity.*;
import com.example.backend.modules.projects.core.dto.ProjectResponseDTO;
import com.example.backend.modules.projects.core.entity.Project;
import com.example.backend.modules.projects.core.mapper.ProjectMapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SupportProjectMapper implements ProjectMapper {

    @Override
    public boolean supports(Project project) {
        return project instanceof SupportProject;
    }

    @Override
    public ProjectResponseDTO map(Project project) {
        SupportProject support = (SupportProject) project;
        SupportProjectResponseDTO dto = new SupportProjectResponseDTO();
        mapBaseFields(support, dto);
        dto.setStatus(support.getStatus() != null ? support.getStatus().name() : null);
        dto.setProjectType("accompagnement");
        dto.setBpmnXml(support.getBpmnXml());
        dto.setCoverageScore(support.getCoverageScore());
        if (support.getDictionaryEntries() != null) {
            dto.setDictionaryEntries(support.getDictionaryEntries().stream()
                    .map(this::mapToDictionaryEntryDTO)
                    .collect(Collectors.toList()));
        }

        if (support.getActors() != null) {
            dto.setActors(support.getActors().stream()
                    .map(this::mapToActorDTO)
                    .collect(Collectors.toList()));
        }

        if (support.getUserStories() != null) {
            dto.setUserStories(support.getUserStories().stream()
                    .map(this::mapToUserStoryDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }


    private DictionaryEntryResponseDTO mapToDictionaryEntryDTO(DictionaryEntry entry) {
        DictionaryEntryResponseDTO dto = new DictionaryEntryResponseDTO();
        dto.setId(entry.getId());
        dto.setName(entry.getName());
        dto.setDescription(entry.getDescription());

        if (entry.getAttributes() != null) {
            dto.setAttributes(entry.getAttributes().stream()
                    .map(this::mapToDictionaryAttributeDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private DictionaryAttributeResponseDTO mapToDictionaryAttributeDTO(DictionaryAttribute attr) {
        DictionaryAttributeResponseDTO dto = new DictionaryAttributeResponseDTO();
        dto.setId(attr.getId());
        dto.setName(attr.getName());
        dto.setDataType(attr.getDataType());
        dto.setSize(attr.getSize());
        dto.setPrimaryKey(attr.getPrimaryKey());
        dto.setNotNull(attr.getNotNull());
        dto.setDescription(attr.getDescription());
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

    private ActorResponseDTO mapToActorDTO(Actor actor) {
        ActorResponseDTO dto = new ActorResponseDTO();
        dto.setId(actor.getId());
        dto.setName(actor.getName());
        return dto;
    }
}