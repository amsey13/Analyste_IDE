package com.example.backend.core.auth.exception;

/**
 * Exception lancée en cas de défaut de configuration de la sécurité.
 */
public class SecurityConfigurationException extends RuntimeException {
    public SecurityConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}