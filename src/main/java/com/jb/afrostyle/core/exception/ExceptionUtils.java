package com.jb.afrostyle.core.exception;

import com.jb.afrostyle.core.enums.EntityType;
import com.jb.afrostyle.core.enums.Operation;
import org.slf4j.Logger;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utilitaires d'exception centralisés avec Java 21 Pattern Matching
 * Version centralisée dans /core avec intégration des enums et amélioration des patterns
 * Remplace tous les gestionnaires d'exceptions éparpillés dans les modules
 * 
 * @version 2.0 - Centralisée dans /core
 * @since Java 21
 */
public final class ExceptionUtils {
    
    // Cache des exceptions pour éviter la création répétitive
    private static final Map<String, Exception> EXCEPTION_CACHE = new ConcurrentHashMap<>();
    
    private ExceptionUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // ==================== ENTITY NOT FOUND EXCEPTIONS ====================
    
    /**
     * Crée une exception 'Not Found' selon le type d'entité avec Pattern Matching Java 21
     * @param entityType Type d'entité non trouvée
     * @param identifier Identifiant recherché
     * @return Exception appropriée selon le type d'entité
     */
    public static Exception createNotFoundException(EntityType entityType, Object identifier) {
        return switch (entityType) {
            case USER -> new BusinessException(
                "User not found with identifier: " + identifier,
                "USER_NOT_FOUND",
                entityType,
                Operation.READ
            );
            case BOOKING -> new BusinessException(
                "Booking not found with ID: " + identifier,
                "BOOKING_NOT_FOUND",
                entityType,
                Operation.READ
            );
            case SALON -> new BusinessException(
                "Salon not found with ID: " + identifier,
                "SALON_NOT_FOUND",
                entityType,
                Operation.READ
            );
            case CATEGORY -> new BusinessException(
                "Category not found with ID: " + identifier,
                "CATEGORY_NOT_FOUND",
                entityType,
                Operation.READ
            );
            case SERVICE_OFFERING -> new BusinessException(
                "Service not found with ID: " + identifier,
                "SERVICE_NOT_FOUND",
                entityType,
                Operation.READ
            );
            case PAYMENT -> new BusinessException(
                "Payment not found with ID: " + identifier,
                "PAYMENT_NOT_FOUND",
                entityType,
                Operation.READ
            );
            case NOTIFICATION_QUEUE -> new BusinessException(
                "Notification not found with ID: " + identifier,
                "NOTIFICATION_NOT_FOUND",
                entityType,
                Operation.READ
            );
            case STRIPE_SESSION -> new BusinessException(
                "Stripe session not found with ID: " + identifier,
                "STRIPE_SESSION_NOT_FOUND",
                entityType,
                Operation.READ
            );
            default -> new BusinessException(
                entityType.getErrorMessage("FIND", identifier),
                "ENTITY_NOT_FOUND",
                entityType,
                Operation.READ
            );
        };
    }
    
    /**
     * Crée une exception 'Not Found' avec logging automatique et mise en cache
     * @param entityType Type d'entité
     * @param identifier Identifiant
     * @param logger Logger pour enregistrer l'erreur
     * @return Exception avec logging
     */
    public static Exception createNotFoundExceptionWithLog(
            EntityType entityType, Object identifier, Logger logger) {
        
        String cacheKey = entityType.name() + "_" + identifier;
        Exception exception = EXCEPTION_CACHE.computeIfAbsent(cacheKey, 
            k -> createNotFoundException(entityType, identifier));
        
        // Log selon le niveau approprié
        if (logger.isWarnEnabled()) {
            logger.warn("{} lookup failed for identifier: {} [Operation: {}]", 
                entityType.getDisplayName(), identifier, Operation.READ.getName());
        }
        
        return exception;
    }
    
    // ==================== AUTHORIZATION EXCEPTIONS ====================
    
