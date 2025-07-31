package com.jb.afrostyle.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jb.afrostyle.core.enums.EntityType;
import com.jb.afrostyle.core.enums.Operation;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Réponse API standardisée centralisée pour AfroStyle
 * Version améliorée avec intégration des enums Operation et EntityType
 * 
 * @version 2.0 - Centralisée dans /core
 * @since Java 21
 */
public record ApiResponse(
        /**
         * Indique si l'opération a réussi
         * true = succès, false = erreur
         */
        Boolean success,
        
        /**
         * Message descriptif du résultat de l'opération
         * Peut contenir un message de succès ou d'erreur
         */
        String message,
        
        /**
         * Code d'erreur spécifique (optionnel)
         * Ex: "VALIDATION_ERROR", "NOT_FOUND", "UNAUTHORIZED"
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String errorCode,
        
        /**
         * Timestamp de la réponse
         * Inclus uniquement si défini
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,
        
        /**
         * Chemin de la requête qui a généré cette réponse
         * Inclus uniquement si défini
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String path,
        
        /**
         * Détails supplémentaires (par exemple, erreurs de validation)
         * Inclus uniquement si défini
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Map<String, Object> details,
        
        /**
         * Opération qui a généré cette réponse (optionnel)
         * Utilisé pour l'audit et le debugging
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String operation,
        
        /**
         * Type d'entité concernée (optionnel)
         * Utilisé pour les opérations CRUD
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String entityType
) {
    
    // ==================== CONSTRUCTEURS STATIQUES DE BASE ====================
    
    /**
     * Constructeur statique pour créer une réponse de succès simple
     * @param message Message de succès
     * @return Instance d'ApiResponse avec success=true
     */
    public static ApiResponse ofSuccess(String message) {
        return new ApiResponse(true, message, null, LocalDateTime.now(), null, null, null, null);
    }

    /**
     * Constructeur statique pour créer une réponse d'erreur simple
     * @param message Message d'erreur
     * @return Instance d'ApiResponse avec success=false
     */
    public static ApiResponse ofError(String message) {
        return new ApiResponse(false, message, null, LocalDateTime.now(), null, null, null, null);
    }

    /**
     * Constructeur statique pour créer une réponse de succès générique
     * @return Instance d'ApiResponse avec message de succès par défaut
     */
    public static ApiResponse ofSuccess() {
        return ofSuccess("Operation completed successfully");
    }

    /**
     * Constructeur statique pour créer une réponse d'erreur générique
     * @return Instance d'ApiResponse avec message d'erreur par défaut
     */
    public static ApiResponse ofError() {
        return ofError("An error occurred");
    }
    
    // ==================== CONSTRUCTEURS AVEC ENUMS ====================
    
    /**
     * Crée une réponse de succès avec Operation et EntityType
     * @param operation Opération effectuée
     * @param entityType Type d'entité concernée
     * @param identifier Identifiant de l'entité (optionnel)
     * @return ApiResponse de succès
     */
    public static ApiResponse ofSuccess(Operation operation, EntityType entityType, Object identifier) {
        return new ApiResponse(
            true,
            operation.getSuccessMessage(entityType, identifier),
            null,
            LocalDateTime.now(),
            null,
            null,
            operation.getName(),
            entityType.getClassName()
        );
    }
    
    /**
     * Crée une réponse d'erreur avec Operation et EntityType
     * @param operation Opération échouée
     * @param entityType Type d'entité concernée
     * @param identifier Identifiant de l'entité (optionnel)
     * @return ApiResponse d'erreur
     */
    public static ApiResponse ofError(Operation operation, EntityType entityType, Object identifier) {
        return new ApiResponse(
            false,
            operation.getErrorMessage(entityType, identifier),
            null,
            LocalDateTime.now(),
            null,
            null,
            operation.getName(),
            entityType.getClassName()
        );
    }
    
    /**
     * Crée une réponse d'erreur avec code d'erreur spécifique
     * @param message Message d'erreur
     * @param errorCode Code d'erreur
     * @return ApiResponse d'erreur avec code
     */
    public static ApiResponse ofError(String message, String errorCode) {
        return new ApiResponse(
            false,
            message,
            errorCode,
            LocalDateTime.now(),
            null,
            null,
            null,
            null
        );
    }
    
    /**
     * Crée une réponse avec tous les détails
     * @param success Succès ou échec
     * @param message Message
     * @param errorCode Code d'erreur (peut être null)
     * @param path Chemin de la requête
     * @param details Détails supplémentaires
     * @param operation Opération
     * @param entityType Type d'entité
     * @return ApiResponse complète
     */
    public static ApiResponse of(
            boolean success, 
            String message, 
            String errorCode,
            String path,
            Map<String, Object> details,
            Operation operation,
            EntityType entityType) {
        return new ApiResponse(
            success,
            message,
            errorCode,
            LocalDateTime.now(),
            path,
            details,
            operation != null ? operation.getName() : null,
            entityType != null ? entityType.getClassName() : null
        );
    }
    
    // ==================== CONSTRUCTEURS SPÉCIALISÉS ====================
    
    /**
     * Crée une réponse de validation échouée
     * @param validationErrors Erreurs de validation
     * @return ApiResponse d'erreur de validation
     */
    public static ApiResponse ofValidationError(Map<String, Object> validationErrors) {
        return new ApiResponse(
            false,
            "Validation failed",
            "VALIDATION_ERROR",
            LocalDateTime.now(),
            null,
            validationErrors,
            Operation.VALIDATE.getName(),
            null
        );
    }
    
    /**
     * Crée une réponse d'erreur d'authentification
     * @param message Message d'erreur
     * @return ApiResponse d'erreur d'authentification
     */
    public static ApiResponse ofAuthenticationError(String message) {
        return new ApiResponse(
            false,
            message,
            "AUTHENTICATION_ERROR",
            LocalDateTime.now(),
            null,
            null,
            Operation.LOGIN.getName(),
            EntityType.USER.getClassName()
        );
    }
    
    /**
     * Crée une réponse d'erreur d'autorisation (403)
     * @param message Message d'erreur
     * @return ApiResponse d'erreur d'autorisation
     */
    public static ApiResponse ofAuthorizationError(String message) {
        return new ApiResponse(
            false,
            message,
            "AUTHORIZATION_ERROR",
            LocalDateTime.now(),
            null,
            null,
            Operation.CHECK_PERMISSION.getName(),
            null
        );
    }
    
    /**
     * Crée une réponse d'entité non trouvée (404)
     * @param entityType Type d'entité
     * @param identifier Identifiant de l'entité
     * @return ApiResponse d'entité non trouvée
     */
    public static ApiResponse ofNotFound(EntityType entityType, Object identifier) {
        return new ApiResponse(
            false,
            entityType.getErrorMessage("FIND", identifier),
            "NOT_FOUND",
            LocalDateTime.now(),
            null,
            null,
            Operation.READ.getName(),
            entityType.getClassName()
        );
    }
    
    /**
     * Crée une réponse de conflit (409)
     * @param message Message de conflit
     * @param entityType Type d'entité concernée
     * @return ApiResponse de conflit
     */
    public static ApiResponse ofConflict(String message, EntityType entityType) {
        return new ApiResponse(
            false,
            message,
            "CONFLICT",
            LocalDateTime.now(),
            null,
            null,
            Operation.CREATE.getName(),
            entityType != null ? entityType.getClassName() : null
        );
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Ajoute le chemin de la requête à la réponse
     * @param path Chemin de la requête
     * @return Nouvelle ApiResponse avec le chemin
     */
    public ApiResponse withPath(String path) {
        return new ApiResponse(
            this.success,
            this.message,
            this.errorCode,
            this.timestamp,
            path,
            this.details,
            this.operation,
            this.entityType
        );
    }
    
    /**
     * Ajoute des détails supplémentaires à la réponse
     * @param details Détails à ajouter
     * @return Nouvelle ApiResponse avec les détails
     */
    public ApiResponse withDetails(Map<String, Object> details) {
        return new ApiResponse(
            this.success,
            this.message,
            this.errorCode,
            this.timestamp,
            this.path,
            details,
            this.operation,
            this.entityType
        );
    }
    
    /**
     * Ajoute un code d'erreur à la réponse
     * @param errorCode Code d'erreur
     * @return Nouvelle ApiResponse avec le code d'erreur
     */
    public ApiResponse withErrorCode(String errorCode) {
        return new ApiResponse(
            this.success,
            this.message,
            errorCode,
            this.timestamp,
            this.path,
            this.details,
            this.operation,
            this.entityType
        );
    }
    
    /**
     * Vérifie si la réponse indique un succès
     * @return true si success=true
     */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(success);
    }
    
    /**
     * Vérifie si la réponse indique un échec
     * @return true si success=false
     */
    public boolean isError() {
        return Boolean.FALSE.equals(success);
    }
    
    /**
     * Vérifie si la réponse a un code d'erreur
     * @return true si errorCode n'est pas null
     */
    public boolean hasErrorCode() {
        return errorCode != null && !errorCode.isEmpty();
    }
    
    /**
     * Vérifie si la réponse a des détails
     * @return true si details n'est pas null et non vide
     */
    public boolean hasDetails() {
        return details != null && !details.isEmpty();
    }
    
    /**
     * Obtient un détail spécifique par clé
     * @param key Clé du détail
     * @return Valeur du détail ou null si non trouvé
     */
    public Object getDetail(String key) {
        return details != null ? details.get(key) : null;
    }
    
    @Override
    public String toString() {
        return String.format("ApiResponse{success=%s, message='%s', errorCode='%s'}", 
                           success, message, errorCode);
    }
}