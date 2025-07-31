package com.jb.afrostyle.core.validation;

import com.jb.afrostyle.core.enums.EntityType;
import com.jb.afrostyle.core.enums.Operation;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.List;
import java.util.ArrayList;

/**
 * Registre centralisé pour tous les validateurs du système AfroStyle
 * Permet l'enregistrement dynamique et la gestion unifiée des validateurs
 * Supporte les validateurs chaînés et la validation par contexte
 * 
 * @version 1.0
 * @since Java 21
 */
@Component
public class ValidatorRegistry {
    
    // Maps pour stocker les différents types de validateurs
    private final Map<String, Function<Object, ValidationResult<?>>> fieldValidators = new ConcurrentHashMap<>();
    private final Map<EntityType, Function<Object, ValidationResult<?>>> entityValidators = new ConcurrentHashMap<>();
    private final Map<Operation, Function<Object, ValidationResult<?>>> operationValidators = new ConcurrentHashMap<>();
    private final Map<String, List<Function<Object, ValidationResult<?>>>> validatorChains = new ConcurrentHashMap<>();
    
    public ValidatorRegistry() {
        registerDefaultValidators();
    }
    
    // ==================== ENREGISTREMENT DE VALIDATEURS ====================
    
    /**
     * Enregistre un validateur pour un champ spécifique
     * @param fieldName Nom du champ
     * @param validator Fonction de validation
     */
    public void registerFieldValidator(String fieldName, Function<Object, ValidationResult<?>> validator) {
        fieldValidators.put(fieldName.toLowerCase(), validator);
    }
    
    /**
     * Enregistre un validateur pour un type d'entité
     * @param entityType Type d'entité
     * @param validator Fonction de validation
     */
    public void registerEntityValidator(EntityType entityType, Function<Object, ValidationResult<?>> validator) {
        entityValidators.put(entityType, validator);
    }
    
    /**
     * Enregistre un validateur pour une opération
     * @param operation Type d'opération
     * @param validator Fonction de validation
     */
    public void registerOperationValidator(Operation operation, Function<Object, ValidationResult<?>> validator) {
        operationValidators.put(operation, validator);
    }
    
    /**
     * Enregistre une chaîne de validateurs pour un contexte
     * @param context Contexte de validation
     * @param validators Liste des validateurs
     */
    public void registerValidatorChain(String context, List<Function<Object, ValidationResult<?>>> validators) {
        validatorChains.put(context.toLowerCase(), new ArrayList<>(validators));
    }
    
    /**
     * Ajoute un validateur à une chaîne existante
     * @param context Contexte de validation
     * @param validator Validateur à ajouter
     */
    public void addToValidatorChain(String context, Function<Object, ValidationResult<?>> validator) {
        validatorChains.computeIfAbsent(context.toLowerCase(), k -> new ArrayList<>()).add(validator);
    }
    
    // ==================== VALIDATION ====================
    
    /**
     * Valide un champ avec son validateur enregistré
     * @param fieldName Nom du champ
     * @param value Valeur à valider
     * @return Résultat de validation
     */
    @SuppressWarnings("unchecked")
    public <T> ValidationResult<T> validateField(String fieldName, Object value) {
        Function<Object, ValidationResult<?>> validator = fieldValidators.get(fieldName.toLowerCase());
        if (validator == null) {
            return ValidationResult.error("No validator found for field: " + fieldName);
        }
        
        try {
            return (ValidationResult<T>) validator.apply(value);
        } catch (Exception e) {
            return ValidationResult.error(
                "Validation failed for field " + fieldName + ": " + e.getMessage(),
                e
            );
        }
    }
    
    /**
     * Valide une entité avec son validateur enregistré
     * @param entityType Type d'entité
     * @param entity Entité à valider
     * @return Résultat de validation
     */
    @SuppressWarnings("unchecked")
    public <T> ValidationResult<T> validateEntity(EntityType entityType, Object entity) {
        Function<Object, ValidationResult<?>> validator = entityValidators.get(entityType);
        if (validator == null) {
            return ValidationResult.error("No validator found for entity: " + entityType.getClassName());
        }
        
        try {
            return (ValidationResult<T>) validator.apply(entity);
        } catch (Exception e) {
            return ValidationResult.error(
                "Validation failed for entity " + entityType.getClassName() + ": " + e.getMessage(),
                e,
                "ENTITY_VALIDATION_ERROR",
                entityType,
                Operation.VALIDATE
            );
        }
    }
    
