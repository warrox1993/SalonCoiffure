package com.jb.afrostyle.integrations.stripe.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service de gestion des événements webhook pour éviter les doublons.
 * 
 * Implémente l'idempotence des webhooks avec fallback :
 * - Redis pour le cache (si disponible)
 * - Cache en mémoire comme fallback
 * 
 * Fonctionnalités :
 * - Détection des événements déjà traités
 * - Cache Redis pour performance (optionnel)
 * - Cache en mémoire comme fallback
 * - TTL configurable pour nettoyage automatique
 */
@Service
@Slf4j
public class WebhookEventService {
    
    // Cache en mémoire comme fallback quand Redis n'est pas disponible
    private final ConcurrentHashMap<String, LocalDateTime> inMemoryProcessedEvents = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> inMemoryProcessingEvents = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> inMemoryFailedEvents = new ConcurrentHashMap<>();
    
    private final RedisTemplate<String, String> redisTemplate;
    private final boolean redisEnabled;
    
    public WebhookEventService(@Autowired(required = false) RedisTemplate<String, String> redisTemplate,
                              @Value("${redis.enabled:false}") boolean redisEnabled) {
        this.redisTemplate = redisTemplate;
        this.redisEnabled = redisEnabled;
        log.info("🔧 WebhookEventService initialized - Redis: {}", redisEnabled ? "ENABLED" : "DISABLED (using in-memory fallback)");
    }
    
    // TTL pour les événements dans Redis (24 heures par défaut)
    private static final Duration EVENT_TTL = Duration.ofHours(24);
    
    // Préfixes Redis pour organisation
    private static final String PROCESSED_PREFIX = "webhook:processed:";
    private static final String PROCESSING_PREFIX = "webhook:processing:";
    private static final String FAILED_PREFIX = "webhook:failed:";
    
    /**
     * Vérifie si un événement a déjà été traité.
     */
    public boolean hasEventBeenProcessed(String eventId) {
        if (redisEnabled && redisTemplate != null) {
            return hasEventBeenProcessedRedis(eventId);
        } else {
            return hasEventBeenProcessedInMemory(eventId);
        }
    }
    
