package com.jb.afrostyle.booking.domain.entity;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.stereotype.Component;

/**
 * Listener pour gérer automatiquement les calculs sur l'entité Booking
 */
@Component
public class BookingEntityListener {

    /**
     * Calcule automatiquement le nombre total de services
     * avant chaque sauvegarde ou mise à jour
     */
    @PrePersist
    @PreUpdate
    public void calculateTotalServices(Booking booking) {
        if (booking.getServiceIds() != null && !booking.getServiceIds().isEmpty()) {
            booking.setTotalServices(booking.getServiceIds().size());
        } else {
            // Si pas de serviceIds, on utilise serviceId (ancien système)
            booking.setTotalServices((booking.getServiceId() != null) ? 1 : 0);
        }
    }

    /**
     * Autres calculs automatiques peuvent être ajoutés ici
     * Par exemple : calculer le prix total, durée totale, etc.
     */
    @PrePersist
    @PreUpdate
    public void calculateOtherFields(Booking booking) {
        // Exemple : vous pourriez calculer automatiquement la durée
        // ou d'autres champs dérivés ici
    }
}