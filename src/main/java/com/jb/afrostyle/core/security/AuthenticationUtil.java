package com.jb.afrostyle.core.security;

import com.jb.afrostyle.core.validation.ValidationResult;
import com.jb.afrostyle.core.exception.BusinessException;
import com.jb.afrostyle.core.enums.EntityType;
import com.jb.afrostyle.core.enums.Operation;
import com.jb.afrostyle.core.constants.SecurityConstants;
import com.jb.afrostyle.user.domain.enums.UserRole;
import com.jb.afrostyle.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Utilitaires d'authentification centralisés pour AfroStyle
 * Version améliorée avec cache, validation et sécurité renforcée
 * Intègre Pattern Matching Java 21 et ValidationResult
 * 
 * @version 2.0 - Centralisée dans /core
 * @since Java 21
 */
@Component
@RequiredArgsConstructor
public class AuthenticationUtil {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationUtil.class);
    private final UserRepository userRepository;
    
    // Cache pour éviter les requêtes répétitives vers la base de données
    private final Map<String, Long> userIdCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> cacheTimestamps = new ConcurrentHashMap<>();
    
    // Durée de cache en minutes
    private static final int CACHE_DURATION_MINUTES = 30;

    // ==================== EXTRACTION D'ID UTILISATEUR ====================

    /**
     * Extrait l'ID utilisateur de l'authentification avec Pattern Matching Java 21
     * @param auth Authentification Spring Security
     * @return ID utilisateur ou null si non trouvé
     */
    public Long extractUserIdFromAuth(Authentication auth) {
        if (logger.isDebugEnabled()) {
            logger.debug("Extracting user ID from authentication");
        }
        
        return switch (auth) {
            case null -> {
                logger.warn("Authentication is null");
                yield null;
            }
            case Authentication a when a.getPrincipal() instanceof CustomUserPrincipal principal -> {
                logger.debug("Found user ID from CustomUserPrincipal: {}", principal.getId());
                yield principal.getId();
            }
            case Authentication a when a.getPrincipal() instanceof String username -> {
                logger.debug("Extracting user ID for username: {}", username);
                yield extractUserIdFromUsername(username);
            }
            case Authentication a when a.getPrincipal() instanceof UserDetails userDetails -> {
                logger.debug("Extracting user ID for UserDetails: {}", userDetails.getUsername());
                yield extractUserIdFromUsername(userDetails.getUsername());
            }
            default -> {
                String principalType = auth.getPrincipal().getClass().getSimpleName();
                logger.warn("Unknown principal type: {}", principalType);
                yield null;
            }
        };
    }
    
    /**
     * Version sécurisée avec ValidationResult pour une meilleure gestion d'erreurs
     * @param auth Authentification Spring Security
     * @return ValidationResult contenant l'ID utilisateur ou erreur
     */
    public ValidationResult<Long> extractUserIdFromAuthSafe(Authentication auth) {
        if (logger.isDebugEnabled()) {
            logger.debug("Safely extracting user ID from authentication");
        }
        
        return switch (auth) {
            case null -> {
                logger.warn("Authentication is null");
                yield ValidationResult.error(
                    "Authentication is required",
                    "AUTH_REQUIRED",
                    EntityType.USER,
                    Operation.READ
                );
            }
            case Authentication a when a.getPrincipal() instanceof CustomUserPrincipal principal -> {
                logger.debug("Found user ID from CustomUserPrincipal: {}", principal.getId());
                yield ValidationResult.success(principal.getId());
            }
            case Authentication a when a.getPrincipal() instanceof String username -> {
                logger.debug("Extracting user ID for username: {}", username);
                yield extractUserIdFromUsernameSafe(username);
            }
            case Authentication a when a.getPrincipal() instanceof UserDetails userDetails -> {
                logger.debug("Extracting user ID for UserDetails: {}", userDetails.getUsername());
                yield extractUserIdFromUsernameSafe(userDetails.getUsername());
            }
            default -> {
                String principalType = auth.getPrincipal().getClass().getSimpleName();
                logger.warn("Unknown principal type: {}", principalType);
                yield ValidationResult.error(
                    "Unsupported principal type: " + principalType,
                    "UNSUPPORTED_PRINCIPAL",
                    EntityType.USER,
                    Operation.READ
                );
            }
        };
    }
    
    /**
     * Extrait l'ID utilisateur à partir du nom d'utilisateur avec cache
     * @param username Nom d'utilisateur
     * @return ID utilisateur ou null
     */
    private Long extractUserIdFromUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        
        // Vérification du cache
        Long cachedId = getCachedUserId(username);
        if (cachedId != null) {
            logger.debug("Found user ID in cache for username: {}", username);
            return cachedId;
        }
        
        try {
            Optional<Long> userId = userRepository.findByUsername(username.trim())
                    .map(user -> user.getId());
            
            if (userId.isPresent()) {
                // Mise en cache
                cacheUserId(username, userId.get());
                logger.debug("Found user ID from database: {}", userId.get());
                return userId.get();
            } else {
                logger.warn("User not found for username: {}", username);
                return null;
            }
        } catch (Exception e) {
            logger.error("Database error while finding user ID for username: {}", username, e);
            return null;
        }
    }
    
    /**
     * Version sécurisée de l'extraction par nom d'utilisateur
     * @param username Nom d'utilisateur
     * @return ValidationResult avec ID utilisateur ou erreur
     */
    private ValidationResult<Long> extractUserIdFromUsernameSafe(String username) {
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.error(
                "Username cannot be empty",
                "EMPTY_USERNAME",
                EntityType.USER,
                Operation.READ
            );
        }
        
        // Vérification du cache
        Long cachedId = getCachedUserId(username);
        if (cachedId != null) {
            logger.debug("Found user ID in cache for username: {}", username);
            return ValidationResult.success(cachedId);
        }
        
        try {
            Optional<Long> userId = userRepository.findByUsername(username.trim())
                    .map(user -> user.getId());
            
            if (userId.isPresent()) {
                cacheUserId(username, userId.get());
                logger.debug("Found user ID from database: {}", userId.get());
                return ValidationResult.success(userId.get());
            } else {
                logger.warn("User not found for username: {}", username);
                return ValidationResult.error(
                    "User not found: " + username,
                    "USER_NOT_FOUND",
                    EntityType.USER,
                    Operation.READ
                );
            }
        } catch (Exception e) {
            logger.error("Database error while finding user ID for username: {}", username, e);
            return new ValidationResult.Error<>(
                "Database error while finding user",
                e,
                "DATABASE_ERROR",
                EntityType.USER,
                Operation.READ
            );
        }
    }
    
    // ==================== CONTEXTE SPRING SECURITY ====================
    
    /**
     * Obtient l'authentification courante du contexte Spring Security
     * @return Authentification courante ou null
     */
    public Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    
    /**
     * Obtient l'ID de l'utilisateur courant
     * @return ID utilisateur courant ou null
     */
    public Long getCurrentUserId() {
        Authentication auth = getCurrentAuthentication();
        return extractUserIdFromAuth(auth);
    }
    
    /**
     * Obtient l'ID de l'utilisateur courant (requis)
     * @return ID utilisateur courant
     * @throws BusinessException si utilisateur non authentifié
     */
    public Long getCurrentUserIdRequired() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("User not authenticated", null, null);
        }
        return userId;
    }
    
    /**
     * Obtient le CustomUserPrincipal courant
     * @return CustomUserPrincipal ou null
     */
    public CustomUserPrincipal getCurrentUserPrincipal() {
        Authentication auth = getCurrentAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal principal) {
            return principal;
        }
        return null;
    }
    
    /**
     * Obtient le nom d'utilisateur courant
     * @return Nom d'utilisateur ou null
     */
    public String getCurrentUsername() {
        Authentication auth = getCurrentAuthentication();
        if (auth != null) {
            return switch (auth.getPrincipal()) {
                case CustomUserPrincipal principal -> principal.getUsername();
                case UserDetails userDetails -> userDetails.getUsername();
                case String username -> username;
                default -> null;
            };
        }
        return null;
    }
    
    // ==================== VÉRIFICATIONS D'AUTHENTIFICATION ====================
    
    /**
     * Vérifie si un utilisateur est authentifié
     * @return true si authentifié
     */
    public boolean isAuthenticated() {
        Authentication auth = getCurrentAuthentication();
        return auth != null && auth.isAuthenticated();
    }
    
    /**
     * Vérifie si l'utilisateur courant a un rôle spécifique
     * @param role Rôle à vérifier
     * @return true si rôle présent
     */
    public boolean hasRole(UserRole role) {
        CustomUserPrincipal principal = getCurrentUserPrincipal();
        return principal != null && principal.getRole() == role;
    }
    
    /**
     * Vérifie si l'utilisateur courant a une autorité spécifique
     * @param authority Autorité à vérifier
     * @return true si autorité présente
     */
    public boolean hasAuthority(String authority) {
        Authentication auth = getCurrentAuthentication();
        if (auth == null) {
            return false;
        }
        
        return auth.getAuthorities().stream()
                   .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals(authority));
    }
    
    /**
     * Vérifie si l'utilisateur courant peut accéder à un endpoint
     * @param endpoint Endpoint à vérifier
     * @return true si accès autorisé
     */
    public boolean canAccessEndpoint(String endpoint) {
        // Endpoints publics
        if (SecurityConstants.isPublicEndpoint(endpoint)) {
            return true;
        }
        
        // Vérification d'authentification
        if (!isAuthenticated()) {
            return false;
        }
        
        // Endpoints admin
        if (SecurityConstants.requiresAdminPrivileges(endpoint)) {
            return hasRole(UserRole.ADMIN);
        }
        
        // Endpoints salon owner
        if (SecurityConstants.requiresSalonOwnerPrivileges(endpoint)) {
            return hasRole(UserRole.ADMIN) || hasRole(UserRole.SALON_OWNER);
        }
        
        return true;
    }
    
    // ==================== GESTION DU CACHE ====================
    
    /**
     * Obtient l'ID utilisateur depuis le cache si valide
     * @param username Nom d'utilisateur
     * @return ID utilisateur ou null si pas en cache ou expiré
     */
    private Long getCachedUserId(String username) {
        LocalDateTime cacheTime = cacheTimestamps.get(username);
        if (cacheTime != null && 
            cacheTime.isAfter(LocalDateTime.now().minusMinutes(CACHE_DURATION_MINUTES))) {
            return userIdCache.get(username);
        }
        
        // Nettoyer le cache expiré
        if (cacheTime != null) {
            userIdCache.remove(username);
            cacheTimestamps.remove(username);
        }
        
        return null;
    }
    
    /**
     * Met en cache l'ID utilisateur
     * @param username Nom d'utilisateur
     * @param userId ID utilisateur
     */
    private void cacheUserId(String username, Long userId) {
        userIdCache.put(username, userId);
        cacheTimestamps.put(username, LocalDateTime.now());
        
        // Nettoyage périodique du cache (garder seulement 1000 entrées max)
        if (userIdCache.size() > 1000) {
            cleanupCache();
        }
    }
    
    /**
     * Nettoie le cache des entrées expirées
     */
    private void cleanupCache() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(CACHE_DURATION_MINUTES);
        
        cacheTimestamps.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isBefore(cutoff);
            if (expired) {
                userIdCache.remove(entry.getKey());
            }
            return expired;
        });
        
        logger.debug("Cache cleanup completed. Current size: {}", userIdCache.size());
    }
    
    /**
     * Vide complètement le cache
     */
    public void clearCache() {
        userIdCache.clear();
        cacheTimestamps.clear();
        logger.debug("Authentication cache cleared");
    }
    
    /**
     * Obtient les statistiques du cache
     * @return Map avec les statistiques
     */
    public Map<String, Object> getCacheStatistics() {
        return Map.of(
            "cacheSize", userIdCache.size(),
            "lastCleanup", LocalDateTime.now(),
            "cacheDurationMinutes", CACHE_DURATION_MINUTES
        );
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
        
        if (!hasRole(role)) {
            throw BusinessException.unauthorized(
                "Role " + role + " required",
                getCurrentUserId(),
                null
            );
        }
    }
    
    /**
     * Assure que l'utilisateur a une autorité spécifique
     * @param authority Autorité requise
     * @throws BusinessException si autorité manquante
     */
    public void requireAuthority(String authority) {
        requireAuthentication();
        
        if (!hasAuthority(authority)) {
            throw BusinessException.unauthorized(
                "Authority " + authority + " required",
                getCurrentUserId(),
                null
            );
        }
    }
    
    /**
     * Assure que l'utilisateur peut accéder à un endpoint
     * @param endpoint Endpoint à vérifier
     * @throws BusinessException si accès refusé
     */
    public void requireEndpointAccess(String endpoint) {
        if (!canAccessEndpoint(endpoint)) {
            throw BusinessException.unauthorized(
                "Access denied to endpoint: " + endpoint,
                getCurrentUserId(),
                endpoint
            );
        }
    }
    
    // ==================== UTILITAIRES ====================
    
    /**
     * Obtient une représentation sécurisée de l'utilisateur courant pour logging
     * @return String sécurisée pour logs
     */
    public String getCurrentUserForLogging() {
        CustomUserPrincipal principal = getCurrentUserPrincipal();
        if (principal != null) {
            return principal.toSecureString();
        }
        
        String username = getCurrentUsername();
        return username != null ? "User{username='" + username + "'}" : "Anonymous";
    }
    
    /**
     * Valide la cohérence de l'authentification
     * @return ValidationResult avec détails de validation
     */
    public ValidationResult<String> validateAuthenticationConsistency() {
        Authentication auth = getCurrentAuthentication();
        if (auth == null) {
            return ValidationResult.error("No authentication found");
        }
        
        if (!auth.isAuthenticated()) {
            return ValidationResult.error("Authentication not validated");
        }
        
        Object principal = auth.getPrincipal();
        if (principal == null) {
            return ValidationResult.error("No principal found in authentication");
        }
        
        if (principal instanceof CustomUserPrincipal customPrincipal) {
            if (!customPrincipal.isEnabled()) {
                return ValidationResult.error("User account is disabled");
            }
            
            if (!customPrincipal.isAccountNonLocked()) {
                return ValidationResult.error("User account is locked");
            }
            
            if (!customPrincipal.isCredentialsNonExpired()) {
                return ValidationResult.error("User credentials have expired");
            }
        }
        
        return ValidationResult.success("Authentication is valid and consistent");
    }
}