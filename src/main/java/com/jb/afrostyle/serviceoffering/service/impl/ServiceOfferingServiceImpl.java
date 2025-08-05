package com.jb.afrostyle.serviceoffering.service;

import com.jb.afrostyle.serviceoffering.modal.ServiceOffering;
import com.jb.afrostyle.serviceoffering.payload.dto.ServiceDTO;
import com.jb.afrostyle.serviceoffering.repository.ServiceOfferingRepository;
import com.jb.afrostyle.salon.service.SalonService;
import com.jb.afrostyle.core.validation.ValidationUtils;
import com.jb.afrostyle.core.exception.ExceptionUtils;
import com.jb.afrostyle.core.validation.ValidationResult;
import com.jb.afrostyle.core.enums.EntityType;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des services offerts
 * Version corrigée avec validation des salons via services locaux
 */
@Service
@RequiredArgsConstructor
public class ServiceOfferingServiceImpl implements ServiceOfferingService {

    private static final Logger log = LoggerFactory.getLogger(ServiceOfferingServiceImpl.class);

    private final ServiceOfferingRepository serviceOfferingRepository;
    private final SalonService salonService;

    /**
     * Crée un nouveau service
     */
    @Override
    @Transactional
    public ServiceOffering createService(ServiceDTO serviceDTO) {
        log.info("Creating service '{}'", serviceDTO.name());

        try {
            // Validation avec Java 21 Pattern Matching
            var nameValidation = ValidationUtils.validateStringLength(
                serviceDTO.name(), "Service name", 2, 100);
            var priceValidation = ValidationUtils.validateAmount(
                serviceDTO.price(), "EUR", "Service price");
                
            // PATTERN MODERNE : Validation avec méthodes utilitaires
            if (nameValidation.isError()) {
                throw ExceptionUtils.createValidationException(
                    ExceptionUtils.ValidationType.INVALID_FORMAT, "Service name", serviceDTO.name());
            }
            String validName = nameValidation.getValueOrThrow(); // Continue
            
            // PATTERN MODERNE : Validation avec méthodes utilitaires
            if (priceValidation.isError()) {
                throw ExceptionUtils.createValidationException(
                    ExceptionUtils.ValidationType.NEGATIVE_VALUE, "Service price", serviceDTO.price());
            }
            BigDecimal validPrice = priceValidation.getValueOrThrow(); // Continue
            
            // MONO-SALON : Création directe du service, plus de validation catégorie
            ServiceOffering serviceOffering = new ServiceOffering();
            serviceOffering.setLegacyImages(serviceDTO.images());
            serviceOffering.setName(serviceDTO.name());
            serviceOffering.setDescription(serviceDTO.description());
            serviceOffering.setPrice(serviceDTO.price());
            serviceOffering.setDuration(serviceDTO.duration());

            ServiceOffering savedService = serviceOfferingRepository.save(serviceOffering);
            log.info("Service created successfully with ID: {}", savedService.getId());
            return savedService;

        } catch (Exception e) {
            log.error("Failed to create service: {}", e.getMessage());
            throw new RuntimeException("Failed to create service: " + e.getMessage());
        }
    }

    /**
     * Met à jour un service existant avec validation pattern matching Java 21
     */
    @Override
    @Transactional
    public ServiceOffering updateService(Long serviceId, ServiceOffering service) throws Exception {
        log.info("Updating service with ID: {}", serviceId);
        
        // PATTERN MODERNE : Validation avec méthodes utilitaires
        var serviceIdValidation = ValidationUtils.validatePositiveId(serviceId, EntityType.SERVICE_OFFERING);
        if (serviceIdValidation.isError()) {
            log.error("Service ID validation failed: {}", serviceIdValidation.getErrorMessage());
            throw ExceptionUtils.createValidationException(
                ExceptionUtils.ValidationType.NEGATIVE_VALUE, "Service ID", serviceId);
        }
        
        Long validServiceId = serviceIdValidation.getValueOrThrow(); // Continue
        
        if (service == null) {
            throw ExceptionUtils.createValidationException(
                ExceptionUtils.ValidationType.NULL_VALUE, "Service update data", null);
        }

        ServiceOffering serviceOffering = serviceOfferingRepository.findById(serviceId)
            .map(foundService -> {
                log.debug("Service found for update: {}", foundService.getName());
                return foundService;
            })
            .orElseThrow(() -> (Exception) ExceptionUtils.createNotFoundExceptionWithLog(
                EntityType.SERVICE_OFFERING, serviceId, log));

        // Update fields conditionnellement avec Java 21 Pattern Matching
        if (service.getImages() != null) {
            log.debug("Updating service images");
            serviceOffering.setImages(service.getImages());
        }
        
        // Validation nom avec Pattern Matching
        if (service.getName() != null) {
            var nameValidation = ValidationUtils.validateStringLength(
                service.getName(), "Service name", 2, 100);
            // PATTERN MODERNE : Validation avec méthodes utilitaires
            if (nameValidation.isSuccess()) {
                String validName = nameValidation.getValueOrThrow();
                log.debug("Updating service name: {}", validName);
                serviceOffering.setName(validName);
            } else {
                log.warn("Invalid name provided: {}, keeping existing name", service.getName());
            }
        }
        
        if (service.getDescription() != null) {
            log.debug("Updating service description");
            serviceOffering.setDescription(service.getDescription());
        }
        
        // Validation prix avec Pattern Matching
        if (service.getPrice() != null) {
            var priceValidation = ValidationUtils.validateAmount(
                service.getPrice(), "EUR", "Service price");
            // PATTERN MODERNE : Validation avec méthodes utilitaires
            if (priceValidation.isSuccess()) {
                BigDecimal validPrice = priceValidation.getValueOrThrow();
                log.debug("Updating service price: {}", validPrice);
                serviceOffering.setPrice(validPrice);
            } else {
                log.warn("Invalid price provided: {}, keeping existing price", service.getPrice());
            }
        }
        
        // Validation durée avec Pattern Matching Java 21
        if (service.getDuration() != 0) {
            int duration = service.getDuration();
            if (duration > 0 && duration <= 480) { // Max 8h
                log.debug("Updating service duration: {} minutes", duration);
                serviceOffering.setDuration(duration);
            } else if (duration <= 0) {
                log.warn("Invalid duration provided: {}, keeping existing duration", duration);
            } else if (duration > 480) {
                log.warn("Duration too long: {} minutes, keeping existing duration", duration);
            }
        }

        ServiceOffering updatedService = serviceOfferingRepository.save(serviceOffering);

        log.info("Service {} updated successfully", serviceId);
        return updatedService;
    }

