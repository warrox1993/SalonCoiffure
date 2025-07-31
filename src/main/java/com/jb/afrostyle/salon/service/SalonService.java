package com.jb.afrostyle.salon.service;

import com.jb.afrostyle.salon.payload.dto.SalonDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service pour la gestion du salon unique
 * Version mono-salon - Interface simplifiée
 */
public interface SalonService {
    
    /**
     * Récupère les paramètres du salon unique
     */
    SalonDTO getSalonSettings();

    /**
     * Met à jour les paramètres du salon unique
     */
    SalonDTO updateSalonSettings(SalonDTO salonDTO);

    /**
     * Vérifie si le salon est configuré
     */
    boolean isSalonConfigured();

    /**
     * Récupère le salon par son ID (pour compatibilité)
     */
    SalonDTO getSalonById(Long salonId);

    /**
     * Récupère le salon par un ID de service (mono-salon: retourne toujours le salon unique)
     */
    SalonDTO getSalonByServiceId(Long serviceId);

    /**
     * Récupère tous les salons avec pagination (mono-salon: retourne le salon unique)
     */
    Page<SalonDTO> getAllSalons(Pageable pageable);

    /**
     * Récupère l'entité Salon par ID (pour GoogleMapsService)
     */
    com.jb.afrostyle.salon.modal.Salon getSalonEntity(Long salonId);

    /**
     * Sauvegarde l'entité Salon (pour GoogleMapsService)
     */
    com.jb.afrostyle.salon.modal.Salon saveSalon(com.jb.afrostyle.salon.modal.Salon salon);
}