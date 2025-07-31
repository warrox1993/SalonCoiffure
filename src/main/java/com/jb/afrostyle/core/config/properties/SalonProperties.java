package com.jb.afrostyle.core.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration des salons
 * Externalise les constantes liées aux salons
 * 
 * @param defaultName Nom du salon par défaut
 * @param defaultAddress Adresse du salon par défaut
 * @param maxSalonsPerOwner Nombre maximum de salons par propriétaire
 * 
 * @version 1.0
 * @since Java 21
 */
@ConfigurationProperties(prefix = "afrostyle.salon")
@Validated
public record SalonProperties(
    
    @NotBlank(message = "Le nom du salon par défaut ne peut pas être vide")
    String defaultName,
    
    @NotBlank(message = "L'adresse du salon par défaut ne peut pas être vide")
    String defaultAddress,
    
    @Min(value = 1, message = "Un propriétaire doit pouvoir avoir au moins 1 salon")
    @Max(value = 50, message = "Maximum 50 salons par propriétaire")
    int maxSalonsPerOwner
    
) {}