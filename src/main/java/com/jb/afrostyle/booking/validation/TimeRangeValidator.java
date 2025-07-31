package com.jb.afrostyle.booking.validation;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Utilitaire centralisé pour la validation des plages horaires
 * Évite la duplication de code entre AvailabilityRequest et BookingRequest
 */
@Component
public class TimeRangeValidator {
    
    /**
     * Valide qu'une heure de fin est après une heure de début
     * @param startTime Heure de début
     * @param endTime Heure de fin
     * @return true si la plage est valide
     */
    public static boolean isValidTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return true; // Laisse les autres validations gérer les valeurs nulles
        }
        return endTime.isAfter(startTime);
    }
    
    /**
     * Valide qu'une date-heure de fin est après une date-heure de début
     * @param startDateTime Date-heure de début
     * @param endDateTime Date-heure de fin
     * @return true si la plage est valide
     */
    public static boolean isValidDateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return true; // Laisse les autres validations gérer les valeurs nulles
        }
        return endDateTime.isAfter(startDateTime);
    }
    
    /**
     * Valide qu'une durée ne dépasse pas un nombre maximum d'heures
     * @param startTime Heure de début
     * @param endTime Heure de fin
     * @param maxHours Nombre maximum d'heures autorisé
     * @return true si la durée est valide
     */
    public static boolean isValidDuration(LocalTime startTime, LocalTime endTime, int maxHours) {
        if (startTime == null || endTime == null) {
            return true; // Laisse les autres validations gérer les valeurs nulles
        }
        
        if (!endTime.isAfter(startTime)) {
            return false; // Heure de fin doit être après heure de début
        }
        
        // Calculer la durée en heures
        long durationHours = java.time.Duration.between(startTime, endTime).toHours();
        return durationHours <= maxHours;
    }
    
    /**
     * Valide qu'une date-heure ne dépasse pas un nombre maximum d'heures
     * @param startDateTime Date-heure de début
     * @param endDateTime Date-heure de fin
     * @param maxHours Nombre maximum d'heures autorisé
     * @return true si la durée est valide
     */
    public static boolean isValidDateTimeDuration(LocalDateTime startDateTime, LocalDateTime endDateTime, int maxHours) {
        if (startDateTime == null || endDateTime == null) {
            return true; // Laisse les autres validations gérer les valeurs nulles
        }
        
        if (!endDateTime.isAfter(startDateTime)) {
            return false; // Date-heure de fin doit être après date-heure de début
        }
        
        // Calculer la durée en heures
        long durationHours = java.time.Duration.between(startDateTime, endDateTime).toHours();
        return durationHours <= maxHours;
    }
}