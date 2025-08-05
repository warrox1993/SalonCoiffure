package com.jb.afrostyle.booking.service;

import com.jb.afrostyle.booking.domain.entity.Booking;
import com.jb.afrostyle.salon.payload.dto.SalonDTO;
import com.jb.afrostyle.serviceoffering.payload.dto.ServiceDTO;
import com.jb.afrostyle.user.dto.UserDTO;

import java.util.Set;

/**
 * Service spécialisé pour les intégrations externes des réservations
 * (Google Calendar, Email, etc.)
 * Extrait de BookingServiceImpl pour respecter le principe Single Responsibility
 */
public interface BookingIntegrationService {
    
    /**
     * Gère toutes les intégrations post-création d'une réservation
     * @param booking Réservation créée
     * @param user Utilisateur
     * @param salon Salon
     * @param services Services
     * @return Réservation enrichie avec les données d'intégration
     */
    Booking handlePostBookingIntegrations(
            Booking booking,
            UserDTO user,
            SalonDTO salon,
            Set<ServiceDTO> services
    );
    
    /**
     * Crée un événement Google Calendar pour la réservation
     * @param booking Réservation
     * @return ID de l'événement Google Calendar ou null si échec
     */
    String createGoogleCalendarEvent(Booking booking);
    
    /**
     * Envoie l'email de confirmation de réservation
     * @param booking Réservation
     * @param user Utilisateur
     * @param salon Salon
     * @param services Services
     */
    void sendBookingConfirmationEmail(
            Booking booking,
            UserDTO user,
            SalonDTO salon,
            Set<ServiceDTO> services
    );
}