    private boolean hasEventBeenProcessedRedis(String eventId) {
        try {
            String key = PROCESSED_PREFIX + eventId;
            Boolean exists = redisTemplate.hasKey(key);
            
            if (Boolean.TRUE.equals(exists)) {
                log.info("🔄 Event {} already processed (Redis), skipping", eventId);
                return true;
            }
            
            // Vérifier aussi s'il est en cours de traitement
            String processingKey = PROCESSING_PREFIX + eventId;
            Boolean processing = redisTemplate.hasKey(processingKey);
            
            if (Boolean.TRUE.equals(processing)) {
                log.info("⏳ Event {} currently being processed (Redis), skipping", eventId);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.warn("⚠️ Redis error, falling back to in-memory cache: {}", e.getMessage());
            return hasEventBeenProcessedInMemory(eventId);
        }
    }
    
    private boolean hasEventBeenProcessedInMemory(String eventId) {
        // Nettoyer les anciens événements (TTL simulation)
        cleanupExpiredInMemoryEvents();
        
        if (inMemoryProcessedEvents.containsKey(eventId)) {
            log.info("🔄 Event {} already processed (in-memory), skipping", eventId);
            return true;
        }
        
        if (inMemoryProcessingEvents.containsKey(eventId)) {
            log.info("⏳ Event {} currently being processed (in-memory), skipping", eventId);
            return true;
        }
        
        return false;
    }
    
    /**
     * Marque un événement comme en cours de traitement.
     */
    public boolean markEventAsProcessing(String eventId) {
        if (redisEnabled && redisTemplate != null) {
            return markEventAsProcessingRedis(eventId);
        } else {
            return markEventAsProcessingInMemory(eventId);
        }
    }
    
    private boolean markEventAsProcessingRedis(String eventId) {
        String key = PROCESSING_PREFIX + eventId;
        
        try {
            // Utiliser SET avec NX (only if not exists) pour atomicité
            Boolean success = redisTemplate.opsForValue().setIfAbsent(
                key, 
                LocalDateTime.now().toString(), 
                Duration.ofMinutes(10) // TTL court pour éviter les blocages
            );
            
            if (Boolean.TRUE.equals(success)) {
                log.debug("🔒 Event {} marked as processing (Redis)", eventId);
                return true;
            } else {
                log.warn("⚠️ Event {} already being processed by another instance (Redis)", eventId);
                return false;
            }
        } catch (Exception e) {
            log.warn("⚠️ Redis error, falling back to in-memory cache: {}", e.getMessage());
            return markEventAsProcessingInMemory(eventId);
        }
    }
    
    private boolean markEventAsProcessingInMemory(String eventId) {
        LocalDateTime now = LocalDateTime.now();
        
        // Atomique avec ConcurrentHashMap
        LocalDateTime existing = inMemoryProcessingEvents.putIfAbsent(eventId, now);
        
        if (existing == null) {
            log.debug("🔒 Event {} marked as processing (in-memory)", eventId);
            return true;
        } else {
            log.warn("⚠️ Event {} already being processed (in-memory)", eventId);
            return false;
        }
    }
    
    /**
     * Marque un événement comme traité avec succès.
     */
    public void markEventAsCompleted(String eventId) {
        if (redisEnabled && redisTemplate != null) {
            markEventAsCompletedRedis(eventId);
        } else {
            markEventAsCompletedInMemory(eventId);
        }
    }
    
    private void markEventAsCompletedRedis(String eventId) {
        try {
            // Marquer comme traité
            String processedKey = PROCESSED_PREFIX + eventId;
            redisTemplate.opsForValue().set(
                processedKey, 
                LocalDateTime.now().toString(), 
                EVENT_TTL
            );
            
            // Supprimer le flag "en cours"
            String processingKey = PROCESSING_PREFIX + eventId;
            redisTemplate.delete(processingKey);
            
            log.info("✅ Event {} marked as completed (Redis)", eventId);
            
        } catch (Exception e) {
            log.warn("⚠️ Redis error, falling back to in-memory cache: {}", e.getMessage());
            markEventAsCompletedInMemory(eventId);
        }
    }
    
    private void markEventAsCompletedInMemory(String eventId) {
        // Marquer comme traité
        inMemoryProcessedEvents.put(eventId, LocalDateTime.now());
        
        // Supprimer le flag "en cours"
        inMemoryProcessingEvents.remove(eventId);
        
        log.info("✅ Event {} marked as completed (in-memory)", eventId);
    }
    
    /**
     * Marque un événement comme échoué.
     */
    public void markEventAsFailed(String eventId, String errorMessage) {
        try {
            // Marquer comme échoué avec détails de l'erreur
            String failedKey = FAILED_PREFIX + eventId;
            String errorData = String.format("{\"timestamp\":\"%s\",\"error\":\"%s\"}", 
                                           LocalDateTime.now(), 
                                           errorMessage.replace("\"", "\\\""));
            
            redisTemplate.opsForValue().set(failedKey, errorData, EVENT_TTL);
            
            // Supprimer le flag "en cours"
            String processingKey = PROCESSING_PREFIX + eventId;
            redisTemplate.delete(processingKey);
            
            log.error("❌ Event {} marked as failed: {}", eventId, errorMessage);
            
        } catch (Exception e) {
            log.error("💥 Failed to mark event {} as failed: {}", eventId, e.getMessage());
        }
    }
    
    /**
     * Libère un événement bloqué en "processing" (pour déblocage manuel).
     */
    public void releaseProcessingLock(String eventId) {
        try {
            String processingKey = PROCESSING_PREFIX + eventId;
            Boolean removed = redisTemplate.delete(processingKey);
            
            if (Boolean.TRUE.equals(removed)) {
                log.info("🔓 Released processing lock for event {}", eventId);
            } else {
                log.warn("⚠️ No processing lock found for event {}", eventId);
            }
        } catch (Exception e) {
            log.error("💥 Failed to release processing lock for event {}: {}", eventId, e.getMessage());
        }
    }
    
    /**
     * Retourne les statistiques des événements webhook.
     */
    public WebhookStats getWebhookStats() {
        try {
            SetOperations<String, String> setOps = redisTemplate.opsForSet();
            
            // Compter les événements par statut
            long processedCount = countKeysByPattern(PROCESSED_PREFIX + "*");
            long processingCount = countKeysByPattern(PROCESSING_PREFIX + "*");
            long failedCount = countKeysByPattern(FAILED_PREFIX + "*");
            
            return WebhookStats.builder()
                    .processedEvents(processedCount)
                    .processingEvents(processingCount)
                    .failedEvents(failedCount)
                    .totalEvents(processedCount + processingCount + failedCount)
                    .build();
            
        } catch (Exception e) {
            log.error("💥 Failed to get webhook stats: {}", e.getMessage());
            return WebhookStats.builder().build();
        }
    }
    
    /**
     * Compte les clés correspondant à un pattern.
     */
    private long countKeysByPattern(String pattern) {
        try {
            return redisTemplate.keys(pattern).size();
        } catch (Exception e) {
            log.warn("⚠️ Failed to count keys for pattern {}: {}", pattern, e.getMessage());
            return 0;
        }
    }
    
    /**
     * Nettoie les anciens événements (appelé par un job schedulé).
     */
    public void cleanupOldEvents() {
        try {
            if (redisEnabled && redisTemplate != null) {
                // Les TTL Redis s'occupent du nettoyage automatique
                log.info("🧹 Webhook events cleanup completed (handled by Redis TTL)");
            } else {
                cleanupExpiredInMemoryEvents();
                log.info("🧹 In-memory webhook events cleanup completed");
            }
        } catch (Exception e) {
            log.error("💥 Failed to cleanup old events: {}", e.getMessage());
        }
    }
    
    /**
     * Nettoie les événements en mémoire expirés (simulation TTL).
     */
    private void cleanupExpiredInMemoryEvents() {
        LocalDateTime cutoff = LocalDateTime.now().minus(EVENT_TTL);
        
        // Nettoyer les événements traités
        inMemoryProcessedEvents.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        
        // Nettoyer les événements en cours (TTL plus court : 10 minutes)
        LocalDateTime processingCutoff = LocalDateTime.now().minusMinutes(10);
        inMemoryProcessingEvents.entrySet().removeIf(entry -> entry.getValue().isBefore(processingCutoff));
        
        // Nettoyer les événements échoués
        inMemoryFailedEvents.entrySet().removeIf(entry -> {
            try {
                LocalDateTime timestamp = LocalDateTime.parse(entry.getValue().split(",")[0]);
                return timestamp.isBefore(cutoff);
            } catch (Exception e) {
                return true; // Supprimer les entrées corrompues
            }
        });
    }
    
    /**
     * Statistiques des événements webhook.
     */
    @lombok.Builder
    @lombok.Data
    public static class WebhookStats {
        private long processedEvents;
        private long processingEvents;
        private long failedEvents;
        private long totalEvents;
        
        public double getSuccessRate() {
            if (totalEvents == 0) return 0.0;
            return (double) processedEvents / totalEvents * 100;
        }
        
        public double getFailureRate() {
            if (totalEvents == 0) return 0.0;
            return (double) failedEvents / totalEvents * 100;
        }
    }
}