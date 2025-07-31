package com.jb.afrostyle.serviceoffering.controller;

import com.jb.afrostyle.serviceoffering.modal.ServiceOffering;
import com.jb.afrostyle.serviceoffering.payload.dto.ServiceDTO;
import com.jb.afrostyle.serviceoffering.service.ServiceOfferingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des services offerts par les salons
 * ENDPOINTS PUBLICS pour consulter les services
 */
@RestController
@RequestMapping("/api/service-offerings")
@RequiredArgsConstructor
public class ServiceOfferingController {

    private static final Logger log = LoggerFactory.getLogger(ServiceOfferingController.class);

    private final ServiceOfferingService serviceOfferingService;

    /**
     * Récupère tous les services, optionnellement filtrés par catégorie
     * MONO-SALON : Plus besoin de salonId car tous les services appartiennent au salon unique
     */
    @GetMapping
    public ResponseEntity<Set<ServiceOffering>> getAllServices(
            @RequestParam(required = false) Long categoryId
    ) {
        try {
            Set<ServiceOffering> serviceOfferings = serviceOfferingService
                    .getAllServicesByCategory(categoryId);

            return ResponseEntity.ok(serviceOfferings);

        } catch (Exception e) {
            log.error("Error fetching services: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère un service par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getServiceById(@PathVariable Long id) {
        try {
            ServiceOffering serviceOffering = serviceOfferingService.getServiceById(id);
            return ResponseEntity.ok(serviceOffering);

        } catch (Exception e) {
            log.error("Error fetching service {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }



    /**
     * 🆕 ENDPOINT MANQUANT : Récupère plusieurs services par leurs IDs
     * Utilisé par BookingService pour valider les services d'une réservation
     */
    @GetMapping("/list/{idsString}")
    public ResponseEntity<?> getServicesByIds(@PathVariable String idsString) {
        try {
            log.info("Fetching services for IDs: {}", idsString);

            // Validation du paramètre
            if (idsString == null || idsString.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Service IDs are required");
            }

            // Parser les IDs depuis la chaîne "1,2,3"
            Set<Long> ids;
            try {
                ids = Arrays.stream(idsString.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toSet());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body("Invalid service IDs format");
            }

            if (ids.isEmpty()) {
                return ResponseEntity.badRequest().body("At least one service ID is required");
            }

            // Récupérer les services
            Set<ServiceOffering> services = serviceOfferingService.getServicesByIds(ids);

            // Vérifier que tous les services ont été trouvés
            Set<Long> foundIds = services.stream()
                    .map(ServiceOffering::getId)
                    .collect(Collectors.toSet());

            Set<Long> missingIds = ids.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toSet());

            if (!missingIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Services not found for IDs: " + missingIds);
            }

            log.info("Successfully fetched {} services", services.size());
            return ResponseEntity.ok(services);

        } catch (Exception e) {
            log.error("Error fetching services for IDs {}: {}", idsString, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch services: " + e.getMessage());
        }
    }
    /**
     * Crée un nouveau service
     */
    @PostMapping
    public ResponseEntity<ServiceOffering> createService(@RequestBody ServiceDTO serviceDTO) {
        try {
            ServiceOffering created = serviceOfferingService.createService(serviceDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(created);
        } catch (Exception e) {
            log.error("Error creating service: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Met à jour un service existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServiceOffering> updateService(
            @PathVariable Long id,
            @RequestBody ServiceOffering serviceOffering
    ) {
        try {
            ServiceOffering updated = serviceOfferingService.updateService(id, serviceOffering);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Error updating service {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprime un service existant
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        try {
            serviceOfferingService.deleteService(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting service {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}