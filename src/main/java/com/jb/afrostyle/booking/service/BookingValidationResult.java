package com.jb.afrostyle.booking.service;

import com.jb.afrostyle.salon.payload.dto.SalonDTO;
import com.jb.afrostyle.serviceoffering.payload.dto.ServiceDTO;
import com.jb.afrostyle.user.dto.UserDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Résultat de validation d'une réservation
 * Contient toutes les données validées et calculées
 */
public record BookingValidationResult(
    // Données validées
    UserDTO validatedUser,
    SalonDTO validatedSalon,
    Set<ServiceDTO> validatedServices,
    
    // Calculs dérivés
    LocalDateTime calculatedStartTime,
    LocalDateTime calculatedEndTime,
    int totalDuration, // en minutes
    BigDecimal totalPrice,
    int totalServices,
    
    // Informations pour Google Calendar
    String userName,
    String salonName
) {
    /**
     * Factory method pour créer un résultat de validation
     */
    public static BookingValidationResult create(
            UserDTO user,
            SalonDTO salon,
            Set<ServiceDTO> services,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int duration,
            BigDecimal price) {
        
        return new BookingValidationResult(
                user,
                salon,
                services,
                startTime,
                endTime,
                duration,
                price,
                services.size(),
                user.fullName(),
                "AfroStyle Salon" // Salon unique
        );
    }
}