package com.jb.afrostyle.core.mapper;

import com.jb.afrostyle.core.enums.EntityType;
import com.jb.afrostyle.core.validation.ValidationResult;
import com.jb.afrostyle.core.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registre centralisé de tous les mappers AfroStyle
 * Fournit un accès unifié à tous les mappers du système
 * Gère la découverte automatique et la validation des mappers
 * 
 * @version 1.0
 * @since Java 21
 */
@Component
public class MapperRegistry {
    
    private static final Logger logger = LoggerFactory.getLogger(MapperRegistry.class);
    
    @Autowired
    private ApplicationContext applicationContext;
    
    // Cache des mappers par type d'entité
    private final Map<EntityType, BaseMapper<?, ?>> mappersByEntityType = new ConcurrentHashMap<>();
    
    // Cache des mappers par nom de classe
    private final Map<String, BaseMapper<?, ?>> mappersByName = new ConcurrentHashMap<>();
    
    // Liste de tous les mappers découverts
    private final List<BaseMapper<?, ?>> allMappers = new ArrayList<>();
    
    // Statistiques du registre
    private final Map<String, Object> registryStats = new ConcurrentHashMap<>();
    
    // ==================== INITIALISATION ====================
    
    /**
     * Initialise le registre après injection des dépendances
     */
    @PostConstruct
    public void initializeRegistry() {
        logger.info("Initializing Mapper Registry...");
        
        try {
            discoverMappers();
            buildCache();
            validateMappers();
            computeStatistics();
            
            logger.info("✅ Mapper Registry initialized successfully with {} mappers", allMappers.size());
            
        } catch (Exception e) {
            logger.error("❌ Failed to initialize Mapper Registry", e);
            throw new BusinessException("Failed to initialize Mapper Registry", 
                                      "MAPPER_REGISTRY_INIT_ERROR", 
                                      EntityType.USER, 
                                      com.jb.afrostyle.core.enums.Operation.READ, 
                                      e);
        }
    }
    
    /**
     * Découvre tous les mappers dans le contexte Spring
     */
    private void discoverMappers() {
        logger.debug("Discovering mappers in application context...");
        
        // Rechercher tous les beans qui implémentent BaseMapper
        Map<String, BaseMapper> mapperBeans = applicationContext.getBeansOfType(BaseMapper.class);
        
        for (Map.Entry<String, BaseMapper> entry : mapperBeans.entrySet()) {
            String beanName = entry.getKey();
            BaseMapper<?, ?> mapper = entry.getValue();
            
            allMappers.add(mapper);
            mappersByName.put(beanName, mapper);
            mappersByName.put(mapper.getClass().getSimpleName(), mapper);
            
            logger.debug("Discovered mapper: {} ({})", beanName, mapper.getClass().getSimpleName());
        }
        
        logger.info("Discovered {} mappers", allMappers.size());
    }
    
    /**
     * Construit le cache des mappers par type d'entité
     */
    private void buildCache() {
        logger.debug("Building mapper cache by entity type...");
        
        for (BaseMapper<?, ?> mapper : allMappers) {
            // Essayer de déterminer le type d'entité supporté
            for (EntityType entityType : EntityType.values()) {
                if (mapper.supportsEntityType(entityType)) {
                    mappersByEntityType.put(entityType, mapper);
                    logger.debug("Mapped {} to entity type {}", 
                               mapper.getClass().getSimpleName(), entityType);
                    break;
                }
            }
        }
        
        logger.info("Built entity type cache with {} mappings", mappersByEntityType.size());
    }
    
    /**
     * Valide tous les mappers découverts
     */
    private void validateMappers() {
        logger.debug("Validating discovered mappers...");
        
        int validMappers = 0;
        int invalidMappers = 0;
        
        for (BaseMapper<?, ?> mapper : allMappers) {
            try {
                // Test basique de validation
                String mapperInfo = mapper.getMapperInfo();
                if (mapperInfo != null && !mapperInfo.trim().isEmpty()) {
                    validMappers++;
                } else {
                    logger.warn("Mapper {} has invalid info", mapper.getClass().getSimpleName());
                    invalidMappers++;
                }
                
            } catch (Exception e) {
                logger.error("Mapper {} failed validation", mapper.getClass().getSimpleName(), e);
                invalidMappers++;
            }
        }
        
        logger.info("Mapper validation completed: {} valid, {} invalid", validMappers, invalidMappers);
        
        if (invalidMappers > 0) {
            logger.warn("⚠️ Some mappers failed validation - system may be unstable");
        }
    }
    
    /**
     * Calcule les statistiques du registre
     */
    private void computeStatistics() {
        registryStats.put("totalMappers", allMappers.size());
        registryStats.put("mappersWithEntityType", mappersByEntityType.size());
        registryStats.put("initializationTime", new Date());
        
        // Compter les mappers par package
        Map<String, Integer> packageCounts = new HashMap<>();
        for (BaseMapper<?, ?> mapper : allMappers) {
            String packageName = mapper.getClass().getPackageName();
            packageCounts.merge(packageName, 1, Integer::sum);
        }
        registryStats.put("mappersByPackage", packageCounts);
        
        logger.debug("Registry statistics computed: {}", registryStats);
    }
    
