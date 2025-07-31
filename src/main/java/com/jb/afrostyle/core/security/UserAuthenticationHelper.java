package com.jb.afrostyle.core.security;

import com.jb.afrostyle.user.dto.UserDTO;
import com.jb.afrostyle.user.service.AuthService;
import com.jb.afrostyle.core.validation.ValidationResult;
import com.jb.afrostyle.core.exception.BusinessException;
import com.jb.afrostyle.core.enums.EntityType;
import com.jb.afrostyle.core.enums.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utilitaire centralisé pour l'extraction des informations d'authentification
 * Version améliorée intégrée au système /core avec ValidationResult et BusinessException
 * Évite la duplication de code d'extraction d'utilisateur dans les contrôleurs
 * 
 * @version 2.0 - Centralisée dans /core
 * @since Java 21
 */
@Component
@RequiredArgsConstructor
public class UserAuthenticationHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(UserAuthenticationHelper.class);
    
    private final AuthService authService;
    private final AuthenticationUtil authenticationUtil;
    private final SecurityContext securityContext;
    
    // ==================== MÉTHODES PRINCIPALES ====================
    
    /**
     * Récupère l'utilisateur actuellement connecté depuis l'authentification
     * Intègre AuthenticationUtil pour une extraction robuste
     * 
     * @param authentication L'objet d'authentification Spring Security
     * @return UserDTO de l'utilisateur connecté
     * @throws BusinessException si l'utilisateur n'est pas trouvé
     */
    public UserDTO getCurrentUser(Authentication authentication) {
        logger.debug("Extracting current user from authentication");
        
        if (authentication == null) {
            logger.error("Authentication object is null");
            throw BusinessException.unauthorized("User not authenticated", null, null);
        }
        
        // Utiliser AuthenticationUtil pour extraction robuste de l'ID
        Long userId = authenticationUtil.extractUserIdFromAuth(authentication);
        if (userId == null) {
            String username = authentication.getName();
            logger.error("User ID not found for username: {}", username);
            throw BusinessException.unauthorized("User not found: " + username, null, username);
        }
        
        try {
            String username = authentication.getName();
            UserDTO user = authService.getCurrentUser(username);
            logger.debug("Current user found: {} (ID: {})", user.username(), user.id());
            return user;
        } catch (Exception e) {
            logger.error("Error retrieving user data for ID: {}", userId, e);
            throw BusinessException.internalError(
                "Failed to retrieve user data",
                userId,
                null,
                e
            );
        }
    }
    
    /**
     * Version sécurisée avec ValidationResult
     * @param authentication L'objet d'authentification Spring Security
     * @return ValidationResult contenant UserDTO ou erreur
     */
    public ValidationResult<UserDTO> getCurrentUserSafe(Authentication authentication) {
        logger.debug("Safely extracting current user from authentication");
        
        if (authentication == null) {
            logger.warn("Authentication object is null");
            return ValidationResult.error(
                "Authentication is required",
                "AUTH_REQUIRED",
                EntityType.USER,
                Operation.READ
            );
        }
        
        // Utiliser la version sécurisée d'AuthenticationUtil
        ValidationResult<Long> userIdResult = authenticationUtil.extractUserIdFromAuthSafe(authentication);
        if (userIdResult instanceof ValidationResult.Error<Long> error) {
            return new ValidationResult.Error<>(
                error.message(),
                error.cause(),
                error.errorCode(),
                error.entityType(),
                error.operation()
            );
        }
        
        Long userId = ((ValidationResult.Success<Long>) userIdResult).value();
        
        try {
            String username = authentication.getName();
            UserDTO user = authService.getCurrentUser(username);
            logger.debug("Current user found: {} (ID: {})", user.username(), user.id());
            return ValidationResult.success(user);
        } catch (Exception e) {
            logger.error("Error retrieving user data for ID: {}", userId, e);
            return new ValidationResult.Error<>(
                "Failed to retrieve user data",
                e,
                "USER_RETRIEVAL_ERROR",
                EntityType.USER,
                Operation.READ
            );
        }
    }
    
    /**
     * Récupère l'ID de l'utilisateur actuellement connecté
     * Utilise AuthenticationUtil pour extraction robuste
     * 
     * @param authentication L'objet d'authentification Spring Security
     * @return ID de l'utilisateur connecté
     * @throws BusinessException si l'utilisateur n'est pas trouvé
     */
    public Long getCurrentUserId(Authentication authentication) {
        logger.debug("Extracting current user ID from authentication");
        
        Long userId = authenticationUtil.extractUserIdFromAuth(authentication);
        if (userId == null) {
            String username = authentication != null ? authentication.getName() : "null";
            logger.error("User ID not found for authentication: {}", username);
            throw BusinessException.unauthorized("User not authenticated", null, username);
        }
        
        logger.debug("Current user ID: {}", userId);
        return userId;
    }
    
    /**
     * Version sécurisée de l'extraction d'ID utilisateur
     * @param authentication L'objet d'authentification Spring Security
     * @return ValidationResult contenant l'ID ou erreur
     */
    public ValidationResult<Long> getCurrentUserIdSafe(Authentication authentication) {
        logger.debug("Safely extracting current user ID from authentication");
        return authenticationUtil.extractUserIdFromAuthSafe(authentication);
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Récupère le nom d'utilisateur depuis l'authentification
     * Méthode utilitaire intégrée avec SecurityContext
     * 
     * @param authentication L'objet d'authentification Spring Security
     * @return Nom d'utilisateur
     * @throws BusinessException si l'authentification est null
     */
    public String getCurrentUsername(Authentication authentication) {
        if (authentication == null) {
            logger.error("Authentication object is null");
            throw BusinessException.unauthorized("Authentication required", null, null);
        }
        
        String username = authentication.getName();
        logger.debug("Current username: {}", username);
        return username;
    }
    
    /**
     * Version sécurisée de l'extraction du nom d'utilisateur
     * @param authentication L'objet d'authentification Spring Security
     * @return ValidationResult contenant le nom d'utilisateur ou erreur
     */
    public ValidationResult<String> getCurrentUsernameSafe(Authentication authentication) {
        if (authentication == null) {
            logger.warn("Authentication object is null");
            return ValidationResult.error(
                "Authentication is required",
                "AUTH_REQUIRED",
                EntityType.USER,
                Operation.READ
            );
        }
        
        String username = authentication.getName();
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.error(
                "Username not found in authentication",
                "USERNAME_MISSING",
                EntityType.USER,
                Operation.READ
            );
        }
        
        logger.debug("Current username: {}", username);
        return ValidationResult.success(username.trim());
    }
    
    /**
     * Vérifie si un utilisateur est authentifié
     * Intègre avec SecurityContext pour cohérence
     * 
     * @param authentication L'objet d'authentification Spring Security
     * @return true si l'utilisateur est authentifié
     */
    public boolean isUserAuthenticated(Authentication authentication) {
        boolean authenticated = authentication != null && authentication.isAuthenticated();
        logger.debug("User authenticated: {}", authenticated);
        return authenticated;
    }
    
    /**
     * Vérifie si l'authentification est valide et utilisable
     * @param authentication L'objet d'authentification Spring Security
     * @return ValidationResult avec détails de validation
     */
    public ValidationResult<String> validateAuthentication(Authentication authentication) {
        if (authentication == null) {
            return ValidationResult.error(
                "Authentication is null",
                "AUTH_NULL",
                EntityType.USER,
                Operation.READ
            );
        }
        
        if (!authentication.isAuthenticated()) {
            return ValidationResult.error(
                "Authentication not validated",
                "AUTH_NOT_VALIDATED",
                EntityType.USER,
                Operation.READ
            );
        }
        
        String username = authentication.getName();
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.error(
                "Username missing from authentication",
                "USERNAME_MISSING",
                EntityType.USER,
                Operation.READ
            );
        }
        
        return ValidationResult.success("Authentication is valid for user: " + username);
    }
    
    // ==================== MÉTHODES DE COMMODITÉ ====================
    
    /**
     * Obtient l'utilisateur courant depuis le contexte Spring Security
     * Utilise SecurityContext pour cohérence
     * @return UserDTO de l'utilisateur courant
     * @throws BusinessException si non authentifié
     */
    public UserDTO getCurrentUserFromContext() {
        Authentication auth = securityContext.getCurrentAuthentication().orElse(null);
        return getCurrentUser(auth);
    }
    
    /**
     * Version sécurisée pour obtenir l'utilisateur depuis le contexte
     * @return ValidationResult contenant UserDTO ou erreur
     */
    public ValidationResult<UserDTO> getCurrentUserFromContextSafe() {
        Optional<Authentication> authOpt = securityContext.getCurrentAuthentication();
        if (authOpt.isEmpty()) {
            return ValidationResult.error(
                "No authentication in security context",
                "NO_AUTH_CONTEXT",
                EntityType.USER,
                Operation.READ
            );
        }
        
        return getCurrentUserSafe(authOpt.get());
    }
    
    /**
     * Obtient l'ID utilisateur depuis le contexte Spring Security
     * @return ID de l'utilisateur courant
     * @throws BusinessException si non authentifié
     */
    public Long getCurrentUserIdFromContext() {
        return securityContext.getCurrentUserIdRequired();
    }
    
    /**
     * Version sécurisée pour obtenir l'ID utilisateur depuis le contexte
     * @return ValidationResult contenant l'ID ou erreur
     */
    public ValidationResult<Long> getCurrentUserIdFromContextSafe() {
        Optional<Long> userIdOpt = securityContext.getCurrentUserId();
        if (userIdOpt.isEmpty()) {
            return ValidationResult.error(
                "No user ID in security context",
                "NO_USER_ID_CONTEXT",
                EntityType.USER,
                Operation.READ
            );
        }
        
        return ValidationResult.success(userIdOpt.get());
    }
    
    // ==================== ASSERTIONS ====================
    
    /**
     * Assure que l'authentification est valide
     * @param authentication L'objet d'authentification Spring Security
     * @throws BusinessException si invalide
     */
    public void requireValidAuthentication(Authentication authentication) {
        ValidationResult<String> validation = validateAuthentication(authentication);
        if (validation instanceof ValidationResult.Error<String> error) {
            throw BusinessException.unauthorized(
                error.message(),
                null,
                authentication != null ? authentication.getName() : null
            );
        }
    }
    
    /**
     * Assure qu'un utilisateur spécifique est authentifié
     * @param authentication L'objet d'authentification Spring Security
     * @param expectedUserId ID utilisateur attendu
     * @throws BusinessException si utilisateur incorrect
     */
    public void requireUserAuthentication(Authentication authentication, Long expectedUserId) {
        requireValidAuthentication(authentication);
        
        Long currentUserId = getCurrentUserId(authentication);
        if (!currentUserId.equals(expectedUserId)) {
            throw BusinessException.unauthorized(
                "Access denied - user mismatch",
                currentUserId,
                expectedUserId
            );
        }
    }
}