package com.jb.afrostyle.core.exception;

import com.jb.afrostyle.core.enums.EntityType;
import com.jb.afrostyle.core.enums.Operation;

/**
 * Exception d'accès non autorisé centralisée pour AfroStyle
 * Remplace toutes les exceptions d'autorisation spécifiques aux modules
 * Intègre avec le système BusinessException pour cohérence
 * 
 * @version 1.0
 * @since Java 21
 */
public class UnauthorizedAccessException extends BusinessException {
    
    private final Long userId;
    private final Object resourceId;
    private final EntityType entityType;
    private final Operation operation;
    
    // ==================== CONSTRUCTEURS ====================
    
    /**
     * Constructeur complet pour accès non autorisé
     * @param message Message d'erreur
     * @param userId ID de l'utilisateur tentant l'accès
     * @param resourceId ID de la ressource
     * @param entityType Type d'entité
     * @param operation Opération tentée
     */
    public UnauthorizedAccessException(String message, Long userId, Object resourceId, 
                                     EntityType entityType, Operation operation) {
        super(message, "UNAUTHORIZED_ACCESS", entityType, operation);
        this.userId = userId;
        this.resourceId = resourceId;
        this.entityType = entityType;
        this.operation = operation;
        
        // Ajouter contexte à l'exception
        this.withContext("userId", userId)
            .withContext("resourceId", resourceId)
            .withContext("entityType", entityType)
            .withContext("operation", operation);
    }
    
    /**
     * Constructeur avec cause
     * @param message Message d'erreur
     * @param userId ID utilisateur
     * @param resourceId ID ressource
     * @param entityType Type d'entité
     * @param operation Opération
     * @param cause Cause de l'exception
     */
    public UnauthorizedAccessException(String message, Long userId, Object resourceId, 
                                     EntityType entityType, Operation operation, Throwable cause) {
        super(message, "UNAUTHORIZED_ACCESS", entityType, operation, cause);
        this.userId = userId;
        this.resourceId = resourceId;
        this.entityType = entityType;
        this.operation = operation;
        
        this.withContext("userId", userId)
            .withContext("resourceId", resourceId)
            .withContext("entityType", entityType)
            .withContext("operation", operation);
    }
    
    /**
     * Constructeur simplifié
     * @param message Message d'erreur
     * @param userId ID utilisateur
     * @param resourceId ID ressource
     */
    public UnauthorizedAccessException(String message, Long userId, Object resourceId) {
        this(message, userId, resourceId, EntityType.USER, Operation.READ);
    }
    
