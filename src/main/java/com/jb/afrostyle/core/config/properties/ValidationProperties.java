package com.jb.afrostyle.core.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration de validation
 * Externalise les constantes liées à la validation
 * 
 * @param maxErrors Nombre maximum d'erreurs de validation affichées
 * @param description Configuration des descriptions
 * 
 * @version 1.0
 * @since Java 21
 */
@ConfigurationProperties(prefix = "afrostyle.validation")
@Validated
public record ValidationProperties(
    
    @Min(value = 1, message = "Il doit y avoir au moins 1 erreur de validation affichée")
    @Max(value = 50, message = "Maximum 50 erreurs de validation affichées")
    int maxErrors,
    
    DescriptionConfiguration description
    
) {
    
    /**
     * Configuration des descriptions
     */
    public record DescriptionConfiguration(
        @Min(value = 1, message = "La longueur minimum des descriptions doit être d'au moins 1 caractère")
        @Max(value = 50, message = "La longueur minimum des descriptions ne peut pas dépasser 50 caractères")
        int minLength,
        
        @Min(value = 10, message = "La longueur maximum des descriptions doit être d'au moins 10 caractères")
        @Max(value = 2000, message = "La longueur maximum des descriptions ne peut pas dépasser 2000 caractères")
        int maxLength
    ) {}
}