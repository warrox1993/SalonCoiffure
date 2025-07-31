package com.jb.afrostyle.core.exception;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Exception de validation centralisée pour AfroStyle
 * Encapsule toutes les erreurs de validation avec contexte détaillé
 * Support des validations simples et multiples
 * 
 * @version 1.0
 * @since Java 21
 */
public class ValidationException extends RuntimeException {
    
    private final String errorCode;
    private final String fieldName;
    private final Object fieldValue;
    private final Map<String, String> fieldErrors;
    private final LocalDateTime timestamp;
    
    // ==================== CONSTRUCTEURS ====================
    
    /**
     * Constructeur de base avec message
     * @param message Message d'erreur
     */
    public ValidationException(String message) {
        super(message);
        this.errorCode = "VALIDATION_ERROR";
        this.fieldName = null;
        this.fieldValue = null;
        this.fieldErrors = new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructeur avec champ spécifique
     * @param message Message d'erreur
     * @param errorCode Code d'erreur
     * @param fieldName Nom du champ en erreur
     * @param fieldValue Valeur du champ en erreur
     */
    public ValidationException(String message, String errorCode, String fieldName, Object fieldValue) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : "VALIDATION_ERROR";
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.fieldErrors = new HashMap<>();
        this.timestamp = LocalDateTime.now();
        
        // Ajouter l'erreur du champ principal
        if (fieldName != null) {
            this.fieldErrors.put(fieldName, message);
        }
    }
    
    /**
     * Constructeur avec cause
     * @param message Message d'erreur
     * @param errorCode Code d'erreur
     * @param fieldName Nom du champ
     * @param fieldValue Valeur du champ
     * @param cause Cause originale
     */
    public ValidationException(String message, String errorCode, String fieldName, Object fieldValue, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : "VALIDATION_ERROR";
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.fieldErrors = new HashMap<>();
        this.timestamp = LocalDateTime.now();
        
        if (fieldName != null) {
            this.fieldErrors.put(fieldName, message);
        }
    }
    
    /**
     * Constructeur pour erreurs multiples
     * @param message Message d'erreur général
     * @param fieldErrors Map des erreurs par champ
     */
    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.errorCode = "VALIDATION_ERROR";
        this.fieldName = null;
        this.fieldValue = null;
        this.fieldErrors = new HashMap<>(fieldErrors);
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructeur pour erreurs multiples avec code
     * @param message Message d'erreur général
     * @param errorCode Code d'erreur
     * @param fieldErrors Map des erreurs par champ
     */
    public ValidationException(String message, String errorCode, Map<String, String> fieldErrors) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : "VALIDATION_ERROR";
        this.fieldName = null;
        this.fieldValue = null;
        this.fieldErrors = new HashMap<>(fieldErrors);
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
     * Obtient le nom du champ principal en erreur
     * @return Nom du champ ou null
     */
    public String getFieldName() {
        return fieldName;
    }
    
    /**
     * Obtient la valeur du champ principal en erreur
     * @return Valeur du champ ou null
     */
    public Object getFieldValue() {
        return fieldValue;
    }
    
    /**
     * Obtient toutes les erreurs de champs
     * @return Map des erreurs par champ
     */
    public Map<String, String> getFieldErrors() {
        return new HashMap<>(fieldErrors);
    }
    
    /**
     * Obtient le timestamp de l'erreur
     * @return Timestamp de création
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    // ==================== MÉTHODES DE GESTION DES ERREURS ====================
    
    /**
     * Ajoute une erreur de champ
     * @param fieldName Nom du champ
     * @param errorMessage Message d'erreur
     * @return Cette exception pour chaînage
     */
    public ValidationException addFieldError(String fieldName, String errorMessage) {
        this.fieldErrors.put(fieldName, errorMessage);
        return this;
    }
    
    /**
     * Ajoute plusieurs erreurs de champs
     * @param errors Map des erreurs à ajouter
     * @return Cette exception pour chaînage
     */
    public ValidationException addFieldErrors(Map<String, String> errors) {
        this.fieldErrors.putAll(errors);
        return this;
    }
    
    /**
     * Supprime une erreur de champ
     * @param fieldName Nom du champ
     * @return Cette exception pour chaînage
     */
    public ValidationException removeFieldError(String fieldName) {
        this.fieldErrors.remove(fieldName);
        return this;
    }
    
    /**
     * Vide toutes les erreurs de champs
     * @return Cette exception pour chaînage
     */
    public ValidationException clearFieldErrors() {
        this.fieldErrors.clear();
        return this;
    }
    
    // ==================== MÉTHODES DE VÉRIFICATION ====================
    
    /**
     * Vérifie si un champ spécifique a une erreur
     * @param fieldName Nom du champ
     * @return true si le champ a une erreur
     */
    public boolean hasFieldError(String fieldName) {
        return fieldErrors.containsKey(fieldName);
    }
    
    /**
     * Vérifie si l'exception a des erreurs de champs
     * @return true si des erreurs de champs existent
     */
    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
    
    /**
     * Obtient l'erreur d'un champ spécifique
     * @param fieldName Nom du champ
     * @return Message d'erreur ou null
     */
    public String getFieldError(String fieldName) {
        return fieldErrors.get(fieldName);
    }
    
    /**
     * Obtient le nombre d'erreurs de champs
     * @return Nombre d'erreurs
     */
    public int getFieldErrorCount() {
        return fieldErrors.size();
    }
    
    /**
     * Obtient la liste des champs en erreur
     * @return Liste des noms de champs
     */
    public List<String> getErrorFields() {
        return new ArrayList<>(fieldErrors.keySet());
    }
    
