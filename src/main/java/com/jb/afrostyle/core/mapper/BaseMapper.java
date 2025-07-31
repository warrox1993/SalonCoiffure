package com.jb.afrostyle.core.mapper;

import com.jb.afrostyle.core.validation.ValidationResult;
import com.jb.afrostyle.core.exception.BusinessException;
import com.jb.afrostyle.core.enums.EntityType;
import com.jb.afrostyle.core.enums.Operation;

import java.util.List;
import java.util.Set;
import java.util.Collection;

/**
 * Interface de base pour tous les mappers AfroStyle
 * Fournit une structure commune et des méthodes utilitaires partagées
 * Intègre avec le système de validation et d'exception centralisé
 * 
 * @param <E> Type d'entité
 * @param <D> Type de DTO
 * 
 * @version 1.0
 * @since Java 21
 */
public interface BaseMapper<E, D> {
    
    // ==================== MÉTHODES DE MAPPING OBLIGATOIRES ====================
    
    /**
     * Convertit une entité en DTO
     * @param entity Entité à convertir
     * @return DTO correspondant
     */
    D toDTO(E entity);
    
    /**
     * Convertit un DTO en entité
     * @param dto DTO à convertir
     * @return Entité correspondante
     */
    E toEntity(D dto);
    
    // ==================== MÉTHODES DE COLLECTION (DEFAULT) ====================
    
    /**
     * Convertit une liste d'entités en liste de DTOs
     * @param entities Liste d'entités
     * @return Liste de DTOs
     */
    default List<D> toDTOList(List<E> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                      .map(this::toDTO)
                      .toList();
    }
    
    /**
     * Convertit une liste de DTOs en liste d'entités
     * @param dtos Liste de DTOs
     * @return Liste d'entités
     */
    default List<E> toEntityList(List<D> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                   .map(this::toEntity)
                   .toList();
    }
    
