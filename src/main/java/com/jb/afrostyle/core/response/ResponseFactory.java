package com.jb.afrostyle.core.response;

import com.jb.afrostyle.core.dto.ApiResponse;
import com.jb.afrostyle.core.enums.EntityType;
import com.jb.afrostyle.core.enums.Operation;
import com.jb.afrostyle.core.validation.ValidationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Factory intelligente centralisée pour toutes les réponses HTTP dans AfroStyle
 * Utilise Java 21 Pattern Matching pour une gestion avancée des réponses
 * Intègre ValidationResult, ExceptionUtils et tous les patterns existants
 * 
 * @version 1.0
 * @since Java 21
 */
public final class ResponseFactory {
    
    private ResponseFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // ==================== RÉPONSES DE SUCCÈS ====================
    
    /**
     * Crée une réponse de succès simple avec données
     * @param data Données à retourner
     * @return ResponseEntity avec status 200 OK
     */
    public static <T> ResponseEntity<T> success(T data) {
        return ResponseEntity.ok(data);
    }
    
    /**
     * Crée une réponse de succès avec ApiResponse
     * @param message Message de succès
     * @return ResponseEntity avec ApiResponse
     */
    public static ResponseEntity<ApiResponse> success(String message) {
        return ResponseEntity.ok(ApiResponse.ofSuccess(message));
    }
    
    /**
     * Crée une réponse de succès avec Operation et EntityType
     * @param operation Opération effectuée
     * @param entityType Type d'entité
     * @param identifier Identifiant de l'entité
     * @return ResponseEntity avec ApiResponse contextuelle
     */
    public static ResponseEntity<ApiResponse> success(Operation operation, EntityType entityType, Object identifier) {
        ApiResponse response = ApiResponse.ofSuccess(operation, entityType, identifier);
        return ResponseEntity.status(operation.getSuccessStatus()).body(response);
    }
    
    /**
     * Crée une réponse de création réussie (201)
     * @param data Données créées
     * @param location URL de la ressource créée
     * @return ResponseEntity avec status 201 CREATED
     */
    public static <T> ResponseEntity<T> created(T data, String location) {
        return ResponseEntity.created(java.net.URI.create(location)).body(data);
    }
    
    /**
     * Crée une réponse de création avec Operation et EntityType
     * @param entityType Type d'entité créée
     * @param identifier Identifiant de l'entité créée
     * @return ResponseEntity 201 avec ApiResponse
     */
    public static ResponseEntity<ApiResponse> created(EntityType entityType, Object identifier) {
        ApiResponse response = ApiResponse.ofSuccess(Operation.CREATE, entityType, identifier);
        String location = entityType.getRestUrl(identifier);
        return ResponseEntity.created(java.net.URI.create(location)).body(response);
    }
    
    // ==================== RÉPONSES D'ERREUR AVEC PATTERN MATCHING ====================
    
    /**
     * Crée une réponse d'erreur intelligente basée sur l'exception avec Pattern Matching Java 21
     * @param exception Exception à traiter
     * @return ResponseEntity avec status et ApiResponse appropriés
     */
    public static ResponseEntity<ApiResponse> errorFromException(Exception exception) {
        return switch (exception) {
            // Exceptions de validation
            case IllegalArgumentException ex -> badRequest(ex.getMessage());
            
            // Exceptions de sécurité
            case SecurityException ex -> forbidden(ex.getMessage());
            
            // Exceptions avec message contenant des mots-clés
            case RuntimeException ex when ex.getMessage().toLowerCase().contains("not found") ->
                notFound(ex.getMessage());
            case RuntimeException ex when ex.getMessage().toLowerCase().contains("already exists") ->
                conflict(ex.getMessage());
            case RuntimeException ex when ex.getMessage().toLowerCase().contains("unauthorized") ->
                unauthorized(ex.getMessage());
            case RuntimeException ex when ex.getMessage().toLowerCase().contains("forbidden") ->
                forbidden(ex.getMessage());
            case RuntimeException ex when ex.getMessage().toLowerCase().contains("invalid") ->
                badRequest(ex.getMessage());
                
            // Exception générique
            case Exception ex -> internalServerError("An unexpected error occurred: " + ex.getMessage());
        };
    }
    
