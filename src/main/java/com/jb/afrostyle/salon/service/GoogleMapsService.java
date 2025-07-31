package com.jb.afrostyle.salon.service;

import com.jb.afrostyle.config.BusinessConstants;
import com.jb.afrostyle.config.GoogleMapsConfig;
import com.jb.afrostyle.salon.modal.Salon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Service Google Maps avec Azure Key Vault - Version Mono-Salon
 * 
 * Ce service utilise GoogleMapsConfig pour sécuriser la clé API via Azure Key Vault
 * et fournit tous les services Google Maps pour le salon unique.
 * 
 * FONCTIONNALITÉS :
 * - Géocodage d'adresses (conversion adresse → coordonnées)
 * - Génération de cartes statiques
 * - Création de liens vers Google Maps pour directions
 * - Calcul de distances haversine
 * 
 * SÉCURITÉ :
 * - Clé API sécurisée via Azure Key Vault
 * - Configuration externalisée
 * - Validation des paramètres
 * 
 * @author AfroStyle Team
 * @since Mono-Salon 2.0 (Azure Key Vault)
 */
@Service
@RequiredArgsConstructor
public class GoogleMapsService {

    private static final Logger log = LoggerFactory.getLogger(GoogleMapsService.class);

    /**
     * Configuration Google Maps avec Azure Key Vault
     * Contient la clé API sécurisée et tous les paramètres de configuration
     */
    private final GoogleMapsConfig googleMapsConfig;
    
    /**
     * Constantes métier externalisées (rayon terre, zoom par défaut, etc.)
     */
    private final BusinessConstants businessConstants;

    /**
     * Client REST pour appels API Google Maps
     */
    private final RestTemplate restTemplate;

    /**
     * Géocode l'adresse du salon unique et met à jour ses coordonnées
     * 
     * @param salon Le salon à géocoder
     * @return true si le géocodage a réussi, false sinon
     */
    public boolean geocodeSalon(Salon salon) {
        if (!googleMapsConfig.isConfigured()) {
            log.warn("Google Maps API non configurée, géocodage non effectué pour le salon {}", salon.getName());
            return false;
        }

        try {
            String address = buildFullAddress(salon);
            Map<String, Object> geocodeResult = geocodeAddress(address);
            
            if (geocodeResult != null && "OK".equals(geocodeResult.get("status"))) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) geocodeResult.get("results");
                if (!results.isEmpty()) {
                    Map<String, Object> firstResult = results.get(0);
                    Map<String, Object> geometry = (Map<String, Object>) firstResult.get("geometry");
                    Map<String, Object> location = (Map<String, Object>) geometry.get("location");
                    
                    // Extraire les coordonnées
                    Double lat = ((Number) location.get("lat")).doubleValue();
                    Double lng = ((Number) location.get("lng")).doubleValue();
                    
                    // Mettre à jour le salon
                    salon.setLatitude(lat);
                    salon.setLongitude(lng);
                    salon.setFormattedAddress((String) firstResult.get("formatted_address"));
                    salon.setGooglePlaceId((String) firstResult.get("place_id"));
                    
                    log.info("Géocodage réussi pour le salon {} : lat={}, lng={}", 
                            salon.getName(), lat, lng);
                    return true;
                }
            }
            
