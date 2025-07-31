package com.jb.afrostyle.core.constants;

import java.time.Duration;

/**
 * Constantes de sécurité centralisées pour AfroStyle
 * Centralise toutes les règles de sécurité, authentification et autorisation
 * 
 * @version 1.0
 * @since Java 21
 */
public final class SecurityConstants {
    
    private SecurityConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // ==================== JWT CONFIGURATION ====================
    
    /** Durée de validité du token JWT (24 heures) */
    public static final Duration JWT_EXPIRATION = Duration.ofHours(24);
    
    /** Durée de validité du refresh token (30 jours) */
    public static final Duration JWT_REFRESH_EXPIRATION = Duration.ofDays(30);
    
    /** Préfixe du header Authorization */
    public static final String BEARER_PREFIX = "Bearer ";
    
    /** Nom du header Authorization */
    public static final String AUTHORIZATION_HEADER = "Authorization";
    
    /** Clé pour l'utilisateur dans le contexte de sécurité */
    public static final String SECURITY_CONTEXT_USER_KEY = "user";
    
    // ==================== SESSION CONFIGURATION ====================
    
    /** Nom du cookie de session */
    public static final String SESSION_COOKIE_NAME = "JSESSIONID";
    
    /** Durée de validité de la session (2 heures) */
    public static final Duration SESSION_TIMEOUT = Duration.ofHours(2);
    
    /** Durée de validité du "Remember Me" (7 jours) */
    public static final Duration REMEMBER_ME_DURATION = Duration.ofDays(7);
    
    // ==================== PASSWORD SECURITY ====================
    
    /** Nombre minimum de caractères spéciaux requis */
    public static final int MIN_SPECIAL_CHARACTERS = 1;
    
    /** Nombre minimum de chiffres requis */
    public static final int MIN_DIGITS = 1;
    
    /** Nombre minimum de lettres majuscules */
    public static final int MIN_UPPERCASE_LETTERS = 1;
    
    /** Nombre minimum de lettres minuscules */
    public static final int MIN_LOWERCASE_LETTERS = 1;
    
    /** Caractères spéciaux autorisés */
    public static final String ALLOWED_SPECIAL_CHARACTERS = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    
    /** Nombre maximum de tentatives de connexion */
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    
    /** Durée de blocage après échec de connexion (minutes) */
    public static final Duration ACCOUNT_LOCKOUT_DURATION = Duration.ofMinutes(30);
    
    // ==================== PASSWORD RESET ====================
    
    /** Durée de validité du token de reset (1 heure) */
    public static final Duration PASSWORD_RESET_TOKEN_DURATION = Duration.ofHours(1);
    
    /** Longueur du token de reset */
    public static final int PASSWORD_RESET_TOKEN_LENGTH = 32;
    
    /** Durée de validité du token de vérification email (24 heures) */
    public static final Duration EMAIL_VERIFICATION_TOKEN_DURATION = Duration.ofHours(24);
    
    // ==================== RATE LIMITING ====================
    
    /** Nombre maximum de requêtes par minute pour l'authentification */
    public static final int MAX_AUTH_REQUESTS_PER_MINUTE = 10;
    
    /** Nombre maximum de requêtes par heure pour les APIs */
    public static final int MAX_API_REQUESTS_PER_HOUR = 1000;
    
    /** Nombre maximum de tentatives de reset password par heure */
    public static final int MAX_PASSWORD_RESET_ATTEMPTS_PER_HOUR = 3;
    
    // ==================== CORS CONFIGURATION ====================
    
    /** Origines autorisées pour CORS */
    public static final String[] ALLOWED_ORIGINS = {
        "http://localhost:4200",
        "http://localhost:3000",
        "https://afrostyle.be",
        "https://www.afrostyle.be"
    };
    
    /** Headers autorisés pour CORS */
    public static final String[] ALLOWED_HEADERS = {
        "Content-Type",
        "Accept",
        "Authorization",
        "X-Requested-With",
        "X-XSRF-TOKEN"
    };
    
    /** Méthodes HTTP autorisées */
    public static final String[] ALLOWED_METHODS = {
        "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
    };
    
    /** Durée de cache pour preflight CORS (1 heure) */
    public static final Duration CORS_MAX_AGE = Duration.ofHours(1);
    
    // ==================== ENDPOINTS PUBLICS ====================
    
    /** Endpoints publics (pas d'authentification requise) */
    public static final String[] PUBLIC_ENDPOINTS = {
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/forgot-password",
        "/api/auth/reset-password",
        "/api/auth/verify-email",
        "/api/salons/public/**",
        "/api/services/public/**",
        "/actuator/health",
        "/actuator/info",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/ws/info",
        "/ws/**"
    };
    