    /**
     * Valide dans le contexte d'une opération
     * @param operation Type d'opération
     * @param data Données à valider
     * @return Résultat de validation
     */
    @SuppressWarnings("unchecked")
    public <T> ValidationResult<T> validateOperation(Operation operation, Object data) {
        Function<Object, ValidationResult<?>> validator = operationValidators.get(operation);
        if (validator == null) {
            return ValidationResult.error("No validator found for operation: " + operation.getName());
        }
        
        try {
            return (ValidationResult<T>) validator.apply(data);
        } catch (Exception e) {
            return ValidationResult.error(
                "Validation failed for operation " + operation.getName() + ": " + e.getMessage(),
                e,
                "OPERATION_VALIDATION_ERROR",
                null,
                operation
            );
        }
    }
    
    /**
     * Exécute une chaîne de validateurs
     * @param context Contexte de validation
     * @param value Valeur à valider
     * @return Résultat de validation (succès si tous passent)
     */
    @SuppressWarnings("unchecked")
    public <T> ValidationResult<T> validateChain(String context, Object value) {
        List<Function<Object, ValidationResult<?>>> validators = validatorChains.get(context.toLowerCase());
        if (validators == null || validators.isEmpty()) {
            return ValidationResult.error("No validator chain found for context: " + context);
        }
        
        Object currentValue = value;
        
        for (Function<Object, ValidationResult<?>> validator : validators) {
            try {
                ValidationResult<?> result = validator.apply(currentValue);
                switch (result) {
                    case ValidationResult.Error<?> error -> {
                        return (ValidationResult<T>) error;
                    }
                    case ValidationResult.Success<?> success -> {
                        currentValue = success.value();
                    }
                }
            } catch (Exception e) {
                return ValidationResult.error(
                    "Validator chain failed at context " + context + ": " + e.getMessage(),
                    e
                );
            }
        }
        
        return ValidationResult.success((T) currentValue);
    }
    
    // ==================== VALIDATION COMPOSITE ====================
    
    /**
     * Valide un objet avec tous les validateurs applicables
     * @param value Valeur à valider
     * @param entityType Type d'entité (optionnel)
     * @param operation Opération (optionnelle)
     * @param fieldName Nom du champ (optionnel)
     * @return Résultat de validation composite
     */
    public <T> ValidationResult<T> validateAll(
            Object value, 
            EntityType entityType, 
            Operation operation, 
            String fieldName) {
        
        // Validation par champ si spécifié
        if (fieldName != null) {
            ValidationResult<T> fieldResult = validateField(fieldName, value);
            if (fieldResult.isError()) {
                return fieldResult;
            }
            value = fieldResult.getValue(); // Utiliser la valeur transformée
        }
        
        // Validation par entité si spécifiée
        if (entityType != null) {
            ValidationResult<T> entityResult = validateEntity(entityType, value);
            if (entityResult.isError()) {
                return entityResult;
            }
            value = entityResult.getValue();
        }
        
        // Validation par opération si spécifiée
        if (operation != null) {
            ValidationResult<T> operationResult = validateOperation(operation, value);
            if (operationResult.isError()) {
                return operationResult;
            }
            value = operationResult.getValue();
        }
        
        return ValidationResult.success((T) value);
    }
    
    // ==================== GESTION DES VALIDATEURS ====================
    
    /**
     * Supprime un validateur de champ
     * @param fieldName Nom du champ
     * @return true si supprimé
     */
    public boolean removeFieldValidator(String fieldName) {
        return fieldValidators.remove(fieldName.toLowerCase()) != null;
    }
    
    /**
     * Supprime un validateur d'entité
     * @param entityType Type d'entité
     * @return true si supprimé
     */
    public boolean removeEntityValidator(EntityType entityType) {
        return entityValidators.remove(entityType) != null;
    }
    
    /**
     * Supprime un validateur d'opération
     * @param operation Type d'opération
     * @return true si supprimé
     */
    public boolean removeOperationValidator(Operation operation) {
        return operationValidators.remove(operation) != null;
    }
    
    /**
     * Vide une chaîne de validateurs
     * @param context Contexte de validation
     * @return true si vidée
     */
    public boolean clearValidatorChain(String context) {
        List<Function<Object, ValidationResult<?>>> chain = validatorChains.get(context.toLowerCase());
        if (chain != null) {
            chain.clear();
            return true;
        }
        return false;
    }
    
    // ==================== INTROSPECTION ====================
    
