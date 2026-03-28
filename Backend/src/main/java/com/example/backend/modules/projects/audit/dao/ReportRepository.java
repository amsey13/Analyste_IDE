package com.example.backend.modules.projects.audit.dao;

import com.example.backend.modules.projects.audit.entity.Report;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findTop2ByProjectIdOrderByCreationDateDesc(UUID projectId);
}
