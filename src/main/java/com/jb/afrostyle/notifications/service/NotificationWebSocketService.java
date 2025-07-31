package com.jb.afrostyle.notifications.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service pour l'envoi de notifications WebSocket en temps réel
 * Gère les notifications de disponibilité, réservations et mises à jour de salon
 */
@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketService.class);

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Envoie une notification à un utilisateur spécifique
     */
    @Async("notificationExecutor")
    public void sendToUser(String userId, String type, Object payload) {
        try {
            Map<String, Object> notification = Map.of(
                "type", type,
                "payload", payload,
                "timestamp", System.currentTimeMillis()
            );
            
            messagingTemplate.convertAndSendToUser(
                userId, 
                "/queue/notifications", 
                notification
            );
            
            log.debug("Notification envoyée à l'utilisateur {}: {}", userId, type);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de notification WebSocket à l'utilisateur {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Diffuse une notification à tous les abonnés du salon
     * MONO-SALON : Plus besoin de salonId car il n'y a qu'un seul salon
     */
    @Async("notificationExecutor")
    public void broadcastToSalon(String type, Object payload) {
        try {
            Map<String, Object> notification = Map.of(
                "type", type,
                "payload", payload,
                "timestamp", System.currentTimeMillis()
            );
            
            messagingTemplate.convertAndSend(
                "/topic/salon", 
                notification
            );
            
            log.debug("Notification diffusée pour le salon unique: {}", type);
        } catch (Exception e) {
            log.error("Erreur lors de la diffusion de notification WebSocket pour le salon unique: {}", e.getMessage());
        }
    }

    /**
     * Diffuse une notification de disponibilité
     * MONO-SALON : Plus besoin de salonId
     */
    @Async("notificationExecutor")
    public void broadcastAvailabilityUpdate(Object availabilityData) {
        broadcastToSalon("AVAILABILITY_UPDATE", availabilityData);
    }

    /**
     * Diffuse une notification de réservation
     * MONO-SALON : Plus besoin de salonId
     */
    @Async("notificationExecutor")
    public void broadcastBookingUpdate(Object bookingData) {
        broadcastToSalon("BOOKING_UPDATE", bookingData);
    }

    /**
     * Envoie une notification de confirmation de réservation
     */
    @Async("notificationExecutor")
    public void sendBookingConfirmation(String userId, Object bookingData) {
        sendToUser(userId, "BOOKING_CONFIRMED", bookingData);
    }

    /**
     * Envoie une notification d'annulation de réservation
     */
    @Async("notificationExecutor")
    public void sendBookingCancellation(String userId, Object bookingData) {
        sendToUser(userId, "BOOKING_CANCELLED", bookingData);
    }

    /**
     * Envoie un ping pour maintenir la connexion
     */
    public void sendPing(String sessionId) {
        try {
            messagingTemplate.convertAndSend("/topic/ping", Map.of(
                "type", "PING",
                "timestamp", System.currentTimeMillis(),
                "sessionId", sessionId
            ));
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du ping: {}", e.getMessage());
        }
    }
}