    /**
     * Crée une réponse d'erreur basée sur ValidationResult avec Pattern Matching
     * @param validationResult Résultat de validation
     * @return ResponseEntity appropriée selon le type d'erreur
     */
    public static <T> ResponseEntity<ApiResponse> errorFromValidation(ValidationResult<T> validationResult) {
        return switch (validationResult) {
            case ValidationResult.Success<T> success -> 
                throw new IllegalArgumentException("Cannot create error response from successful validation");
                
            case ValidationResult.Error<T> error -> switch (error.message()) {
                case String msg when msg.toLowerCase().contains("cannot be null") ->
                    badRequest(msg);
                case String msg when msg.toLowerCase().contains("cannot be empty") ->
                    badRequest(msg);
                case String msg when msg.toLowerCase().contains("invalid format") ->
                    badRequest(msg);
                case String msg when msg.toLowerCase().contains("not found") ->
                    notFound(msg);
                case String msg when msg.toLowerCase().contains("unauthorized") ->
                    forbidden(msg);
                case String msg when msg.toLowerCase().contains("already exists") ->
                    conflict(msg);
                default -> badRequest(error.message());
            };
        };
    }
    
    /**
     * Crée une réponse d'erreur avec gestion intelligente des codes d'erreur
     * @param httpStatus Status HTTP
     * @param message Message d'erreur
     * @param errorCode Code d'erreur spécifique
     * @return ResponseEntity avec ApiResponse d'erreur
     */
    public static ResponseEntity<ApiResponse> error(HttpStatus httpStatus, String message, String errorCode) {
        ApiResponse response = ApiResponse.ofError(message, errorCode);
        return ResponseEntity.status(httpStatus).body(response);
    }
    
    // ==================== RÉPONSES D'ERREUR SPÉCIFIQUES ====================
    
    /**
     * Réponse Bad Request (400)
     * @param message Message d'erreur
     * @return ResponseEntity 400
     */
    public static ResponseEntity<ApiResponse> badRequest(String message) {
        return error(HttpStatus.BAD_REQUEST, message, "BAD_REQUEST");
    }
    
    /**
     * Réponse Unauthorized (401)
     * @param message Message d'erreur
     * @return ResponseEntity 401
     */
    public static ResponseEntity<ApiResponse> unauthorized(String message) {
        return error(HttpStatus.UNAUTHORIZED, message, "UNAUTHORIZED");
    }
    
    /**
     * Réponse Forbidden (403)
     * @param message Message d'erreur
     * @return ResponseEntity 403
     */
    public static ResponseEntity<ApiResponse> forbidden(String message) {
        return error(HttpStatus.FORBIDDEN, message, "FORBIDDEN");
    }
    
    /**
     * Réponse Not Found (404)
     * @param message Message d'erreur
     * @return ResponseEntity 404
     */
    public static ResponseEntity<ApiResponse> notFound(String message) {
        return error(HttpStatus.NOT_FOUND, message, "NOT_FOUND");
    }
    
