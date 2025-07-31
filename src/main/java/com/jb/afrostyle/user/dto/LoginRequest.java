package com.jb.afrostyle.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO pour la demande de connexion
 * Permet la connexion avec nom d'utilisateur ou email
 * Migré vers Java Record pour réduire le boilerplate
 */
public record LoginRequest(
    /**
     * Nom d'utilisateur ou adresse email
     * L'utilisateur peut se connecter avec l'un ou l'autre
     */
    @NotNull
    @NotBlank(message = "Username or email is mandatory")
    String usernameOrEmail,
    
    /**
     * Mot de passe en clair
     * Sera comparé avec le hash stocké en base de données
     */
    @NotNull
    @NotBlank(message = "Password is mandatory")
    String password
) {}