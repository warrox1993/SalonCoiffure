package com.jb.afrostyle.core.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.time.LocalTime;
import java.util.Set;

/**
 * Configuration des heures d'ouverture du salon
 * Externalise les constantes liées aux horaires business
 * 
 * @param openTime Heure d'ouverture (format HH:mm)
 * @param closeTime Heure de fermeture (format HH:mm)
 * @param timeSlotIntervalMinutes Intervalles de créneaux en minutes
 * @param closedDays Jours de fermeture (1=Lundi, 7=Dimanche)
 * 
 * @version 1.0
 * @since Java 21
 */
@ConfigurationProperties(prefix = "afrostyle.business.hours")
@Validated
public record BusinessHoursProperties(
    
    @NotNull(message = "L'heure d'ouverture ne peut pas être nulle")
    LocalTime openTime,
    
    @NotNull(message = "L'heure de fermeture ne peut pas être nulle")
    LocalTime closeTime,
    
    @Min(value = 5, message = "L'intervalle de créneaux doit être d'au moins 5 minutes")
    @Max(value = 60, message = "L'intervalle de créneaux ne peut pas dépasser 60 minutes")
    int timeSlotIntervalMinutes,
    
    @NotNull(message = "Les jours de fermeture ne peuvent pas être nuls")
    Set<@Min(1) @Max(7) Integer> closedDays
    
) {
    
    @ConstructorBinding
    public BusinessHoursProperties {
        // Validation personnalisée
        if (openTime != null && closeTime != null && !closeTime.isAfter(openTime)) {
            throw new IllegalArgumentException("L'heure de fermeture doit être après l'heure d'ouverture");
        }
    }
    
    /**
     * Vérifie si un jour est ouvert
     * @param dayOfWeek Jour de la semaine (1=Lundi, 7=Dimanche)
     * @return true si ouvert, false si fermé
     */
    public boolean isBusinessDay(int dayOfWeek) {
        return !closedDays.contains(dayOfWeek);
    }
    
    /**
     * Vérifie si une heure est dans les heures d'ouverture
     * @param time Heure à vérifier
     * @return true si dans les heures d'ouverture
     */
    public boolean isWithinBusinessHours(LocalTime time) {
        return !time.isBefore(openTime) && !time.isAfter(closeTime);
    }
}