    /**
     * Convertit un Set d'entités en Set de DTOs
     * @param entities Set d'entités
     * @return Set de DTOs
     */
    default Set<D> toDTOSet(Set<E> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                      .map(this::toDTO)
                      .collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * Convertit un Set de DTOs en Set d'entités
     * @param dtos Set de DTOs
     * @return Set d'entités
     */
    default Set<E> toEntitySet(Set<D> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                   .map(this::toEntity)
                   .collect(java.util.stream.Collectors.toSet());
    }
    
    // ==================== MÉTHODES SÉCURISÉES AVEC VALIDATION ====================
    
    /**
     * Convertit une entité en DTO avec validation
     * @param entity Entité à convertir
     * @param entityType Type d'entité pour le contexte d'erreur
     * @return ValidationResult contenant le DTO ou erreur
     */
    default ValidationResult<D> toDTOSafe(E entity, EntityType entityType) {
        if (entity == null) {
            return ValidationResult.error(
                "Entity cannot be null",
                "NULL_ENTITY",
                entityType,
                Operation.READ
            );
        }
        
        try {
            D dto = toDTO(entity);
            if (dto == null) {
                return ValidationResult.error(
                    "Mapping resulted in null DTO",
                    "MAPPING_FAILED",
                    entityType,
                    Operation.READ
                );
            }
            return ValidationResult.success(dto);
        } catch (Exception e) {
            return new ValidationResult.Error<>(
                "Mapping failed: " + e.getMessage(),
                e,
                "MAPPING_ERROR",
                entityType,
                Operation.READ
            );
        }
    }
    
    /**
     * Convertit un DTO en entité avec validation
     * @param dto DTO à convertir
     * @param entityType Type d'entité pour le contexte d'erreur
     * @return ValidationResult contenant l'entité ou erreur
     */
    default ValidationResult<E> toEntitySafe(D dto, EntityType entityType) {
        if (dto == null) {
            return ValidationResult.error(
                "DTO cannot be null",
                "NULL_DTO",
                entityType,
                Operation.CREATE
            );
        }
        
        try {
            E entity = toEntity(dto);
            if (entity == null) {
                return ValidationResult.error(
                    "Mapping resulted in null entity",
                    "MAPPING_FAILED",
                    entityType,
                    Operation.CREATE
                );
            }
            return ValidationResult.success(entity);
        } catch (Exception e) {
            return new ValidationResult.Error<>(
                "Mapping failed: " + e.getMessage(),
                e,
                "MAPPING_ERROR",
                entityType,
                Operation.CREATE
            );
        }
    }
    
    /**
     * Convertit une liste d'entités en DTOs avec validation
     * @param entities Liste d'entités
     * @param entityType Type d'entité
     * @return ValidationResult contenant la liste de DTOs ou erreur
     */
    default ValidationResult<List<D>> toDTOListSafe(List<E> entities, EntityType entityType) {
        if (entities == null) {
            return ValidationResult.success(null);
        }
        
        try {
            List<D> dtos = toDTOList(entities);
            return ValidationResult.success(dtos);
        } catch (Exception e) {
            return new ValidationResult.Error<>(
                "List mapping failed: " + e.getMessage(),
                e,
                "LIST_MAPPING_ERROR",
                entityType,
                Operation.READ
            );
        }
    }
    
    // ==================== MÉTHODES AVEC ASSERTIONS ====================
    
    /**
     * Convertit une entité en DTO avec assertion (lance exception si échec)
     * @param entity Entité à convertir
     * @param entityType Type d'entité
     * @return DTO
     * @throws BusinessException si mapping échoue
     */
    default D toDTORequired(E entity, EntityType entityType) {
        ValidationResult<D> result = toDTOSafe(entity, entityType);
        if (result instanceof ValidationResult.Error<D> error) {
            throw BusinessException.internalError(
                error.message(),
                null,
                entity,
                error.cause()
            );
        }
        return ((ValidationResult.Success<D>) result).value();
    }
    
    /**
     * Convertit un DTO en entité avec assertion (lance exception si échec)
     * @param dto DTO à convertir
     * @param entityType Type d'entité
     * @return Entité
     * @throws BusinessException si mapping échoue
     */
    default E toEntityRequired(D dto, EntityType entityType) {
        ValidationResult<E> result = toEntitySafe(dto, entityType);
        if (result instanceof ValidationResult.Error<E> error) {
            throw BusinessException.internalError(
                error.message(),
                null,
                dto,
                error.cause()
            );
        }
        return ((ValidationResult.Success<E>) result).value();
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Vérifie si une entité peut être mappée
     * @param entity Entité à vérifier
     * @return true si mappable
     */
    default boolean canMapToDTO(E entity) {
        if (entity == null) {
            return false;
        }
        
        try {
            D dto = toDTO(entity);
            return dto != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Vérifie si un DTO peut être mappé
     * @param dto DTO à vérifier
     * @return true si mappable
     */
    default boolean canMapToEntity(D dto) {
        if (dto == null) {
            return false;
        }
        
        try {
            E entity = toEntity(dto);
            return entity != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Compte le nombre d'éléments mappables dans une collection
     * @param entities Collection d'entités
     * @return Nombre d'éléments mappables
     */
    default long countMappableEntities(Collection<E> entities) {
        if (entities == null) {
            return 0;
        }
        
        return entities.stream()
                      .filter(this::canMapToDTO)
                      .count();
    }
    
    /**
     * Filtre les entités mappables d'une collection
     * @param entities Collection d'entités
     * @return Liste des entités mappables seulement
     */
    default List<E> filterMappableEntities(Collection<E> entities) {
        if (entities == null) {
            return List.of();
        }
        
        return entities.stream()
                      .filter(this::canMapToDTO)
                      .toList();
    }
    
    // ==================== MÉTHODES D'INFORMATION ====================
    
    /**
     * Obtient le nom du mapper
     * @return Nom du mapper
     */
    default String getMapperName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * Obtient des informations sur le mapper
     * @return String avec informations du mapper
     */
    default String getMapperInfo() {
        return String.format("Mapper: %s, Package: %s", 
                           getMapperName(), 
                           this.getClass().getPackageName());
    }
    
    /**
     * Vérifie si le mapper supporte un type d'entité
     * @param entityType Type d'entité à vérifier
     * @return true si supporté (à implémenter dans les mappers spécifiques)
     */
    default boolean supportsEntityType(EntityType entityType) {
        // Par défaut, les mappers ne spécifient pas leur type
        // Les implémentations peuvent override cette méthode
        return false;
    }
}