    /**
     * Récupère tous les services du salon
     */
    @Override
    public Set<ServiceOffering> getAllServices() {
        log.info("Fetching all services");

        // MONO-SALON : Tous les services du salon unique
        Set<ServiceOffering> services = Set.copyOf(serviceOfferingRepository.findAll());

        log.info("Found {} services", services.size());
        return services;
    }
    
    // =========================
    // NOUVELLES MÉTHODES JPA OPTIMISÉES - ARCHITECTURE TFE
    // =========================
    
    /**
     * Récupère tous les services actifs avec leurs relations (tags + images).
     * Performance ultra-optimisée pour l'affichage public.
     * 
     * @return Services actifs avec relations chargées
     */
    public List<ServiceOffering> getActiveServicesWithRelations() {
        log.info("Fetching active services with relations for public display");
        return serviceOfferingRepository.findActiveWithRelations();
    }
    
    /**
     * Récupère un service avec ses relations (tags + images).
     * Performance optimisée - évite les requêtes N+1.
     * 
     * @param id ID du service
     * @return Service avec relations chargées
     * @throws Exception Si service introuvable
     */
    public ServiceOffering getServiceWithRelations(Long id) throws Exception {
        log.debug("Fetching service with relations - ID: {}", id);
        
        var validationResult = ValidationUtils.validatePositiveId(id, EntityType.SERVICE_OFFERING);
        if (validationResult.isError()) {
            throw ExceptionUtils.createValidationException(
                ExceptionUtils.ValidationType.NEGATIVE_VALUE, "Service ID", id);
        }
        
        Long validId = validationResult.getValueOrThrow();
        return serviceOfferingRepository.findByIdWithRelations(validId)
                .map(service -> {
                    log.debug("Service with relations found: {} (tags: {}, images: {})", 
                             service.getName(), service.getTags().size(), service.getImages().size());
                    return service;
                })
                .orElseThrow(() -> {
                    log.warn("Service not found with ID: {}", validId);
                    return ExceptionUtils.createNotFoundException(EntityType.SERVICE_OFFERING, validId);
                });
    }
    
    /**
     * Récupère plusieurs services avec leurs relations (ultra-optimisé).
     * Performance maximale - 1 seule requête pour tout charger.
     * 
     * @param ids IDs des services
     * @return Services avec relations chargées
     */
    public List<ServiceOffering> getServicesByIdsWithRelations(Set<Long> ids) {
        log.info("Fetching {} services with relations (ultra-optimized)", ids.size());
        
        var idsValidation = ValidationUtils.validateNotEmptyCollection(ids, "Service IDs");
        if (idsValidation.isError()) {
            log.warn("Invalid service IDs: {}", idsValidation.getErrorMessage());
            return List.of();
        }
        
        // PERFORMANCE MAXIMALE : 1 requête pour services + tags + images
        List<ServiceOffering> services = serviceOfferingRepository.findByIdInWithRelations(ids);
        
        log.info("Found {} services with full relations loaded", services.size());
        return services;
    }

