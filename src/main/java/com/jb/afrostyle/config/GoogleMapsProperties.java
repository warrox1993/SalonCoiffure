package com.jb.afrostyle.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Google Maps API configuration properties - URLS EXTERNALISÉES
 * 
 * Cette classe centralise TOUTES les URLs Google Maps pour éviter les valeurs
 * hardcodées dans le code et permettre des tests avec des mocks.
 * 
 * CONFIGURATION REQUISE dans application.properties :
 * google.maps.api.geocode-url=${GOOGLE_GEOCODE_URL:https://maps.googleapis.com/maps/api/geocode/json}
 * google.maps.api.static-map-url=${GOOGLE_STATICMAP_URL:https://maps.googleapis.com/maps/api/staticmap}
 * google.maps.api.directions-url=${GOOGLE_DIRECTIONS_URL:https://www.google.com/maps/dir/?api=1&destination=}
 * 
 * USAGE :
 * - GoogleMapsService utilise ces URLs pour les appels API
 * - Permet de mocker les services Google Maps en test
 * - Configuration différente selon l'environnement
 * 
 * @author AfroStyle Team
 * @since 1.0
 */
@ConfigurationProperties(prefix = "google.maps.api")
@Component
@Data
public class GoogleMapsProperties {
    
    /**
     * URL de l'API Google Geocoding pour convertir adresses en coordonnées.
     * 
     * CONFIGURATION : google.maps.api.geocode-url dans application.properties
     * VALEUR PAR DÉFAUT : https://maps.googleapis.com/maps/api/geocode/json
     * 
     * USAGE : Transformation d'adresses de salons en latitude/longitude
     * FORMAT : L'URL est complétée avec les paramètres ?address=XXX&key=API_KEY
     */
    private String geocodeUrl;
    
    /**
     * URL de l'API Google Static Maps pour générer des images de cartes.
     * 
     * CONFIGURATION : google.maps.api.static-map-url dans application.properties  
     * VALEUR PAR DÉFAUT : https://maps.googleapis.com/maps/api/staticmap
     * 
     * USAGE : Génération d'aperçus de cartes pour les salons
     * FORMAT : L'URL est complétée avec les paramètres de position, zoom, taille
     */
    private String staticMapUrl;
    
    /**
     * URL de base Google Maps pour les directions (itinéraires).
     * 
     * CONFIGURATION : google.maps.api.directions-url dans application.properties
     * VALEUR PAR DÉFAUT : https://www.google.com/maps/dir/?api=1&destination=
     * 
     * USAGE : Génération de liens vers Google Maps pour itinéraires
     * FORMAT : L'URL est complétée avec les coordonnées de destination
     */
    private String directionsUrl;
    
    /**
     * Active/désactive l'intégration Google Maps dans l'application.
     * 
     * CONFIGURATION : google.maps.api.enabled dans application.properties
     * VALEUR PAR DÉFAUT : false (pour éviter les erreurs si pas d'API key)
     * 
     * USAGE : Si false, les services Google Maps retournent des valeurs nulles/par défaut
     */
    private boolean enabled = false;
    
    /**
     * Niveau de zoom par défaut pour les cartes statiques.
     * 
     * CONFIGURATION : google.maps.api.default-zoom dans application.properties
     * VALEUR PAR DÉFAUT : 15 (niveau quartier/rue)
     * 
     * USAGE : Utilisé lors de la génération d'aperçus de cartes pour les salons
     */
    private int defaultZoom = 15;
    
    /**
     * Dimensions par défaut des cartes statiques générées.
     * 
     * CONFIGURATION : Peut être configuré via les propriétés si nécessaire
     * VALEUR PAR DÉFAUT : 400x300 pixels (optimisé pour affichage web)
     */
    private MapDimensions defaultSize = new MapDimensions();
    
    @Data
    public static class MapDimensions {
        /**
         * Largeur par défaut des cartes statiques en pixels.
         */
        private int width = 400;
        
        /**
         * Hauteur par défaut des cartes statiques en pixels.
         */
        private int height = 300;
    }
}