    // ==================== ACCÈS AUX MAPPERS ====================
    
    /**
     * Obtient un mapper par type d'entité
     * @param entityType Type d'entité
     * @return Mapper correspondant ou null
     */
    @SuppressWarnings("unchecked")
    public <E, D> BaseMapper<E, D> getMapperForEntityType(EntityType entityType) {
        BaseMapper<?, ?> mapper = mappersByEntityType.get(entityType);
        
        if (mapper == null) {
            logger.debug("No mapper found for entity type: {}", entityType);
            return null;
        }
        
        logger.debug("Found mapper {} for entity type {}", 
                   mapper.getClass().getSimpleName(), entityType);
        
        return (BaseMapper<E, D>) mapper;
    }
    
    /**
     * Obtient un mapper par nom de classe
     * @param mapperName Nom du mapper
     * @return Mapper correspondant ou null
     */
    @SuppressWarnings("unchecked")
    public <E, D> BaseMapper<E, D> getMapperByName(String mapperName) {
        BaseMapper<?, ?> mapper = mappersByName.get(mapperName);
        
        if (mapper == null) {
            logger.debug("No mapper found with name: {}", mapperName);
            return null;
        }
        
        logger.debug("Found mapper {} by name", mapper.getClass().getSimpleName());
        
        return (BaseMapper<E, D>) mapper;
    }
    
    /**
     * Obtient un mapper par type d'entité avec assertion
     * @param entityType Type d'entité
     * @return Mapper correspondant
     * @throws BusinessException si mapper non trouvé
     */
    public <E, D> BaseMapper<E, D> getMapperForEntityTypeRequired(EntityType entityType) {
        BaseMapper<E, D> mapper = getMapperForEntityType(entityType);
        
        if (mapper == null) {
            throw BusinessException.internalError(
                "No mapper found for entity type: " + entityType,
                null,
                entityType
            );
        }
        
        return mapper;
    }
    
    /**
     * Obtient un mapper par nom avec assertion
     * @param mapperName Nom du mapper
     * @return Mapper correspondant
     * @throws BusinessException si mapper non trouvé
     */
    public <E, D> BaseMapper<E, D> getMapperByNameRequired(String mapperName) {
        BaseMapper<E, D> mapper = getMapperByName(mapperName);
        
        if (mapper == null) {
            throw BusinessException.internalError(
                "No mapper found with name: " + mapperName,
                null,
                mapperName
            );
        }
        
        return mapper;
    }
    
    // ==================== OPÉRATIONS DE MAPPING GÉNÉRIQUES ====================
    
    /**
     * Mappe une entité en DTO en utilisant le type d'entité
     * @param entity Entité à mapper
     * @param entityType Type d'entité
     * @return ValidationResult contenant le DTO ou erreur
     */
    @SuppressWarnings("unchecked")
    public <E, D> ValidationResult<D> mapToDTO(E entity, EntityType entityType) {
        if (entity == null) {
            return ValidationResult.error(
                "Entity cannot be null",
                "NULL_ENTITY",
                entityType,
                com.jb.afrostyle.core.enums.Operation.READ
            );
        }
        
        BaseMapper<E, D> mapper = getMapperForEntityType(entityType);
        if (mapper == null) {
            return ValidationResult.error(
                "No mapper found for entity type: " + entityType,
                "MAPPER_NOT_FOUND",
                entityType,
                com.jb.afrostyle.core.enums.Operation.READ
            );
        }
        
        return mapper.toDTOSafe(entity, entityType);
    }
    
    /**
     * Mappe un DTO en entité en utilisant le type d'entité
     * @param dto DTO à mapper
     * @param entityType Type d'entité
     * @return ValidationResult contenant l'entité ou erreur
     */
    @SuppressWarnings("unchecked")
    public <E, D> ValidationResult<E> mapToEntity(D dto, EntityType entityType) {
        if (dto == null) {
            return ValidationResult.error(
                "DTO cannot be null",
                "NULL_DTO",
                entityType,
                com.jb.afrostyle.core.enums.Operation.CREATE
            );
        }
        
        BaseMapper<E, D> mapper = getMapperForEntityType(entityType);
        if (mapper == null) {
            return ValidationResult.error(
                "No mapper found for entity type: " + entityType,
                "MAPPER_NOT_FOUND",
                entityType,
                com.jb.afrostyle.core.enums.Operation.CREATE
            );
        }
        
        return mapper.toEntitySafe(dto, entityType);
    }
    
