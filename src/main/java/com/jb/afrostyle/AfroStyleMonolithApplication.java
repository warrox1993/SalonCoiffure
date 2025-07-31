package com.jb.afrostyle;

import com.jb.afrostyle.core.config.properties.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application principale AfroStyle Monolith
 * 
 * MIGRATION @ConfigurationProperties COMPLÉTÉE !
 * - Toutes les constantes BusinessConstants externalisées vers application.yml
 * - Validation automatique avec Bean Validation
 * - Compatibilité ascendante maintenue
 * - Configuration par profil supportée (dev/prod/docker)
 * 
 * @version 2.0
 * @since Java 21
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({
    AfroStyleProperties.class,
    BusinessHoursProperties.class,
    BookingProperties.class,
    PaymentProperties.class,
    UserProperties.class,
    SalonProperties.class,
    NotificationProperties.class,
    PaginationProperties.class,
    CacheProperties.class,
    ValidationProperties.class
})
public class AfroStyleMonolithApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AfroStyleMonolithApplication.class, args);
    }
}