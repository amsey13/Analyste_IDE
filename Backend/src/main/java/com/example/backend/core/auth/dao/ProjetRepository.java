package com.example.backend.core.auth.dao;

import com.example.backend.core.entity.Projet;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, UUID> {

    List<Projet> findByUserId(UUID userId);
}
