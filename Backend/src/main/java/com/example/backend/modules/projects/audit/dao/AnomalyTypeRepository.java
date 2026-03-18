package com.example.backend.modules.projects.audit.dao;

import com.example.backend.modules.projects.audit.entity.AnomalyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AnomalyTypeRepository extends JpaRepository<AnomalyType, UUID> {

    Optional<AnomalyType> findByWording(String word);
}
