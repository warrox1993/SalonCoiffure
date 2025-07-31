package com.jb.afrostyle.core.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration des réservations
 * Externalise les constantes liées aux bookings
 * 
 * @param minDurationMinutes Durée minimum d'une réservation en minutes
 * @param maxDurationMinutes Durée maximum d'une réservation en minutes
 * @param maxServicesPerBooking Nombre maximum de services par réservation
 * @param minDurationPerServiceMinutes Durée minimum par service en minutes
 * @param cancellationDeadlineHours Délai d'annulation en heures
 * @param timeToleranceMinutes Tolérance pour réservations en minutes
 * @param maxAdvanceBookingMonths Maximum de mois pour réserver à l'avance
 * 
 * @version 1.0
 * @since Java 21
 */
@ConfigurationProperties(prefix = "afrostyle.booking")
@Validated
public record BookingProperties(
    
    @Min(value = 15, message = "La durée minimum de réservation doit être d'au moins 15 minutes")
    @Max(value = 60, message = "La durée minimum de réservation ne peut pas dépasser 60 minutes")
    int minDurationMinutes,
    
    @Min(value = 60, message = "La durée maximum de réservation doit être d'au moins 60 minutes")
    @Max(value = 600, message = "La durée maximum de réservation ne peut pas dépasser 600 minutes (10h)")
    int maxDurationMinutes,
    
    @Min(value = 1, message = "Il doit y avoir au moins 1 service par réservation")
    @Max(value = 10, message = "Maximum 10 services par réservation")
    int maxServicesPerBooking,
    
    @Min(value = 15, message = "La durée minimum par service doit être d'au moins 15 minutes")
    @Max(value = 180, message = "La durée minimum par service ne peut pas dépasser 180 minutes")
    int minDurationPerServiceMinutes,
    
    @Min(value = 1, message = "Le délai d'annulation doit être d'au moins 1 heure")
    @Max(value = 168, message = "Le délai d'annulation ne peut pas dépasser 168 heures (7 jours)")
    int cancellationDeadlineHours,
    
    @Min(value = 0, message = "La tolérance ne peut pas être négative")
    @Max(value = 30, message = "La tolérance ne peut pas dépasser 30 minutes")
    int timeToleranceMinutes,
    
    @Min(value = 1, message = "Il faut pouvoir réserver au moins 1 mois à l'avance")
    @Max(value = 12, message = "Maximum 12 mois de réservation à l'avance")
    int maxAdvanceBookingMonths
    
) {
    
    @ConstructorBinding
    public BookingProperties {
        // Validation personnalisée
        if (minDurationMinutes >= maxDurationMinutes) {
            throw new IllegalArgumentException("La durée minimum doit être inférieure à la durée maximum");
        }
        
        if (minDurationPerServiceMinutes > minDurationMinutes) {
            throw new IllegalArgumentException("La durée minimum par service ne peut pas être supérieure à la durée minimum de réservation");
        }
    }
}