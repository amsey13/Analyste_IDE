package com.example.backend.modules.projects.audit.dao;

import com.example.backend.modules.projects.audit.entity.AuditProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AuditProjectRepository extends JpaRepository<AuditProject, UUID> {

    @Override
    Optional<AuditProject> findById(UUID uuid);

    @Query("SELECT COUNT(a) FROM AuditProject a WHERE EXISTS (SELECT r FROM Report r WHERE r.project.id = a.id)")
    long countCompletedAudits();
}
