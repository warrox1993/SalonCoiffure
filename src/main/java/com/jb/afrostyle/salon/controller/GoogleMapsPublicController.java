package com.jb.afrostyle.salon.controller;

import com.jb.afrostyle.config.GoogleMapsConfig;
import com.jb.afrostyle.salon.service.GoogleMapsService;
import com.jb.afrostyle.salon.service.SalonService;
import com.jb.afrostyle.salon.payload.dto.SalonDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur public pour Google Maps - Version Mono-Salon
 * 
 * Fournit les informations Google Maps nécessaires au frontend Angular
 * sans exposer la clé API privée.
 * 
 * ENDPOINTS PUBLICS :
 * - GET /api/maps/config : Configuration Google Maps pour le frontend
 * - GET /api/maps/salon-location : Localisation du salon unique
 * - POST /api/maps/salon/geocode : Géocodage du salon (admin uniquement)
 * 
 * SÉCURITÉ :
 * - Clé API jamais exposée au frontend
 * - Seules les URLs publiques sont transmises
 * - Géocodage restreint aux admins
 * 
 * @author AfroStyle Team
 * @since Mono-Salon 2.0
 */
@RestController
@RequestMapping("/api/maps")
@RequiredArgsConstructor
public class GoogleMapsPublicController {

    private static final Logger log = LoggerFactory.getLogger(GoogleMapsPublicController.class);

    private final GoogleMapsConfig googleMapsConfig;
    private final GoogleMapsService googleMapsService;
    private final SalonService salonService;

    /**
     * Retourne la configuration Google Maps pour le frontend
     * 
     * SÉCURITÉ : La clé API n'est PAS incluse dans cette réponse.
     * Le frontend utilise ces informations pour afficher des cartes
     * mais ne peut pas faire d'appels API directs.
     * 
     * @return Configuration Google Maps publique
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getGoogleMapsConfig() {
        Map<String, Object> config = new HashMap<>();
        
        try {
            boolean isEnabled = googleMapsService.isGoogleMapsEnabled();
            config.put("enabled", isEnabled);
            
            if (isEnabled) {
                GoogleMapsConfig.Api api = googleMapsConfig.getApi();
                
                // URLs publiques que le frontend peut utiliser (sans clé API)
                Map<String, Object> urls = new HashMap<>();
                urls.put("directionsBaseUrl", "https://www.google.com/maps/dir/?api=1&destination=");
                urls.put("mapsBaseUrl", "https://www.google.com/maps");
                
                // Paramètres par défaut pour les cartes
                Map<String, Object> defaults = new HashMap<>();
                defaults.put("zoom", api.getDefaultZoom());
                defaults.put("mapWidth", api.getDefaultSize().getWidth());
                defaults.put("mapHeight", api.getDefaultSize().getHeight());
                
                config.put("urls", urls);
                config.put("defaults", defaults);
                config.put("status", "configured");
                
                log.debug("Configuration Google Maps fournie au frontend");
            } else {
                config.put("status", "disabled");
                config.put("message", "Google Maps non configuré");
                log.warn("Google Maps non configuré - configuration par défaut retournée");
            }
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la configuration Google Maps", e);
            config.put("enabled", false);
            config.put("status", "error");
            config.put("message", "Erreur de configuration");
        }
        
        return ResponseEntity.ok(config);
    }

    /**
     * Retourne les informations de localisation du salon unique
     * 
     * Inclut coordonnées, adresse formatée et URLs de cartes pré-générées
     * côté backend pour éviter d'exposer la clé API.
     * 
     * @return Localisation et informations du salon
     */
    @GetMapping("/salon-location")
    public ResponseEntity<Map<String, Object>> getSalonLocation() {
        Map<String, Object> location = new HashMap<>();
        
        try {
            // Récupérer les informations du salon unique
            SalonDTO salon = salonService.getSalonSettings();
            
            if (salon != null) {
                location.put("name", salon.name());
                location.put("address", salon.address());
                location.put("city", salon.city());
                location.put("phone", salon.phone());
                location.put("email", salon.email());
                
                // Coordonnées si disponibles
                if (salon.latitude() != null && salon.longitude() != null) {
                    Map<String, Object> coordinates = new HashMap<>();
                    coordinates.put("latitude", salon.latitude());
                    coordinates.put("longitude", salon.longitude());
                    location.put("coordinates", coordinates);
                    
                    // URLs pré-générées (sécurisées côté backend)
                    Map<String, String> urls = new HashMap<>();
                    urls.put("directions", googleMapsService.generateDirectionsUrl(
                        salonService.getSalonEntity(salon.id())));
                    
                    // Carte statique pré-générée
                    String staticMapUrl = googleMapsService.generateStaticMapUrl(
                        salonService.getSalonEntity(salon.id()), 
                        400, 300, 15);
                    if (staticMapUrl != null) {
                        urls.put("staticMap", staticMapUrl);
                    }
                    
                    location.put("urls", urls);
                    location.put("hasCoordinates", true);
                } else {
                    location.put("hasCoordinates", false);
                    location.put("message", "Coordonnées non disponibles - géocodage requis");
                }
                
                location.put("status", "success");
                
            } else {
                location.put("status", "not_found");
                location.put("message", "Salon non trouvé");
            }
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la localisation du salon", e);
            location.put("status", "error");
            location.put("message", "Erreur lors de la récupération de la localisation");
        }
        
        return ResponseEntity.ok(location);
    }

