package com.jb.afrostyle.security.validation;

import com.jb.afrostyle.security.config.SecuritySecretsProperties;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Validateur centralisé pour tous les secrets de l'application
 * 
 * Effectue une validation complète au démarrage de l'application et
 * arrête l'application si des secrets critiques sont manquants ou invalides.
 * 
 * @author AfroStyle Security Team
 */
@Component
public class SecretsValidator {

    private static final Logger log = LoggerFactory.getLogger(SecretsValidator.class);

    @Autowired
    SecuritySecretsProperties secretsProperties; // Package-private pour tests

    @Autowired
    Environment environment; // Package-private pour tests

    // Secrets dangereux qui ne doivent jamais être utilisés en production
    private static final String[] DANGEROUS_SECRETS = {
        "MyVerySecure",
        "CHANGE_ME_IN_PRODUCTION_OR_SECURITY_BREACH_GUARANTEED",
        "mySecretKeyForJWTTokenThatShouldBeVeryLongAndSecureInProduction123456789",
        "secret",
        "changeme", 
        "default",
        "test",
        "demo",
        "password",
        "123456"
    };

    /**
     * Validation complète des secrets au démarrage de l'application
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateAllSecrets() {
        log.info("🔐 Starting comprehensive secrets validation...");
        
        List<String> validationErrors = new ArrayList<>();
        
        try {
            // 1. JWT supprimé - utilise session-based auth
            // validateJwtSecret(validationErrors); // Supprimé car JWT remplacé par sessions
            
            // 2. Validation Stripe (selon environnement)
            validateStripeSecrets(validationErrors);
            
            // 3. Validation Email (selon environnement) 
            validateEmailSecrets(validationErrors);
            
            // 4. Validation Twilio (optionnel)
            validateTwilioSecrets(validationErrors);
            
            // 5. Validation Google (optionnel)
            validateGoogleSecrets(validationErrors);
            
            // 6. Vérification finale
            if (!validationErrors.isEmpty()) {
                log.error("🚨🚨🚨 CRITICAL SECURITY VALIDATION FAILED 🚨🚨🚨");
                for (String error : validationErrors) {
                    log.error("   ❌ {}", error);
                }
                log.error("🛑 APPLICATION STOPPED - FIX SECURITY ISSUES BEFORE RESTART");
                throw new SecurityException("Critical secrets validation failed: " + validationErrors.size() + " errors");
            }
            
            log.info("✅ All secrets validation passed successfully");
            
        } catch (Exception e) {
            if (e instanceof SecurityException) {
                throw e; // Re-throw security exceptions
            }
            log.error("🚨 Unexpected error during secrets validation: {}", e.getMessage(), e);
            throw new RuntimeException("Secrets validation failed", e);
        }
    }

    /**
     * Validation du secret JWT (CRITIQUE - arrêt si invalide)
     */
    private void validateJwtSecret(List<String> errors) {
        String jwtSecret = resolveSecret(
                "JWT-SECRET",
                "app.security.jwt.secret",
                secretsProperties.getJwt().getSecret());
        
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            errors.add("JWT SECRET is missing - application cannot start without JWT secret");
            return;
        }
        
        // Vérification des secrets dangereux
        for (String dangerous : DANGEROUS_SECRETS) {
            if (dangerous.equals(jwtSecret)) {
                errors.add("JWT SECRET uses dangerous default value: " + dangerous);
                return;
            }
        }
        
