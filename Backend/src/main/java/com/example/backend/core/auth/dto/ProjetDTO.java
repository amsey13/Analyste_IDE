package com.example.backend.core.auth.dto;

import java.sql.Date;
import java.util.UUID;

public record ProjetDTO(
        UUID idProjet,
        String nom,
        String description,
        Date dateCreation,
        Date dateModification
) {
}
