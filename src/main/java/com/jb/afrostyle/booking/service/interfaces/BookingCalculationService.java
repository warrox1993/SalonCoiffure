package com.jb.afrostyle.booking.service;

import com.jb.afrostyle.serviceoffering.payload.dto.ServiceDTO;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Service spécialisé pour les calculs liés aux réservations
 * Extrait de BookingServiceImpl pour respecter le principe Single Responsibility
 */
public interface BookingCalculationService {
    
    /**
     * Calcule la durée totale d'une réservation
     * @param services Services sélectionnés
     * @return Durée totale en minutes
     */
    int calculateTotalDuration(Set<ServiceDTO> services);
    
    /**
     * Calcule le prix total d'une réservation
     * @param services Services sélectionnés
     * @return Prix total
     */
    BigDecimal calculateTotalPrice(Set<ServiceDTO> services);
    
    /**
     * Calcule les données dérivées d'une réservation
     * @param services Services sélectionnés
     * @return Objet contenant durée et prix calculés
     */
    BookingCalculation calculateBookingMetrics(Set<ServiceDTO> services);
}