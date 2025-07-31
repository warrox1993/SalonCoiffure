package com.jb.afrostyle.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration properties - URLS EXTERNALISÉES
 * 
 * Cette classe centralise TOUTES les URLs CORS pour éviter les valeurs hardcodées
 * dans le code. Les URLs sont configurées via application.properties.
 * 
 * CONFIGURATION REQUISE dans application.properties :
 * app.cors.allowed-origins=http://localhost:4200,http://localhost:3000,https://afrostyle.be
 * 
 * SÉCURITÉ : Les origines autorisées sont critiques pour la sécurité CORS.
 * Ne jamais utiliser de valeurs par défaut en production !
 * 
 * @author AfroStyle Team
 * @since 1.0
 */
@ConfigurationProperties(prefix = "app.cors")
@Component
@Data
public class CorsProperties {
    
    /**
     * Liste des origines autorisées pour les requêtes CORS.
     * 
     * CONFIGURATION : app.cors.allowed-origins dans application.properties
     * 
     * EXEMPLES :
     * - Développement : http://localhost:4200,http://localhost:3000
     * - Production : https://afrostyle.be,https://www.afrostyle.be
     * 
     * SÉCURITÉ : Cette liste détermine quels domaines peuvent faire des requêtes
     * vers l'API. Une configuration incorrecte peut exposer l'API à des attaques.
     */
    private List<String> allowedOrigins;
    
    /**
     * Liste des méthodes HTTP autorisées pour les requêtes CORS.
     * 
     * CONFIGURATION : app.cors.allowed-methods dans application.properties
     * VALEUR PAR DÉFAUT : GET,POST,PUT,PATCH,DELETE,OPTIONS (sûre)
     */
    private List<String> allowedMethods = Arrays.asList(
        "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
    );
    
    /**
     * Liste des headers autorisés pour les requêtes CORS.
     * 
     * CONFIGURATION : app.cors.allowed-headers dans application.properties
     * VALEUR PAR DÉFAUT : Headers standards sécurisés
     */
    private List<String> allowedHeaders = Arrays.asList(
        "Authorization", "Content-Type", "X-Requested-With", "Accept",
        "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"
    );
    
    /**
     * Autorise les credentials (cookies, headers d'auth) dans les requêtes CORS.
     * 
     * CONFIGURATION : app.cors.allow-credentials dans application.properties
     * VALEUR PAR DÉFAUT : true (nécessaire pour JWT dans headers Authorization)
     */
    private boolean allowCredentials = true;
    
    /**
     * Durée de cache (en secondes) pour les requêtes CORS preflight.
     * 
     * CONFIGURATION : app.cors.max-age dans application.properties
     * VALEUR PAR DÉFAUT : 3600 secondes (1 heure)
     */
    private long maxAge = 3600L;
}