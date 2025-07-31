package com.jb.afrostyle.core.security;

import com.jb.afrostyle.user.domain.enums.UserRole;
import com.jb.afrostyle.core.constants.SecurityConstants;
import com.jb.afrostyle.core.exception.BusinessException;
import com.jb.afrostyle.core.enums.EntityType;
import com.jb.afrostyle.core.enums.Operation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Contexte de sécurité centralisé pour AfroStyle
 * Fournit un accès unifié aux informations de sécurité et d'authentification
 * Intègre avec Spring Security et notre système CustomUserPrincipal
 * 
 * @version 1.0
 * @since Java 21
 */
@Component
public class SecurityContext {
    
    // ==================== ACCÈS AU CONTEXTE SPRING SECURITY ====================
    
    /**
     * Obtient l'authentification courante
     * @return Optional contenant l'authentification ou vide si non authentifié
     */
    public Optional<Authentication> getCurrentAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Optional.ofNullable(auth);
    }
    
    /**
     * Obtient le principal d'authentification courant
     * @return Optional contenant le principal ou vide
     */
    public Optional<Object> getCurrentPrincipal() {
        return getCurrentAuthentication()
                .map(Authentication::getPrincipal);
    }
    
    /**
     * Obtient le CustomUserPrincipal courant
     * @return Optional contenant CustomUserPrincipal ou vide
     */
    public Optional<CustomUserPrincipal> getCurrentUserPrincipal() {
        return getCurrentPrincipal()
                .filter(CustomUserPrincipal.class::isInstance)
                .map(CustomUserPrincipal.class::cast);
    }
    
    /**
     * Obtient les détails de l'utilisateur courant
     * @return Optional contenant UserDetails ou vide
     */
    public Optional<UserDetails> getCurrentUserDetails() {
        return getCurrentPrincipal()
                .filter(UserDetails.class::isInstance)
                .map(UserDetails.class::cast);
    }
    
    // ==================== INFORMATIONS UTILISATEUR ====================
    
    /**
     * Obtient l'ID de l'utilisateur courant
     * @return Optional contenant l'ID utilisateur ou vide
     */
    public Optional<Long> getCurrentUserId() {
        return getCurrentUserPrincipal()
                .map(CustomUserPrincipal::getId);
    }
    
    /**
     * Obtient l'ID de l'utilisateur courant (requis)
     * @return ID de l'utilisateur courant
     * @throws BusinessException si utilisateur non authentifié
     */
    public Long getCurrentUserIdRequired() {
        return getCurrentUserId()
                .orElseThrow(() -> BusinessException.unauthorized(
                    "User not authenticated", null, null));
    }
    
    /**
     * Obtient le nom d'utilisateur courant
     * @return Optional contenant le nom d'utilisateur ou vide
     */
    public Optional<String> getCurrentUsername() {
        return getCurrentUserDetails()
                .map(UserDetails::getUsername);
    }
    
    /**
     * Obtient le nom d'utilisateur courant (requis)
     * @return Nom d'utilisateur courant
     * @throws BusinessException si utilisateur non authentifié
     */
    public String getCurrentUsernameRequired() {
        return getCurrentUsername()
                .orElseThrow(() -> BusinessException.unauthorized(
                    "User not authenticated", null, null));
    }
    
    /**
     * Obtient l'email de l'utilisateur courant
     * @return Optional contenant l'email ou vide
     */
    public Optional<String> getCurrentUserEmail() {
        return getCurrentUserPrincipal()
                .map(CustomUserPrincipal::getEmail);
    }
    
    /**
     * Obtient le rôle de l'utilisateur courant
     * @return Optional contenant le rôle ou vide
     */
    public Optional<UserRole> getCurrentUserRole() {
        return getCurrentUserPrincipal()
                .map(CustomUserPrincipal::getRole);
    }
    
    /**
     * Obtient le nom complet de l'utilisateur courant
     * @return Optional contenant le nom complet ou vide
     */
    public Optional<String> getCurrentUserFullName() {
        return getCurrentUserPrincipal()
                .map(CustomUserPrincipal::getFullName);
    }
    
    // ==================== VÉRIFICATIONS D'AUTHENTIFICATION ====================
    
    /**
     * Vérifie si un utilisateur est authentifié
     * @return true si authentifié
     */
    public boolean isAuthenticated() {
        return getCurrentAuthentication()
                .map(Authentication::isAuthenticated)
                .orElse(false);
    }
    
    /**
     * Vérifie si l'utilisateur courant est pleinement valide
     * @return true si utilisateur valide et opérationnel
     */
    public boolean isFullyAuthenticated() {
        return getCurrentUserPrincipal()
                .map(CustomUserPrincipal::isFullyValid)
                .orElse(false);
    }
    
    /**
     * Vérifie si l'utilisateur courant a une connexion récente
     * @return true si connexion récente
     */
    public boolean hasRecentLogin() {
        return getCurrentUserPrincipal()
                .map(CustomUserPrincipal::hasRecentLogin)
                .orElse(false);
    }
    
    /**
     * Vérifie si l'utilisateur courant nécessite une action
     * @return true si action requise
     */
    public boolean requiresUserAction() {
        return getCurrentUserPrincipal()
                .map(CustomUserPrincipal::requiresAction)
                .orElse(true);
    }
    
    // ==================== VÉRIFICATIONS DE RÔLES ====================
    
    /**
     * Vérifie si l'utilisateur courant est un administrateur
     * @return true si admin
     */
    public boolean isCurrentUserAdmin() {
        return getCurrentUserPrincipal()
                .map(CustomUserPrincipal::isAdmin)
                .orElse(false);
    }
    
    /**
     * Vérifie si l'utilisateur courant est un propriétaire de salon
     * @return true si salon owner
     */
    public boolean isCurrentUserSalonOwner() {
        return getCurrentUserPrincipal()
                .map(CustomUserPrincipal::isSalonOwner)
                .orElse(false);
    }
    
    /**
     * Vérifie si l'utilisateur courant est un client
     * @return true si customer
     */
    public boolean isCurrentUserCustomer() {
        return getCurrentUserPrincipal()
                .map(CustomUserPrincipal::isCustomer)
                .orElse(false);
    }
    
    /**
     * Vérifie si l'utilisateur courant peut gérer des salons
     * @return true si peut gérer des salons
     */
    public boolean canCurrentUserManageSalons() {
        return getCurrentUserPrincipal()
                .map(CustomUserPrincipal::canManageSalons)
                .orElse(false);
    }
    
    /**
     * Vérifie si l'utilisateur courant peut effectuer des réservations
     * @return true si peut réserver
     */
    public boolean canCurrentUserMakeBookings() {
        return getCurrentUserPrincipal()
                .map(CustomUserPrincipal::canMakeBookings)
                .orElse(false);
    }
    
    /**
     * Vérifie si l'utilisateur courant a des privilèges administrateur
     * @return true si privilèges admin
     */
    public boolean hasCurrentUserAdminPrivileges() {
        return getCurrentUserPrincipal()
                .map(CustomUserPrincipal::hasAdminPrivileges)
                .orElse(false);
    }
    
    // ==================== VÉRIFICATIONS D'AUTORISATION ====================
    
    /**
     * Vérifie si l'utilisateur courant a une autorité spécifique
     * @param authority Autorité à vérifier
     * @return true si autorisé
     */
    public boolean hasAuthority(String authority) {
        return getCurrentUserPrincipal()
                .map(principal -> principal.hasAuthority(authority))
                .orElse(false);
    }
    
    /**
     * Vérifie si l'utilisateur courant a au moins une des autorités
     * @param authorities Autorités à vérifier
     * @return true si au moins une autorité présente
     */
    public boolean hasAnyAuthority(String... authorities) {
        return getCurrentUserPrincipal()
                .map(principal -> principal.hasAnyAuthority(authorities))
                .orElse(false);
    }
    
    /**
     * Vérifie si l'utilisateur courant peut accéder à un endpoint
     * @param endpoint Endpoint à vérifier
     * @return true si accès autorisé
     */
    public boolean canAccessEndpoint(String endpoint) {
        return getCurrentUserPrincipal()
                .map(principal -> principal.canAccessEndpoint(endpoint))
                .orElse(SecurityConstants.isPublicEndpoint(endpoint));
    }
    
    /**
     * Vérifie si l'utilisateur courant peut accéder à une ressource
     * @param entityType Type d'entité
     * @param resourceId ID de la ressource
     * @param operation Opération tentée
     * @return true si accès autorisé
     */
    public boolean canAccessResource(EntityType entityType, Object resourceId, Operation operation) {
        if (!isAuthenticated()) {
            return false;
        }
        
        // Les admins ont accès à tout
        if (isCurrentUserAdmin()) {
            return true;
        }
        
        // Vérifications spécifiques par type d'entité
        return switch (entityType) {
            case USER -> canAccessUser(resourceId, operation);
            case SALON -> canAccessSalon(resourceId, operation);
            case BOOKING -> canAccessBooking(resourceId, operation);
            case PAYMENT -> canAccessPayment(resourceId, operation);
            default -> operation == Operation.READ; // Lecture par défaut pour les autres
        };
    }
    
    // ==================== VÉRIFICATIONS SPÉCIFIQUES PAR ENTITÉ ====================
    
    /**
     * Vérifie l'accès à un utilisateur
     * @param userId ID de l'utilisateur
     * @param operation Opération
     * @return true si autorisé
     */
    private boolean canAccessUser(Object userId, Operation operation) {
        Long currentUserId = getCurrentUserId().orElse(null);
        if (currentUserId == null) {
            return false;
        }
        
        // L'utilisateur peut toujours accéder à ses propres données
        if (currentUserId.equals(userId)) {
            return true;
        }
        
        // Les salon owners peuvent lire les données de leurs clients
        return isCurrentUserSalonOwner() && operation == Operation.READ;
    }
    
    /**
     * Vérifie l'accès à un salon
     * @param salonId ID du salon
     * @param operation Opération
     * @return true si autorisé
     */
    private boolean canAccessSalon(Object salonId, Operation operation) {
        // Les salon owners peuvent gérer leurs salons
        if (isCurrentUserSalonOwner()) {
            // TODO: Vérifier que le salon appartient à l'utilisateur
            return true;
        }
        
        // Les clients peuvent consulter les salons
        return isCurrentUserCustomer() && operation == Operation.READ;
    }
    
    /**
     * Vérifie l'accès à une réservation
     * @param bookingId ID de la réservation
     * @param operation Opération
     * @return true si autorisé
     */
    private boolean canAccessBooking(Object bookingId, Operation operation) {
        // TODO: Vérifier la propriété de la réservation
        // Pour l'instant, autoriser selon le rôle
        return isCurrentUserCustomer() || canCurrentUserManageSalons();
    }
    
    /**
     * Vérifie l'accès à un paiement
     * @param paymentId ID du paiement
     * @param operation Opération
     * @return true si autorisé
     */
    private boolean canAccessPayment(Object paymentId, Operation operation) {
        // TODO: Vérifier la propriété du paiement
        // Pour l'instant, autoriser selon le rôle
        return isCurrentUserCustomer() || canCurrentUserManageSalons();
    }
    
    // ==================== ASSERTIONS DE SÉCURITÉ ====================
    
    /**
     * Assure que l'utilisateur est authentifié
     * @throws BusinessException si non authentifié
     */
    public void requireAuthentication() {
        if (!isAuthenticated()) {
            throw BusinessException.unauthorized("Authentication required", null, null);
        }
    }
    
    /**
     * Assure que l'utilisateur a un rôle spécifique
     * @param role Rôle requis
     * @throws BusinessException si rôle incorrect
     */
    public void requireRole(UserRole role) {
        requireAuthentication();
        
        UserRole currentRole = getCurrentUserRole().orElse(null);
        if (currentRole != role) {
            throw BusinessException.unauthorized(
                "Role " + role + " required",
                getCurrentUserId().orElse(null),
                null
            );
        }
    }
    
    /**
     * Assure que l'utilisateur a des privilèges administrateur
     * @throws BusinessException si pas admin
     */
    public void requireAdminPrivileges() {
        requireAuthentication();
        
        if (!isCurrentUserAdmin()) {
            throw BusinessException.unauthorized(
                "Admin privileges required",
                getCurrentUserId().orElse(null),
                null
            );
        }
    }
    
    /**
     * Assure que l'utilisateur peut gérer des salons
     * @throws BusinessException si pas autorisé
     */
    public void requireSalonManagementPrivileges() {
        requireAuthentication();
        
        if (!canCurrentUserManageSalons()) {
            throw BusinessException.unauthorized(
                "Salon management privileges required",
                getCurrentUserId().orElse(null),
                null
            );
        }
    }
    
    /**
     * Assure que l'utilisateur peut accéder à une ressource
     * @param entityType Type d'entité
     * @param resourceId ID de la ressource
     * @param operation Opération
     * @throws BusinessException si accès refusé
     */
    public void requireResourceAccess(EntityType entityType, Object resourceId, Operation operation) {
        if (!canAccessResource(entityType, resourceId, operation)) {
            throw BusinessException.unauthorized(
                "Access denied to " + entityType.getDisplayName() + " " + resourceId,
                getCurrentUserId().orElse(null),
                resourceId
            );
        }
    }
    
    // ==================== UTILITAIRES ====================
    
    /**
     * Obtient une représentation sécurisée de l'utilisateur courant pour logging
     * @return String sécurisée pour logs
     */
    public String getCurrentUserForLogging() {
        return getCurrentUserPrincipal()
                .map(CustomUserPrincipal::toSecureString)
                .orElse("Anonymous");
    }
    
    /**
     * Vide le contexte de sécurité
     */
    public void clearContext() {
        SecurityContextHolder.clearContext();
    }
    
    /**
     * Vérifie si le contexte de sécurité est vide
     * @return true si contexte vide
     */
    public boolean isContextEmpty() {
        return SecurityContextHolder.getContext().getAuthentication() == null;
    }
}