package com.jb.afrostyle.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import com.jb.afrostyle.booking.validation.ValidBusinessHours;
import com.jb.afrostyle.booking.validation.TimeRangeValidator;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO pour la demande de création de disponibilité
 * Permet aux salons de définir leurs créneaux disponibles
 */
@ValidBusinessHours
public record AvailabilityRequest(
    /**
     * Date de la disponibilité
     * Doit être aujourd'hui ou dans le futur
     */
    @NotNull(message = "Date is mandatory")
    @FutureOrPresent(message = "Date must be today or in the future")
    LocalDate date,

    /**
     * Heure de début de la disponibilité
     * Doit être spécifiée
     */
    @NotNull(message = "Start time is mandatory")
    LocalTime startTime,

    /**
     * Heure de fin de la disponibilité
     * Doit être postérieure à l'heure de début
     */
    @NotNull(message = "End time is mandatory")
    LocalTime endTime,

    /**
     * Description optionnelle de la disponibilité
     * Peut contenir des informations complémentaires
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description
) {
    /**
     * Validation personnalisée pour s'assurer que l'heure de fin est après l'heure de début
     * REFACTORISÉ : Utilise TimeRangeValidator centralisé
     * @return true si la validation passe
     */
    @AssertTrue(message = "End time must be after start time")
    public boolean isValidTimeRange() {
        return TimeRangeValidator.isValidTimeRange(startTime, endTime);
    }
    
    /**
     * Validation pour s'assurer qu'une disponibilité ne dépasse pas 12 heures
     * REFACTORISÉ : Utilise TimeRangeValidator centralisé
     * @return true si la validation passe
     */
    @AssertTrue(message = "Availability duration cannot exceed 12 hours")
    public boolean isValidDuration() {
        return TimeRangeValidator.isValidDuration(startTime, endTime, 12);
    }
}