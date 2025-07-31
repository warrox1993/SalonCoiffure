package com.jb.afrostyle.user.dto;

import com.jb.afrostyle.user.domain.enums.UserRole;

import java.time.LocalDateTime;

/**
 * DTO pour les informations utilisateur (sans données sensibles)
 * Utilisé pour les réponses API et l'affichage côté client
 * Ne contient jamais le mot de passe hashé
 * Migré vers Java Record pour réduire le boilerplate
 */
public record UserDTO(
    /**
     * Identifiant unique de l'utilisateur
     */
    Long id,

    /**
     * Nom d'utilisateur
     * Utilisé pour la connexion et l'affichage
     */
    String username,

    /**
     * Adresse email
     * Utilisée pour les notifications et la récupération de compte
     */
    String email,

    /**
     * Nom complet de l'utilisateur
     * Affiché dans l'interface utilisateur
     */
    String fullName,

    /**
     * Numéro de téléphone (optionnel)
     */
    String phone,

    /**
     * Rôle de l'utilisateur dans le système
     * Détermine les permissions et l'interface affichée
     */
    UserRole role,

    /**
     * Statut d'activation du compte
     * false = compte désactivé (ne peut pas se connecter)
     */
    Boolean isActive,

    /**
     * Statut de vérification de l'email
     * false = email non vérifié (fonctionnalités limitées)
     */
    Boolean emailVerified,

    /**
     * Date et heure de création du compte
     * Information pour l'administration et les statistiques
     */
    LocalDateTime createdAt
) {}