package com.jb.afrostyle.core.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration centrale AfroStyle
 * Regroupe toutes les propriétés business externalisées
 * 
 * @param businessHours Configuration des heures d'ouverture
 * @param booking Configuration des réservations  
 * @param payment Configuration des paiements
 * @param user Configuration des utilisateurs
 * @param salon Configuration des salons
 * @param notification Configuration des notifications
 * @param pagination Configuration de la pagination
 * @param cache Configuration du cache
 * @param validation Configuration de validation
 * 
 * @version 1.0
 * @since Java 21
 */
@ConfigurationProperties(prefix = "afrostyle")
@Validated
public record AfroStyleProperties(
    
    @Valid
    @NotNull(message = "La configuration des heures d'ouverture ne peut pas être nulle")
    BusinessHoursProperties businessHours,
    
    @Valid
    @NotNull(message = "La configuration des réservations ne peut pas être nulle")
    BookingProperties booking,
    
    @Valid
    @NotNull(message = "La configuration des paiements ne peut pas être nulle")
    PaymentProperties payment,
    
    @Valid 
    @NotNull(message = "La configuration des utilisateurs ne peut pas être nulle")
    UserProperties user,
    
    @Valid
    @NotNull(message = "La configuration des salons ne peut pas être nulle")
    SalonProperties salon,
    
    @Valid
    @NotNull(message = "La configuration des notifications ne peut pas être nulle")
    NotificationProperties notification,
    
    @Valid
    @NotNull(message = "La configuration de la pagination ne peut pas être nulle")
    PaginationProperties pagination,
    
    @Valid
    @NotNull(message = "La configuration du cache ne peut pas être nulle")
    CacheProperties cache,
    
    @Valid
    @NotNull(message = "La configuration de validation ne peut pas être nulle")
    ValidationProperties validation
    
) {
    
    @ConstructorBinding
    public AfroStyleProperties {
        // Validations croisées si nécessaire
        // Toutes les validations individuelles sont gérées par les records respectifs
    }
}