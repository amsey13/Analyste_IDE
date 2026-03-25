package com.example.backend.modules.projects.acc.dao;

import com.example.backend.modules.projects.acc.entity.DictionaryAssociation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DictionaryAssociationRepository extends JpaRepository<DictionaryAssociation, UUID> {

    @Query("SELECT a FROM DictionaryAssociation a WHERE a.source.project.id = :projectId")
    List<DictionaryAssociation> findByProjectId(@Param("projectId") UUID projectId);
}