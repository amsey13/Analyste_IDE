package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Attention, c'est UUID ici, pas IDENTITY
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name") // Fait le lien avec full_name en base
    private String fullName;

    @Column(name = "external_id", unique = true) // Fait le lien avec external_id en base
    private String externalId;

    @Column(name = "is_active")
    private boolean isActive = true;
}