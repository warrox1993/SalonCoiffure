package com.jb.afrostyle.core.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;

/**
 * Configuration MapStruct centralisée pour AfroStyle
 * Définit les règles de mapping communes à tous les mappers
 * Standardise la génération de code et les politiques de mapping
 * 
 * @version 1.0
 * @since Java 21
 */
@MapperConfig(
    // ==================== CONFIGURATION SPRING ====================
    componentModel = MappingConstants.ComponentModel.SPRING,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    
    // ==================== GESTION DES VALEURS NULLES ====================
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION,
    
    // ==================== GESTION DES COLLECTIONS ====================
    collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    
    // ==================== POLITIQUES DE REPORTING ====================
    unmappedTargetPolicy = ReportingPolicy.WARN,
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    typeConversionPolicy = ReportingPolicy.ERROR,
    
    // ==================== IMPORTATION D'UTILITAIRES ====================
    uses = {
        DateTimeMapperUtils.class,
        CollectionMapperUtils.class,
        ValidationMapperUtils.class
    }
)
public interface AfroStyleMapperConfig {
    
    // Cette interface sert uniquement de configuration globale
    // Tous les mappers doivent l'utiliser avec @Mapper(config = MapperConfig.class)
    
    /*
     * EXPLICATION DES CONFIGURATIONS :
     * 
     * componentModel = SPRING : 
     *   - Génère des beans Spring auto-injectables
     *   - Compatible avec @Autowired et injection de dépendances
     * 
     * injectionStrategy = CONSTRUCTOR :
     *   - Injection par constructeur (plus sûre)
     *   - Compatible avec les final fields
     * 
     * nullValuePropertyMappingStrategy = IGNORE :
     *   - Les propriétés null sont ignorées lors du mapping
     *   - Évite d'écraser des valeurs existantes avec null
     * 
     * nullValueCheckStrategy = ON_IMPLICIT_CONVERSION :
     *   - Vérification null uniquement lors des conversions implicites
     *   - Optimise les performances
     * 
     * collectionMappingStrategy = ADDER_PREFERRED :
     *   - Utilise les méthodes addXxx() quand disponibles
     *   - Meilleure performance pour les collections
     * 
     * unmappedTargetPolicy = WARN :
     *   - Avertissement pour les champs cibles non mappés
     *   - Aide à identifier les oublis de mapping
     * 
     * unmappedSourcePolicy = IGNORE :
     *   - Ignore les champs sources non utilisés
     *   - Évite les warnings inutiles
     * 
     * typeConversionPolicy = ERROR :
     *   - Erreur de compilation pour conversions dangereuses
     *   - Type safety maximale
     * 
     * uses = [...] :
     *   - Importe les utilitaires de mapping partagés
     *   - Réutilise les méthodes communes
     */
}