package com.jb.afrostyle.core.security;

import com.jb.afrostyle.user.domain.enums.UserRole;
import com.jb.afrostyle.core.constants.SecurityConstants;
import com.jb.afrostyle.core.exception.BusinessException;
import com.jb.afrostyle.core.enums.EntityType;
import com.jb.afrostyle.core.enums.Operation;
import com.jb.afrostyle.core.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vérificateur de permissions centralisé pour AfroStyle
 * Gère toutes les vérifications d'autorisation et de sécurité
 * Intègre avec SecurityContext et BusinessException
 * 
 * @version 1.0
 * @since Java 21
 */
@Component
@RequiredArgsConstructor
public class PermissionChecker {
    
    private static final Logger logger = LoggerFactory.getLogger(PermissionChecker.class);
    
    private final SecurityContext securityContext;
    
    // Cache des permissions pour optimisation
    private final Map<String, Boolean> permissionCache = new ConcurrentHashMap<>();
    
    // ==================== VÉRIFICATIONS GÉNÉRALES ====================
    
    /**
     * Vérifie si l'utilisateur courant peut effectuer une opération sur une entité
     * @param entityType Type d'entité
     * @param resourceId ID de la ressource
     * @param operation Opération à effectuer
     * @return true si autorisé
     */
    public boolean canPerformOperation(EntityType entityType, Object resourceId, Operation operation) {
        logger.debug("Checking permission for operation {} on {} {}", operation, entityType, resourceId);
        
        // Cache key pour optimisation
        String cacheKey = buildCacheKey(entityType, resourceId, operation);
        Boolean cachedResult = permissionCache.get(cacheKey);
        if (cachedResult != null) {
            logger.debug("Permission result from cache: {}", cachedResult);
            return cachedResult;
        }
        
        // Vérification de l'authentification
        if (!securityContext.isAuthenticated()) {
            logger.debug("User not authenticated");
            permissionCache.put(cacheKey, false);
            return false;
        }
        
        // Les admins ont accès à tout (sauf opérations sensibles)
        if (securityContext.isCurrentUserAdmin() && !isSensitiveOperation(operation)) {
            logger.debug("Admin access granted");
            permissionCache.put(cacheKey, true);
            return true;
        }
        
        // Vérifications spécifiques par entité
        boolean result = switch (entityType) {
            case USER -> canAccessUser(resourceId, operation);
            case SALON -> canAccessSalon(resourceId, operation);
            case BOOKING -> canAccessBooking(resourceId, operation);
            case PAYMENT -> canAccessPayment(resourceId, operation);
            case SERVICE_OFFERING -> canAccessServiceOffering(resourceId, operation);
            case CATEGORY -> canAccessCategory(resourceId, operation);
            default -> operation == Operation.READ; // Lecture par défaut pour nouveaux types
        };
        
        logger.debug("Permission result: {}", result);
        permissionCache.put(cacheKey, result);
        return result;
    }
    
    /**
     * Version sécurisée avec ValidationResult
     * @param entityType Type d'entité
     * @param resourceId ID de la ressource
     * @param operation Opération à effectuer
     * @return ValidationResult avec permission ou erreur
     */
    public ValidationResult<Boolean> canPerformOperationSafe(EntityType entityType, Object resourceId, Operation operation) {
        try {
            boolean canPerform = canPerformOperation(entityType, resourceId, operation);
            return ValidationResult.success(canPerform);
        } catch (Exception e) {
            logger.error("Error checking permission for {} {} {}", entityType, resourceId, operation, e);
            return new ValidationResult.Error<>(
                "Permission check failed",
                e,
                "PERMISSION_CHECK_ERROR",
                entityType,
                operation
            );
        }
    }
    
    // ==================== VÉRIFICATIONS SPÉCIFIQUES PAR ENTITÉ ====================
    
    /**
     * Vérifie l'accès à un utilisateur
     * @param userId ID de l'utilisateur
     * @param operation Opération
     * @return true si autorisé
     */
    private boolean canAccessUser(Object userId, Operation operation) {
        Optional<Long> currentUserIdOpt = securityContext.getCurrentUserId();
        if (currentUserIdOpt.isEmpty()) {
            return false;
        }
        
        Long currentUserId = currentUserIdOpt.get();
        
        // Accès à ses propres données
        if (currentUserId.equals(userId)) {
            return switch (operation) {
                case READ, UPDATE -> true;
                case DELETE -> false; // Les utilisateurs ne peuvent pas se supprimer
                case CREATE -> false; // Pas applicable
                default -> false;
            };
        }
        
        // Salon owners peuvent lire les données de leurs clients (TODO: vérifier relation)
        if (securityContext.isCurrentUserSalonOwner()) {
            return operation == Operation.READ;
        }
        
        return false;
    }
    
