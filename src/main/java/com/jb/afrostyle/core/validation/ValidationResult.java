package com.jb.afrostyle.core.validation;

import com.jb.afrostyle.core.enums.EntityType;
import com.jb.afrostyle.core.enums.Operation;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Sealed interface centralisée pour représenter le résultat d'une validation
 * Version améliorée avec intégration des enums EntityType et Operation
 * Utilise les Pattern Matching features de Java 21 pour une approche type-safe
 * 
 * @param <T> Le type de la valeur validée
 * @version 2.0 - Centralisée dans /core
 * @since Java 21
 */
public sealed interface ValidationResult<T> 
    permits ValidationResult.Success, ValidationResult.Error {
    
    // ==================== RECORDS SCELLÉS ====================
    
    /**
     * Résultat de validation réussie
     * @param value Valeur validée avec succès
     */
    record Success<T>(T value) implements ValidationResult<T> {}
    
    /**
     * Résultat de validation échouée avec contexte enrichi
     * @param message Message d'erreur
     * @param cause Cause de l'erreur (optionnelle)
     * @param errorCode Code d'erreur spécifique (optionnel)
     * @param entityType Type d'entité concernée (optionnel)
     * @param operation Opération qui a échoué (optionnelle)
     */
    record Error<T>(
        String message, 
        Throwable cause,
        String errorCode,
        EntityType entityType,
        Operation operation
    ) implements ValidationResult<T> {
        
        /**
         * Constructeur avec message uniquement
         */
        public Error(String message) {
            this(message, null, null, null, null);
        }
        
        /**
         * Constructeur avec message et cause
         */
        public Error(String message, Throwable cause) {
            this(message, cause, null, null, null);
        }
        
        /**
         * Constructeur avec message et code d'erreur
         */
        public Error(String message, String errorCode) {
            this(message, null, errorCode, null, null);
        }
        
        /**
         * Constructeur avec contexte métier complet
         */
        public Error(String message, EntityType entityType, Operation operation) {
            this(message, null, null, entityType, operation);
        }
        
        /**
         * Constructeur avec contexte métier et code d'erreur
         */
        public Error(String message, String errorCode, EntityType entityType, Operation operation) {
            this(message, null, errorCode, entityType, operation);
        }
    }
    
    // ==================== MÉTHODES DE VÉRIFICATION ====================
    
    /**
     * Vérifie si la validation est réussie
     * @return true si Success
     */
    default boolean isSuccess() { 
        return this instanceof Success; 
    }
    
    /**
     * Vérifie si la validation a échoué
     * @return true si Error
     */
    default boolean isError() { 
        return this instanceof Error; 
    }
    
    /**
     * Vérifie si l'erreur a un code spécifique
     * @param errorCode Code à vérifier
     * @return true si l'erreur a ce code
     */
    default boolean hasErrorCode(String errorCode) {
        return switch (this) {
            case Error<T> error -> errorCode.equals(error.errorCode());
            case Success<T> success -> false;
        };
    }
    
    /**
     * Vérifie si l'erreur concerne un type d'entité spécifique
     * @param entityType Type d'entité à vérifier
     * @return true si l'erreur concerne ce type
     */
    default boolean isEntityError(EntityType entityType) {
        return switch (this) {
            case Error<T> error -> entityType.equals(error.entityType());
            case Success<T> success -> false;
        };
    }
    
    // ==================== RÉCUPÉRATION DE VALEURS ====================
    
    /**
     * Récupère la valeur si la validation est réussie
     * Lève une exception si la validation a échoué
     * @return Valeur validée
     * @throws IllegalStateException si validation échouée
     */
    default T getValue() {
        return switch (this) {
            case Success<T>(var value) -> value;
            case Error<T>(var message, var ignored1, var ignored2, var ignored3, var ignored4) -> 
                throw new IllegalStateException(message);
        };
    }
    
    /**
     * Récupère la valeur ou une valeur par défaut
     * @param defaultValue Valeur par défaut
     * @return Valeur validée ou valeur par défaut
     */
    default T getValueOrDefault(T defaultValue) {
        return switch (this) {
            case Success<T>(var value) -> value;
            case Error<T> error -> defaultValue;
        };
    }
    
    /**
     * Récupère le message d'erreur si la validation a échoué
     * @return Message d'erreur
     * @throws IllegalStateException si validation réussie
     */
    default String getError() {
        return switch (this) {
            case Error<T>(var message, var ignored1, var ignored2, var ignored3, var ignored4) -> message;
            case Success<T> success -> throw new IllegalStateException("No error in successful validation");
        };
    }
    
    /**
     * Récupère la cause de l'erreur si disponible
     * @return Cause de l'erreur ou null
     */
    default Throwable getCause() {
        return switch (this) {
            case Error<T>(var _, var cause, var _, var _, var _) -> cause;
            case Success<T> success -> null;
        };
    }
    
    /**
     * Récupère le code d'erreur si disponible
     * @return Code d'erreur ou null
     */
    default String getErrorCode() {
        return switch (this) {
            case Error<T>(var _, var _, var errorCode, var _, var _) -> errorCode;
            case Success<T> success -> null;
        };
    }
    
    /**
     * Récupère le type d'entité concernée si disponible
     * @return Type d'entité ou null
     */
    default EntityType getEntityType() {
        return switch (this) {
            case Error<T>(var _, var _, var _, var entityType, var _) -> entityType;
            case Success<T> success -> null;
        };
    }
    
    /**
     * Récupère l'opération concernée si disponible
     * @return Opération ou null
     */
    default Operation getOperation() {
        return switch (this) {
            case Error<T>(var _, var _, var _, var _, var operation) -> operation;
            case Success<T> success -> null;
        };
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Crée un résultat de validation réussie
     * @param value Valeur validée
     * @return ValidationResult de succès
     */
    static <T> ValidationResult<T> success(T value) {
        return new Success<>(value);
    }
    
    /**
     * Crée un résultat de validation échouée avec message
     * @param message Message d'erreur
     * @return ValidationResult d'erreur
     */
    static <T> ValidationResult<T> error(String message) {
        return new Error<>(message);
    }
    
    /**
     * Crée un résultat de validation échouée avec message et cause
     * @param message Message d'erreur
     * @param cause Cause de l'erreur
     * @return ValidationResult d'erreur
     */
    static <T> ValidationResult<T> error(String message, Throwable cause) {
        return new Error<>(message, cause);
    }
    
    /**
     * Crée un résultat d'erreur avec code d'erreur
     * @param message Message d'erreur
     * @param errorCode Code d'erreur
     * @return ValidationResult d'erreur avec code
     */
    static <T> ValidationResult<T> error(String message, String errorCode) {
        return new Error<>(message, errorCode);
    }
    
    /**
     * Crée un résultat d'erreur avec contexte métier
     * @param message Message d'erreur
     * @param entityType Type d'entité concernée
     * @param operation Opération concernée
     * @return ValidationResult d'erreur avec contexte
     */
    static <T> ValidationResult<T> error(String message, EntityType entityType, Operation operation) {
        return new Error<>(message, entityType, operation);
    }
    
    /**
     * Crée un résultat d'erreur avec contexte complet
     * @param message Message d'erreur
     * @param errorCode Code d'erreur
     * @param entityType Type d'entité
     * @param operation Opération
     * @return ValidationResult d'erreur complet
     */
    static <T> ValidationResult<T> error(String message, String errorCode, EntityType entityType, Operation operation) {
        return new Error<>(message, null, errorCode, entityType, operation);
    }
    
    /**
     * Crée un résultat d'erreur avec contexte complet incluant la cause
     * @param message Message d'erreur
     * @param cause Cause de l'erreur
     * @param errorCode Code d'erreur
     * @param entityType Type d'entité
     * @param operation Opération
     * @return ValidationResult d'erreur complet
     */
    static <T> ValidationResult<T> error(String message, Throwable cause, String errorCode, EntityType entityType, Operation operation) {
        return new Error<>(message, cause, errorCode, entityType, operation);
    }
    
    // ==================== MÉTHODES FONCTIONNELLES ====================
    
    /**
     * Applique une transformation si la validation est réussie
     * @param mapper Fonction de transformation
     * @return Nouveau ValidationResult transformé
     */
    default <U> ValidationResult<U> map(Function<T, U> mapper) {
        return switch (this) {
            case Success<T>(var value) -> success(mapper.apply(value));
            case Error<T>(var message, var cause, var errorCode, var entityType, var operation) -> 
                new Error<>(message, cause, errorCode, entityType, operation);
        };
    }
    
    /**
     * Chaîne une autre validation si celle-ci est réussie
     * @param mapper Fonction qui retourne un ValidationResult
     * @return Résultat de la validation chaînée
     */
    default <U> ValidationResult<U> flatMap(Function<T, ValidationResult<U>> mapper) {
        return switch (this) {
            case Success<T>(var value) -> mapper.apply(value);
            case Error<T>(var message, var cause, var errorCode, var entityType, var operation) -> 
                new Error<>(message, cause, errorCode, entityType, operation);
        };
    }
    
    /**
     * Filtre le résultat selon un prédicat
     * @param predicate Prédicat à appliquer
     * @param errorMessage Message d'erreur si le prédicat échoue
     * @return ValidationResult filtré
     */
    default ValidationResult<T> filter(Predicate<T> predicate, String errorMessage) {
        return switch (this) {
            case Success<T>(var value) -> 
                predicate.test(value) ? this : error(errorMessage);
            case Error<T> error -> error;
        };
    }
    
    /**
     * Combine deux ValidationResult (tous deux doivent être des succès)
     * @param other Autre ValidationResult
     * @param combiner Fonction de combinaison
     * @return ValidationResult combiné
     */
    default <U, R> ValidationResult<R> combine(
            ValidationResult<U> other, 
            java.util.function.BiFunction<T, U, R> combiner) {
        return switch (this) {
            case Success<T>(var value1) -> switch (other) {
                case Success<U>(var value2) -> success(combiner.apply(value1, value2));
                case Error<U> error -> new Error<>(error.message(), error.cause(), 
                    error.errorCode(), error.entityType(), error.operation());
            };
            case Error<T> error -> new Error<>(error.message(), error.cause(), 
                error.errorCode(), error.entityType(), error.operation());
        };
    }
    
    /**
     * Exécute une action si la validation est réussie
     * @param action Action à exécuter
     * @return Ce ValidationResult pour chaînage
     */
    default ValidationResult<T> ifSuccess(java.util.function.Consumer<T> action) {
        if (this instanceof Success<T>(var value)) {
            action.accept(value);
        }
        return this;
    }
    
    /**
     * Exécute une action si la validation a échoué
     * @param action Action à exécuter
     * @return Ce ValidationResult pour chaînage
     */
    default ValidationResult<T> ifError(java.util.function.Consumer<String> action) {
        if (this instanceof Error<T>(var message, var _, var _, var _, var _)) {
            action.accept(message);
        }
        return this;
    }
    
    // ==================== UTILITAIRES STATIQUES ====================
    
    /**
     * Combine plusieurs ValidationResult en un seul
     * Retourne une erreur si au moins un ValidationResult est une erreur
     * @param results Liste des ValidationResult à combiner
     * @return ValidationResult combiné
     */
    @SafeVarargs
    static <T> ValidationResult<java.util.List<T>> combine(ValidationResult<T>... results) {
        java.util.List<T> values = new java.util.ArrayList<>();
        
        for (ValidationResult<T> result : results) {
            switch (result) {
                case Success<T>(var value) -> values.add(value);
                case Error<T> error -> {
                    return new Error<>(
                        "Combined validation failed: " + error.message(),
                        error.cause(), error.errorCode(), error.entityType(), error.operation()
                    );
                }
            }
        }
        
        return success(values);
    }
    
    /**
     * Valide une condition et retourne Success ou Error
     * @param condition Condition à vérifier
     * @param value Valeur si condition vraie
     * @param errorMessage Message si condition fausse
     * @return ValidationResult selon la condition
     */
    static <T> ValidationResult<T> validate(boolean condition, T value, String errorMessage) {
        return condition ? success(value) : error(errorMessage);
    }
    
    /**
     * Valide une condition avec contexte métier
     * @param condition Condition à vérifier
     * @param value Valeur si condition vraie
     * @param errorMessage Message si condition fausse
     * @param entityType Type d'entité
     * @param operation Opération
     * @return ValidationResult avec contexte
     */
    static <T> ValidationResult<T> validate(
            boolean condition, 
            T value, 
            String errorMessage,
            EntityType entityType,
            Operation operation) {
        return condition ? success(value) : error(errorMessage, entityType, operation);
    }
    
    @Override
    String toString();
}