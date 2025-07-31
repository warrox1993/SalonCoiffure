package com.jb.afrostyle.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration Redis OPTIONNELLE pour la gestion des webhooks.
 * 
 * Cette configuration n'est activée que si redis.enabled=true
 * Utilisé pour :
 * - Idempotence des événements webhook
 * - Cache des événements traités
 * - Verrous de traitement distribués
 * 
 * @author AfroStyle Team
 */
@Configuration
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true", matchIfMissing = false)
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /**
     * Configuration de la connexion Redis (seulement si Redis activé).
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    /**
     * Configuration du RedisTemplate pour les opérations webhook (seulement si Redis activé).
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Utiliser StringRedisSerializer pour les clés et valeurs
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}