    // ==================== MÉTHODES DE FORMATAGE ====================
    
    /**
     * Génère un message d'erreur détaillé avec tous les champs
     * @return Message complet
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder(getMessage());
        
        if (!fieldErrors.isEmpty()) {
            sb.append("\nField errors:");
            fieldErrors.forEach((field, error) -> 
                sb.append("\n  - ").append(field).append(": ").append(error)
            );
        }
        
        sb.append("\n[Code: ").append(errorCode).append("]");
        sb.append(" [Time: ").append(timestamp).append("]");
        
        return sb.toString();
    }
    
    /**
     * Génère une liste des erreurs pour l'utilisateur
     * @return Liste formatée des erreurs
     */
    public List<String> getUserFriendlyErrors() {
        List<String> errors = new ArrayList<>();
        
        if (fieldName != null && !fieldErrors.containsKey(fieldName)) {
            errors.add(getMessage());
        }
        
        fieldErrors.values().forEach(errors::add);
        
        return errors;
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
        
        if (fieldName != null) {
            map.put("fieldName", fieldName);
        }
        
        if (fieldValue != null) {
            map.put("fieldValue", fieldValue);
        }
        
        if (!fieldErrors.isEmpty()) {
            map.put("fieldErrors", fieldErrors);
        }
        
        if (getCause() != null) {
            map.put("cause", getCause().getMessage());
        }
        
        return map;
    }
    
    /**
     * Convertit l'exception en format adapté aux API REST
     * @return Map pour réponse API
     */
    public Map<String, Object> toApiResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Validation failed");
        response.put("code", errorCode);
        response.put("timestamp", timestamp);
        
        if (hasFieldErrors()) {
            response.put("fields", fieldErrors);
        } else {
            response.put("message", getMessage());
        }
        
        return response;
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Crée une ValidationException pour champ requis
     * @param fieldName Nom du champ
     * @return ValidationException configurée
     */
    public static ValidationException required(String fieldName) {
        return new ValidationException(
            fieldName + " is required",
            "FIELD_REQUIRED",
            fieldName,
            null
        );
    }
    
    /**
     * Crée une ValidationException pour format invalide
     * @param fieldName Nom du champ
     * @param value Valeur invalide
     * @return ValidationException configurée
     */
    public static ValidationException invalidFormat(String fieldName, Object value) {
        return new ValidationException(
            fieldName + " has invalid format",
            "INVALID_FORMAT",
            fieldName,
            value
        );
    }
    
    /**
     * Crée une ValidationException pour valeur hors limites
     * @param fieldName Nom du champ
     * @param value Valeur
     * @param min Valeur minimum
     * @param max Valeur maximum
     * @return ValidationException configurée
     */
    public static ValidationException outOfRange(String fieldName, Object value, Object min, Object max) {
        return new ValidationException(
            String.format("%s must be between %s and %s, got %s", fieldName, min, max, value),
            "OUT_OF_RANGE",
            fieldName,
            value
        );
    }
    
    /**
     * Crée une ValidationException pour longueur invalide
     * @param fieldName Nom du champ
     * @param value Valeur
     * @param minLength Longueur minimum
     * @param maxLength Longueur maximum
     * @return ValidationException configurée
     */
    public static ValidationException invalidLength(String fieldName, String value, int minLength, int maxLength) {
        int actualLength = value != null ? value.length() : 0;
        return new ValidationException(
            String.format("%s length must be between %d and %d characters, got %d", 
                         fieldName, minLength, maxLength, actualLength),
            "INVALID_LENGTH",
            fieldName,
            value
        );
    }
    
    /**
     * Crée une ValidationException pour valeur dupliquée
     * @param fieldName Nom du champ
     * @param value Valeur dupliquée
     * @return ValidationException configurée
     */
    public static ValidationException duplicate(String fieldName, Object value) {
        return new ValidationException(
            fieldName + " already exists: " + value,
            "DUPLICATE_VALUE",
            fieldName,
            value
        );
    }
    
    /**
     * Crée une ValidationException pour contrainte métier
     * @param message Message de contrainte
     * @param fieldName Nom du champ
     * @param value Valeur
     * @return ValidationException configurée
     */
    public static ValidationException constraint(String message, String fieldName, Object value) {
        return new ValidationException(
            message,
            "CONSTRAINT_VIOLATION",
            fieldName,
            value
        );
    }
    
    /**
     * Combine plusieurs ValidationException en une seule
     * @param exceptions Liste d'exceptions à combiner
     * @return ValidationException combinée
     */
    public static ValidationException combine(List<ValidationException> exceptions) {
        if (exceptions.isEmpty()) {
            return new ValidationException("No validation errors");
        }
        
        if (exceptions.size() == 1) {
            return exceptions.get(0);
        }
        
        Map<String, String> allErrors = new HashMap<>();
        StringBuilder messageBuilder = new StringBuilder("Multiple validation errors:");
        
        for (ValidationException ex : exceptions) {
            allErrors.putAll(ex.getFieldErrors());
            if (ex.getFieldName() != null && !allErrors.containsKey(ex.getFieldName())) {
                allErrors.put(ex.getFieldName(), ex.getMessage());
            }
            messageBuilder.append("\n- ").append(ex.getMessage());
        }
        
        return new ValidationException(
            messageBuilder.toString(),
            "MULTIPLE_VALIDATION_ERRORS",
            allErrors
        );
    }
    
    @Override
    public String toString() {
        return String.format("ValidationException{errorCode='%s', fieldName='%s', fieldErrors=%d, message='%s'}", 
                           errorCode, fieldName, fieldErrors.size(), getMessage());
    }
}