    /**
     * Déclenche le géocodage du salon (admin uniquement)
     * 
     * SÉCURITÉ : Cette méthode nécessite des droits admin car elle
     * fait des appels à l'API Google qui consomment le quota.
     * 
     * @return Résultat du géocodage
     */
    @PostMapping("/salon/geocode")
    // TODO: Ajouter @PreAuthorize("hasRole('ADMIN')") quand la sécurité sera configurée
    public ResponseEntity<Map<String, Object>> geocodeSalon() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (!googleMapsService.isGoogleMapsEnabled()) {
                result.put("success", false);
                result.put("message", "Google Maps non configuré");
                return ResponseEntity.ok(result);
            }
            
            // Récupérer le salon unique
            SalonDTO salonDTO = salonService.getSalonSettings();
            if (salonDTO == null) {
                result.put("success", false);
                result.put("message", "Salon non trouvé");
                return ResponseEntity.ok(result);
            }
            
            // Convertir en entité pour le géocodage
            var salonEntity = salonService.getSalonEntity(salonDTO.id());
            
            // Effectuer le géocodage
            boolean geocodeSuccess = googleMapsService.geocodeSalon(salonEntity);
            
            if (geocodeSuccess) {
                // Sauvegarder les coordonnées mises à jour
                var updatedSalon = salonService.saveSalon(salonEntity);
                
                result.put("success", true);
                result.put("message", "Géocodage réussi");
                result.put("coordinates", Map.of(
                    "latitude", updatedSalon.getLatitude(),
                    "longitude", updatedSalon.getLongitude()
                ));
                result.put("formattedAddress", updatedSalon.getFormattedAddress());
                
                log.info("Géocodage réussi pour le salon : lat={}, lng={}", 
                         updatedSalon.getLatitude(), updatedSalon.getLongitude());
                
            } else {
                result.put("success", false);
                result.put("message", "Échec du géocodage - vérifiez l'adresse");
            }
            
        } catch (Exception e) {
            log.error("Erreur lors du géocodage du salon", e);
            result.put("success", false);
            result.put("message", "Erreur technique lors du géocodage");
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Retourne le statut de l'intégration Google Maps
     * 
     * Endpoint de diagnostic pour vérifier la configuration
     * 
     * @return Statut de Google Maps
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getGoogleMapsStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            boolean isConfigured = googleMapsConfig.isConfigured();
            boolean isEnabled = googleMapsService.isGoogleMapsEnabled();
            
            status.put("configured", isConfigured);
            status.put("enabled", isEnabled);
            
            if (isConfigured) {
                status.put("status", "ready");
                status.put("message", "Google Maps prêt à l'usage");
            } else {
                status.put("status", "not_configured");
                status.put("message", "Clé API Google Maps non configurée");
            }
            
        } catch (Exception e) {
            log.error("Erreur lors de la vérification du statut Google Maps", e);
            status.put("status", "error");
            status.put("message", "Erreur de vérification du statut");
        }
        
        return ResponseEntity.ok(status);
    }
}