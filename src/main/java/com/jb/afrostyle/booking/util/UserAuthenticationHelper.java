package com.jb.afrostyle.booking.util;

import com.jb.afrostyle.user.dto.UserDTO;
import com.jb.afrostyle.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Utilitaire centralisé pour l'extraction des informations d'authentification
 * Évite la duplication de code d'extraction d'utilisateur dans les contrôleurs booking
 */
@Component
@RequiredArgsConstructor
public class UserAuthenticationHelper {
    
    private static final Logger log = LoggerFactory.getLogger(UserAuthenticationHelper.class);
    
    private final AuthService authService;
    
    /**
     * Récupère l'utilisateur actuellement connecté depuis l'authentification
     * CENTRALISE : authentication.getName() + userService.findByUsername()
     * 
     * @param authentication L'objet d'authentification Spring Security
     * @return UserDTO de l'utilisateur connecté
     * @throws RuntimeException si l'utilisateur n'est pas trouvé
     */
    public UserDTO getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            log.error("❌ Authentication object is null");
            throw new RuntimeException("User not authenticated");
        }
        
        String username = authentication.getName();
        log.info("🔍 Extracting current user from authentication: {}", username);
        
        try {
            UserDTO user = authService.getCurrentUser(username);
            log.info("✅ Current user found: {} (ID: {})", user.username(), user.id());
            return user;
        } catch (Exception e) {
            log.error("❌ User not found for username: {}", username, e);
            throw new RuntimeException("User not found: " + username);
        }
    }
    
    /**
     * Récupère l'ID de l'utilisateur actuellement connecté
     * Raccourci pour getCurrentUser(auth).getId()
     * 
     * @param authentication L'objet d'authentification Spring Security
     * @return ID de l'utilisateur connecté
     * @throws RuntimeException si l'utilisateur n'est pas trouvé
     */
    public Long getCurrentUserId(Authentication authentication) {
        return getCurrentUser(authentication).id();
    }
    
    /**
     * Récupère le nom d'utilisateur depuis l'authentification
     * Méthode utilitaire simple
     * 
     * @param authentication L'objet d'authentification Spring Security
     * @return Nom d'utilisateur
     * @throws RuntimeException si l'authentification est null
     */
    public String getCurrentUsername(Authentication authentication) {
        if (authentication == null) {
            log.error("❌ Authentication object is null");
            throw new RuntimeException("User not authenticated");
        }
        return authentication.getName();
    }
    
    /**
     * Vérifie si un utilisateur est authentifié
     * 
     * @param authentication L'objet d'authentification Spring Security
     * @return true si l'utilisateur est authentifié
     */
    public boolean isUserAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }
}