package com.jb.afrostyle.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO pour la demande de réinitialisation de mot de passe
 * Utilisé quand l'utilisateur a oublié son mot de passe
 */
public record ForgotPasswordRequest(
    /**
     * Adresse email de l'utilisateur qui a oublié son mot de passe
     * Un email avec un lien de réinitialisation sera envoyé à cette adresse
     */
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    String email
) {}