package com.example.backend.modules.projects.acc.dao;

import com.example.backend.modules.projects.acc.entity.UserStory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserStoryRepository extends JpaRepository<UserStory, UUID> {

}