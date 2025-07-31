package com.jb.afrostyle.core.dto;

/**
 * Réponse d'authentification compatible avec le frontend Angular
 * Simule une AuthResponse JWT tout en utilisant les sessions Spring Security
 * Version centralisée dans /core pour réutilisation dans tous les modules
 * 
 * @param accessToken Token d'accès (session ID pour compatibilité frontend)
 * @param tokenType Type de token (toujours "Bearer" pour compatibilité)
 * @param expiresIn Durée d'expiration en secondes
 * @param refreshToken Token de rafraîchissement (null pour sessions)
 * @param user Informations utilisateur
 * 
 * @version 2.0 - Centralisée dans /core
 * @since Java 21
 */
public record SessionAuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    String refreshToken,
    UserResponse user
) {
    
    /**
     * Créé une réponse d'authentification par session
     * @param sessionId ID de session Spring Security
     * @param expiresIn Durée d'expiration en secondes
     * @param user Informations utilisateur
     * @return SessionAuthResponse configurée
     */
    public static SessionAuthResponse fromSession(String sessionId, long expiresIn, UserResponse user) {
        return new SessionAuthResponse(
            sessionId,
            "Bearer", // Compatibilité avec frontend Angular
            expiresIn,
            null, // Pas de refresh token pour les sessions
            user
        );
    }
    
    /**
     * Créé une réponse d'authentification avec session par défaut
     * @param user Informations utilisateur
     * @return SessionAuthResponse avec durée par défaut (24h)
     */
    public static SessionAuthResponse defaultSession(UserResponse user) {
        return new SessionAuthResponse(
            "session-" + System.currentTimeMillis(),
            "Bearer",
            86400, // 24 heures par défaut
            null,
            user
        );
    }
    
    /**
     * Créé une réponse d'authentification invalide pour déconnexion
     * @return SessionAuthResponse vide
     */
    public static SessionAuthResponse invalid() {
        return new SessionAuthResponse(
            null,
            "Bearer",
            0,
            null,
            null
        );
    }
    
    /**
     * Vérifie si la réponse est valide
     * @return true si session valide
     */
    public boolean isValid() {
        return accessToken != null && user != null && expiresIn > 0;
    }
}