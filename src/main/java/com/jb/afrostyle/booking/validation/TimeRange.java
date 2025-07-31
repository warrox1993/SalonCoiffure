package com.jb.afrostyle.booking.validation;

import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalTime;

/**
 * Record pour représenter une plage horaire avec validation business
 * Utilise Java 21 Pattern Matching pour une validation moderne
 */
public record TimeRange(LocalTime startTime, LocalTime endTime, String errorMessage) {
    
    /**
     * Constructeur principal avec validation
     */
    public TimeRange {
        // Validation des arguments dans le compact constructor
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }
    
    /**
     * Constructeur simplifié sans message d'erreur
     */
    public TimeRange(LocalTime startTime, LocalTime endTime) {
        this(startTime, endTime, null);
    }
    
    /**
     * Factory method pour une plage valide
     */
    public static TimeRange valid(LocalTime start, LocalTime end) {
        return new TimeRange(start, end);
    }
    
    /**
     * Factory method pour marquer comme valide (pas de validation nécessaire)
     */
    public static TimeRange valid() {
        return new TimeRange(null, null, null);
    }
    
    /**
     * Factory method pour une plage invalide avec message
     */
    public static TimeRange invalid(String errorMessage) {
        return new TimeRange(null, null, errorMessage);
    }
    
    /**
     * Vérifie si cette plage est dans les heures d'ouverture business
     * MODERNISÉ avec Pattern Matching Java 21
     */
    public boolean isValidBusinessHours(LocalTime openingTime, LocalTime closingTime, 
                                      ConstraintValidatorContext context) {
        
        // Cas d'erreur prédéfinie
        if (this.errorMessage() != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(this.errorMessage()).addConstraintViolation();
            return false;
        }
        
        // Cas où les temps ne sont pas définis - laisser les autres validations gérer
        if (this.startTime() == null || this.endTime() == null) {
            return true;
        }
        
        // Cas de validation business normale
        return validateBusinessHours(this.startTime(), this.endTime(), openingTime, closingTime, context);
    }
    
    /**
     * Validation des heures business avec Pattern Matching
     */
    private boolean validateBusinessHours(LocalTime start, LocalTime end, 
                                        LocalTime opening, LocalTime closing,
                                        ConstraintValidatorContext context) {
        
        if (isWithinBusinessHours(start, end, opening, closing)) {
            return true;
        }
        
        // Déterminer le type d'erreur
        ValidationState validation;
        if (start.isBefore(opening) || start.isAfter(closing)) {
            if (end.isBefore(opening) || end.isAfter(closing)) {
                validation = ValidationState.INVALID_BOTH;
            } else {
                validation = ValidationState.INVALID_START_TIME;
            }
        } else {
            validation = ValidationState.INVALID_END_TIME;
        }
        
        return switch (validation) {
            case ValidationState.VALID -> true;
            case ValidationState.INVALID_START_TIME -> {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    String.format("Start time %s must be between %s and %s", start, opening, closing)
                ).addConstraintViolation();
                yield false;
            }
            case ValidationState.INVALID_END_TIME -> {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    String.format("End time %s must be between %s and %s", end, opening, closing)
                ).addConstraintViolation();
                yield false;
            }
            case ValidationState.INVALID_BOTH -> {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    String.format("Times must be between %s and %s", opening, closing)
                ).addConstraintViolation();
                yield false;
            }
        };
    }
    
    /**
     * Vérifie si les temps sont dans la plage business
     */
    private boolean isWithinBusinessHours(LocalTime start, LocalTime end, 
                                        LocalTime opening, LocalTime closing) {
        return !start.isBefore(opening) && !start.isAfter(closing) &&
               !end.isBefore(opening) && !end.isAfter(closing);
    }
    
    /**
     * États de validation pour Pattern Matching
     */
    private enum ValidationState {
        VALID,
        INVALID_START_TIME,
        INVALID_END_TIME,
        INVALID_BOTH
    }
}