    /** Endpoints d'administration (ADMIN uniquement) */
    public static final String[] ADMIN_ENDPOINTS = {
        "/api/admin/**",
        "/actuator/**",
        "/api/users/admin/**",
        "/api/salons/admin/**"
    };
    
    /** Endpoints propriétaire de salon */
    public static final String[] SALON_OWNER_ENDPOINTS = {
        "/api/salons/owner/**",
        "/api/bookings/salon/**",
        "/api/services/salon/**",
        "/api/categories/salon/**"
    };
    
    // ==================== ENCRYPTION ====================
    
    /** Algorithme de hachage pour les mots de passe */
    public static final String PASSWORD_ENCODER_ALGORITHM = "bcrypt";
    
    /** Force du hachage bcrypt */
    public static final int BCRYPT_STRENGTH = 12;
    
    /** Algorithme pour les tokens */
    public static final String TOKEN_ALGORITHM = "HmacSHA256";
    
    /** Longueur minimum de la clé secrète JWT (en caractères) */
    public static final int MIN_JWT_SECRET_LENGTH = 32;
    
    // ==================== AUDIT ET LOGS ====================
    
    /** Événements de sécurité à auditer */
    public static final String[] AUDIT_EVENTS = {
        "LOGIN_SUCCESS",
        "LOGIN_FAILURE",
        "LOGOUT",
        "PASSWORD_CHANGE",
        "PASSWORD_RESET",
        "ACCOUNT_LOCKED",
        "UNAUTHORIZED_ACCESS",
        "PRIVILEGE_ESCALATION"
    };
    
    /** Niveau de log pour les événements de sécurité */
    public static final String SECURITY_LOG_LEVEL = "WARN";
    
    // ==================== HEADERS DE SÉCURITÉ ====================
    
    /** Content Security Policy */
    public static final String CSP_HEADER = "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'";
    
    /** X-Frame-Options */
    public static final String FRAME_OPTIONS = "DENY";
    
    /** X-Content-Type-Options */
    public static final String CONTENT_TYPE_OPTIONS = "nosniff";
    
    /** Strict-Transport-Security (HSTS) */
    public static final String HSTS_HEADER = "max-age=31536000; includeSubDomains";
    
    // ==================== VALIDATION SÉCURISÉE ====================
    
    /** Pattern pour validation email sécurisée */
    public static final String SECURE_EMAIL_PATTERN = 
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    
    /** Pattern pour validation username (alphanumerique + underscore) */
    public static final String SECURE_USERNAME_PATTERN = "^[a-zA-Z0-9_]{3,30}$";
    
    /** Pattern pour validation nom (lettres, espaces, tirets) */
    public static final String SECURE_NAME_PATTERN = "^[a-zA-ZÀ-ÿ\\s\\-']{2,50}$";
    
    /** Caractères interdits dans les entrées utilisateur */
    public static final String FORBIDDEN_CHARACTERS = "<>\"'&%;(){}[]|\\^~`";
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Vérifie si un endpoint est public
     * @param endpoint Chemin de l'endpoint
     * @return true si public
     */
    public static boolean isPublicEndpoint(String endpoint) {
        for (String publicEndpoint : PUBLIC_ENDPOINTS) {
            if (endpoint.matches(publicEndpoint.replace("**", ".*"))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Vérifie si un endpoint nécessite des privilèges administrateur
     * @param endpoint Chemin de l'endpoint
     * @return true si admin requis
     */
    public static boolean requiresAdminPrivileges(String endpoint) {
        for (String adminEndpoint : ADMIN_ENDPOINTS) {
            if (endpoint.matches(adminEndpoint.replace("**", ".*"))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Vérifie si un endpoint nécessite des privilèges propriétaire de salon
     * @param endpoint Chemin de l'endpoint
     * @return true si salon owner requis
     */
    public static boolean requiresSalonOwnerPrivileges(String endpoint) {
        for (String ownerEndpoint : SALON_OWNER_ENDPOINTS) {
            if (endpoint.matches(ownerEndpoint.replace("**", ".*"))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Génère la durée d'expiration pour un type de token
     * @param tokenType Type de token (ACCESS, REFRESH, RESET, etc.)
     * @return Durée d'expiration
     */
    public static Duration getTokenExpiration(String tokenType) {
        return switch (tokenType.toUpperCase()) {
            case "ACCESS", "JWT" -> JWT_EXPIRATION;
            case "REFRESH" -> JWT_REFRESH_EXPIRATION;
            case "RESET", "PASSWORD_RESET" -> PASSWORD_RESET_TOKEN_DURATION;
            case "EMAIL_VERIFICATION" -> EMAIL_VERIFICATION_TOKEN_DURATION;
            case "REMEMBER_ME" -> REMEMBER_ME_DURATION;
            default -> JWT_EXPIRATION;
        };
    }
}