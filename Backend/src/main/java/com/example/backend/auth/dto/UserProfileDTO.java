package com.example.backend.auth.dto;

/**
 * Contrat d'API définissant exactement ce que le Front-End a le droit de recevoir.
 */


public record UserProfileDTO(
        String email,
        String fullName,
        String externalId
){}
