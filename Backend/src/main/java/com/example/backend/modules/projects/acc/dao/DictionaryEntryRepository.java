package com.example.backend.modules.projects.acc.dao;

import com.example.backend.modules.projects.acc.entity.DictionaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DictionaryEntryRepository extends JpaRepository<DictionaryEntry, UUID> {
}
