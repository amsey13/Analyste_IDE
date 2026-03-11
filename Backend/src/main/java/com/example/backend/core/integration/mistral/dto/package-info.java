/**
 * Contient les Objets de Transfert de Données (DTO) servant exclusivement
 * à la communication HTTP avec l'API externe Mistral AI.
 * <p>
 * Ces classes modélisent la structure exacte des requêtes JSON envoyées
 * et des réponses reçues (payloads). Elles garantissent que les changements
 * de format de l'API Mistral n'impactent que ce package, isolant ainsi
 * le reste de l'application de ces dépendances externes.
 * </p>
 */
package com.example.backend.core.integration.mistral.dto;