package com.jb.afrostyle.booking.service.impl;

import com.jb.afrostyle.booking.domain.entity.SalonAvailability;
import com.jb.afrostyle.booking.dto.AvailabilityRequest;
import com.jb.afrostyle.booking.repository.AvailabilityRepository;
import com.jb.afrostyle.booking.service.interfaces.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Implémentation du service de gestion des créneaux de disponibilité
 * Mono-salon : Gestion simplifiée pour LE salon AfroStyle
 */
@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(AvailabilityServiceImpl.class);

    private final AvailabilityRepository availabilityRepository;

    @Override
    @Transactional
    public SalonAvailability createAvailability(AvailabilityRequest request) throws Exception {
        log.info("Creating availability for date {} from {} to {}", 
                request.date(), request.startTime(), request.endTime());

        // Vérifier les chevauchements
        List<SalonAvailability> overlapping = availabilityRepository.findOverlappingAvailabilities(
                request.date(), request.startTime(), request.endTime());

        if (!overlapping.isEmpty()) {
            throw new Exception("Time slot conflicts with existing availability");
        }

        // Créer le nouveau créneau
        SalonAvailability availability = new SalonAvailability();
        availability.setDate(request.date());
        availability.setStartTime(request.startTime());
        availability.setEndTime(request.endTime());
        availability.setDescription(request.description());
        availability.setAvailable(true);

        SalonAvailability saved = availabilityRepository.save(availability);
        log.info("Availability created successfully with ID: {}", saved.getId());

        return saved;
    }

    @Override
    @Transactional
    public SalonAvailability updateAvailability(Long id, AvailabilityRequest request) throws Exception {
        log.info("Updating availability with ID: {}", id);

        SalonAvailability existing = availabilityRepository.findById(id)
                .orElseThrow(() -> new Exception("Availability not found with id: " + id));

        // Vérifier les chevauchements (en excluant le créneau actuel)
        List<SalonAvailability> overlapping = availabilityRepository.findOverlappingAvailabilities(
                request.date(), request.startTime(), request.endTime());
        
        overlapping.removeIf(a -> a.getId().equals(id)); // Exclure le créneau actuel

        if (!overlapping.isEmpty()) {
            throw new Exception("Time slot conflicts with existing availability");
        }

        // Mettre à jour
        existing.setDate(request.date());
        existing.setStartTime(request.startTime());
        existing.setEndTime(request.endTime());
        existing.setDescription(request.description());

        SalonAvailability updated = availabilityRepository.save(existing);
        log.info("Availability {} updated successfully", id);

        return updated;
    }

    @Override
    @Transactional
    public void deleteAvailability(Long id) throws Exception {
        log.info("Deleting availability with ID: {}", id);

        SalonAvailability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new Exception("Availability not found with id: " + id));

        availabilityRepository.delete(availability);
        log.info("Availability {} deleted successfully", id);
    }

    @Override
    public SalonAvailability getAvailabilityById(Long id) throws Exception {
        log.info("Fetching availability with ID: {}", id);

        return availabilityRepository.findById(id)
                .orElseThrow(() -> new Exception("Availability not found with id: " + id));
    }

    @Override
    public List<SalonAvailability> getAllAvailabilities() {
        log.info("Fetching all availabilities");
        return availabilityRepository.findAllByOrderByDateAscStartTimeAsc();
    }

    @Override
    public List<SalonAvailability> getAvailabilitiesByDate(LocalDate date) {
        log.info("Fetching availabilities for date: {}", date);
        return availabilityRepository.findByDate(date);
    }

    @Override
    public List<SalonAvailability> getAvailableSlotsByDate(LocalDate date) {
        log.info("Fetching available slots for date: {}", date);
        return availabilityRepository.findByDateAndIsAvailableTrue(date);
    }

    @Override
    public List<SalonAvailability> getAvailabilitiesBetweenDates(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching availabilities between {} and {}", startDate, endDate);
        return availabilityRepository.findByDateBetween(startDate, endDate);
    }
}