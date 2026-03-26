package com.example.backend.modules.analytics.dao;

import com.example.backend.modules.analytics.entity.LogExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface LogExecutionRepository extends JpaRepository<LogExecution, UUID> {

    // Récupère les audits d'un projet par ordre chronologique (du plus vieux au plus récent)
    @Query("SELECT COALESCE(AVG(l.durationMs), 0) FROM LogExecution l WHERE l.operation = 'AUDIT_IA' AND l.status = 'SUCCESS'")
    Double getAverageIaDuration();
    List<LogExecution> findByProjectIdAndOperationAndStatusOrderByStartTimeAsc(
            UUID projectId, String operation, String status
    );
}