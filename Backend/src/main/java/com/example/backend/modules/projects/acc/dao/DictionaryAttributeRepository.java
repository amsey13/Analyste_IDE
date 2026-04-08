package com.example.backend.modules.projects.acc.dao;

import com.example.backend.modules.projects.acc.entity.DictionaryAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DictionaryAttributeRepository extends JpaRepository<DictionaryAttribute, UUID> {}