        // Vérification de la longueur minimale pour HS256
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            errors.add("JWT SECRET too short: " + keyBytes.length + " bytes (minimum 32 bytes for HS256)");
            return;
        }
        
        // Test de création de clé HMAC
        try {
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);
            log.info("✅ JWT Secret configured - {} bytes, algorithm: {}", keyBytes.length, key.getAlgorithm());
        } catch (Exception e) {
            errors.add("JWT SECRET invalid for HMAC-SHA: " + e.getMessage());
        }
    }

    /**
     * Validation des secrets Stripe
     */
    private void validateStripeSecrets(List<String> errors) {
        // Remplacer les 3 appels resolveSecret() par :
        String stripeSecret = secretsProperties.getStripe().getSecretKey();
        String stripePublishable = secretsProperties.getStripe().getPublishableKey();
        String stripeWebhookSecret = secretsProperties.getStripe().getWebhookSecret();
        
        boolean hasStripeSecret = isValidSecret(stripeSecret);
        boolean hasStripePublishable = isValidSecret(stripePublishable);
        boolean hasWebhookSecret = isValidSecret(stripeWebhookSecret);
        
        if (hasStripeSecret || hasStripePublishable) {
            // Si au moins un secret Stripe est présent, valider la cohérence
            if (!hasStripeSecret) {
                errors.add("Stripe publishable key present but secret key missing");
            }
            if (!hasStripePublishable) {
                errors.add("Stripe secret key present but publishable key missing");
            }
            
            // Validation du format des clés Stripe
            if (hasStripeSecret && !stripeSecret.startsWith("sk_")) {
                errors.add("Stripe secret key invalid format (should start with 'sk_')");
            }
            if (hasStripePublishable && !stripePublishable.startsWith("pk_")) {
                errors.add("Stripe publishable key invalid format (should start with 'pk_')");
            }
            
            // Validation du webhook secret (optionnel mais recommandé)
            if (hasWebhookSecret && !stripeWebhookSecret.startsWith("whsec_")) {
                errors.add("Stripe webhook secret invalid format (should start with 'whsec_')");
            }
            
            if (hasStripeSecret && hasStripePublishable) {
                log.info("✅ Stripe configuration present - Secret: CONFIGURED, Publishable: CONFIGURED");
                
                if (hasWebhookSecret) {
                    log.info("   - Webhook Secret: CONFIGURED");
                } else {
                    log.warn("   ⚠️ Webhook Secret not configured - webhook signature validation disabled");
                }
            }
        } else {
            log.warn("⚠️ Stripe secrets not configured - payment features disabled");
        }
    }

    /**
     * Validation des secrets Email
     */
    private void validateEmailSecrets(List<String> errors) {
        String emailPassword = resolveSecret("EMAIL-PASSWORD", "app.security.email.password", secretsProperties.getEmail().getPassword());
        
        if (isValidSecret(emailPassword)) {
            log.info("✅ Email configuration present");
        } else {
            log.warn("⚠️ Email password not configured - email features disabled");
        }
    }

    /**
     * Validation des secrets Twilio
     */
    private void validateTwilioSecrets(List<String> errors) {
        String twilioSid = resolveSecret("TWILIO-ACCOUNT-SID", "app.security.twilio.account-sid", secretsProperties.getTwilio().getAccountSid());
        String twilioToken = resolveSecret("TWILIO-AUTH-TOKEN", "app.security.twilio.auth-token", secretsProperties.getTwilio().getAuthToken());
        
        boolean hasTwilioSid = isValidSecret(twilioSid);
        boolean hasTwilioToken = isValidSecret(twilioToken);
        
        if (hasTwilioSid || hasTwilioToken) {
            if (!hasTwilioSid) {
                errors.add("Twilio auth token present but account SID missing");
            }
            if (!hasTwilioToken) {
                errors.add("Twilio account SID present but auth token missing");
            }
            
            if (hasTwilioSid && hasTwilioToken) {
                log.info("✅ Twilio SMS configuration present");
            }
        } else {
            log.warn("⚠️ Twilio secrets not configured - SMS features disabled");
        }
    }

    /**
     * Validation des secrets Google
     */
    private void validateGoogleSecrets(List<String> errors) {
        String googleClientSecret = resolveSecret("GOOGLE-CLIENT-SECRET", "app.security.google.client-secret", secretsProperties.getGoogle().getClientSecret());
        String googleMapsKey = resolveSecret("GOOGLE-MAPS-API-KEY", "app.security.google.maps-api-key", secretsProperties.getGoogle().getMapsApiKey());
        
        if (isValidSecret(googleClientSecret)) {
            log.info("✅ Google OAuth2 configuration present");
        } else {
            log.warn("⚠️ Google OAuth2 not configured - Google login disabled");
        }
        
        if (isValidSecret(googleMapsKey)) {
            log.info("✅ Google Maps API configuration present");
        } else {
            log.warn("⚠️ Google Maps API not configured - maps features disabled");
        }
    }

    /**
     * Résolution d'un secret avec ordre de priorité :
     * 1. Variable d'environnement
     * 2. Propriété Spring
     * 3. Valeur par défaut depuis @ConfigurationProperties
     */
    private String resolveSecret(String envVarName, String propertyName, String defaultValue) {
        // 1. Variable d'environnement (priorité maximale)
        String envValue = System.getenv(envVarName);
        if (isValidSecret(envValue)) {
            log.debug("🌍 Secret '{}' resolved from environment variable", envVarName);
            return envValue;
        }
        
        // 2. Propriété Spring (application.properties)
        String propValue = environment.getProperty(propertyName);
        if (isValidSecret(propValue)) {
            log.debug("🔧 Secret '{}' resolved from Spring property '{}'", envVarName, propertyName);
            return propValue;
        }
        
        // 3. Valeur par défaut
        if (isValidSecret(defaultValue)) {
            log.debug("📋 Secret '{}' using default value", envVarName);
            return defaultValue;
        }
        
        log.debug("❌ Secret '{}' not found in any source", envVarName);
        return null;
    }

    /**
     * Vérifie si un secret est valide (non null, non vide, pas un placeholder)
     */
    private boolean isValidSecret(String secret) {
        if (secret == null || secret.trim().isEmpty()) {
            return false;
        }
        
        // Ignorer les placeholders courants
        return !secret.equals("changeme") && 
               !secret.equals("your_secret_here") && 
               !secret.equals("CHANGE_ME") &&
               !secret.startsWith("your_") &&
               !secret.contains("changeme");
    }
}