    /**
     * Vérifie l'accès à un salon
     * @param salonId ID du salon
     * @param operation Opération
     * @return true si autorisé
     */
    private boolean canAccessSalon(Object salonId, Operation operation) {
        // Salon owners peuvent gérer leurs salons
        if (securityContext.isCurrentUserSalonOwner()) {
            // TODO: Vérifier que le salon appartient à l'utilisateur
            return switch (operation) {
                case READ, UPDATE, CREATE -> true;
                case DELETE -> securityContext.isCurrentUserAdmin(); // Seuls les admins peuvent supprimer
                default -> false;
            };
        }
        
        // Clients peuvent consulter les salons
        if (securityContext.isCurrentUserCustomer()) {
            return operation == Operation.READ;
        }
        
        return false;
    }
    
    /**
     * Vérifie l'accès à une réservation
     * @param bookingId ID de la réservation
     * @param operation Opération
     * @return true si autorisé
     */
    private boolean canAccessBooking(Object bookingId, Operation operation) {
        // TODO: Vérifier la propriété de la réservation via BookingService
        
        // Pour l'instant, autoriser selon le rôle
        if (securityContext.isCurrentUserCustomer()) {
            return switch (operation) {
                case READ, CREATE -> true; // Clients peuvent lire et créer leurs réservations
                case UPDATE -> false; // Modifications via endpoints spéciaux (cancel, etc.)
                case DELETE -> false; // Suppressions via cancel
                default -> false;
            };
        }
        
        // Salon owners peuvent gérer les réservations de leurs salons
        if (securityContext.canCurrentUserManageSalons()) {
            return switch (operation) {
                case READ, UPDATE -> true; // Peuvent voir et modifier (confirmer, etc.)
                case CREATE -> false; // Ne créent pas de réservations pour les clients
                case DELETE -> true; // Peuvent annuler des réservations
                default -> false;
            };
        }
        
        return false;
    }
    
    /**
     * Vérifie l'accès à un paiement
     * @param paymentId ID du paiement
     * @param operation Opération
     * @return true si autorisé
     */
    private boolean canAccessPayment(Object paymentId, Operation operation) {
        // TODO: Vérifier la propriété du paiement via PaymentService
        
        // Clients peuvent voir leurs paiements et en créer
        if (securityContext.isCurrentUserCustomer()) {
            return switch (operation) {
                case READ, CREATE -> true;
                case UPDATE, DELETE -> false; // Paiements immutables après création
                default -> false;
            };
        }
        
        // Salon owners peuvent voir les paiements de leurs salons
        if (securityContext.canCurrentUserManageSalons()) {
            return operation == Operation.READ;
        }
        
        return false;
    }
    
    /**
     * Vérifie l'accès aux services offerts
     * @param serviceId ID du service
     * @param operation Opération
     * @return true si autorisé
     */
    private boolean canAccessServiceOffering(Object serviceId, Operation operation) {
        // Salon owners peuvent gérer les services de leurs salons
        if (securityContext.canCurrentUserManageSalons()) {
            return switch (operation) {
                case READ, CREATE, UPDATE, DELETE -> true;
                default -> false;
            };
        }
        
        // Clients peuvent consulter les services
        if (securityContext.isCurrentUserCustomer()) {
            return operation == Operation.READ;
        }
        
        return false;
    }
    
    /**
     * Vérifie l'accès aux catégories
     * @param categoryId ID de la catégorie
     * @param operation Opération
     * @return true si autorisé
     */
    private boolean canAccessCategory(Object categoryId, Operation operation) {
        // Salon owners peuvent gérer les catégories de leurs salons
        if (securityContext.canCurrentUserManageSalons()) {
            return switch (operation) {
                case READ, CREATE, UPDATE, DELETE -> true;
                default -> false;
            };
        }
        
        // Clients peuvent consulter les catégories
        if (securityContext.isCurrentUserCustomer()) {
            return operation == Operation.READ;
        }
        
        return false;
    }
    
    // ==================== VÉRIFICATIONS DE RÔLES ====================
    
