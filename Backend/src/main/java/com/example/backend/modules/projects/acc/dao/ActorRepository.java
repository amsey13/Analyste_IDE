package com.example.backend.modules.projects.acc.dao;

import com.example.backend.modules.projects.acc.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActorRepository extends JpaRepository<Actor, UUID> {

}