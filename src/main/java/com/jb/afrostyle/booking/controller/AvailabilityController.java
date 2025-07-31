package com.jb.afrostyle.booking.controller;

import com.jb.afrostyle.booking.domain.entity.SalonAvailability;
import com.jb.afrostyle.booking.dto.AvailabilityRequest;
import com.jb.afrostyle.booking.service.AvailabilityService;
import com.jb.afrostyle.booking.util.BookingResponseHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des créneaux de disponibilité
 * Mono-salon : Gestion des créneaux pour LE salon AfroStyle
 */
@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private static final Logger log = LoggerFactory.getLogger(AvailabilityController.class);

    private final AvailabilityService availabilityService;
    private final BookingResponseHandler responseHandler;

    /**
     * Crée un nouveau créneau de disponibilité
     * Réservé aux admins et staff du salon
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SALON_OWNER')")
    public ResponseEntity<?> createAvailability(@Valid @RequestBody AvailabilityRequest request) {
        return responseHandler.executeWithErrorHandling("createAvailability", () -> {
            SalonAvailability created = availabilityService.createAvailability(request);
            return created;
        }, "Availability created successfully");
    }

    /**
     * Met à jour un créneau existant
     * Réservé aux admins et staff du salon
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SALON_OWNER')")
    public ResponseEntity<?> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityRequest request) {
        // REFACTORISÉ : Utilise BookingResponseHandler
        return responseHandler.executeWithErrorHandling("updateAvailability", () -> {
            SalonAvailability updated = availabilityService.updateAvailability(id, request);
            return updated;
        }, "Availability updated successfully");
    }

    /**
     * Supprime un créneau de disponibilité
     * Réservé aux admins et staff du salon
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SALON_OWNER')")
    public ResponseEntity<?> deleteAvailability(@PathVariable Long id) {
        // REFACTORISÉ : Utilise BookingResponseHandler
        return responseHandler.executeWithErrorHandling("deleteAvailability", () -> {
            availabilityService.deleteAvailability(id);
            return "Availability deleted";
        }, "Availability deleted successfully");
    }

    /**
     * Récupère un créneau par son ID
     * Public pour consultation
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAvailabilityById(@PathVariable Long id) {
        return responseHandler.executeWithErrorHandling("getAvailabilityById", () -> {
            SalonAvailability availability = availabilityService.getAvailabilityById(id);
            return availability;
        });
    }

    /**
     * Récupère tous les créneaux du salon
     * Public pour consultation
     */
    @GetMapping
    public ResponseEntity<?> getAllAvailabilities() {
        return responseHandler.executeWithErrorHandling("getAllAvailabilities", () -> {
            List<SalonAvailability> availabilities = availabilityService.getAllAvailabilities();
            return availabilities;
        });
    }

    /**
     * Récupère les créneaux pour une date donnée
     * Public pour consultation
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<?> getAvailabilitiesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return responseHandler.executeWithErrorHandling("getAvailabilitiesByDate", () -> {
            List<SalonAvailability> availabilities = availabilityService.getAvailabilitiesByDate(date);
            return availabilities;
        });
    }

    /**
     * Récupère les créneaux disponibles pour une date donnée
     * Public pour réservation
     */
    @GetMapping("/available/{date}")
    public ResponseEntity<?> getAvailableSlotsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return responseHandler.executeWithErrorHandling("getAvailableSlotsByDate", () -> {
            List<SalonAvailability> availableSlots = availabilityService.getAvailableSlotsByDate(date);
            return availableSlots;
        });
    }

    /**
     * Récupère les créneaux entre deux dates
     * Public pour consultation
     */
    @GetMapping("/period")
    public ResponseEntity<?> getAvailabilitiesBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return responseHandler.executeWithErrorHandling("getAvailabilitiesBetweenDates", () -> {
            List<SalonAvailability> availabilities = availabilityService.getAvailabilitiesBetweenDates(startDate, endDate);
            return availabilities;
        });
    }
}