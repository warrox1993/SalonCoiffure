package com.jb.afrostyle.security;

/**
 * Réponse d'authentification compatible avec le frontend Angular
 * Simule une AuthResponse JWT tout en utilisant les sessions Spring Security
 * Migré vers Java Record pour réduire le boilerplate
 */
public record SessionAuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    String refreshToken,
    UserResponse user
) {}