package com.jb.afrostyle.security;

import com.jb.afrostyle.user.domain.enums.UserRole;

import java.time.LocalDateTime;

/**
 * Réponse utilisateur pour l'authentification
 * Compatible avec l'interface User du frontend Angular
 * Migré vers Java Record pour réduire le boilerplate
 */
public record UserResponse(
    Long id,
    String username,
    String email,
    String fullName,
    String phone,
    UserRole role,
    boolean isActive,
    boolean emailVerified,
    LocalDateTime createdAt
) {}