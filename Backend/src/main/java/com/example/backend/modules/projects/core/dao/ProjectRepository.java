package com.example.backend.modules.projects.core.dao;

import com.example.backend.core.auth.entity.User;
import com.example.backend.modules.projects.core.entity.Project;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query("SELECT COALESCE(ROUND((SUM(CASE WHEN p.projectType = 'ACCOMPAGNEMENT' THEN 1 ELSE 0 END) * 100.0) / NULLIF(COUNT(p), 0), 2), 0.0) FROM Project p")
    Double getAccompagnementAdoptionRate();
    Optional<Project> findByUserId(UUID userId);
    List<Project> findByUser(User user);
}
