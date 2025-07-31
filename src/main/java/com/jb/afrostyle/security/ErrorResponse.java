package com.jb.afrostyle.security;

/**
 * Réponse d'erreur pour l'authentification
 * Migré vers Java Record pour réduire le boilerplate
 */
public record ErrorResponse(
    String error,
    String message,
    String timestamp
) {}