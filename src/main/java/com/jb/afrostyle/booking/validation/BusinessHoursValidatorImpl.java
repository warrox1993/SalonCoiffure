package com.jb.afrostyle.booking.validation;

import com.jb.afrostyle.booking.dto.AvailabilityRequest;
import com.jb.afrostyle.booking.dto.BookingRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalTime;

/**
 * Implémentation moderne de la validation des heures d'ouverture
 * MODERNISÉ avec Java 21 Pattern Matching
 * Vérifie que les créneaux sont dans les heures d'ouverture normales
 */
public class BusinessHoursValidatorImpl implements ConstraintValidator<ValidBusinessHours, Object> {

    private static final LocalTime OPENING_TIME = LocalTime.of(7, 0);  // 7:00 AM
    private static final LocalTime CLOSING_TIME = LocalTime.of(22, 0); // 10:00 PM

    @Override
    public void initialize(ValidBusinessHours constraintAnnotation) {
        // Aucune initialisation nécessaire
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        
        // JAVA 21 COMPATIBLE - Type-safe validation with instanceof patterns
        var timeRange = switch (object) {
            case null -> TimeRange.valid(); // null est considéré comme valide
            
            case AvailabilityRequest req -> 
                TimeRange.valid(req.startTime(), req.endTime());
            
            case BookingRequest req when req.startTime() != null && req.endTime() != null ->
                TimeRange.valid(req.startTime().toLocalTime(), req.endTime().toLocalTime());
            
            case BookingRequest req when req.startTime() == null || req.endTime() == null ->
                TimeRange.valid(); // Laisser les autres validations gérer les temps manquants
            
            default -> TimeRange.invalid("Unsupported object type for business hours validation: " + 
                                       object.getClass().getSimpleName());
        };
        
        // Validation avec la nouvelle approche Pattern Matching
        return timeRange.isValidBusinessHours(OPENING_TIME, CLOSING_TIME, context);
    }
}