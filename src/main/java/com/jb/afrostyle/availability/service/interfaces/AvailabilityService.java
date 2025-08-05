package com.jb.afrostyle.booking.service.interfaces;

import com.jb.afrostyle.booking.domain.entity.SalonAvailability;
import com.jb.afrostyle.booking.dto.AvailabilityRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface du service de gestion des créneaux de disponibilité
 * Mono-salon : Gestion des créneaux pour LE salon AfroStyle
 */
public interface AvailabilityService {

    /**
     * Crée un nouveau créneau de disponibilité
     * @param request Données du créneau à créer
     * @return Créneau créé
     * @throws Exception si chevauchement ou erreur
     */
    SalonAvailability createAvailability(AvailabilityRequest request) throws Exception;

    /**
     * Met à jour un créneau existant
     * @param id ID du créneau à mettre à jour
     * @param request Nouvelles données
     * @return Créneau mis à jour
     * @throws Exception si créneau inexistant ou chevauchement
     */
    SalonAvailability updateAvailability(Long id, AvailabilityRequest request) throws Exception;

    /**
     * Supprime un créneau de disponibilité
     * @param id ID du créneau à supprimer
     * @throws Exception si créneau inexistant
     */
    void deleteAvailability(Long id) throws Exception;

    /**
     * Récupère un créneau par son ID
     * @param id ID du créneau
     * @return Créneau trouvé
     * @throws Exception si créneau inexistant
     */
    SalonAvailability getAvailabilityById(Long id) throws Exception;

    /**
     * Récupère tous les créneaux du salon
     * @return Liste de tous les créneaux
     */
    List<SalonAvailability> getAllAvailabilities();

    /**
     * Récupère les créneaux pour une date donnée
     * @param date Date recherchée
     * @return Liste des créneaux pour cette date
     */
    List<SalonAvailability> getAvailabilitiesByDate(LocalDate date);

    /**
     * Récupère les créneaux disponibles pour une date donnée
     * @param date Date recherchée
     * @return Liste des créneaux disponibles pour cette date
     */
    List<SalonAvailability> getAvailableSlotsByDate(LocalDate date);

    /**
     * Récupère les créneaux entre deux dates
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Liste des créneaux dans la période
     */
    List<SalonAvailability> getAvailabilitiesBetweenDates(LocalDate startDate, LocalDate endDate);
}