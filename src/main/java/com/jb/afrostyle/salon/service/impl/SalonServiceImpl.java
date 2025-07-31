package com.jb.afrostyle.salon.service.impl;

import com.jb.afrostyle.salon.exception.SalonNotFoundException;
import com.jb.afrostyle.salon.mapper.SalonMapper;
import com.jb.afrostyle.salon.modal.Salon;
import com.jb.afrostyle.salon.payload.dto.SalonDTO;
import com.jb.afrostyle.salon.repository.SalonRepository;
import com.jb.afrostyle.salon.service.SalonService;
import com.jb.afrostyle.core.validation.ValidationUtils;
import com.jb.afrostyle.core.exception.ExceptionUtils;
import com.jb.afrostyle.core.validation.ValidationResult;
import com.jb.afrostyle.core.enums.EntityType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

/**
 * Implémentation du service salon pour le modèle mono-salon
 * Version simplifiée - gestion d'un salon unique
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SalonServiceImpl implements SalonService {

    private static final Logger log = LoggerFactory.getLogger(SalonServiceImpl.class);
    
    private final SalonRepository salonRepository;
    
    private static final Long SALON_UNIQUE_ID = 1L; // ID fixe pour le salon unique

    /**
     * Initialise le salon par défaut au démarrage de l'application
     */
    @PostConstruct
    @Transactional
    public void initDefaultSalon() {
        // Vérifier s'il existe au moins un salon
        if (salonRepository.count() == 0) {
            log.info("Creating default salon at startup");
            try {
                createDefaultSalon();
            } catch (Exception e) {
                log.warn("Could not create default salon during startup (may already exist): {}", e.getMessage());
            }
        } else {
            log.info("Default salon already exists");
        }
    }

    /**
     * Récupère les paramètres du salon unique avec pattern matching
     */
    @Override
    public synchronized SalonDTO getSalonSettings() {
        log.info("Fetching salon settings");
        
        // Récupérer le premier salon existant (mono-salon)
        Salon salon = salonRepository.findAll().stream().findFirst()
            .map(foundSalon -> {
                log.debug("Salon found: {}", foundSalon.getName());
                return foundSalon;
            })
            .orElseGet(() -> {
                log.info("No salon found, creating default salon");
                return createDefaultSalon();
            });
                
        return SalonMapper.mapToDTO(salon);
    }

    /**
     * Met à jour les paramètres du salon unique avec validation pattern matching
     */
    @Override
    public SalonDTO updateSalonSettings(SalonDTO salonDTO) {
        log.info("Updating salon settings");
        
        // Validation avec Java 21 Pattern Matching
        if (salonDTO == null) {
            throw ExceptionUtils.createValidationException(
                ExceptionUtils.ValidationType.NULL_VALUE, "Salon settings", null);
        }
        
        // Récupérer le premier salon existant (mono-salon)
        Salon existingSalon = salonRepository.findAll().stream().findFirst()
            .map(salon -> {
                log.debug("Existing salon found for update: {}", salon.getName());
                return salon;
            })
            .orElseGet(() -> {
                log.info("No salon found for update, creating default salon");
                return createDefaultSalon();
            });
        
        // Mise à jour des champs avec Java 21 Pattern Matching
        if (salonDTO.name() != null) {
            var nameValidation = ValidationUtils.validateStringLength(
                salonDTO.name(), "Salon name", 2, 100);
            switch (nameValidation) {
                case ValidationResult.Success(var validName) -> {
                    log.debug("Updating salon name: {}", validName);
                    existingSalon.setName(validName);
                }
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                    log.warn("Invalid salon name provided: {}, keeping existing", salonDTO.name());
                }
            }
        }
        
        if (salonDTO.address() != null) {
            var addressValidation = ValidationUtils.validateNotNullOrEmpty(
                salonDTO.address(), "Salon address");
            switch (addressValidation) {
                case ValidationResult.Success(var validAddress) -> {
                    log.debug("Updating salon address");
                    existingSalon.setAddress(validAddress);
                }
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                    log.warn("Invalid address provided, keeping existing");
                }
            }
        }
        
        if (salonDTO.city() != null) {
            var cityValidation = ValidationUtils.validateStringLength(
                salonDTO.city(), "Salon city", 2, 50);
            switch (cityValidation) {
                case ValidationResult.Success(var validCity) -> {
                    log.debug("Updating salon city: {}", validCity);
                    existingSalon.setCity(validCity);
                }
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                    log.warn("Invalid city provided: {}, keeping existing", salonDTO.city());
                }
            }
        }
        
        if (salonDTO.phone() != null) {
            var phoneValidation = ValidationUtils.validatePhoneNumber(salonDTO.phone());
            switch (phoneValidation) {
                case ValidationResult.Success(var validPhone) -> {
                    log.debug("Updating salon phone: {}", validPhone);
                    existingSalon.setPhone(validPhone);
                }
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                    log.warn("Invalid phone provided: {}, keeping existing", salonDTO.phone());
                }
            }
        }
        
        if (salonDTO.email() != null) {
            var emailValidation = ValidationUtils.validateEmail(salonDTO.email());
            switch (emailValidation) {
                case ValidationResult.Success(var validEmail) -> {
                    log.debug("Updating salon email: {}", validEmail);
                    existingSalon.setEmail(validEmail);
                }
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                    log.warn("Invalid email provided: {}, keeping existing", salonDTO.email());
                }
            }
        }
        
        if (salonDTO.images() != null) {
            log.debug("Updating salon images");
            existingSalon.setImages(salonDTO.images());
        }
        
        // Validation heures d'ouverture avec Java 21 Pattern Matching
        if (salonDTO.openTime() != null) {
            var openTimeValidation = ValidationUtils.validateBusinessHours(
                salonDTO.openTime(), "Open time");
            switch (openTimeValidation) {
                case ValidationResult.Success(var validOpenTime) -> {
                    log.debug("Updating salon open time: {}", validOpenTime);
                    existingSalon.setOpenTime(validOpenTime);
                }
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                    log.warn("Invalid open time provided: {}, keeping existing", salonDTO.openTime());
                }
            }
        }
        
        if (salonDTO.closeTime() != null) {
            var closeTimeValidation = ValidationUtils.validateBusinessHours(
                salonDTO.closeTime(), "Close time");
            switch (closeTimeValidation) {
                case ValidationResult.Success(var validCloseTime) -> {
                    // Valider que l'heure de fermeture est après l'heure d'ouverture
                    LocalTime currentOpenTime = existingSalon.getOpenTime();
                    if (currentOpenTime != null) {
                        var timeRangeValidation = ValidationUtils.validateTimeRange(
                            currentOpenTime, validCloseTime);
                        switch (timeRangeValidation) {
                            case ValidationResult.Success(var validTimeRange) -> {
                                log.debug("Updating salon close time: {}", validTimeRange.endTime());
                                existingSalon.setCloseTime(validTimeRange.endTime());
                            }
                            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                                log.warn("Close time must be after open time, keeping existing");
                            }
                        }
                    } else {
                        log.debug("Updating salon close time: {}", validCloseTime);
                        existingSalon.setCloseTime(validCloseTime);
                    }
                }
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                    log.warn("Invalid close time provided: {}, keeping existing", salonDTO.closeTime());
                }
            }
        }
        
        // Mise à jour coordonnées GPS
        if (salonDTO.latitude() != null) {
            log.debug("Updating salon latitude: {}", salonDTO.latitude());
            existingSalon.setLatitude(salonDTO.latitude());
        }
        
        if (salonDTO.longitude() != null) {
            log.debug("Updating salon longitude: {}", salonDTO.longitude());
            existingSalon.setLongitude(salonDTO.longitude());
        }
        
        if (salonDTO.googlePlaceId() != null) {
            log.debug("Updating salon Google Place ID");
            existingSalon.setGooglePlaceId(salonDTO.googlePlaceId());
        }
        
        if (salonDTO.formattedAddress() != null) {
            log.debug("Updating salon formatted address");
            existingSalon.setFormattedAddress(salonDTO.formattedAddress());
        }
        
        Salon updatedSalon = salonRepository.save(existingSalon);
        
        log.info("Salon settings updated successfully");
        return SalonMapper.mapToDTO(updatedSalon);
    }

    /**
     * Vérifie si le salon est configuré
     */
    @Override
    public boolean isSalonConfigured() {
        return salonRepository.count() > 0;
    }

    /**
     * Récupère le salon par son ID avec validation pattern matching
     */
    @Override
    public SalonDTO getSalonById(Long salonId) {
        log.info("Fetching salon by id: {}", salonId);
        
        // Validation avec Java 21 Pattern Matching
        var idValidation = ValidationUtils.validatePositiveId(salonId, EntityType.SALON);
        switch (idValidation) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.error("Salon ID validation failed: {}", message);
                throw (SalonNotFoundException) ExceptionUtils.createNotFoundExceptionWithLog(
                    EntityType.SALON, salonId, log);
            }
            case ValidationResult.Success(var validId) -> { /* Continue */ }
        }
        
        Salon salon = salonRepository.findById(salonId)
            .map(foundSalon -> {
                log.debug("Salon found: {}", foundSalon.getName());
                return foundSalon;
            })
            .orElseThrow(() -> (SalonNotFoundException) ExceptionUtils.createNotFoundExceptionWithLog(
                EntityType.SALON, salonId, log));
                
        return SalonMapper.mapToDTO(salon);
    }

    /**
     * Crée un salon par défaut si aucun n'existe
     */
    private Salon createDefaultSalon() {
        log.info("Creating default salon settings");
        
        Salon defaultSalon = new Salon();
        // NE PAS forcer l'ID - laisser Hibernate le générer automatiquement
        defaultSalon.setName("AfroStyle Salon");
        defaultSalon.setAddress("123 Rue Example");
        defaultSalon.setCity("Bruxelles");
        defaultSalon.setPhone("+32123456789");
        defaultSalon.setEmail("contact@afrostyle.be");
        defaultSalon.setOpenTime(LocalTime.of(9, 0));
        defaultSalon.setCloseTime(LocalTime.of(18, 0));
        
        Salon savedSalon = salonRepository.save(defaultSalon);
        log.info("Default salon created with ID: {}", savedSalon.getId());
        return savedSalon;
    }

    /**
     * Récupère le salon par un ID de service (mono-salon: retourne toujours le salon unique)
     */
    @Override  
    public synchronized SalonDTO getSalonByServiceId(Long serviceId) {
        log.info("Fetching salon for service ID: {} (mono-salon: returning unique salon)", serviceId);
        return getSalonSettings();
    }

    /**
     * Récupère tous les salons avec pagination (mono-salon: retourne le salon unique)
     */
    @Override
    public Page<SalonDTO> getAllSalons(Pageable pageable) {
        log.info("Fetching all salons (mono-salon: returning unique salon)");
        SalonDTO salonDTO = getSalonSettings();
        return new PageImpl<>(Collections.singletonList(salonDTO), pageable, 1);
    }

    /**
     * Récupère l'entité Salon par ID avec validation pattern matching (pour GoogleMapsService)
     */
    @Override
    public Salon getSalonEntity(Long salonId) {
        log.info("Fetching salon entity by id: {}", salonId);
        
        // Validation avec Java 21 Pattern Matching
        var idValidation = ValidationUtils.validatePositiveId(salonId, EntityType.SALON);
        switch (idValidation) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.error("Salon entity ID validation failed: {}", message);
                throw (SalonNotFoundException) ExceptionUtils.createNotFoundExceptionWithLog(
                    EntityType.SALON, salonId, log);
            }
            case ValidationResult.Success(var validId) -> { /* Continue */ }
        }
        
        Salon salon = salonRepository.findById(salonId)
            .map(foundSalon -> {
                log.debug("Salon entity found: {}", foundSalon.getName());
                return foundSalon;
            })
            .orElseThrow(() -> (SalonNotFoundException) ExceptionUtils.createNotFoundExceptionWithLog(
                EntityType.SALON, salonId, log));
        return salon;
    }

    /**
     * Sauvegarde l'entité Salon avec validation pattern matching (pour GoogleMapsService)  
     */
    @Override
    public Salon saveSalon(Salon salon) {
        // Validation avec Java 21 Pattern Matching
        if (salon == null) {
            throw ExceptionUtils.createValidationException(
                ExceptionUtils.ValidationType.NULL_VALUE, "Salon entity", null);
        }
        
        var nameValidation = ValidationUtils.validateStringLength(
            salon.getName(), "Salon name", 2, 100);
        switch (nameValidation) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> {
                log.error("Salon name validation failed: {}", message);
                throw ExceptionUtils.createValidationException(
                    ExceptionUtils.ValidationType.INVALID_FORMAT, "Salon name", salon.getName());
            }
            case ValidationResult.Success(var validName) -> {
                log.info("Saving salon entity: {}", validName);
                Salon savedSalon = salonRepository.save(salon);
                log.debug("Salon entity saved with ID: {}", savedSalon.getId());
                return savedSalon;
            }
        }
    }
}