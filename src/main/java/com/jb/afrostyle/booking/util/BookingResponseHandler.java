package com.jb.afrostyle.booking.util;

import com.jb.afrostyle.user.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilitaire centralisé pour la gestion des réponses HTTP dans le module booking
 * Évite la duplication du pattern try/catch + log.error + ResponseEntity dans les contrôleurs
 * Standardise les réponses d'erreur et de succès
 */
@Component
public class BookingResponseHandler {
    
    private static final Logger log = LoggerFactory.getLogger(BookingResponseHandler.class);
    
    /**
     * Gère une réponse de succès avec données
     */
    public <T> ResponseEntity<T> handleSuccess(T data) {
        return ResponseEntity.ok(data);
    }
    
    /**
     * Gère une réponse de succès avec message personnalisé
     */
    public ResponseEntity<ApiResponse> handleSuccess(String message) {
        return ResponseEntity.ok(ApiResponse.ofSuccess(message));
    }
    
    /**
     * Gère une réponse de succès avec données et message
     */
    public <T> ResponseEntity<Map<String, Object>> handleSuccess(T data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gère une erreur avec logging automatique et réponse standardisée
     * CENTRALISE : log.error + ResponseEntity.badRequest
     */
    public ResponseEntity<ApiResponse> handleError(String operation, String message, Exception e) {
        log.error("❌ Error in {}: {}", operation, message, e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.ofError(message));
    }
    
    /**
     * Gère une erreur avec status HTTP personnalisé
     */
    public ResponseEntity<ApiResponse> handleError(String operation, String message, Exception e, HttpStatus status) {
        log.error("❌ Error in {} ({}): {}", operation, status.value(), message, e);
        return ResponseEntity.status(status)
                .body(ApiResponse.ofError(message));
    }
    
    /**
     * Gère une erreur simple sans exception
     */
    public ResponseEntity<ApiResponse> handleError(String operation, String message) {
        log.error("❌ Error in {}: {}", operation, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.ofError(message));
    }
    
    /**
     * Gère une erreur de validation
     */
    public ResponseEntity<ApiResponse> handleValidationError(String operation, String message) {
        log.warn("⚠️ Validation error in {}: {}", operation, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.ofError(message));
    }
    
    /**
     * Gère une erreur de ressource non trouvée
     */
    public ResponseEntity<ApiResponse> handleNotFoundError(String operation, String resource, String identifier) {
        String message = String.format("%s not found with identifier: %s", resource, identifier);
        log.warn("⚠️ Not found in {}: {}", operation, message);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.ofError(message));
    }
    
    /**
     * Gère une erreur d'autorisation
     */
    public ResponseEntity<ApiResponse> handleAuthorizationError(String operation, String message) {
        log.warn("🔒 Authorization error in {}: {}", operation, message);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.ofError(message));
    }
    
    /**
     * Exécute une opération avec gestion d'erreur automatique
     * Pattern pour éliminer les try/catch répétitifs
     */
    public <T> ResponseEntity<?> executeWithErrorHandling(String operation, ThrowingSupplier<T> supplier) {
        try {
            T result = supplier.get();
            return handleSuccess(result);
        } catch (Exception e) {
            return handleError(operation, e.getMessage(), e);
        }
    }
    
    /**
     * Exécute une opération avec gestion d'erreur et message de succès personnalisé
     */
    public <T> ResponseEntity<?> executeWithErrorHandling(String operation, ThrowingSupplier<T> supplier, String successMessage) {
        try {
            T result = supplier.get();
            return handleSuccess(result, successMessage);
        } catch (Exception e) {
            return handleError(operation, e.getMessage(), e);
        }
    }
    
    /**
     * Interface fonctionnelle pour les opérations qui peuvent lever des exceptions
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}