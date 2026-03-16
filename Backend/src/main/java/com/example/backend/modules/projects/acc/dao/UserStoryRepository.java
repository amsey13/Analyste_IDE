package com.example.backend.modules.projects.acc.dao;

import com.example.backend.modules.projects.acc.entity.UserStory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserStoryRepository extends JpaRepository<UserStory, UUID> {
    @Modifying
    @Query("DELETE FROM UserStory u WHERE u.actor.id = :actorId")
    void deleteByActorId(@Param("actorId") UUID actorId);
}