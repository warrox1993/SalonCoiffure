package com.jb.afrostyle.core.exception;

import com.jb.afrostyle.core.enums.EntityType;
import com.jb.afrostyle.core.enums.Operation;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Exception métier centralisée pour AfroStyle
 * Encapsule toutes les erreurs de logique métier avec contexte enrichi
 * Support des enums EntityType et Operation pour un debugging avancé
 * 
 * @version 1.0
 * @since Java 21
 */
public class BusinessException extends RuntimeException {
    
    private final String errorCode;
    private final EntityType entityType;
    private final Operation operation;
    private final Map<String, Object> context;
    private final LocalDateTime timestamp;
    
    // ==================== CONSTRUCTEURS ====================
    
    /**
     * Constructeur de base avec message
     * @param message Message d'erreur
     */
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.entityType = null;
        this.operation = null;
        this.context = new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructeur avec message et cause
     * @param message Message d'erreur
     * @param cause Cause de l'erreur
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
        this.entityType = null;
        this.operation = null;
        this.context = new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructeur avec code d'erreur
     * @param message Message d'erreur
     * @param errorCode Code d'erreur spécifique
     */
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : "BUSINESS_ERROR";
        this.entityType = null;
        this.operation = null;
        this.context = new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructeur avec contexte métier complet
     * @param message Message d'erreur
     * @param errorCode Code d'erreur
     * @param entityType Type d'entité concernée
     * @param operation Opération concernée
     */
    public BusinessException(String message, String errorCode, EntityType entityType, Operation operation) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : "BUSINESS_ERROR";
        this.entityType = entityType;
        this.operation = operation;
        this.context = new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructeur complet avec cause
     * @param message Message d'erreur
     * @param errorCode Code d'erreur
     * @param entityType Type d'entité
     * @param operation Opération
     * @param cause Cause originale
     */
    public BusinessException(String message, String errorCode, EntityType entityType, Operation operation, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : "BUSINESS_ERROR";
        this.entityType = entityType;
        this.operation = operation;
        this.context = new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }
    
    // ==================== GETTERS ====================
    
    /**
     * Obtient le code d'erreur
     * @return Code d'erreur
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Obtient le type d'entité concernée
     * @return Type d'entité ou null
     */
    public EntityType getEntityType() {
        return entityType;
    }
    
    /**
     * Obtient l'opération concernée
     * @return Opération ou null
     */
    public Operation getOperation() {
        return operation;
    }
    
    /**
     * Obtient le contexte d'erreur
     * @return Map du contexte
     */
    public Map<String, Object> getContext() {
        return new HashMap<>(context);
    }
    
    /**
     * Obtient le timestamp de l'erreur
     * @return Timestamp de création
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    // ==================== MÉTHODES DE CONTEXTE ====================
    
    /**
     * Ajoute un élément au contexte
     * @param key Clé
     * @param value Valeur
     * @return Cette exception pour chaînage
     */
    public BusinessException withContext(String key, Object value) {
        this.context.put(key, value);
        return this;
    }
    
    /**
     * Ajoute plusieurs éléments au contexte
     * @param contextData Map de données contextuelles
     * @return Cette exception pour chaînage
     */
    public BusinessException withContext(Map<String, Object> contextData) {
        this.context.putAll(contextData);
        return this;
    }
    
    /**
     * Ajoute l'ID de l'utilisateur au contexte
     * @param userId ID utilisateur
     * @return Cette exception pour chaînage
     */
    public BusinessException withUserId(Long userId) {
        return withContext("userId", userId);
    }
    
    /**
     * Ajoute l'ID de la ressource au contexte
     * @param resourceId ID de la ressource
     * @return Cette exception pour chaînage
     */
    public BusinessException withResourceId(Object resourceId) {
        return withContext("resourceId", resourceId);
    }
    
