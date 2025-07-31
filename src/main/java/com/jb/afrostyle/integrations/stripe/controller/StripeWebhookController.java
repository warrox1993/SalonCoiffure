package com.jb.afrostyle.integrations.stripe.controller;

import com.jb.afrostyle.integrations.stripe.service.WebhookVersionManager;
import com.jb.afrostyle.integrations.stripe.service.WebhookEventService;
import com.jb.afrostyle.integrations.stripe.service.impl.StripeCheckoutServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur webhook Stripe avec gestion avancée des versions.
 * 
 * Fonctionnalités :
 * - Gestion des versions d'API (blue-green deployment)
 * - Idempotence des événements
 * - Monitoring et métriques
 * - Support des paramètres de version dans l'URL
 * 
 * URLs supportées :
 * - /api/payments/webhook (version par défaut)
 * - /api/payments/webhook?version=2024-06-20 (version spécifique)
 * 
 * @author AfroStyle Team
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final StripeCheckoutServiceImpl stripeCheckoutService;
    private final WebhookVersionManager versionManager;
    private final WebhookEventService eventService;

    /**
     * Endpoint webhook principal avec support des versions.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature,
            @RequestParam(value = "version", required = false) String version) {
        
        try {
            log.info("🔔 Received Stripe webhook");
            log.info("📝 Payload length: {}", payload != null ? payload.length() : 0);
            log.info("🔑 Signature present: {}", signature != null);
            log.info("🏷️ Version parameter: {}", version);
            
            // Traiter le webhook avec gestion des versions
            stripeCheckoutService.processCheckoutWebhookWithVersion(payload, signature, version);
            
            log.info("✅ Webhook processed successfully");
            return ResponseEntity.ok("OK");
            
        } catch (Exception e) {
            log.error("💥 Webhook processing failed: {}", e.getMessage(), e);
            
            // Retourner des codes d'erreur appropriés selon le contexte
            if (e.getMessage().contains("Legacy API version deprecated")) {
                // Retourner 400 pour permettre les retries automatiques
                return ResponseEntity.badRequest().body("Legacy version deprecated");
            } else if (e.getMessage().contains("Invalid webhook signature")) {
                // Erreur de signature - ne pas retry
                return ResponseEntity.badRequest().body("Invalid signature");
            } else {
                // Erreur générale - retry possible
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                   .body("Processing error");
            }
        }
    }

    /**
     * Endpoint pour obtenir le statut de migration des webhooks.
     * Utile pour le monitoring et le debugging.
     */
    @GetMapping("/webhook/migration-status")
    public ResponseEntity<WebhookVersionManager.MigrationStatus> getMigrationStatus() {
        try {
            WebhookVersionManager.MigrationStatus status = versionManager.getMigrationStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Failed to get migration status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint pour obtenir les statistiques des événements webhook.
     * Utile pour le monitoring et les métriques.
     */
    @GetMapping("/webhook/stats")
    public ResponseEntity<WebhookEventService.WebhookStats> getWebhookStats() {
        try {
            WebhookEventService.WebhookStats stats = eventService.getWebhookStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Failed to get webhook stats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint pour débloquer manuellement un événement bloqué.
     * Utile pour les opérations de maintenance.
     */
    @PostMapping("/webhook/release-lock/{eventId}")
    public ResponseEntity<String> releaseLock(@PathVariable String eventId) {
        try {
            eventService.releaseProcessingLock(eventId);
            return ResponseEntity.ok("Lock released for event: " + eventId);
        } catch (Exception e) {
            log.error("Failed to release lock for event {}: {}", eventId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Failed to release lock");
        }
    }

    /**
     * Endpoint pour nettoyer les anciens événements.
     * Appelé par un job schedulé ou manuellement.
     */
    @PostMapping("/webhook/cleanup")
    public ResponseEntity<String> cleanupOldEvents() {
        try {
            eventService.cleanupOldEvents();
            return ResponseEntity.ok("Cleanup completed");
        } catch (Exception e) {
            log.error("Failed to cleanup old events: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Cleanup failed");
        }
    }

    /**
     * Endpoint de santé spécifique aux webhooks.
     */
    @GetMapping("/webhook/health")
    public ResponseEntity<WebhookHealthStatus> getWebhookHealth() {
        try {
            WebhookEventService.WebhookStats stats = eventService.getWebhookStats();
            WebhookVersionManager.MigrationStatus migrationStatus = versionManager.getMigrationStatus();
            
            WebhookHealthStatus health = WebhookHealthStatus.builder()
                    .healthy(stats.getFailureRate() < 10.0) // Seuil de 10% d'erreur
                    .totalEvents(stats.getTotalEvents())
                    .successRate(stats.getSuccessRate())
                    .failureRate(stats.getFailureRate())
                    .migrationPhase(migrationStatus.getPhase())
                    .currentVersion(migrationStatus.getCurrentApiVersion())
                    .build();
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Failed to get webhook health: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Statut de santé des webhooks pour monitoring.
     */
    @lombok.Builder
    @lombok.Data
    public static class WebhookHealthStatus {
        private boolean healthy;
        private long totalEvents;
        private double successRate;
        private double failureRate;
        private String migrationPhase;
        private String currentVersion;
        private String status;
        
        public String getStatus() {
            if (healthy) {
                return "HEALTHY";
            } else if (failureRate > 50.0) {
                return "CRITICAL";
            } else {
                return "WARNING";
            }
        }
    }
}