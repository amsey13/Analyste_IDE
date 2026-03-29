package com.example.backend.modules.projects.audit.dao;

import com.example.backend.modules.projects.audit.entity.Report;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    Optional<Report> findFirstByProjectIdOrderByCreationDateDesc(UUID projectId);

}
