package com.jb.afrostyle.user.dto;

import com.jb.afrostyle.user.domain.enums.UserRole;
import com.jb.afrostyle.user.validation.ValidPassword;
import com.jb.afrostyle.user.validation.ValidPhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la demande d'inscription d'un nouvel utilisateur
 * Contient toutes les informations nécessaires pour créer un compte
 */
public record RegisterRequest(
    /**
     * Nom d'utilisateur souhaité (unique dans le système)
     * Doit contenir entre 3 et 20 caractères : lettres, chiffres, points, tirets, underscores
     */
    @NotBlank(message = "Username is mandatory")
    @Pattern(regexp = "^[a-zA-Z0-9._-]{3,20}$",
            message = "Username must be 3-20 characters and contain only letters, numbers, dots, underscores, and hyphens")
    String username,

    /**
     * Adresse email de l'utilisateur (unique dans le système)
     * Sera utilisée pour la récupération de mot de passe et les notifications
     */
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    String email,

    /**
     * Mot de passe en clair (sera hashé avant stockage)
     * Doit respecter les critères de sécurité définis dans ValidPassword
     */
    @NotBlank(message = "Password is mandatory")
    @ValidPassword
    String password,

    /**
     * Nom complet de l'utilisateur (optionnel)
     * Affiché dans l'interface utilisateur
     */
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    String fullName,

    /**
     * Numéro de téléphone (optionnel mais unique s'il est fourni)
     * Format belge ou international accepté
     */
    @ValidPhoneNumber
    String phone,

    /**
     * Rôle souhaité pour l'utilisateur
     * Détermine les permissions et fonctionnalités accessibles
     */
    @NotNull(message = "Role is mandatory")
    UserRole role
) {}