    /**
     * Crée une exception d'autorisation selon le contexte avec Pattern Matching
     * @param context Contexte d'autorisation
     * @param userId ID de l'utilisateur
     * @param resourceId ID de la ressource
     * @return Exception d'autorisation appropriée
     */
    public static RuntimeException createUnauthorizedException(
            AuthorizationContext context, Long userId, Object resourceId) {
        
        return switch (context) {
            case SALON_ACCESS -> new AccessDeniedException(
                String.format("User %d not authorized to access salon %s", userId, resourceId)
            );
            case CATEGORY_ACCESS -> new AccessDeniedException(
                String.format("User %d not authorized to access category %s", userId, resourceId)
            );
            case BOOKING_ACCESS -> new AccessDeniedException(
                String.format("User %d not authorized to access booking %s", userId, resourceId)
            );
            case PAYMENT_ACCESS -> new AccessDeniedException(
                String.format("User %d not authorized to access payment %s", userId, resourceId)
            );
            case ADMIN_REQUIRED -> new AccessDeniedException(
                String.format("User %d requires admin privileges for this operation", userId)
            );
            case OWNER_REQUIRED -> new AccessDeniedException(
                String.format("User %d requires owner privileges for resource %s", userId, resourceId)
            );
            case READ -> new AccessDeniedException(
                String.format("User %d not authorized to read resource %s", userId, resourceId)
            );
            case WRITE -> new AccessDeniedException(
                String.format("User %d not authorized to modify resource %s", userId, resourceId)
            );
            case DELETE -> new AccessDeniedException(
                String.format("User %d not authorized to delete resource %s", userId, resourceId)
            );
        };
    }
    
    /**
     * Crée une exception d'autorisation avec logging de sécurité
     * @param context Contexte d'autorisation
     * @param userId ID utilisateur
     * @param resourceId ID ressource
     * @param logger Logger de sécurité
     * @return Exception avec audit de sécurité
     */
    public static RuntimeException createUnauthorizedExceptionWithLog(
            AuthorizationContext context, Long userId, Object resourceId, Logger logger) {
        
        RuntimeException exception = createUnauthorizedException(context, userId, resourceId);
        
        // Log de sécurité avec niveau ERROR pour audit
        if (logger.isErrorEnabled()) {
            String action = switch (context) {
                case SALON_ACCESS -> "salon access";
                case CATEGORY_ACCESS -> "category access";
                case BOOKING_ACCESS -> "booking access"; 
                case PAYMENT_ACCESS -> "payment access";
                case ADMIN_REQUIRED -> "admin operation";
                case OWNER_REQUIRED -> "owner operation";
                case READ -> "read operation";
                case WRITE -> "write operation";
                case DELETE -> "delete operation";
            };
            
            logger.error("SECURITY ALERT: Unauthorized {} attempt by user {} for resource {} [IP: {}]", 
                action, userId, resourceId, getCurrentUserIp());
        }
        
        return exception;
    }
    
    // ==================== VALIDATION EXCEPTIONS ====================
    
    /**
     * Crée une exception de validation avec Pattern Matching Java 21
     * @param type Type de validation échouée
     * @param fieldName Nom du champ
     * @param value Valeur invalide
     * @return Exception de validation appropriée
     */
    public static ValidationException createValidationException(
            ValidationType type, String fieldName, Object value) {
        
        return switch (type) {
            case NULL_VALUE -> new ValidationException(
                fieldName + " cannot be null",
                "NULL_VALUE_ERROR",
                fieldName,
                value
            );
            case EMPTY_VALUE -> new ValidationException(
                fieldName + " cannot be empty",
                "EMPTY_VALUE_ERROR",
                fieldName,
                value
            );
            case INVALID_FORMAT -> new ValidationException(
                fieldName + " has invalid format: " + value,
                "INVALID_FORMAT_ERROR",
                fieldName,
                value
            );
            case OUT_OF_RANGE -> new ValidationException(
                fieldName + " is out of valid range: " + value,
                "OUT_OF_RANGE_ERROR",
                fieldName,
                value
            );
            case NEGATIVE_VALUE -> new ValidationException(
                fieldName + " must be positive, got: " + value,
                "NEGATIVE_VALUE_ERROR",
                fieldName,
                value
            );
            case TOO_LONG -> new ValidationException(
                fieldName + " exceeds maximum length: " + value,
                "TOO_LONG_ERROR",
                fieldName,
                value
            );
            case TOO_SHORT -> new ValidationException(
                fieldName + " below minimum length: " + value,
                "TOO_SHORT_ERROR",
                fieldName,
                value
            );
            case DUPLICATE_VALUE -> new ValidationException(
                fieldName + " already exists: " + value,
                "DUPLICATE_VALUE_ERROR",
                fieldName,
                value
            );
            case CONSTRAINT_VIOLATION -> new ValidationException(
                fieldName + " violates business constraint: " + value,
                "CONSTRAINT_VIOLATION_ERROR",
                fieldName,
                value
            );
        };
    }
    
    // ==================== AUTHENTICATION EXCEPTIONS ====================
    
