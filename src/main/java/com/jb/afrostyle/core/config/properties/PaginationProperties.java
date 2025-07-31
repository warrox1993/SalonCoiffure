package com.jb.afrostyle.core.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration de la pagination
 * Externalise les constantes liées à la pagination
 * 
 * @param defaultPageSize Taille de page par défaut
 * @param maxPageSize Taille de page maximum
 * @param defaultPageNumber Page par défaut (0-based)
 * 
 * @version 1.0
 * @since Java 21
 */
@ConfigurationProperties(prefix = "afrostyle.pagination")
@Validated
public record PaginationProperties(
    
    @Min(value = 1, message = "La taille de page par défaut doit être d'au moins 1")
    @Max(value = 50, message = "La taille de page par défaut ne peut pas dépasser 50")
    int defaultPageSize,
    
    @Min(value = 1, message = "La taille de page maximum doit être d'au moins 1")
    @Max(value = 500, message = "La taille de page maximum ne peut pas dépasser 500")
    int maxPageSize,
    
    @Min(value = 0, message = "Le numéro de page par défaut ne peut pas être négatif")
    int defaultPageNumber
    
) {}