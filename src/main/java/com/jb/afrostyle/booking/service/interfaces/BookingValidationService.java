package com.jb.afrostyle.booking.service;

import com.jb.afrostyle.booking.dto.BookingRequest;
import com.jb.afrostyle.salon.payload.dto.SalonDTO;
import com.jb.afrostyle.serviceoffering.payload.dto.ServiceDTO;
import com.jb.afrostyle.user.dto.UserDTO;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Service spécialisé pour la validation des données de réservation
 * Extrait de BookingServiceImpl pour respecter le principe Single Responsibility
 */
public interface BookingValidationService {
    
    /**
     * Valide toutes les données d'une réservation
     * @param request Données de la réservation
     * @param user Utilisateur demandeur
     * @param salon Salon cible
     * @param services Services demandés
     * @return Résultat de validation avec données validées
     * @throws Exception si une validation échoue
     */
    BookingValidationResult validateBookingData(
            BookingRequest request,
            UserDTO user,
            SalonDTO salon,
            Set<ServiceDTO> services
    ) throws Exception;
    
    /**
     * Vérifie la disponibilité d'un créneau horaire
     * @param salon Salon concerné
     * @param startTime Heure de début
     * @param endTime Heure de fin
     * @throws Exception si le créneau n'est pas disponible
     */
    void validateTimeSlotAvailability(
            SalonDTO salon,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) throws Exception;
}