package com.jb.afrostyle.serviceoffering.service;

import com.jb.afrostyle.serviceoffering.modal.ServiceOffering;
import com.jb.afrostyle.serviceoffering.payload.dto.ServiceDTO;

import java.util.Set;

/**
 * Interface du service de gestion des services offerts par les salons
 * Version corrigée avec signatures cohérentes
 */
public interface ServiceOfferingService {

    /**
     * Crée un nouveau service
     * @param serviceDTO Données du service à créer
     * @return Service créé
     */
    ServiceOffering createService(ServiceDTO serviceDTO);

    /**
     * Met à jour un service existant
     * @param serviceId ID du service à mettre à jour
     * @param service Nouvelles données du service
     * @return Service mis à jour
     * @throws Exception si le service n'existe pas
     */
    ServiceOffering updateService(Long serviceId, ServiceOffering service) throws Exception;

    /**
     * Récupère tous les services, optionnellement filtrés par catégorie
     * @param categoryId ID de la catégorie (optionnel)
     * @return Set des services
     */
    Set<ServiceOffering> getAllServicesByCategory(Long categoryId);

    /**
     * Récupère plusieurs services par leurs IDs
     * @param ids Set des IDs des services à récupérer
     * @return Set des services trouvés
     */
    Set<ServiceOffering> getServiceById(Set<Long> ids);

    /**
     * Récupère un service par son ID
     * @param id ID du service à récupérer
     * @return Service trouvé
     * @throws RuntimeException si le service n'existe pas
     */
    ServiceOffering getServiceById(Long id);

    /**
     * Récupère plusieurs services par leurs IDs (méthode alternative)
     * @param ids Set des IDs des services à récupérer
     * @return Set des services trouvés
     */
    Set<ServiceOffering> getServicesByIds(Set<Long> ids);

    /**
     * Récupère un service par son ID (alias pour compatibilité)
     * @param id ID du service à récupérer
     * @return Service trouvé
     */
    ServiceOffering getServiceOfferingById(Long id);

    /**
     * Supprime un service par son ID
     * @param id ID du service à supprimer
     * @throws Exception si le service n'existe pas
     */
    void deleteService(Long id) throws Exception;
}