    /**
     * Vérifie si un validateur de champ existe
     * @param fieldName Nom du champ
     * @return true si existe
     */
    public boolean hasFieldValidator(String fieldName) {
        return fieldValidators.containsKey(fieldName.toLowerCase());
    }
    
    /**
     * Vérifie si un validateur d'entité existe
     * @param entityType Type d'entité
     * @return true si existe
     */
    public boolean hasEntityValidator(EntityType entityType) {
        return entityValidators.containsKey(entityType);
    }
    
    /**
     * Vérifie si un validateur d'opération existe
     * @param operation Type d'opération
     * @return true si existe
     */
    public boolean hasOperationValidator(Operation operation) {
        return operationValidators.containsKey(operation);
    }
    
    /**
     * Obtient le nombre de validateurs enregistrés par type
     * @return Map avec les statistiques
     */
    public Map<String, Integer> getValidatorStatistics() {
        return Map.of(
            "fieldValidators", fieldValidators.size(),
            "entityValidators", entityValidators.size(),
            "operationValidators", operationValidators.size(),
            "validatorChains", validatorChains.size()
        );
    }
    
    // ==================== VALIDATEURS PAR DÉFAUT ====================
    
    /**
     * Enregistre les validateurs par défaut du système
     */
    private void registerDefaultValidators() {
        // Validateurs de champs communs
        registerFieldValidator("email", value -> 
            ValidationUtils.validateEmail(String.valueOf(value)));
        registerFieldValidator("phone", value -> 
            ValidationUtils.validatePhoneNumber(String.valueOf(value)));
        registerFieldValidator("password", value -> 
            ValidationUtils.validatePassword(String.valueOf(value)));
        registerFieldValidator("username", value -> 
            ValidationUtils.validateUsername(String.valueOf(value)));
        
        // Validateurs d'entités
        registerEntityValidator(EntityType.USER, this::validateUserEntity);
        registerEntityValidator(EntityType.BOOKING, this::validateBookingEntity);
        registerEntityValidator(EntityType.PAYMENT, this::validatePaymentEntity);
        
        // Validateurs d'opérations
        registerOperationValidator(Operation.CREATE, this::validateCreateOperation);
        registerOperationValidator(Operation.UPDATE, this::validateUpdateOperation);
        registerOperationValidator(Operation.DELETE, this::validateDeleteOperation);
        
        // Chaînes de validation communes
        registerValidatorChain("user-registration", List.of(
            value -> ValidationUtils.validateNotNullOrEmpty(String.valueOf(value), "User data"),
            this::validateUserEntity
        ));
    }
    
    // ==================== VALIDATEURS PRIVÉS ====================
    
    @SuppressWarnings("unchecked")
    private ValidationResult<Object> validateUserEntity(Object user) {
        // Validation générique pour entité utilisateur
        if (user == null) {
            return ValidationResult.error("User entity cannot be null");
        }
        // Ici on pourrait ajouter d'autres validations spécifiques aux utilisateurs
        return ValidationResult.success(user);
    }
    
    @SuppressWarnings("unchecked")
    private ValidationResult<Object> validateBookingEntity(Object booking) {
        // Validation générique pour entité réservation
        if (booking == null) {
            return ValidationResult.error("Booking entity cannot be null");
        }
        // Ici on pourrait ajouter d'autres validations spécifiques aux réservations
        return ValidationResult.success(booking);
    }
    
    @SuppressWarnings("unchecked")
    private ValidationResult<Object> validatePaymentEntity(Object payment) {
        // Validation générique pour entité paiement
        if (payment == null) {
            return ValidationResult.error("Payment entity cannot be null");
        }
        // Ici on pourrait ajouter d'autres validations spécifiques aux paiements
        return ValidationResult.success(payment);
    }
    
    @SuppressWarnings("unchecked")
    private ValidationResult<Object> validateCreateOperation(Object data) {
        // Validation générique pour opération de création
        if (data == null) {
            return ValidationResult.error("Data for create operation cannot be null");
        }
        return ValidationResult.success(data);
    }
    
    @SuppressWarnings("unchecked")
    private ValidationResult<Object> validateUpdateOperation(Object data) {
        // Validation générique pour opération de mise à jour
        if (data == null) {
            return ValidationResult.error("Data for update operation cannot be null");
        }
        return ValidationResult.success(data);
    }
    
    @SuppressWarnings("unchecked")
    private ValidationResult<Object> validateDeleteOperation(Object data) {
        // Validation générique pour opération de suppression
        if (data == null) {
            return ValidationResult.error("Data for delete operation cannot be null");
        }
        return ValidationResult.success(data);
    }
}