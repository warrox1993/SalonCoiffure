package com.jb.afrostyle.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration Google Maps avec Azure Key Vault
 * 
 * Cette classe centralise la configuration Google Maps en utilisant
 * Azure Key Vault pour sécuriser la clé API.
 * 
 * CONFIGURATION REQUISE dans application.properties :
 * app.security.google.maps-api-key=${GOOGLE-MAPS-API-KEY}
 * 
 * URLS SUPPORTÉES :
 * - Geocoding API : Convertir adresses en coordonnées
 * - Static Maps API : Générer images de cartes
 * - Directions : Liens vers Google Maps
 * 
 * @author AfroStyle Team
 * @since Mono-Salon 2.0
 */
@Configuration
@ConfigurationProperties(prefix = "google.maps")
@Data
public class GoogleMapsConfig {
    
    /**
     * Clé API Google Maps sécurisée via Azure Key Vault
     * 
     * CONFIGURATION : app.security.google.maps-api-key dans application.properties
     * SOURCE : Azure Key Vault secret GOOGLE-MAPS-API-KEY
     * 
     * SÉCURITÉ : Cette clé est externalisée et ne doit JAMAIS être commitée
     */
    @Value("${app.security.google.maps-api-key:}")
    private String apiKey;
    
    /**
     * Active/désactive l'intégration Google Maps
     * 
     * CONFIGURATION : google.maps.enabled dans application.properties
     * VALEUR PAR DÉFAUT : true si clé API présente, false sinon
     */
    private boolean enabled = true;
    
    // URLs de base Google Maps (peuvent être overridées si nécessaire)
    private Api api = new Api();
    
    @Data
    public static class Api {
        /**
         * URL de l'API Google Geocoding
         */
        private String geocodeUrl = "https://maps.googleapis.com/maps/api/geocode/json";
        
        /**
         * URL de l'API Google Static Maps
         */
        private String staticMapUrl = "https://maps.googleapis.com/maps/api/staticmap";
        
        /**
         * URL de base pour les directions Google Maps
         */
        private String directionsUrl = "https://www.google.com/maps/dir/?api=1&destination=";
        
        /**
         * Niveau de zoom par défaut pour les cartes
         */
        private int defaultZoom = 15;
        
        /**
         * Dimensions par défaut des cartes statiques
         */
        private MapSize defaultSize = new MapSize();
    }
    
    @Data
    public static class MapSize {
        private int width = 400;
        private int height = 300;
    }
    
    /**
     * Vérifie si Google Maps est configuré et actif
     * 
     * @return true si la clé API est présente et le service activé
     */
    public boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.trim().isEmpty();
    }
    
    /**
     * Obtient l'URL complète pour l'API Geocoding
     * 
     * @param address Adresse à géocoder
     * @return URL complète avec clé API
     */
    public String getGeocodeUrl(String address) {
        if (!isConfigured()) {
            return null;
        }
        
        try {
            String encodedAddress = java.net.URLEncoder.encode(address, "UTF-8");
            return String.format("%s?address=%s&key=%s", 
                api.getGeocodeUrl(), encodedAddress, apiKey);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Obtient l'URL pour une carte statique
     * 
     * @param latitude Latitude du centre
     * @param longitude Longitude du centre
     * @return URL de la carte statique
     */
    public String getStaticMapUrl(double latitude, double longitude) {
        if (!isConfigured()) {
            return null;
        }
        
        return String.format("%s?center=%f,%f&zoom=%d&size=%dx%d&key=%s",
            api.getStaticMapUrl(), latitude, longitude, api.getDefaultZoom(),
            api.getDefaultSize().getWidth(), api.getDefaultSize().getHeight(), apiKey);
    }
    
    /**
     * Obtient l'URL pour les directions vers un point
     * 
     * @param latitude Latitude de destination
     * @param longitude Longitude de destination
     * @return URL vers Google Maps pour directions
     */
    public String getDirectionsUrl(double latitude, double longitude) {
        return String.format("%s%f,%f", api.getDirectionsUrl(), latitude, longitude);
    }
    
    /**
     * Configuration du RestTemplate pour les appels Google Maps API
     * 
     * @return RestTemplate configuré pour les appels HTTP
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}