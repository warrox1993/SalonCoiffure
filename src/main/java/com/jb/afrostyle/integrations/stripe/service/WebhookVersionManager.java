package com.jb.afrostyle.integrations.stripe.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Gestionnaire de versions d'API webhook Stripe.
 * 
 * Implémente la stratégie blue-green deployment pour les mises à jour
 * d'API webhook sans temps d'arrêt, conforme aux meilleures pratiques Stripe.
 * 
 * @see https://docs.stripe.com/webhooks/versioning
 */
@Component
@Slf4j
public class WebhookVersionManager {
    
    // Versions API supportées
    private static final String CURRENT_API_VERSION = "2025-06-30.basil";
    private static final String LEGACY_API_VERSION = "2024-09-30.acacia";
    
    @Value("${stripe.api.version:2025-06-30.basil}")
    private String configuredApiVersion;
    
    @Value("${stripe.webhook.migration.enabled:false}")
    private boolean migrationEnabled;
    
    @Value("${stripe.webhook.migration.process-legacy:true}")
    private boolean processLegacyEvents;
    
    @Value("${stripe.webhook.migration.process-new:false}")
    private boolean processNewEvents;
    
    /**
     * Détermine si une version d'API est considérée comme legacy.
     */
    public boolean isLegacyVersion(String apiVersion) {
        if (apiVersion == null) {
            return true; // Considérer null comme legacy
        }
        return LEGACY_API_VERSION.equals(apiVersion) || 
               apiVersion.compareTo(LEGACY_API_VERSION) < 0;
    }
    
    /**
     * Détermine si une version d'API est la version courante.
     */
    public boolean isCurrentVersion(String apiVersion) {
        return CURRENT_API_VERSION.equals(apiVersion);
    }
    
    /**
     * Détermine si une version d'API est supportée.
     */
    public boolean isSupportedVersion(String apiVersion) {
        return isLegacyVersion(apiVersion) || isCurrentVersion(apiVersion);
    }
    
    /**
     * Détermine si les événements legacy doivent être traités.
     * 
     * Phases de migration :
     * - Phase 1 : Traiter seulement legacy (processLegacyEvents=true, processNewEvents=false)
     * - Phase 2 : Traiter les deux (processLegacyEvents=true, processNewEvents=true)
     * - Phase 3 : Traiter seulement nouveau (processLegacyEvents=false, processNewEvents=true)
     */
    public boolean shouldProcessLegacyEvents() {
        if (!migrationEnabled) {
            return true; // Mode normal, traiter legacy
        }
        return processLegacyEvents;
    }
    
    /**
     * Détermine si les nouveaux événements doivent être traités.
     */
    public boolean shouldProcessNewEvents() {
        if (!migrationEnabled) {
            return false; // Mode normal, pas encore de nouveaux événements
        }
        return processNewEvents;
    }
    
    /**
     * Détermine l'action à prendre pour un événement donné.
     */
    public WebhookAction determineAction(String apiVersion, String versionParam) {
        // Détecter la version depuis le paramètre URL ou l'en-tête
        String detectedVersion = determineActualVersion(apiVersion, versionParam);
        
        log.debug("🔍 Version detection: apiVersion={}, versionParam={}, detected={}", 
                 apiVersion, versionParam, detectedVersion);
        
        if (isLegacyVersion(detectedVersion)) {
            if (shouldProcessLegacyEvents()) {
                log.info("✅ Processing legacy event (version: {})", detectedVersion);
                return WebhookAction.PROCESS;
            } else {
                log.info("❌ Rejecting legacy event during migration (version: {})", detectedVersion);
                return WebhookAction.REJECT_LEGACY; // Retourner 400 pour retry
            }
        } else if (isCurrentVersion(detectedVersion)) {
            if (shouldProcessNewEvents()) {
                log.info("✅ Processing new event (version: {})", detectedVersion);
                return WebhookAction.PROCESS;
            } else {
                log.info("⏳ Ignoring new event during preparation (version: {})", detectedVersion);
                return WebhookAction.IGNORE_NEW; // Retourner 200 sans traiter
            }
        } else {
            log.warn("⚠️ Unsupported API version: {}", detectedVersion);
            return WebhookAction.UNSUPPORTED;
        }
    }
    
    /**
     * Détermine la version réelle en combinant plusieurs sources.
     */
    private String determineActualVersion(String apiVersion, String versionParam) {
        // Priorité 1 : Paramètre URL version
        if (versionParam != null && !versionParam.isEmpty()) {
            return versionParam;
        }
        
        // Priorité 2 : Version dans l'événement Stripe
        if (apiVersion != null && !apiVersion.isEmpty()) {
            return apiVersion;
        }
        
        // Priorité 3 : Version configurée par défaut
        return configuredApiVersion;
    }
    
    /**
     * Retourne les informations sur l'état de la migration.
     */
    public MigrationStatus getMigrationStatus() {
        return MigrationStatus.builder()
                .migrationEnabled(migrationEnabled)
                .processLegacyEvents(processLegacyEvents)
                .processNewEvents(processNewEvents)
                .currentApiVersion(CURRENT_API_VERSION)
                .legacyApiVersion(LEGACY_API_VERSION)
                .configuredApiVersion(configuredApiVersion)
                .build();
    }
    
    /**
     * Actions possibles pour un webhook.
     */
    public enum WebhookAction {
        PROCESS,        // Traiter l'événement normalement
        REJECT_LEGACY,  // Rejeter l'événement legacy (400) pour retry
        IGNORE_NEW,     // Ignorer l'événement nouveau (200) sans traitement
        UNSUPPORTED     // Version non supportée (400)
    }
    
    /**
     * Statut de migration pour monitoring.
     */
    @lombok.Builder
    @lombok.Data
    public static class MigrationStatus {
        private boolean migrationEnabled;
        private boolean processLegacyEvents;
        private boolean processNewEvents;
        private String currentApiVersion;
        private String legacyApiVersion;
        private String configuredApiVersion;
        
        public String getPhase() {
            if (!migrationEnabled) {
                return "NORMAL";
            }
            if (processLegacyEvents && !processNewEvents) {
                return "PREPARATION";
            }
            if (processLegacyEvents && processNewEvents) {
                return "MIGRATION";
            }
            if (!processLegacyEvents && processNewEvents) {
                return "COMPLETED";
            }
            return "UNKNOWN";
        }
    }
}