    /**
     * CORRIGÉ : Récupère plusieurs services par leurs IDs avec validation pattern matching
     * Anciennement retournait HashSet<ServiceOffering>, maintenant Set<ServiceOffering>
     */
    @Override
    public Set<ServiceOffering> getServiceById(Set<Long> ids) {
        log.info("Fetching services for IDs: {}", ids);

        // Validation avec Java 21 Pattern Matching
        var idsValidation = ValidationUtils.validateNotEmptyCollection(ids, "Service IDs");
        // PATTERN MODERNE : Validation avec méthodes utilitaires
        if (idsValidation.isError()) {
            log.warn("Service IDs validation failed: {}", idsValidation.getErrorMessage());
            return new HashSet<>();
        }
        
        Set<Long> validIds = new HashSet<>(idsValidation.getValueOrThrow());
        
        // Validate all IDs are positive avec Stream et Pattern Matching
        var filteredIds = validIds.stream()
            .filter(id -> switch (id) {
                case null -> {
                    log.warn("Null ID found in service IDs set, skipping");
                    yield false;
                }
                case Long idValue when idValue <= 0 -> {
                    log.warn("Invalid service ID found: {}, skipping", idValue);
                    yield false;
                }
                case Long validId -> true;
            })
            .collect(Collectors.toSet());
            
        if (filteredIds.isEmpty()) {
            log.warn("No valid service IDs after filtering");
            return new HashSet<>();
        }
        
        List<ServiceOffering> services = serviceOfferingRepository.findAllById(filteredIds);
        Set<ServiceOffering> result = new HashSet<>(services);
        
        log.info("Found {} services out of {} requested", result.size(), filteredIds.size());
        return result;
    }

    /**
     * Récupère un service par son ID avec validation pattern matching
     */
    @Override
    public ServiceOffering getServiceById(Long id) {
        log.info("Fetching service with ID: {}", id);
        
        // Validation avec Java 21 Pattern Matching
        var idValidation = ValidationUtils.validatePositiveId(id, EntityType.SERVICE_OFFERING);
        // PATTERN MODERNE : Validation avec méthodes utilitaires
        if (idValidation.isError()) {
            log.error("Service ID validation failed: {}", idValidation.getErrorMessage());
            throw ExceptionUtils.createValidationException(
                ExceptionUtils.ValidationType.NEGATIVE_VALUE, "Service ID", id);
        }
        
        Long validId = idValidation.getValueOrThrow(); // Continue

        ServiceOffering serviceOffering = serviceOfferingRepository.findById(id)
            .map(foundService -> {
                log.debug("Service found: {}", foundService.getName());
                return foundService;
            })
            .orElseThrow(() -> (RuntimeException) ExceptionUtils.createNotFoundExceptionWithLog(
                EntityType.SERVICE_OFFERING, id, log));
        return serviceOffering;
    }

    /**
     * OPTIMISÉ : Récupère plusieurs services par leurs IDs (performance JPA).
     * Remplace N requêtes individuelles par 1 seule requête IN.
     */
    @Override
    public Set<ServiceOffering> getServicesByIds(Set<Long> ids) {
        log.info("Fetching {} services with optimized query", ids.size());
        
        // Validation avec Java 21 Pattern Matching
        var idsValidation = ValidationUtils.validateNotEmptyCollection(ids, "Service IDs");
        if (idsValidation.isError()) {
            log.warn("Invalid service IDs provided: {}", idsValidation.getErrorMessage());
            return new HashSet<>();
        }
        
        // OPTIMISATION JPA : 1 requête IN au lieu de N requêtes individuelles
        List<ServiceOffering> services = serviceOfferingRepository.findByIdIn(ids);
        
        log.info("Found {} services out of {} requested", services.size(), ids.size());
        return new HashSet<>(services);
    }

    /**
     * Récupère un service par son ID (alias pour compatibilité)
     */
    @Override
    public ServiceOffering getServiceOfferingById(Long id) {
        return getServiceById(id); // Déléguer à la méthode existante
    }

    /**
     * Supprime un service par son ID avec validation pattern matching
     */
    @Override
    @Transactional
    public void deleteService(Long id) throws Exception {
        log.info("Deleting service with ID: {}", id);
        
        // Validation avec Java 21 Pattern Matching
        var idValidation = ValidationUtils.validatePositiveId(id, EntityType.SERVICE_OFFERING);
        // PATTERN MODERNE : Validation avec méthodes utilitaires
        if (idValidation.isError()) {
            log.error("Service ID validation failed for deletion: {}", idValidation.getErrorMessage());
            throw ExceptionUtils.createValidationException(
                ExceptionUtils.ValidationType.NEGATIVE_VALUE, "Service ID", id);
        }
        
        Long validId = idValidation.getValueOrThrow(); // Continue

        ServiceOffering serviceOffering = serviceOfferingRepository.findById(id)
            .map(foundService -> {
                log.info("Deleting service: {} (ID: {})", foundService.getName(), id);
                return foundService;
            })
            .orElseThrow(() -> (Exception) ExceptionUtils.createNotFoundExceptionWithLog(
                EntityType.SERVICE_OFFERING, id, log));
        serviceOfferingRepository.delete(serviceOffering);
        log.info("Service {} deleted successfully", id);
    }
}