    /**
     * Mappe une liste d'entités en DTOs
     * @param entities Liste d'entités
     * @param entityType Type d'entité
     * @return ValidationResult contenant la liste de DTOs ou erreur
     */
    @SuppressWarnings("unchecked")
    public <E, D> ValidationResult<List<D>> mapToDTOList(List<E> entities, EntityType entityType) {
        if (entities == null) {
            return ValidationResult.success(null);
        }
        
        BaseMapper<E, D> mapper = getMapperForEntityType(entityType);
        if (mapper == null) {
            return ValidationResult.error(
                "No mapper found for entity type: " + entityType,
                "MAPPER_NOT_FOUND",
                entityType,
                com.jb.afrostyle.core.enums.Operation.READ
            );
        }
        
        return mapper.toDTOListSafe(entities, entityType);
    }
    
    // ==================== INFORMATIONS ET STATISTIQUES ====================
    
    /**
     * Obtient la liste de tous les mappers
     * @return Liste immutable de tous les mappers
     */
    public List<BaseMapper<?, ?>> getAllMappers() {
        return Collections.unmodifiableList(allMappers);
    }
    
    /**
     * Obtient les statistiques du registre
     * @return Map des statistiques
     */
    public Map<String, Object> getRegistryStatistics() {
        return Collections.unmodifiableMap(registryStats);
    }
    
    /**
     * Vérifie si un mapper existe pour un type d'entité
     * @param entityType Type d'entité
     * @return true si mapper disponible
     */
    public boolean hasMapperForEntityType(EntityType entityType) {
        return mappersByEntityType.containsKey(entityType);
    }
    
    /**
     * Vérifie si un mapper existe avec un nom donné
     * @param mapperName Nom du mapper
     * @return true si mapper disponible
     */
    public boolean hasMapperWithName(String mapperName) {
        return mappersByName.containsKey(mapperName);
    }
    
    /**
     * Obtient le nombre total de mappers
     * @return Nombre de mappers
     */
    public int getMapperCount() {
        return allMappers.size();
    }
    
    /**
     * Obtient les types d'entités supportés
     * @return Set des types d'entités avec mappers
     */
    public Set<EntityType> getSupportedEntityTypes() {
        return Collections.unmodifiableSet(mappersByEntityType.keySet());
    }
    
    /**
     * Obtient la liste des noms de mappers
     * @return Set des noms de mappers
     */
    public Set<String> getMapperNames() {
        return Collections.unmodifiableSet(mappersByName.keySet());
    }
    
    /**
     * Génère un rapport détaillé du registre
     * @return String avec rapport complet
     */
    public String generateRegistryReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== MAPPER REGISTRY REPORT ===\n");
        report.append(String.format("Total Mappers: %d\n", allMappers.size()));
        report.append(String.format("Entity Type Mappings: %d\n", mappersByEntityType.size()));
        report.append(String.format("Named Mappers: %d\n", mappersByName.size()));
        
        report.append("\nSupported Entity Types:\n");
        for (EntityType entityType : getSupportedEntityTypes()) {
            BaseMapper<?, ?> mapper = mappersByEntityType.get(entityType);
            report.append(String.format("  - %s: %s\n", 
                                       entityType, 
                                       mapper.getClass().getSimpleName()));
        }
        
        report.append("\nAll Mappers:\n");
        for (BaseMapper<?, ?> mapper : allMappers) {
            report.append(String.format("  - %s (%s)\n", 
                                       mapper.getClass().getSimpleName(),
                                       mapper.getClass().getPackageName()));
        }
        
        report.append("\nStatistics:\n");
        for (Map.Entry<String, Object> entry : registryStats.entrySet()) {
            report.append(String.format("  - %s: %s\n", entry.getKey(), entry.getValue()));
        }
        
        report.append("===============================");
        
        return report.toString();
    }
    
    // ==================== GESTION D'ERREURS ====================
    
    /**
     * Valide l'état du registre
     * @return ValidationResult avec état de validation
     */
    public ValidationResult<String> validateRegistryState() {
        List<String> issues = new ArrayList<>();
        
        if (allMappers.isEmpty()) {
            issues.add("No mappers discovered");
        }
        
        if (mappersByEntityType.isEmpty()) {
            issues.add("No entity type mappings configured");
        }
        
        // Vérifier les types d'entités critiques
        EntityType[] criticalTypes = {EntityType.USER, EntityType.BOOKING, EntityType.PAYMENT};
        for (EntityType entityType : criticalTypes) {
            if (!hasMapperForEntityType(entityType)) {
                issues.add("No mapper for critical entity type: " + entityType);
            }
        }
        
        if (!issues.isEmpty()) {
            return ValidationResult.error(
                "Registry validation failed: " + String.join(", ", issues),
                "REGISTRY_VALIDATION_ERROR",
                EntityType.USER,
                com.jb.afrostyle.core.enums.Operation.READ
            );
        }
        
        return ValidationResult.success("Registry is valid and operational");
    }
    
    /**
     * Réinitialise le registre (pour testing ou rechargement)
     */
    public void resetRegistry() {
        logger.info("Resetting Mapper Registry...");
        
        allMappers.clear();
        mappersByEntityType.clear();
        mappersByName.clear();
        registryStats.clear();
        
        initializeRegistry();
        
        logger.info("Registry reset completed");
    }
}