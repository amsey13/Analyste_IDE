package com.example.backend.core.modules.projects.dao;

import com.example.backend.core.auth.entity.User;
import com.example.backend.core.modules.projects.entity.Project;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {


    Optional<Project> findByUserId(UUID userId);
    List<Project> findByUser(User user);
}
