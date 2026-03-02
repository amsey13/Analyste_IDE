package com.example.backend.core.modules.projects.dao;

import com.example.backend.core.auth.entity.User;
import com.example.backend.core.modules.projects.entity.Projet;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, UUID> {


    Optional<Projet> findByUserId(UUID userId);
    List<Projet> findByUser(User user);
}
