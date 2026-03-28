package com.example.backend.modules.analytics.dao;

import com.example.backend.modules.analytics.entity.LogExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Query("""
    SELECT 
        CAST(SUM(CASE WHEN l.status = 'FAILURE' THEN 1 ELSE 0 END) * 100.0 / COUNT(l) AS double)
    FROM LogExecution l 
    WHERE l.operation = :operationName AND l.startTime > :since
""")
    double getFailureRateByOperation(String operationName, LocalDateTime since);

    @Query("SELECT AVG(l.durationMs) FROM LogExecution l WHERE l.operation = 'PDF_GENERATION'")
    Double getAvgPdfGenerationTime();

    /**
     * Calcule le taux d'échec d'une opération spécifique pour un projet donné.
     * Retourne 0.0 si aucune opération n'a été effectuée.
     */
    @Query("""
        SELECT COALESCE(
            CAST(SUM(CASE WHEN l.status = 'FAILURE' THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(l), 0) AS double), 
            0.0)
        FROM LogExecution l 
        WHERE l.operation = :operation 
        AND l.projectId = :projectId
    """)
    double getFailureRateByOperation(@Param("operation") String operation, @Param("projectId") UUID projectId);

    /**
     * Calcule la durée moyenne en millisecondes pour une opération réussie.
     */
    @Query("""
        SELECT COALESCE(AVG(l.durationMs), 0.0) 
        FROM LogExecution l 
        WHERE l.operation = :operation 
        AND l.projectId = :projectId 
        AND l.status = 'SUCCESS'
    """)
    double getAverageDurationByOperation(@Param("operation") String operation, @Param("projectId") UUID projectId);

}