    /**
     * Crée des exceptions d'authentification avec Pattern Matching et sécurité renforcée
     * @param error Type d'erreur d'authentification
     * @param username Nom d'utilisateur
     * @return Exception d'authentification appropriée
     */
    public static RuntimeException createAuthenticationException(
            AuthenticationError error, String username) {
        
        return switch (error) {
            case INVALID_CREDENTIALS -> new BadCredentialsException(
                "Invalid credentials provided"  // Message générique pour sécurité
            );
            case ACCOUNT_DISABLED -> new BadCredentialsException(
                "Account is disabled"
            );
            case ACCOUNT_LOCKED -> new BadCredentialsException(
                "Account is temporarily locked"
            );
            case EXPIRED_TOKEN -> new BadCredentialsException(
                "Authentication token has expired"
            );
            case USER_NOT_FOUND -> new BadCredentialsException(
                "Invalid credentials provided"  // Message générique pour sécurité
            );
            case PASSWORD_EXPIRED -> new BadCredentialsException(
                "Password has expired and must be changed"
            );
            case USER_NOT_AUTHENTICATED -> new BadCredentialsException(
                "Authentication required"
            );
            case TOO_MANY_ATTEMPTS -> new BadCredentialsException(
                "Too many failed login attempts. Account temporarily locked."
            );
            case SESSION_EXPIRED -> new BadCredentialsException(
                "Session has expired. Please login again."
            );
        };
    }
    
    /**
     * Crée une exception d'authentification avec logging de sécurité et délai anti-brute force
     * @param error Type d'erreur
     * @param username Nom d'utilisateur
     * @param logger Logger de sécurité
     * @return Exception avec mesures de sécurité
     */
    public static RuntimeException createAuthenticationExceptionWithLog(
            AuthenticationError error, String username, Logger logger) {
        
        RuntimeException exception = createAuthenticationException(error, username);
        
        // Log de sécurité selon le niveau d'alerte
        String logLevel = switch (error) {
            case INVALID_CREDENTIALS, USER_NOT_FOUND -> "WARN";
            case TOO_MANY_ATTEMPTS, ACCOUNT_LOCKED -> "ERROR";
            default -> "INFO";
        };
        
        String logMessage = String.format(
            "SECURITY: Authentication failure [Error: %s, User: %s, IP: %s, Time: %s]",
            error.name(), maskUsername(username), getCurrentUserIp(), java.time.LocalDateTime.now()
        );
        
        switch (logLevel) {
            case "ERROR" -> logger.error(logMessage);
            case "WARN" -> logger.warn(logMessage);
            default -> logger.info(logMessage);
        }
        
        // Délai anti-brute force progressif
        implementSecurityDelay(error);
        
        return exception;
    }
    
    // ==================== BUSINESS LOGIC EXCEPTIONS ====================
    
    /**
     * Crée des exceptions de logique métier avec Pattern Matching et contexte enrichi
     * @param error Type d'erreur métier
     * @param params Paramètres pour le message
     * @return Exception métier appropriée
     */
    public static BusinessException createBusinessException(
            BusinessError error, Object... params) {
        
        return switch (error) {
            case BOOKING_CONFLICT -> new BusinessException(
                String.format("Booking conflict detected for time slot %s to %s", params),
                "BOOKING_CONFLICT",
                EntityType.BOOKING,
                Operation.CREATE
            );
            case INSUFFICIENT_BALANCE -> new BusinessException(
                String.format("Insufficient balance. Required: %s, Available: %s", params),
                "INSUFFICIENT_BALANCE",
                EntityType.PAYMENT,
                Operation.PROCESS_PAYMENT
            );
            case SERVICE_UNAVAILABLE -> new BusinessException(
                String.format("Service %s is currently unavailable", params),
                "SERVICE_UNAVAILABLE",
                EntityType.SERVICE_OFFERING,
                Operation.READ
            );
            case SALON_CLOSED -> new BusinessException(
                String.format("Salon is closed at %s", params),
                "SALON_CLOSED",
                EntityType.SALON,
                Operation.BOOK
            );
            case MAXIMUM_BOOKINGS_EXCEEDED -> new BusinessException(
                String.format("Maximum bookings (%s) exceeded for user %s", params),
                "MAX_BOOKINGS_EXCEEDED",
                EntityType.BOOKING,
                Operation.CREATE
            );
            case PAYMENT_ALREADY_PROCESSED -> new BusinessException(
                String.format("Payment %s has already been processed", params),
                "PAYMENT_PROCESSED",
                EntityType.PAYMENT,
                Operation.PROCESS_PAYMENT
            );
            case BOOKING_CANCELLATION_DEADLINE -> new BusinessException(
                String.format("Booking %s cannot be cancelled less than %s hours before start time", params),
                "CANCELLATION_DEADLINE",
                EntityType.BOOKING,
                Operation.CANCEL_BOOKING
            );
            case INVALID_TIME_SLOT -> new BusinessException(
                String.format("Invalid time slot: %s", params),
                "INVALID_TIME_SLOT",
                EntityType.BOOKING,
                Operation.VALIDATE
            );
            case CONCURRENT_MODIFICATION -> new BusinessException(
                String.format("Resource %s was modified by another user", params),
                "CONCURRENT_MODIFICATION",
                null,
                Operation.UPDATE
            );
        };
    }
    