    /**
     * Réponse Not Found avec EntityType
     * @param entityType Type d'entité non trouvée
     * @param identifier Identifiant recherché
     * @return ResponseEntity 404 avec message contextualisé
     */
    public static ResponseEntity<ApiResponse> notFound(EntityType entityType, Object identifier) {
        ApiResponse response = ApiResponse.ofNotFound(entityType, identifier);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Réponse Conflict (409)
     * @param message Message de conflit
     * @return ResponseEntity 409
     */
    public static ResponseEntity<ApiResponse> conflict(String message) {
        return error(HttpStatus.CONFLICT, message, "CONFLICT");
    }
    
    /**
     * Réponse Unprocessable Entity (422)
     * @param message Message d'erreur
     * @return ResponseEntity 422
     */
    public static ResponseEntity<ApiResponse> unprocessableEntity(String message) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, message, "UNPROCESSABLE_ENTITY");
    }
    
    /**
     * Réponse Internal Server Error (500)
     * @param message Message d'erreur
     * @return ResponseEntity 500
     */
    public static ResponseEntity<ApiResponse> internalServerError(String message) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, message, "INTERNAL_SERVER_ERROR");
    }
    
    // ==================== RÉPONSES MÉTIER SPÉCIALISÉES ====================
    
    /**
     * Réponses d'authentification avec Pattern Matching
     * @param result Résultat d'authentification
     * @param username Nom d'utilisateur (optionnel)
     * @return ResponseEntity appropriée
     */
    public static ResponseEntity<ApiResponse> authentication(AuthResult result, String username) {
        return switch (result) {
            case LOGIN_SUCCESS -> success("Authentication successful for user: " + username);
            case LOGIN_FAILED -> unauthorized("Invalid credentials");
            case ACCOUNT_DISABLED -> forbidden("Account is disabled");
            case ACCOUNT_LOCKED -> forbidden("Account is locked");
            case PASSWORD_EXPIRED -> forbidden("Password has expired");
            case TOKEN_EXPIRED -> unauthorized("Authentication token has expired");
            case LOGOUT_SUCCESS -> success("Logout successful");
        };
    }
    
    /**
     * Réponses de réservation avec Pattern Matching
     * @param result Résultat de l'opération de réservation
     * @param bookingId ID de la réservation
     * @return ResponseEntity appropriée
     */
    public static ResponseEntity<ApiResponse> booking(BookingResult result, Object bookingId) {
        return switch (result) {
            case BOOKING_CREATED -> created(EntityType.BOOKING, bookingId);
            case BOOKING_CONFIRMED -> success("Booking confirmed successfully");
            case BOOKING_CANCELLED -> success("Booking cancelled successfully");
            case TIME_SLOT_UNAVAILABLE -> conflict("The requested time slot is not available");
            case BOOKING_NOT_FOUND -> notFound(EntityType.BOOKING, bookingId);
            case CANCELLATION_DEADLINE_PASSED -> 
                badRequest("Booking cannot be cancelled less than 24 hours before start time");
        };
    }
    
    /**
     * Réponses de paiement avec Pattern Matching
     * @param result Résultat de l'opération de paiement
     * @param paymentId ID du paiement
     * @return ResponseEntity appropriée
     */
    public static ResponseEntity<ApiResponse> payment(PaymentResult result, Object paymentId) {
        return switch (result) {
            case PAYMENT_CREATED -> created(EntityType.PAYMENT, paymentId);
            case PAYMENT_COMPLETED -> success("Payment completed successfully");
            case PAYMENT_FAILED -> badRequest("Payment processing failed");
            case INSUFFICIENT_FUNDS -> badRequest("Insufficient funds");
            case PAYMENT_ALREADY_PROCESSED -> conflict("Payment has already been processed");
            case REFUND_PROCESSED -> success("Refund processed successfully");
        };
    }
    
    // ==================== GESTION AVANCÉE DES ERREURS DE VALIDATION ====================
    
    /**
     * Crée une réponse d'erreur de validation avec détails
     * @param validationErrors Map des erreurs de validation
     * @return ResponseEntity 400 avec détails des erreurs
     */
    public static ResponseEntity<ApiResponse> validationError(Map<String, Object> validationErrors) {
        ApiResponse response = ApiResponse.ofValidationError(validationErrors);
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Crée une réponse d'erreur d'authentification
     * @param message Message d'erreur d'authentification
     * @return ResponseEntity 401 avec contexte d'authentification
     */
    public static ResponseEntity<ApiResponse> authenticationError(String message) {
        ApiResponse response = ApiResponse.ofAuthenticationError(message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Crée une réponse d'erreur d'autorisation
     * @param message Message d'erreur d'autorisation
     * @return ResponseEntity 403 avec contexte d'autorisation
     */
    public static ResponseEntity<ApiResponse> authorizationError(String message) {
        ApiResponse response = ApiResponse.ofAuthorizationError(message);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    // ==================== UTILITAIRES AVANCÉS ====================
    
    /**
     * Exécute une opération et retourne le résultat ou l'erreur appropriée
     * @param operation Supplier de l'opération à exécuter
     * @param entityType Type d'entité concernée
     * @param operationType Type d'opération
     * @return ResponseEntity avec le résultat ou l'erreur
     */
    public static <T> ResponseEntity<?> executeOperation(
            Supplier<T> operation, 
            EntityType entityType, 
            Operation operationType) {
        try {
            T result = operation.get();
            if (result == null) {
                return notFound(entityType, null);
            }
            return success(result);
        } catch (Exception e) {
            return errorFromException(e);
        }
    }
    
    /**
     * Crée une réponse conditionnelle basée sur un booléen
     * @param condition Condition à évaluer
     * @param successMessage Message de succès
     * @param errorMessage Message d'erreur
     * @return ResponseEntity selon la condition
     */
    public static ResponseEntity<ApiResponse> conditional(
            boolean condition, 
            String successMessage, 
            String errorMessage) {
        return condition ? success(successMessage) : badRequest(errorMessage);
    }
    
    /**
     * Crée une réponse avec gestion automatique du chemin de requête
     * @param response ApiResponse de base
     * @param requestPath Chemin de la requête
     * @param httpStatus Status HTTP
     * @return ResponseEntity avec chemin ajouté
     */
    public static ResponseEntity<ApiResponse> withPath(
            ApiResponse response, 
            String requestPath, 
            HttpStatus httpStatus) {
        ApiResponse responseWithPath = response.withPath(requestPath);
        return ResponseEntity.status(httpStatus).body(responseWithPath);
    }
    
    // ==================== ENUMS POUR PATTERN MATCHING ====================
    
    /**
     * Résultats d'authentification
     */
    public enum AuthResult {
        LOGIN_SUCCESS, LOGIN_FAILED, ACCOUNT_DISABLED, ACCOUNT_LOCKED,
        PASSWORD_EXPIRED, TOKEN_EXPIRED, LOGOUT_SUCCESS
    }
    
    /**
     * Résultats d'opérations de réservation
     */
    public enum BookingResult {
        BOOKING_CREATED, BOOKING_CONFIRMED, BOOKING_CANCELLED,
        TIME_SLOT_UNAVAILABLE, BOOKING_NOT_FOUND, CANCELLATION_DEADLINE_PASSED
    }
    
    /**
     * Résultats d'opérations de paiement
     */
    public enum PaymentResult {
        PAYMENT_CREATED, PAYMENT_COMPLETED, PAYMENT_FAILED,
        INSUFFICIENT_FUNDS, PAYMENT_ALREADY_PROCESSED, REFUND_PROCESSED
    }
    
    /**
     * Opérations sur les entités
     */
    public enum EntityOperation {
        CREATE, READ, UPDATE, DELETE, VALIDATE, SEARCH
    }
    
    // ==================== MÉTHODES SUPPLÉMENTAIRES ====================
    
    /**
     * Crée une réponse de succès avec message personnalisé
     * @param message Message de succès
     * @return ResponseEntity avec succès et message
     */
    public static ResponseEntity<ApiResponse> successWithMessage(String message) {
        ApiResponse response = ApiResponse.ofSuccess(message);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Crée une réponse de succès avec données et message personnalisé
     * Note: ApiResponse ne supporte pas les generics, utilise details pour les données
     * @param data Données à retourner
     * @param message Message de succès
     * @return ResponseEntity avec succès, données et message
     */
    public static <T> ResponseEntity<ApiResponse> successWithMessage(T data, String message) {
        ApiResponse response = ApiResponse.of(
            true, message, null, null, 
            Map.of("data", data), null, null
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * Crée une réponse pour une opération sur entité
     * @param operation Type d'opération
     * @param entityName Nom de l'entité
     * @param entityId ID de l'entité
     * @return ResponseEntity avec succès d'opération
     */
    public static ResponseEntity<ApiResponse> entityResponse(EntityOperation operation, String entityName, Object entityId) {
        String message = switch (operation) {
            case CREATE -> entityName + " created successfully";
            case READ -> entityName + " retrieved successfully";
            case UPDATE -> entityName + " updated successfully";
            case DELETE -> entityName + " deleted successfully";
            case VALIDATE -> entityName + " validated successfully";
            case SEARCH -> entityName + " search completed";
        };
        
        ApiResponse response = ApiResponse.of(
            true, message, null, null,
            Map.of("entityId", entityId, "operation", operation.name()), 
            null, null
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * Crée une réponse de paiement basée sur le résultat
     * @param result Résultat de paiement
     * @param paymentId ID du paiement
     * @return ResponseEntity appropriée
     */
    public static ResponseEntity<ApiResponse> paymentResponse(PaymentResult result, Long paymentId) {
        return switch (result) {
            case PAYMENT_CREATED -> success("Payment created successfully with ID: " + paymentId);
            case PAYMENT_COMPLETED -> success("Payment completed successfully");
            case PAYMENT_FAILED -> badRequest("Payment failed");
            case INSUFFICIENT_FUNDS -> badRequest("Insufficient funds");
            case PAYMENT_ALREADY_PROCESSED -> conflict("Payment already processed");
            case REFUND_PROCESSED -> success("Refund processed successfully");
        };
    }
}