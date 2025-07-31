package com.jb.afrostyle.core.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration du cache
 * Externalise les constantes liées au cache
 * 
 * @param userCacheDurationMinutes Durée de cache pour données utilisateur (minutes)
 * @param serviceCacheDurationHours Durée de cache pour services (heures)
 * @param salonCacheDurationHours Durée de cache pour salons (heures)
 * 
 * @version 1.0
 * @since Java 21
 */
@ConfigurationProperties(prefix = "afrostyle.cache")
@Validated
public record CacheProperties(
    
    @Min(value = 1, message = "La durée de cache utilisateur doit être d'au moins 1 minute")
    @Max(value = 1440, message = "La durée de cache utilisateur ne peut pas dépasser 1440 minutes (24h)")
    int userCacheDurationMinutes,
    
    @Min(value = 1, message = "La durée de cache service doit être d'au moins 1 heure")
    @Max(value = 24, message = "La durée de cache service ne peut pas dépasser 24 heures")
    int serviceCacheDurationHours,
    
    @Min(value = 1, message = "La durée de cache salon doit être d'au moins 1 heure")
    @Max(value = 48, message = "La durée de cache salon ne peut pas dépasser 48 heures")
    int salonCacheDurationHours
    
) {}