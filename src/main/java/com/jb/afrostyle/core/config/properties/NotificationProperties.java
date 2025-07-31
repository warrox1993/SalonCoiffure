package com.jb.afrostyle.core.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration des notifications
 * Externalise les constantes liées aux notifications
 * 
 * @param reminderHours Délai avant envoi de notification de rappel (heures)
 * @param maxAttempts Nombre maximum de tentatives d'envoi
 * @param retryDelayMinutes Délai entre tentatives d'envoi (minutes)
 * 
 * @version 1.0
 * @since Java 21
 */
@ConfigurationProperties(prefix = "afrostyle.notification")
@Validated
public record NotificationProperties(
    
    @Min(value = 1, message = "Le délai de rappel doit être d'au moins 1 heure")
    @Max(value = 168, message = "Le délai de rappel ne peut pas dépasser 168 heures (7 jours)")
    int reminderHours,
    
    @Min(value = 1, message = "Il doit y avoir au moins 1 tentative d'envoi")
    @Max(value = 10, message = "Maximum 10 tentatives d'envoi")
    int maxAttempts,
    
    @Min(value = 1, message = "Le délai de retry doit être d'au moins 1 minute")
    @Max(value = 60, message = "Le délai de retry ne peut pas dépasser 60 minutes")
    int retryDelayMinutes
    
) {}