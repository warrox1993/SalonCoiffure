package com.jb.afrostyle.user.dto;

import com.jb.afrostyle.user.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO pour la demande de changement de mot de passe
 * L'utilisateur doit fournir son ancien mot de passe pour sécurité
 */
public record ChangePasswordRequest(
    /**
     * Mot de passe actuel de l'utilisateur
     * Nécessaire pour vérifier l'identité avant le changement
     */
    @NotBlank(message = "Current password is mandatory")
    String currentPassword,

    /**
     * Nouveau mot de passe souhaité
     * Doit respecter les mêmes critères de sécurité que lors de l'inscription
     */
    @NotBlank(message = "New password is mandatory")
    @ValidPassword
    String newPassword
) {}