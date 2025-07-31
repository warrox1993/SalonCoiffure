package com.jb.afrostyle.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Constantes métier externalisées pour l'application AfroStyle.
 * 
 * Cette classe centralise TOUTES les constantes métier qui étaient précédemment
 * hardcodées dans le code. Cela permet une configuration flexible selon
 * l'environnement et facilite les tests avec des valeurs différentes.
 * 
 * CONFIGURATION dans application.properties :
 * app.business.earth-radius-km=6371
 * app.business.stripe.session-expires-minutes=30
 * app.business.pagination.default-page-size=10
 * app.business.pagination.max-page-size=1000
 * 
 * AVANTAGES :
 * - Pas de valeurs magiques dans le code
 * - Configuration par environnement (dev/prod)
 * - Tests avec des valeurs spécifiques
 * - Documentation centralisée
 * 
 * @author AfroStyle Team
 * @since 1.0
 */
@ConfigurationProperties(prefix = "app.business")
@Component
@Data
public class BusinessConstants {

    /**
     * Configuration géographique pour les calculs de distance.
     */
    private Geography geography = new Geography();
    
    /**
     * Configuration des timeouts et durées Stripe.
     */
    private Stripe stripe = new Stripe();
    
    
    /**
     * Configuration de pagination par défaut.
     */
    private Pagination pagination = new Pagination();

    @Data
    public static class Geography {
        /**
         * Rayon de la Terre en kilomètres pour les calculs de distance haversine.
         * 
         * CONFIGURATION : app.business.earth-radius-km
         * VALEUR PAR DÉFAUT : 6371 km (rayon moyen de la Terre)
         * 
         * USAGE : Calculs de distance entre coordonnées GPS (salon/utilisateur)
         * TESTS : Peut être modifié pour tester des calculs spécifiques
         */
        private int earthRadiusKm = 6371;
        
        /**
         * Zoom par défaut pour les cartes statiques Google Maps.
         * 
         * CONFIGURATION : app.business.maps.default-zoom
         * VALEUR PAR DÉFAUT : 15 (niveau rue/quartier)
         * 
         * USAGE : Génération d'aperçus de cartes pour les salons
         */
        private int defaultMapZoom = 15;
    }

    @Data
    public static class Stripe {
        /**
         * Durée d'expiration des sessions Stripe Checkout en minutes.
         * 
         * CONFIGURATION : app.business.stripe.session-expires-minutes
         * VALEUR PAR DÉFAUT : 30 minutes (recommandé par Stripe)
         * 
         * USAGE : Configuration des sessions de paiement Stripe
         * SÉCURITÉ : Une durée trop longue peut poser des risques de sécurité
         */
        private int sessionExpiresMinutes = 30;
    }


    @Data
    public static class Pagination {
        /**
         * Taille de page par défaut pour les listes paginées.
         * 
         * CONFIGURATION : app.business.pagination.default-page-size
         * VALEUR PAR DÉFAUT : 10 éléments par page
         * 
         * USAGE : Contrôleurs REST pour listes de salons, réservations, etc.
         * PERFORMANCE : Équilibre entre performance et UX
         */
        private int defaultPageSize = 10;
        
        /**
         * Taille de page maximale autorisée (sécurité).
         * 
         * CONFIGURATION : app.business.pagination.max-page-size
         * VALEUR PAR DÉFAUT : 1000 éléments max
         * 
         * USAGE : Protection contre les requêtes trop volumineuses
         * SÉCURITÉ : Évite les attaques par déni de service via pagination
         */
        private int maxPageSize = 1000;
        
        /**
         * Index de page par défaut (0-based).
         * 
         * VALEUR PAR DÉFAUT : 0 (première page)
         * 
         * USAGE : Point de départ pour la pagination
         */
        private int defaultPageIndex = 0;
    }

    /**
     * Configuration des limitations métier de l'application.
     */
    private Limits limits = new Limits();

    @Data
    public static class Limits {
        /**
         * Durée maximale d'une réservation en minutes.
         * 
         * CONFIGURATION : app.business.limits.max-booking-duration-minutes
         * VALEUR PAR DÉFAUT : 480 minutes (8 heures)
         * 
         * USAGE : Validation des réservations trop longues
         */
        private int maxBookingDurationMinutes = 480;
        
        /**
         * Nombre maximum de services par réservation.
         * 
         * CONFIGURATION : app.business.limits.max-services-per-booking
         * VALEUR PAR DÉFAUT : 10 services
         * 
         * USAGE : Validation du nombre de services sélectionnés
         */
        private int maxServicesPerBooking = 10;
        
        /**
         * Distance maximale de recherche de salons en kilomètres.
         * 
         * CONFIGURATION : app.business.limits.max-search-radius-km
         * VALEUR PAR DÉFAUT : 50 km
         * 
         * USAGE : Limitation des recherches géographiques de salons
         */
        private int maxSearchRadiusKm = 50;
    }
}