    /**
     * Ajoute des détails de validation au contexte
     * @param fieldName Nom du champ
     * @param fieldValue Valeur du champ
     * @return Cette exception pour chaînage
     */
    public BusinessException withValidationDetails(String fieldName, Object fieldValue) {
        return withContext("validationField", fieldName)
               .withContext("validationValue", fieldValue);
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Vérifie si l'exception concerne une entité spécifique
     * @param entityType Type d'entité à vérifier
     * @return true si l'exception concerne cette entité
     */
    public boolean isEntityException(EntityType entityType) {
        return this.entityType == entityType;
    }
    
    /**
     * Vérifie si l'exception concerne une opération spécifique
     * @param operation Opération à vérifier
     * @return true si l'exception concerne cette opération
     */
    public boolean isOperationException(Operation operation) {
        return this.operation == operation;
    }
    
    /**
     * Vérifie si l'exception a un code d'erreur spécifique
     * @param errorCode Code à vérifier
     * @return true si l'exception a ce code
     */
    public boolean hasErrorCode(String errorCode) {
        return this.errorCode.equals(errorCode);
    }
    
    /**
     * Obtient une valeur du contexte
     * @param key Clé à chercher
     * @return Valeur ou null si non trouvée
     */
    public Object getContextValue(String key) {
        return context.get(key);
    }
    
    /**
     * Obtient une valeur du contexte avec type spécifique
     * @param key Clé à chercher
     * @param type Type attendu
     * @return Valeur typée ou null
     */
    @SuppressWarnings("unchecked")
    public <T> T getContextValue(String key, Class<T> type) {
        Object value = context.get(key);
        return type.isInstance(value) ? (T) value : null;
    }
    
    /**
     * Vérifie si le contexte contient une clé
     * @param key Clé à vérifier
     * @return true si la clé existe
     */
    public boolean hasContextKey(String key) {
        return context.containsKey(key);
    }
    
    /**
     * Génère un message d'erreur enrichi avec le contexte
     * @return Message complet avec contexte
     */
    public String getEnrichedMessage() {
        StringBuilder sb = new StringBuilder(getMessage());
        
        if (entityType != null) {
            sb.append(" [Entity: ").append(entityType.getDisplayName()).append("]");
        }
        
        if (operation != null) {
            sb.append(" [Operation: ").append(operation.getName()).append("]");
        }
        
        sb.append(" [Code: ").append(errorCode).append("]");
        sb.append(" [Time: ").append(timestamp).append("]");
        
        if (!context.isEmpty()) {
            sb.append(" [Context: ").append(context).append("]");
        }
        
        return sb.toString();
    }
    
    /**
     * Convertit l'exception en Map pour sérialisation JSON
     * @return Map représentant l'exception
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("message", getMessage());
        map.put("errorCode", errorCode);
        map.put("timestamp", timestamp);
        
        if (entityType != null) {
            map.put("entityType", entityType.name());
            map.put("entityDisplayName", entityType.getDisplayName());
        }
        
        if (operation != null) {
            map.put("operation", operation.name());
            map.put("operationDisplayName", operation.getName());
        }
        
        if (!context.isEmpty()) {
            map.put("context", context);
        }
        
        if (getCause() != null) {
            map.put("cause", getCause().getMessage());
        }
        
        return map;
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Crée une BusinessException pour entité non trouvée
     * @param entityType Type d'entité
     * @param identifier Identifiant recherché
     * @return BusinessException configurée
     */
    public static BusinessException notFound(EntityType entityType, Object identifier) {
        return new BusinessException(
            entityType.getErrorMessage("FIND", identifier),
            "NOT_FOUND",
            entityType,
            Operation.READ
        ).withResourceId(identifier);
    }
    
    /**
     * Crée une BusinessException pour conflit métier
     * @param message Message de conflit
     * @param entityType Type d'entité
     * @param operation Opération
     * @return BusinessException configurée
     */
    public static BusinessException conflict(String message, EntityType entityType, Operation operation) {
        return new BusinessException(message, "CONFLICT", entityType, operation);
    }
    
    /**
     * Crée une BusinessException pour validation échouée
     * @param message Message de validation
     * @param fieldName Champ en erreur
     * @param fieldValue Valeur en erreur
     * @return BusinessException configurée
     */
    public static BusinessException validation(String message, String fieldName, Object fieldValue) {
        return new BusinessException(message, "VALIDATION_ERROR", null, Operation.VALIDATE)
                .withValidationDetails(fieldName, fieldValue);
    }
    
    /**
     * Crée une BusinessException pour erreur d'autorisation
     * @param message Message d'autorisation
     * @param userId ID utilisateur
     * @param resourceId ID ressource
     * @return BusinessException configurée
     */
    public static BusinessException unauthorized(String message, Long userId, Object resourceId) {
        return new BusinessException(message, "UNAUTHORIZED", null, Operation.CHECK_PERMISSION)
                .withUserId(userId)
                .withResourceId(resourceId);
    }
    
    /**
     * Crée une BusinessException pour état invalide
     * @param message Message d'état
     * @param entityType Type d'entité
     * @param operation Opération tentée
     * @return BusinessException configurée
     */
    public static BusinessException invalidState(String message, EntityType entityType, Operation operation) {
        return new BusinessException(message, "INVALID_STATE", entityType, operation);
    }
    
    /**
     * Crée une BusinessException pour erreur interne
     * @param message Message d'erreur
     * @param userId ID utilisateur
     * @param resourceId ID ressource
     * @param cause Cause de l'erreur
     * @return BusinessException configurée
     */
    public static BusinessException internalError(String message, Long userId, Object resourceId, Throwable cause) {
        return new BusinessException(message, "INTERNAL_ERROR", null, Operation.READ, cause)
                .withUserId(userId)
                .withResourceId(resourceId);
    }
    
    /**
     * Crée une BusinessException pour erreur interne avec entité
     * @param message Message d'erreur
     * @param userId ID utilisateur
     * @param entityType Type d'entité
     * @param cause Cause de l'erreur
     * @return BusinessException configurée
     */
    public static BusinessException internalError(String message, Long userId, EntityType entityType, Throwable cause) {
        return new BusinessException(message, "INTERNAL_ERROR", entityType, Operation.READ, cause)
                .withUserId(userId);
    }
    
    
    /**
     * Crée une BusinessException pour erreur interne avec EntityType
     * @param message Message d'erreur
     * @param userId ID utilisateur
     * @param entityType Type d'entité
     * @return BusinessException configurée
     */
    public static BusinessException internalError(String message, Long userId, EntityType entityType) {
        return new BusinessException(message, "INTERNAL_ERROR", entityType, Operation.READ)
                .withUserId(userId);
    }
    
    /**
     * Crée une BusinessException pour erreur interne avec String
     * @param message Message d'erreur
     * @param userId ID utilisateur
     * @param info Information supplémentaire
     * @return BusinessException configurée
     */
    public static BusinessException internalError(String message, Long userId, String info) {
        return new BusinessException(message, "INTERNAL_ERROR", null, Operation.READ)
                .withUserId(userId)
                .withContext("info", info);
    }
    
    @Override
    public String toString() {
        return String.format("BusinessException{errorCode='%s', entityType=%s, operation=%s, message='%s'}", 
                           errorCode, entityType, operation, getMessage());
    }
}