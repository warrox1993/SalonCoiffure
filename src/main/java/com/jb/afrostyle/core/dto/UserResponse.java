package com.jb.afrostyle.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jb.afrostyle.user.domain.enums.UserRole;

import java.time.LocalDateTime;

/**
 * Réponse utilisateur centralisée pour l'authentification AfroStyle
 * Compatible avec l'interface User du frontend Angular
 * Version centralisée dans /core avec fonctionnalités étendues
 * 
 * @version 2.0 - Centralisée dans /core
 * @since Java 21
 */
public record UserResponse(
    /**
     * Identifiant unique de l'utilisateur
     */
    Long id,
    
    /**
     * Nom d'utilisateur unique
     */
    String username,
    
    /**
     * Adresse email de l'utilisateur
     */
    String email,
    
    /**
     * Nom complet de l'utilisateur
     */
    String fullName,
    
    /**
     * Numéro de téléphone (optionnel)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String phone,
    
    /**
     * Rôle de l'utilisateur dans le système
     */
    UserRole role,
    
    /**
     * Statut d'activation du compte
     */
    boolean isActive,
    
    /**
     * Statut de vérification de l'email
     */
    boolean emailVerified,
    
    /**
     * Date de création du compte
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    
    /**
     * Dernière connexion (optionnel)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime lastLoginAt,
    
    /**
     * Nombre de salons possédés (pour SALON_OWNER)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer salonCount,
    
    /**
     * Préférences utilisateur (optionnel)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    UserPreferences preferences
) {
    
    // ==================== CONSTRUCTEURS STATIQUES ====================
    
    /**
     * Crée une UserResponse basique sans informations optionnelles
     * @param id ID utilisateur
     * @param username Nom d'utilisateur
     * @param email Email
     * @param fullName Nom complet
     * @param role Rôle
     * @param isActive Statut actif
     * @param emailVerified Email vérifié
     * @param createdAt Date de création
     * @return UserResponse basique
     */
    public static UserResponse basic(
            Long id,
            String username, 
            String email,
            String fullName,
            UserRole role,
            boolean isActive,
            boolean emailVerified,
            LocalDateTime createdAt) {
        return new UserResponse(
            id, username, email, fullName, null, role, 
            isActive, emailVerified, createdAt, null, null, null
        );
    }
    
    /**
     * Crée une UserResponse complète avec toutes les informations
     * @param id ID utilisateur
     * @param username Nom d'utilisateur
     * @param email Email
     * @param fullName Nom complet
     * @param phone Téléphone
     * @param role Rôle
     * @param isActive Statut actif
     * @param emailVerified Email vérifié
     * @param createdAt Date de création
     * @param lastLoginAt Dernière connexion
     * @param salonCount Nombre de salons
     * @return UserResponse complète
     */
    public static UserResponse complete(
            Long id,
            String username,
            String email,
            String fullName,
            String phone,
            UserRole role,
            boolean isActive,
            boolean emailVerified,
            LocalDateTime createdAt,
            LocalDateTime lastLoginAt,
            Integer salonCount) {
        return new UserResponse(
            id, username, email, fullName, phone, role,
            isActive, emailVerified, createdAt, lastLoginAt, 
            salonCount, null
        );
    }
    
    /**
     * Crée une UserResponse pour un propriétaire de salon
     * @param id ID utilisateur  
     * @param username Nom d'utilisateur
     * @param email Email
     * @param fullName Nom complet
     * @param phone Téléphone
     * @param isActive Statut actif
     * @param emailVerified Email vérifié
     * @param createdAt Date de création
     * @param salonCount Nombre de salons possédés
     * @return UserResponse pour salon owner
     */
    public static UserResponse salonOwner(
            Long id,
            String username,
            String email,
            String fullName,
            String phone,
            boolean isActive,
            boolean emailVerified,
            LocalDateTime createdAt,
            Integer salonCount) {
        return new UserResponse(
            id, username, email, fullName, phone, UserRole.SALON_OWNER,
            isActive, emailVerified, createdAt, null, salonCount, null
        );
    }
    
    /**
     * Crée une UserResponse pour un client
     * @param id ID utilisateur
     * @param username Nom d'utilisateur
     * @param email Email
     * @param fullName Nom complet
     * @param phone Téléphone
     * @param isActive Statut actif
     * @param emailVerified Email vérifié
     * @param createdAt Date de création
     * @return UserResponse pour client
     */
    public static UserResponse customer(
            Long id,
            String username,
            String email,
            String fullName,
            String phone,
            boolean isActive,
            boolean emailVerified,
            LocalDateTime createdAt) {
        return new UserResponse(
            id, username, email, fullName, phone, UserRole.CUSTOMER,
            isActive, emailVerified, createdAt, null, null, null
        );
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Vérifie si l'utilisateur est un administrateur
     * @return true si ADMIN
     */
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
    
    /**
     * Vérifie si l'utilisateur est un propriétaire de salon
     * @return true si SALON_OWNER
     */
    public boolean isSalonOwner() {
        return role == UserRole.SALON_OWNER;
    }
    
    /**
     * Vérifie si l'utilisateur est un client
     * @return true si CUSTOMER
     */
    public boolean isCustomer() {
        return role == UserRole.CUSTOMER;
    }
    
    /**
     * Vérifie si l'utilisateur peut gérer des salons
     * @return true si ADMIN ou SALON_OWNER
     */
    public boolean canManageSalons() {
        return isAdmin() || isSalonOwner();
    }
    
    /**
     * Vérifie si l'utilisateur peut effectuer des réservations
     * @return true si le compte est actif et l'email vérifié
     */
    public boolean canMakeBookings() {
        return isActive && emailVerified;
    }
    
    /**
     * Vérifie si l'utilisateur a des privilèges administrateur
     * @return true si ADMIN
     */
    public boolean hasAdminPrivileges() {
        return isAdmin();
    }
    
    /**
     * Vérifie si l'utilisateur possède au moins un salon
     * @return true si salonCount > 0
     */
    public boolean ownsSalons() {
        return salonCount != null && salonCount > 0;
    }
    
    /**
     * Vérifie si l'utilisateur peut posséder plus de salons
     * @return true si salonCount < limite maximale
     */
    public boolean canOwnMoreSalons() {
        return salonCount == null || salonCount < 10; // BusinessConstants.MAX_SALONS_PER_OWNER
    }
    
    /**
     * Obtient le nombre de salons possédés (0 si null)
     * @return Nombre de salons
     */
    public int getSalonCount() {
        return salonCount != null ? salonCount : 0;
    }
    
    /**
     * Vérifie si l'utilisateur s'est connecté récemment (moins de 30 jours)
     * @return true si connexion récente
     */
    public boolean hasRecentLogin() {
        return lastLoginAt != null && 
               lastLoginAt.isAfter(LocalDateTime.now().minusDays(30));
    }
    
    /**
     * Crée une version "publique" de la réponse (sans informations sensibles)
     * @return UserResponse publique
     */
    public UserResponse toPublic() {
        return new UserResponse(
            id, username, null, fullName, null, role,
            isActive, emailVerified, createdAt, null, null, null
        );
    }
    
    /**
     * Ajoute les préférences utilisateur
     * @param preferences Préférences
     * @return Nouvelle UserResponse avec préférences
     */
    public UserResponse withPreferences(UserPreferences preferences) {
        return new UserResponse(
            id, username, email, fullName, phone, role,
            isActive, emailVerified, createdAt, lastLoginAt, 
            salonCount, preferences
        );
    }
    
    /**
     * Met à jour la dernière connexion
     * @param lastLoginAt Timestamp de dernière connexion
     * @return Nouvelle UserResponse avec lastLoginAt mis à jour
     */
    public UserResponse withLastLogin(LocalDateTime lastLoginAt) {
        return new UserResponse(
            id, username, email, fullName, phone, role,
            isActive, emailVerified, createdAt, lastLoginAt,
            salonCount, preferences
        );
    }
    
    /**
     * Met à jour le nombre de salons
     * @param salonCount Nouveau nombre de salons
     * @return Nouvelle UserResponse avec salonCount mis à jour
     */
    public UserResponse withSalonCount(Integer salonCount) {
        return new UserResponse(
            id, username, email, fullName, phone, role,
            isActive, emailVerified, createdAt, lastLoginAt,
            salonCount, preferences
        );
    }
    
    // ==================== RECORD IMBRIQUÉ - PRÉFÉRENCES ====================
    
    /**
     * Préférences utilisateur
     */
    public record UserPreferences(
        /**
         * Langue préférée (fr, en, nl, de)
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String language,
        
        /**
         * Fuseau horaire
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String timezone,
        
        /**
         * Recevoir notifications email
         */
        boolean emailNotifications,
        
        /**
         * Recevoir notifications SMS
         */
        boolean smsNotifications,
        
        /**
         * Mode sombre activé
         */
        boolean darkMode,
        
        /**
         * Format de date préféré
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String dateFormat
    ) {
        
        /**
         * Préférences par défaut
         * @return Préférences avec valeurs par défaut
         */
        public static UserPreferences defaults() {
            return new UserPreferences(
                "fr", "Europe/Brussels", true, false, false, "dd/MM/yyyy"
            );
        }
    }
    
    @Override
    public String toString() {
        return String.format("UserResponse{id=%d, username='%s', role=%s, active=%s}", 
                           id, username, role, isActive);
    }
}