            log.warn("Aucun résultat de géocodage pour l'adresse : {}", address);
            return false;
            
        } catch (Exception e) {
            log.error("Erreur lors du géocodage du salon {}", salon.getName(), e);
            return false;
        }
    }

    private String buildFullAddress(Salon salon) {
        StringBuilder address = new StringBuilder();
        address.append(salon.getAddress());
        if (salon.getCity() != null && !salon.getCity().isEmpty()) {
            address.append(", ").append(salon.getCity());
        }
        return address.toString();
    }

    /**
     * Effectue l'appel à l'API Google Geocoding
     * 
     * @param address Adresse à géocoder
     * @return Réponse de l'API ou null en cas d'erreur
     */
    private Map<String, Object> geocodeAddress(String address) {
        try {
            String url = googleMapsConfig.getGeocodeUrl(address);
            if (url == null) {
                log.warn("Impossible de construire l'URL de géocodage pour l'adresse: {}", address);
                return null;
            }
            
            log.debug("Appel API Geocoding: {}", url.replaceAll("key=[^&]*", "key=***"));
            return restTemplate.getForObject(url, Map.class);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'appel à l'API Google Geocoding pour l'adresse: {}", address, e);
            return null;
        }
    }

    /**
     * Génère une URL de carte statique pour le salon
     * 
     * @param salon Le salon à afficher
     * @param width Largeur de l'image
     * @param height Hauteur de l'image  
     * @param zoom Niveau de zoom
     * @return URL de la carte statique ou null si impossible
     */
    public String generateStaticMapUrl(Salon salon, int width, int height, int zoom) {
        if (!googleMapsConfig.isConfigured() || 
            salon.getLatitude() == null || salon.getLongitude() == null) {
            return null;
        }

        try {
            // Utiliser les dimensions personnalisées ou les valeurs par défaut
            GoogleMapsConfig.Api api = googleMapsConfig.getApi();
            String baseUrl = api.getStaticMapUrl();
            String apiKey = googleMapsConfig.getApiKey();
            
            return String.format(
                "%s?center=%f,%f&zoom=%d&size=%dx%d&markers=color:red%%7C%f,%f&key=%s",
                baseUrl,
                salon.getLatitude(), salon.getLongitude(), 
                zoom > 0 ? zoom : api.getDefaultZoom(), 
                width > 0 ? width : api.getDefaultSize().getWidth(), 
                height > 0 ? height : api.getDefaultSize().getHeight(),
                salon.getLatitude(), salon.getLongitude(), 
                apiKey
            );
        } catch (Exception e) {
            log.error("Erreur lors de la génération de l'URL de carte statique pour le salon {}", salon.getName(), e);
            return null;
        }
    }

    /**
     * Génère une URL de directions vers le salon
     * 
     * @param salon Le salon de destination
     * @return URL vers Google Maps pour les directions ou null si impossible
     */
    public String generateDirectionsUrl(Salon salon) {
        return googleMapsConfig.getDirectionsUrl(
            salon.getLatitude() != null ? salon.getLatitude() : 0.0,
            salon.getLongitude() != null ? salon.getLongitude() : 0.0
        );
    }

    public Double calculateDistance(Double fromLat, Double fromLng, Salon salon) {
        if (fromLat == null || fromLng == null || 
            salon.getLatitude() == null || salon.getLongitude() == null) {
            return null;
        }

        // Calcul de distance haversine (en kilomètres)
        // Rayon de la Terre externalisé dans BusinessConstants
        final int R = businessConstants.getGeography().getEarthRadiusKm();

        double latDistance = Math.toRadians(salon.getLatitude() - fromLat);
        double lonDistance = Math.toRadians(salon.getLongitude() - fromLng);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(fromLat)) * Math.cos(Math.toRadians(salon.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    /**
     * Vérifie si l'intégration Google Maps est active et configurée
     * 
     * @return true si Google Maps est utilisable, false sinon
     */
    public boolean isGoogleMapsEnabled() {
        return googleMapsConfig.isConfigured();
    }

    public List<Salon> findNearBySalons(Double latitude, Double longitude, Double radiusKm, List<Salon> allSalons) {
        if (latitude == null || longitude == null || radiusKm == null || allSalons == null) {
            return allSalons;
        }

        return allSalons.stream()
                .filter(salon -> salon.getLatitude() != null && salon.getLongitude() != null)
                .filter(salon -> {
                    Double distance = calculateDistance(latitude, longitude, salon);
                    return distance != null && distance <= radiusKm;
                })
                .sorted((s1, s2) -> {
                    Double d1 = calculateDistance(latitude, longitude, s1);
                    Double d2 = calculateDistance(latitude, longitude, s2);
                    if (d1 == null && d2 == null) return 0;
                    if (d1 == null) return 1;
                    if (d2 == null) return -1;
                    return d1.compareTo(d2);
                })
                .toList();
    }
}