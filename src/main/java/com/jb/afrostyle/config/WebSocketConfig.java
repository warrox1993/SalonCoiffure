package com.jb.afrostyle.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration WebSocket pour les notifications en temps réel
 * Permet aux clients de s'abonner aux mises à jour de disponibilité et de réservation
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Active le broker simple pour les destinations /topic et /queue
        config.enableSimpleBroker("/topic", "/queue");
        
        // Préfixe pour les messages envoyés par les clients
        config.setApplicationDestinationPrefixes("/app");
        
        // Préfixe pour les messages destinés à des utilisateurs spécifiques
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket principal avec fallback SockJS
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Permet toutes les origines en développement
                .withSockJS();
        
        // Endpoint WebSocket natif sans fallback
        registry.addEndpoint("/websocket")
                .setAllowedOriginPatterns("*");
    }
}