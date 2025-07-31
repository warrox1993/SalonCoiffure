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
                
            switch (nameValidation) {
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                    throw ExceptionUtils.createValidationException(
                        ExceptionUtils.ValidationType.INVALID_FORMAT, "Service name", serviceDTO.name());
                }
                case ValidationResult.Success(var validName) -> { /* Continue */ }
            }
            
            switch (priceValidation) {
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                    throw ExceptionUtils.createValidationException(
                        ExceptionUtils.ValidationType.NEGATIVE_VALUE, "Service price", serviceDTO.price());
                }
                case ValidationResult.Success(var validPrice) -> { /* Continue */ }
            }
            
            // MONO-SALON : Création directe du service, plus de validation catégorie
            ServiceOffering serviceOffering = new ServiceOffering();
            serviceOffering.setImages(serviceDTO.images());
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
        
        // Validation avec Java 21 Pattern Matching
        var serviceIdValidation = ValidationUtils.validatePositiveId(serviceId, EntityType.SERVICE_OFFERING);
        switch (serviceIdValidation) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.error("Service ID validation failed: {}", message);
                throw ExceptionUtils.createValidationException(
                    ExceptionUtils.ValidationType.NEGATIVE_VALUE, "Service ID", serviceId);
            }
            case ValidationResult.Success(var validServiceId) -> { /* Continue */ }
        }
        
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
            switch (nameValidation) {
                case ValidationResult.Success(var validName) -> {
                    log.debug("Updating service name: {}", validName);
                    serviceOffering.setName(validName);
                }
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                    log.warn("Invalid name provided: {}, keeping existing name", service.getName());
                }
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
            switch (priceValidation) {
                case ValidationResult.Success(var validPrice) -> {
                    log.debug("Updating service price: {}", validPrice);
                    serviceOffering.setPrice(validPrice);
                }
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                    log.warn("Invalid price provided: {}, keeping existing price", service.getPrice());
                }
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
    public Set<ServiceOffering> getAllServicesByCategory(Long categoryId) {
        log.info("Fetching all services (category filter ignored)");

        // MONO-SALON : Plus de filtre par catégorie, on retourne tous les services
        Set<ServiceOffering> services = Set.copyOf(serviceOfferingRepository.findAll());

        log.info("Found {} services", services.size());
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
        return switch (idsValidation) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.warn("Service IDs validation failed: {}", message);
                yield new HashSet<>();
            }
            case ValidationResult.Success(var validIds) -> {
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
                    yield new HashSet<>();
                }
                
                List<ServiceOffering> services = serviceOfferingRepository.findAllById(filteredIds);
                Set<ServiceOffering> result = new HashSet<>(services);
                
                log.info("Found {} services out of {} requested", result.size(), filteredIds.size());
                yield result;
            }
        };
    }

    /**
     * Récupère un service par son ID avec validation pattern matching
     */
    @Override
    public ServiceOffering getServiceById(Long id) {
        log.info("Fetching service with ID: {}", id);
        
        // Validation avec Java 21 Pattern Matching
        var idValidation = ValidationUtils.validatePositiveId(id, EntityType.SERVICE_OFFERING);
        switch (idValidation) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.error("Service ID validation failed: {}", message);
                throw ExceptionUtils.createValidationException(
                    ExceptionUtils.ValidationType.NEGATIVE_VALUE, "Service ID", id);
            }
            case ValidationResult.Success(var validId) -> { /* Continue */ }
        }

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
     * Récupère plusieurs services par leurs IDs (méthode alternative)
     */
    @Override
    public Set<ServiceOffering> getServicesByIds(Set<Long> ids) {
        return getServiceById(ids); // Déléguer à la méthode corrigée ci-dessus
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
        switch (idValidation) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.error("Service ID validation failed for deletion: {}", message);
                throw ExceptionUtils.createValidationException(
                    ExceptionUtils.ValidationType.NEGATIVE_VALUE, "Service ID", id);
            }
            case ValidationResult.Success(var validId) -> { /* Continue */ }
        }

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