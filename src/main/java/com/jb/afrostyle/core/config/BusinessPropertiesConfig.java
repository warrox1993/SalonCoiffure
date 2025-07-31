package com.jb.afrostyle.core.config;

import com.jb.afrostyle.core.config.properties.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration centralisée des propriétés business
 * Active toutes les classes @ConfigurationProperties
 * 
 * Cette classe centralise l'activation de toutes les propriétés externalisées
 * et assure leur validation automatique au démarrage de l'application.
 * 
 * @version 1.0
 * @since Java 21
 */
@Configuration
@EnableConfigurationProperties({
    AfroStyleProperties.class,
    BusinessHoursProperties.class,
    BookingProperties.class,
    PaymentProperties.class,
    UserProperties.class,
    SalonProperties.class,
    NotificationProperties.class,
    PaginationProperties.class,
    CacheProperties.class,
    ValidationProperties.class
})
@Validated
public class BusinessPropertiesConfig {
    
    /**
     * Cette classe configure automatiquement toutes les propriétés business
     * externalisées via @ConfigurationProperties.
     * 
     * Les propriétés sont validées automatiquement au démarrage grâce à
     * l'annotation @Validated sur chaque record de propriétés.
     * 
     * Si une propriété est invalide ou manquante, l'application ne démarrera
     * pas et affichera des erreurs de validation détaillées.
     */
}