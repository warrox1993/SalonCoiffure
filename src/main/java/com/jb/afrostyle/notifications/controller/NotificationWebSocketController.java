package com.jb.afrostyle.notifications.controller;

import com.jb.afrostyle.notifications.service.NotificationWebSocketService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * Contrôleur WebSocket pour les notifications en temps réel
 * Gère les abonnements et la diffusion de notifications
 */
@Controller
@RequiredArgsConstructor
public class NotificationWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketController.class);

    private final NotificationWebSocketService notificationService;

    /**
     * Gestion de l'abonnement aux notifications personnelles
     */
    @SubscribeMapping("/user/queue/notifications")
    public void subscribeToPersonalNotifications(Principal principal) {
        if (principal != null) {
            log.info("Utilisateur {} abonné aux notifications personnelles", principal.getName());
        }
    }

    /**
     * Gestion de l'abonnement aux mises à jour du salon
     * MONO-SALON : Plus besoin de salonId car il n'y a qu'un seul salon
     */
    @SubscribeMapping("/topic/salon")
    public void subscribeToSalonUpdates(Principal principal) {
        String userId = principal != null ? principal.getName() : "anonymous";
        log.info("Utilisateur {} abonné aux mises à jour du salon unique", userId);
    }

    /**
     * Gestion des messages de ping pour maintenir la connexion
     */
    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public Map<String, Object> handlePing(Map<String, Object> message, Principal principal) {
        String userId = principal != null ? principal.getName() : "anonymous";
        log.debug("Ping reçu de l'utilisateur {}", userId);
        
        return Map.of(
            "type", "PONG",
            "timestamp", System.currentTimeMillis(),
            "userId", userId
        );
    }

    /**
     * Abonnement aux notifications de disponibilité du salon
     * MONO-SALON : Plus besoin de salonId car il n'y a qu'un seul salon
     */
    @SubscribeMapping("/topic/availability")
    public void subscribeToAvailabilityUpdates(Principal principal) {
        String userId = principal != null ? principal.getName() : "anonymous";
        log.info("Utilisateur {} abonné aux disponibilités du salon unique", userId);
    }

    /**
     * Gestion des préférences de notification
     */
    @MessageMapping("/notification-preferences/{userId}")
    public void updateNotificationPreferences(
            @DestinationVariable String userId,
            Map<String, Object> preferences,
            Principal principal) {
        
        if (principal != null && principal.getName().equals(userId)) {
            log.info("Mise à jour des préférences de notification pour l'utilisateur {}: {}", userId, preferences);
            
            // Ici vous pourriez sauvegarder les préférences en base de données
            // notificationPreferenceService.updatePreferences(userId, preferences);
            
            // Confirmation à l'utilisateur
            notificationService.sendToUser(userId, "PREFERENCES_UPDATED", preferences);
        } else {
            log.warn("Tentative non autorisée de mise à jour des préférences pour l'utilisateur {}", userId);
        }
    }

    /**
     * Test de connexion WebSocket
     */
    @MessageMapping("/test-connection")
    @SendTo("/topic/test")
    public Map<String, Object> testConnection(Map<String, Object> message, Principal principal) {
        String userId = principal != null ? principal.getName() : "anonymous";
        log.info("Test de connexion WebSocket pour l'utilisateur {}", userId);
        
        return Map.of(
            "type", "CONNECTION_TEST_RESPONSE",
            "message", "Connexion WebSocket fonctionnelle",
            "timestamp", System.currentTimeMillis(),
            "userId", userId
        );
    }
}