    /**
     * Constructeur minimal
     * @param message Message d'erreur
     */
    public UnauthorizedAccessException(String message) {
        this(message, null, null, EntityType.USER, Operation.READ);
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * Crée une exception pour accès refusé à un salon
     * @param salonId ID du salon
     * @param userId ID de l'utilisateur
     * @return UnauthorizedAccessException pour salon
     */
    public static UnauthorizedAccessException salonAccess(Long salonId, Long userId) {
        return new UnauthorizedAccessException(
            String.format("User %d is not authorized to access salon %d", userId, salonId),
            userId,
            salonId,
            EntityType.SALON,
            Operation.READ
        );
    }
    
    /**
     * Crée une exception pour modification refusée d'un salon
     * @param salonId ID du salon
     * @param userId ID de l'utilisateur
     * @return UnauthorizedAccessException pour modification salon
     */
    public static UnauthorizedAccessException salonModification(Long salonId, Long userId) {
        return new UnauthorizedAccessException(
            String.format("User %d is not authorized to modify salon %d", userId, salonId),
            userId,
            salonId,
            EntityType.SALON,
            Operation.UPDATE
        );
    }
    
    /**
     * Crée une exception pour accès refusé à une catégorie
     * @param categoryId ID de la catégorie
     * @param userId ID de l'utilisateur
     * @return UnauthorizedAccessException pour catégorie
     */
    public static UnauthorizedAccessException categoryAccess(Long categoryId, Long userId) {
        return new UnauthorizedAccessException(
            String.format("User %d is not authorized to access category %d", userId, categoryId),
            userId,
            categoryId,
            EntityType.CATEGORY,
            Operation.READ
        );
    }
    
    /**
     * Crée une exception pour modification refusée d'une catégorie
     * @param categoryId ID de la catégorie
     * @param userId ID de l'utilisateur
     * @return UnauthorizedAccessException pour modification catégorie
     */
    public static UnauthorizedAccessException categoryModification(Long categoryId, Long userId) {
        return new UnauthorizedAccessException(
            String.format("User %d is not authorized to modify category %d", userId, categoryId),
            userId,
            categoryId,
            EntityType.CATEGORY,
            Operation.UPDATE
        );
    }
    
    /**
     * Crée une exception pour accès refusé à une réservation
     * @param bookingId ID de la réservation
     * @param userId ID de l'utilisateur
     * @return UnauthorizedAccessException pour réservation
     */
    public static UnauthorizedAccessException bookingAccess(Long bookingId, Long userId) {
        return new UnauthorizedAccessException(
            String.format("User %d is not authorized to access booking %d", userId, bookingId),
            userId,
            bookingId,
            EntityType.BOOKING,
            Operation.READ
        );
    }
    
    /**
     * Crée une exception pour modification refusée d'une réservation
     * @param bookingId ID de la réservation
     * @param userId ID de l'utilisateur
     * @return UnauthorizedAccessException pour modification réservation
     */
    public static UnauthorizedAccessException bookingModification(Long bookingId, Long userId) {
        return new UnauthorizedAccessException(
            String.format("User %d is not authorized to modify booking %d", userId, bookingId),
            userId,
            bookingId,
            EntityType.BOOKING,
            Operation.UPDATE
        );
    }
    
    /**
     * Crée une exception pour accès refusé à un paiement
     * @param paymentId ID du paiement
     * @param userId ID de l'utilisateur
     * @return UnauthorizedAccessException pour paiement
     */
    public static UnauthorizedAccessException paymentAccess(Long paymentId, Long userId) {
        return new UnauthorizedAccessException(
            String.format("User %d is not authorized to access payment %d", userId, paymentId),
            userId,
            paymentId,
            EntityType.PAYMENT,
            Operation.READ
        );
    }
    
    /**
     * Crée une exception pour accès refusé à un utilisateur
     * @param targetUserId ID de l'utilisateur cible
     * @param currentUserId ID de l'utilisateur courant
     * @return UnauthorizedAccessException pour utilisateur
     */
    public static UnauthorizedAccessException userAccess(Long targetUserId, Long currentUserId) {
        return new UnauthorizedAccessException(
            String.format("User %d is not authorized to access user %d", currentUserId, targetUserId),
            currentUserId,
            targetUserId,
            EntityType.USER,
            Operation.READ
        );
    }
    
    /**
     * Crée une exception pour privilèges administrateur requis
     * @param userId ID de l'utilisateur
     * @param operation Opération tentée
     * @return UnauthorizedAccessException pour privilèges admin
     */
    public static UnauthorizedAccessException adminRequired(Long userId, Operation operation) {
        return new UnauthorizedAccessException(
            String.format("User %d requires admin privileges for operation %s", userId, operation),
            userId,
            null,
            EntityType.USER,
            operation
        );
    }
    
    /**
     * Crée une exception pour rôle salon owner requis
     * @param userId ID de l'utilisateur
     * @param operation Opération tentée
     * @return UnauthorizedAccessException pour privilèges salon owner
     */
    public static UnauthorizedAccessException salonOwnerRequired(Long userId, Operation operation) {
        return new UnauthorizedAccessException(
            String.format("User %d requires salon owner privileges for operation %s", userId, operation),
            userId,
            null,
            EntityType.SALON,
            operation
        );
    }
    
    /**
     * Crée une exception pour authentification requise
     * @return UnauthorizedAccessException pour authentification
     */
    public static UnauthorizedAccessException authenticationRequired() {
        return new UnauthorizedAccessException(
            "Authentication is required to access this resource",
            null,
            null,
            EntityType.USER,
            Operation.READ
        );
    }
    
    /**
     * Crée une exception pour session expirée
     * @param userId ID de l'utilisateur
     * @return UnauthorizedAccessException pour session expirée
     */
    public static UnauthorizedAccessException sessionExpired(Long userId) {
        return new UnauthorizedAccessException(
            "Session has expired, please login again",
            userId,
            null,
            EntityType.USER,
            Operation.READ
        );
    }
    
    /**
     * Crée une exception pour compte désactivé
     * @param userId ID de l'utilisateur
     * @return UnauthorizedAccessException pour compte désactivé
     */
    public static UnauthorizedAccessException accountDisabled(Long userId) {
        return new UnauthorizedAccessException(
            "Account is disabled",
            userId,
            null,
            EntityType.USER,
            Operation.READ
        );
    }
    
    /**
     * Crée une exception pour endpoint non autorisé
     * @param endpoint Endpoint
     * @param userId ID de l'utilisateur
     * @return UnauthorizedAccessException pour endpoint
     */
    public static UnauthorizedAccessException endpointAccess(String endpoint, Long userId) {
        return new UnauthorizedAccessException(
            String.format("User %d is not authorized to access endpoint %s", userId, endpoint),
            userId,
            endpoint,
            EntityType.USER,
            Operation.READ
        );
    }
    
    // ==================== GETTERS ====================
    
    /**
     * Obtient l'ID de l'utilisateur
     * @return ID utilisateur
     */
    public Long getUserId() {
        return userId;
    }
    
    /**
     * Obtient l'ID de la ressource
     * @return ID ressource
     */
    public Object getResourceId() {
        return resourceId;
    }
    
    /**
     * Obtient le type d'entité
     * @return Type d'entité
     */
    public EntityType getEntityType() {
        return entityType;
    }
    
    /**
     * Obtient l'opération
     * @return Opération
     */
    public Operation getOperation() {
        return operation;
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Obtient un message détaillé pour logging
     * @return Message détaillé
     */
    public String getDetailedMessage() {
        return String.format(
            "UnauthorizedAccess: %s [User=%d, Resource=%s, Entity=%s, Operation=%s]",
            getMessage(),
            userId,
            resourceId,
            entityType,
            operation
        );
    }
    
    /**
     * Vérifie si l'exception concerne un utilisateur spécifique
     * @param userId ID utilisateur à vérifier
     * @return true si concerne cet utilisateur
     */
    public boolean concernsUser(Long userId) {
        return this.userId != null && this.userId.equals(userId);
    }
    
    /**
     * Vérifie si l'exception concerne une ressource spécifique
     * @param resourceId ID ressource à vérifier
     * @return true si concerne cette ressource
     */
    public boolean concernsResource(Object resourceId) {
        return this.resourceId != null && this.resourceId.equals(resourceId);
    }
    
    /**
     * Vérifie si l'exception concerne un type d'entité spécifique
     * @param entityType Type d'entité à vérifier
     * @return true si concerne ce type
     */
    public boolean concernsEntityType(EntityType entityType) {
        return this.entityType == entityType;
    }
    
    /**
     * Vérifie si l'exception concerne une opération spécifique
     * @param operation Opération à vérifier
     * @return true si concerne cette opération
     */
    public boolean concernsOperation(Operation operation) {
        return this.operation == operation;
    }
}