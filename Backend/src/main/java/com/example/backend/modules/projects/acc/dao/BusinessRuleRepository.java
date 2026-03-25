package com.example.backend.modules.projects.acc.dao;

import com.example.backend.modules.projects.acc.entity.BusinessRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BusinessRuleRepository extends JpaRepository<BusinessRule, UUID> {
    List<BusinessRule> findByProject_Id(UUID projectId);
}