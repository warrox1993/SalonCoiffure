package com.jb.afrostyle.salon.controller;

import com.jb.afrostyle.salon.payload.dto.SalonDTO;
import com.jb.afrostyle.salon.service.SalonService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour la gestion des paramètres du salon unique
 * Version mono-salon - remplace SalonController
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settings")
public class SettingsController {

    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);

    private final SalonService salonService;

    /**
     * Récupère les paramètres du salon unique
     */
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<SalonDTO> getSalonSettings() {
        log.info("Fetching salon settings");
        
        SalonDTO salon = salonService.getSalonSettings();
        return ResponseEntity.ok(salon);
    }

    /**
     * Met à jour les paramètres du salon (réservé aux ADMIN)
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SalonDTO> updateSalonSettings(
            @Valid @RequestBody SalonDTO salonDTO,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        log.info("Updating salon settings by user: {}", userId);
        
        SalonDTO updatedSalon = salonService.updateSalonSettings(salonDTO);
        return ResponseEntity.ok(updatedSalon);
    }

    /**
     * Vérifier si le salon est configuré
     */
    @GetMapping("/configured")
    public ResponseEntity<Boolean> isSalonConfigured() {
        log.info("Checking if salon is configured");
        
        try {
            SalonDTO salon = salonService.getSalonSettings();
            boolean isConfigured = salon != null && salon.name() != null && !salon.name().trim().isEmpty();
            return ResponseEntity.ok(isConfigured);
        } catch (Exception e) {
            log.warn("Salon not configured: {}", e.getMessage());
            return ResponseEntity.ok(false);
        }
    }
}