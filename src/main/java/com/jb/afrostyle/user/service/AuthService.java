package com.jb.afrostyle.user.service;

import com.jb.afrostyle.user.dto.*;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Interface pour les services d'authentification SESSION-BASED
 */
public interface AuthService {

    /**
     * Enregistre un nouvel utilisateur
     */
    UserDTO registerUser(RegisterRequest registerRequest) throws Exception;
    
    /**
     * Enregistre un nouvel utilisateur avec informations de requête HTTP
     */
    UserDTO registerUser(RegisterRequest registerRequest, HttpServletRequest request) throws Exception;

    /**
     * Authentifie un utilisateur (retourne Authentication pour le contrôleur)
     */
    org.springframework.security.core.Authentication authenticateUser(LoginRequest loginRequest) throws Exception;

    /**
     * Change le mot de passe d'un utilisateur
     */
    ApiResponse changePassword(Long userId, ChangePasswordRequest changePasswordRequest) throws Exception;

    /**
     * Initie la réinitialisation de mot de passe
     */
    ApiResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest) throws Exception;
    
    /**
     * Initie la réinitialisation de mot de passe avec informations de requête HTTP
     */
    ApiResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest, HttpServletRequest request) throws Exception;

    /**
     * Réinitialise le mot de passe avec token
     */
    ApiResponse resetPassword(String token, String newPassword) throws Exception;

    /**
     * Active un compte utilisateur
     */
    ApiResponse activateAccount(String token) throws Exception;

    /**
     * Renvoie un email d'activation
     */
    ApiResponse resendActivationEmail(String email) throws Exception;
    
    /**
     * Renvoie un email d'activation avec informations de requête HTTP
     */
    ApiResponse resendActivationEmail(String email, HttpServletRequest request) throws Exception;

    /**
     * Vérifie la disponibilité d'un nom d'utilisateur
     */
    boolean isUsernameAvailable(String username);

    /**
     * Vérifie la disponibilité d'un email
     */
    boolean isEmailAvailable(String email);

    /**
     * Déconnecte un utilisateur (invalidate session)
     */
    ApiResponse logout(HttpServletRequest request);

    /**
     * Récupère l'utilisateur connecté
     */
    UserDTO getCurrentUser(String username) throws Exception;
}