    // ==================== EXCEPTION WRAPPING ====================
    
    /**
     * Wrape une exception générique en BusinessException avec contexte
     * @param cause Exception originale
     * @param entityType Type d'entité concernée
     * @param operation Opération concernée
     * @return BusinessException avec contexte
     */
    public static BusinessException wrapException(
            Throwable cause, EntityType entityType, Operation operation) {
        return new BusinessException(
            "Operation failed: " + cause.getMessage(),
            "OPERATION_FAILED",
            entityType,
            operation,
            cause
        );
    }
    
    /**
     * Convertit une ValidationResult.Error en exception
     * @param validationError Erreur de validation
     * @return Exception appropriée
     */
    public static ValidationException fromValidationError(
            com.jb.afrostyle.core.validation.ValidationResult.Error<?> validationError) {
        return new ValidationException(
            validationError.message(),
            validationError.errorCode() != null ? validationError.errorCode() : "VALIDATION_ERROR",
            "unknown",
            null,
            validationError.cause()
        );
    }
    
    // ==================== UTILITAIRES PRIVÉS ====================
    
    /**
     * Masque le nom d'utilisateur pour les logs de sécurité
     */
    private static String maskUsername(String username) {
        if (username == null || username.length() < 3) {
            return "***";
        }
        return username.substring(0, 2) + "*".repeat(username.length() - 2);
    }
    
    /**
     * Obtient l'IP de l'utilisateur actuel (placeholder)
     */
    private static String getCurrentUserIp() {
        // TODO: Intégrer avec HttpServletRequest pour obtenir la vraie IP
        return "unknown";
    }
    
    /**
     * Implémente un délai de sécurité progressif contre les attaques brute force
     */
    private static void implementSecurityDelay(AuthenticationError error) {
        int delay = switch (error) {
            case INVALID_CREDENTIALS, USER_NOT_FOUND -> 1000; // 1 seconde
            case TOO_MANY_ATTEMPTS -> 5000; // 5 secondes
            case ACCOUNT_LOCKED -> 10000; // 10 secondes
            default -> 0;
        };
        
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    // ==================== ENUMS POUR PATTERN MATCHING ====================
    
    /**
     * Contextes d'autorisation pour les exceptions
     */
    public enum AuthorizationContext {
        SALON_ACCESS,
        CATEGORY_ACCESS,
        BOOKING_ACCESS,
        PAYMENT_ACCESS,
        ADMIN_REQUIRED,
        OWNER_REQUIRED,
        READ,
        WRITE,
        DELETE
    }
    
    /**
     * Types de validation pour les exceptions
     */
    public enum ValidationType {
        NULL_VALUE,
        EMPTY_VALUE,
        INVALID_FORMAT,
        OUT_OF_RANGE,
        NEGATIVE_VALUE,
        TOO_LONG,
        TOO_SHORT,
        DUPLICATE_VALUE,
        CONSTRAINT_VIOLATION
    }
    
    /**
     * Erreurs d'authentification
     */
    public enum AuthenticationError {
        INVALID_CREDENTIALS,
        ACCOUNT_DISABLED,
        ACCOUNT_LOCKED,
        EXPIRED_TOKEN,
        USER_NOT_FOUND,
        PASSWORD_EXPIRED,
        USER_NOT_AUTHENTICATED,
        TOO_MANY_ATTEMPTS,
        SESSION_EXPIRED
    }
    
    /**
     * Erreurs de logique métier
     */
    public enum BusinessError {
        BOOKING_CONFLICT,
        INSUFFICIENT_BALANCE,
        SERVICE_UNAVAILABLE,
        SALON_CLOSED,
        MAXIMUM_BOOKINGS_EXCEEDED,
        PAYMENT_ALREADY_PROCESSED,
        BOOKING_CANCELLATION_DEADLINE,
        INVALID_TIME_SLOT,
        CONCURRENT_MODIFICATION
    }
}