    /**
     * Vérifie si l'utilisateur courant a un rôle spécifique
     * @param requiredRole Rôle requis
     * @return true si utilisateur a le rôle
     */
    public boolean hasRole(UserRole requiredRole) {
        Optional<UserRole> currentRoleOpt = securityContext.getCurrentUserRole();
        if (currentRoleOpt.isEmpty()) {
            return false;
        }
        
        UserRole currentRole = currentRoleOpt.get();
        
        // Vérification directe du rôle
        if (currentRole == requiredRole) {
            return true;
        }
        
        // Hiérarchie des rôles : ADMIN a tous les privilèges
        if (currentRole == UserRole.ADMIN) {
            return true;
        }
        
        // SALON_OWNER peut agir comme CUSTOMER pour certaines opérations
        if (currentRole == UserRole.SALON_OWNER && requiredRole == UserRole.CUSTOMER) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Vérifie si l'utilisateur courant a au moins l'un des rôles
     * @param roles Rôles acceptés
     * @return true si au moins un rôle correspond
     */
    public boolean hasAnyRole(UserRole... roles) {
        for (UserRole role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }
    
    // ==================== VÉRIFICATIONS D'ENDPOINTS ====================
    
    /**
     * Vérifie si l'utilisateur peut accéder à un endpoint
     * @param endpoint Chemin de l'endpoint
     * @return true si accès autorisé
     */
    public boolean canAccessEndpoint(String endpoint) {
        return securityContext.canAccessEndpoint(endpoint);
    }
    
    /**
     * Vérifie les permissions pour plusieurs endpoints
     * @param endpoints Endpoints à vérifier
     * @return Map avec résultats pour chaque endpoint
     */
    public Map<String, Boolean> canAccessEndpoints(String... endpoints) {
        Map<String, Boolean> results = new ConcurrentHashMap<>();
        for (String endpoint : endpoints) {
            results.put(endpoint, canAccessEndpoint(endpoint));
        }
        return results;
    }
    
    // ==================== ASSERTIONS ====================
    
    /**
     * Assure que l'utilisateur peut effectuer une opération
     * @param entityType Type d'entité
     * @param resourceId ID de la ressource
     * @param operation Opération
     * @throws BusinessException si accès refusé
     */
    public void requirePermission(EntityType entityType, Object resourceId, Operation operation) {
        if (!canPerformOperation(entityType, resourceId, operation)) {
            Long currentUserId = securityContext.getCurrentUserId().orElse(null);
            throw BusinessException.unauthorized(
                String.format("Access denied for %s on %s %s", 
                    operation.name(), entityType.getDisplayName(), resourceId),
                currentUserId,
                resourceId
            );
        }
    }
    
    /**
     * Assure que l'utilisateur a un rôle spécifique
     * @param requiredRole Rôle requis
     * @throws BusinessException si rôle incorrect
     */
    public void requireRole(UserRole requiredRole) {
        if (!hasRole(requiredRole)) {
            Long currentUserId = securityContext.getCurrentUserId().orElse(null);
            UserRole currentRole = securityContext.getCurrentUserRole().orElse(null);
            throw BusinessException.unauthorized(
                String.format("Role %s required, but user has role %s", requiredRole, currentRole),
                currentUserId,
                requiredRole
            );
        }
    }
    
    /**
     * Assure que l'utilisateur peut accéder à un endpoint
     * @param endpoint Endpoint
     * @throws BusinessException si accès refusé
     */
    public void requireEndpointAccess(String endpoint) {
        if (!canAccessEndpoint(endpoint)) {
            Long currentUserId = securityContext.getCurrentUserId().orElse(null);
            throw BusinessException.unauthorized(
                "Access denied to endpoint: " + endpoint,
                currentUserId,
                endpoint
            );
        }
    }
    
    // ==================== UTILITAIRES ====================
    
    /**
     * Vérifie si une opération est sensible (nécessite vérifications supplémentaires)
     * @param operation Opération à vérifier
     * @return true si opération sensible
     */
    private boolean isSensitiveOperation(Operation operation) {
        return switch (operation) {
            case DELETE -> true; // Suppressions toujours sensibles
            // Ajouter d'autres opérations sensibles selon les besoins
            default -> false;
        };
    }
    
    /**
     * Construit une clé de cache pour les permissions
     * @param entityType Type d'entité
     * @param resourceId ID de la ressource
     * @param operation Opération
     * @return Clé de cache
     */
    private String buildCacheKey(EntityType entityType, Object resourceId, Operation operation) {
        Long currentUserId = securityContext.getCurrentUserId().orElse(0L);
        return String.format("%d:%s:%s:%s", currentUserId, entityType, resourceId, operation);
    }
    
    /**
     * Vide le cache des permissions
     */
    public void clearPermissionCache() {
        permissionCache.clear();
        logger.debug("Permission cache cleared");
    }
    
    /**
     * Obtient les statistiques du cache
     * @return Map avec statistiques
     */
    public Map<String, Object> getCacheStatistics() {
        return Map.of(
            "cacheSize", permissionCache.size(),
            "entries", Set.copyOf(permissionCache.keySet())
        );
    }
    
    /**
     * Vérifie les permissions pour un utilisateur spécifique (pour testing)
     * @param userId ID utilisateur
     * @param entityType Type d'entité
     * @param resourceId ID ressource
     * @param operation Opération
     * @return ValidationResult avec permission
     */
    public ValidationResult<Boolean> checkPermissionForUser(Long userId, EntityType entityType, Object resourceId, Operation operation) {
        // TODO: Implémenter vérification pour utilisateur spécifique
        // Pour l'instant, utiliser l'utilisateur courant
        return canPerformOperationSafe(entityType, resourceId, operation);
    }
}