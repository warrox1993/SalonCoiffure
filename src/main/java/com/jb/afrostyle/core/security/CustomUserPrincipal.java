package com.jb.afrostyle.core.security;

import com.jb.afrostyle.user.domain.enums.UserRole;
import com.jb.afrostyle.core.constants.SecurityConstants;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.Objects;

/**
 * Principal d'utilisateur centralisé pour AfroStyle
 * Version améliorée avec fonctionnalités de sécurité avancées
 * Compatible avec Spring Security et intégration complète du système
 * 
 * @version 2.0 - Centralisée dans /core
 * @since Java 21
 */
public class CustomUserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String email;
    private final String fullName;
    private final String password;
    private final UserRole role;
    private final boolean enabled;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean emailVerified;
    private final Collection<? extends GrantedAuthority> authorities;
    private final LocalDateTime createdAt;
    private final LocalDateTime lastLoginAt;
    private final String sessionId;
    
    // ==================== CONSTRUCTEURS ====================
    
    /**
     * Constructeur complet pour CustomUserPrincipal
     */
    public CustomUserPrincipal(
            Long id, 
            String username, 
            String email, 
            String fullName,
            String password, 
            UserRole role, 
            boolean enabled,
            boolean accountNonExpired,
            boolean accountNonLocked,
            boolean credentialsNonExpired,
            boolean emailVerified,
            Collection<? extends GrantedAuthority> authorities,
            LocalDateTime createdAt,
            LocalDateTime lastLoginAt,
            String sessionId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.emailVerified = emailVerified;
        this.authorities = authorities;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
        this.sessionId = sessionId;
    }
    
    /**
     * Constructeur simplifié (compatibilité avec l'ancien code)
     */
    public CustomUserPrincipal(
            Long id, 
            String username, 
            String email, 
            String password, 
            UserRole role, 
            boolean enabled, 
            Collection<? extends GrantedAuthority> authorities) {
        this(id, username, email, null, password, role, enabled, true, true, true, false,
             authorities, LocalDateTime.now(), null, null);
    }
    
    // ==================== MÉTHODES USERDETAILS ====================
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    // ==================== GETTERS PERSONNALISÉS ====================
    
    /**
     * Obtient l'ID de l'utilisateur
     * @return ID utilisateur
     */
    public Long getId() {
        return id;
    }
    
    /**
     * Obtient l'email de l'utilisateur
     * @return Email
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Obtient le nom complet de l'utilisateur
     * @return Nom complet
     */
    public String getFullName() {
        return fullName;
    }
    
    /**
     * Obtient le rôle de l'utilisateur
     * @return Rôle utilisateur
     */
    public UserRole getRole() {
        return role;
    }
    
    /**
     * Vérifie si l'email est vérifié
     * @return true si email vérifié
     */
    public boolean isEmailVerified() {
        return emailVerified;
    }
    
    /**
     * Obtient la date de création du compte
     * @return Date de création
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Obtient la date de dernière connexion
     * @return Date de dernière connexion
     */
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    /**
     * Obtient l'ID de session
     * @return ID de session
     */
    public String getSessionId() {
        return sessionId;
    }
    
    // ==================== MÉTHODES DE VÉRIFICATION DES RÔLES ====================
    
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
     * @return true si autorisé à réserver
     */
    public boolean canMakeBookings() {
        return enabled && emailVerified && (isCustomer() || isAdmin());
    }
    
    /**
     * Vérifie si l'utilisateur a des privilèges administrateur
     * @return true si privilèges admin
     */
    public boolean hasAdminPrivileges() {
        return isAdmin();
    }
    
    /**
     * Vérifie si l'utilisateur a une autorité spécifique
     * @param authority Autorité à vérifier
     * @return true si l'utilisateur a cette autorité
     */
    public boolean hasAuthority(String authority) {
        return authorities.stream()
                         .anyMatch(auth -> auth.getAuthority().equals(authority));
    }
    
    /**
     * Vérifie si l'utilisateur a au moins une des autorités spécifiées
     * @param authorities Autorités à vérifier
     * @return true si au moins une autorité présente
     */
    public boolean hasAnyAuthority(String... authorities) {
        for (String authority : authorities) {
            if (hasAuthority(authority)) {
                return true;
            }
        }
        return false;
    }
    
    // ==================== MÉTHODES DE VÉRIFICATION DE SÉCURITÉ ====================
    
    /**
     * Vérifie si le compte est pleinement opérationnel
     * @return true si compte valide et opérationnel
     */
    public boolean isFullyValid() {
        return enabled && accountNonExpired && accountNonLocked && 
               credentialsNonExpired && emailVerified;
    }
    
    /**
     * Vérifie si l'utilisateur peut accéder à un endpoint spécifique
     * @param endpoint Endpoint à vérifier
     * @return true si accès autorisé
     */
    public boolean canAccessEndpoint(String endpoint) {
        // Vérification des endpoints publics
        if (SecurityConstants.isPublicEndpoint(endpoint)) {
            return true;
        }
        
        // Vérification des endpoints admin
        if (SecurityConstants.requiresAdminPrivileges(endpoint)) {
            return hasAdminPrivileges();
        }
        
        // Vérification des endpoints salon owner
        if (SecurityConstants.requiresSalonOwnerPrivileges(endpoint)) {
            return canManageSalons();
        }
        
        // Par défaut, nécessite un compte valide
        return isFullyValid();
    }
    
    /**
     * Vérifie si l'utilisateur s'est connecté récemment
     * @return true si connexion récente (moins de 24h)
     */
    public boolean hasRecentLogin() {
        return lastLoginAt != null && 
               lastLoginAt.isAfter(LocalDateTime.now().minusHours(24));
    }
    
    /**
     * Vérifie si le compte nécessite une action (email non vérifié, etc.)
     * @return true si action requise
     */
    public boolean requiresAction() {
        return !emailVerified || !credentialsNonExpired;
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Crée un CustomUserPrincipal pour un administrateur
     * @param id ID utilisateur
     * @param username Nom d'utilisateur
     * @param email Email
     * @param fullName Nom complet
     * @param password Mot de passe
     * @return CustomUserPrincipal admin
     */
    public static CustomUserPrincipal createAdmin(
            Long id, String username, String email, String fullName, String password) {
        Set<GrantedAuthority> authorities = Set.of(
            new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("ROLE_USER")
        );
        
        return new CustomUserPrincipal(
            id, username, email, fullName, password, UserRole.ADMIN,
            true, true, true, true, true, authorities,
            LocalDateTime.now(), null, null
        );
    }
    
    /**
     * Crée un CustomUserPrincipal pour un propriétaire de salon
     * @param id ID utilisateur
     * @param username Nom d'utilisateur
     * @param email Email
     * @param fullName Nom complet
     * @param password Mot de passe
     * @return CustomUserPrincipal salon owner
     */
    public static CustomUserPrincipal createSalonOwner(
            Long id, String username, String email, String fullName, String password) {
        Set<GrantedAuthority> authorities = Set.of(
            new SimpleGrantedAuthority("ROLE_SALON_OWNER"),
            new SimpleGrantedAuthority("ROLE_USER")
        );
        
        return new CustomUserPrincipal(
            id, username, email, fullName, password, UserRole.SALON_OWNER,
            true, true, true, true, false, authorities,
            LocalDateTime.now(), null, null
        );
    }
    
    /**
     * Crée un CustomUserPrincipal pour un client
     * @param id ID utilisateur
     * @param username Nom d'utilisateur
     * @param email Email
     * @param fullName Nom complet
     * @param password Mot de passe
     * @return CustomUserPrincipal customer
     */
    public static CustomUserPrincipal createCustomer(
            Long id, String username, String email, String fullName, String password) {
        Set<GrantedAuthority> authorities = Set.of(
            new SimpleGrantedAuthority("ROLE_CUSTOMER"),
            new SimpleGrantedAuthority("ROLE_USER")
        );
        
        return new CustomUserPrincipal(
            id, username, email, fullName, password, UserRole.CUSTOMER,
            true, true, true, true, false, authorities,
            LocalDateTime.now(), null, null
        );
    }
    
    /**
     * Crée une copie avec dernière connexion mise à jour
     * @param lastLoginAt Nouvelle date de connexion
     * @return Nouvelle instance avec date mise à jour
     */
    public CustomUserPrincipal withLastLogin(LocalDateTime lastLoginAt) {
        return new CustomUserPrincipal(
            id, username, email, fullName, password, role, enabled,
            accountNonExpired, accountNonLocked, credentialsNonExpired,
            emailVerified, authorities, createdAt, lastLoginAt, sessionId
        );
    }
    
    /**
     * Crée une copie avec session ID mise à jour
     * @param sessionId Nouvel ID de session
     * @return Nouvelle instance avec session mise à jour
     */
    public CustomUserPrincipal withSessionId(String sessionId) {
        return new CustomUserPrincipal(
            id, username, email, fullName, password, role, enabled,
            accountNonExpired, accountNonLocked, credentialsNonExpired,
            emailVerified, authorities, createdAt, lastLoginAt, sessionId
        );
    }
    
    /**
     * Crée une copie avec email vérifié
     * @return Nouvelle instance avec email vérifié
     */
    public CustomUserPrincipal withEmailVerified() {
        return new CustomUserPrincipal(
            id, username, email, fullName, password, role, enabled,
            accountNonExpired, accountNonLocked, credentialsNonExpired,
            true, authorities, createdAt, lastLoginAt, sessionId
        );
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Convertit en String pour logging (sans informations sensibles)
     * @return Représentation string sécurisée
     */
    public String toSecureString() {
        return String.format("CustomUserPrincipal{id=%d, username='%s', role=%s, enabled=%s}", 
                           id, username, role, enabled);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomUserPrincipal that = (CustomUserPrincipal) o;
        return Objects.equals(id, that.id) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return toSecureString();
    }
}