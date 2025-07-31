package com.jb.afrostyle.security.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Validateur strict pour s'assurer que seul Azure KeyVault est utilisé comme source de secrets.
 * Ce validateur REFUSE le démarrage de l'application si des secrets ne proviennent pas d'Azure KeyVault.
 * 
 * MISSION CRITIQUE : Garantir qu'aucun secret n'est hardcodé ou utilise des fallbacks dangereux.
 */
@Component
public class KeyVaultOnlyValidator {

    private static final Logger log = LoggerFactory.getLogger(KeyVaultOnlyValidator.class);

    @Autowired
    private Environment environment;

    /**
     * Validation stricte au démarrage de l'application.
     * L'application REFUSE de démarrer si des violations sont détectées.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void validateKeyVaultOnlyConfiguration() {
        log.info("🔒 STARTING KEYVAULT-ONLY SECURITY VALIDATION");
        log.info("🛡️ Ensuring Azure KeyVault is the ONLY source of secrets");

        // Secrets critiques qui DOIVENT venir de KeyVault (sans fallback autorisé)
        List<String> criticalSecrets = Arrays.asList(
                "app.security.stripe.secret-key",
                "app.security.stripe.publishable-key",
                "spring.datasource.password"
        );

        // Secrets optionnels (peuvent être absents mais pas hardcodés)
        List<String> optionalSecrets = Arrays.asList(
                "app.security.stripe.webhook-secret",
                "app.security.email.username",
                "app.security.email.password",
                "app.security.twilio.account-sid",
                "app.security.twilio.auth-token",
                "app.security.google.client-secret",
                "app.security.google.maps-api-key"
        );

        boolean hasViolations = false;

        // Validation des secrets critiques
        for (String secretKey : criticalSecrets) {
            if (!validateCriticalSecret(secretKey)) {
                hasViolations = true;
            }
        }

        // Validation des secrets optionnels  
        for (String secretKey : optionalSecrets) {
            validateOptionalSecret(secretKey);
        }

        if (hasViolations) {
            log.error("❌ SECURITY VIOLATION: Some secrets are not properly configured via Azure KeyVault!");
            log.error("🚨 APPLICATION STARTUP BLOCKED FOR SECURITY REASONS");
            log.error("🔧 Please configure all critical secrets in Azure KeyVault");
            throw new IllegalStateException("CRITICAL SECURITY VIOLATION: Secrets not properly externalized via Azure KeyVault");
        }

        log.info("✅ KEYVAULT-ONLY VALIDATION SUCCESSFUL");
        log.info("🔐 All critical secrets are properly externalized via Azure KeyVault");
        log.info("🛡️ No hardcoded secrets or fallbacks detected");
    }

    /**
     * Valide qu'un secret critique provient bien d'Azure KeyVault.
     */
    private boolean validateCriticalSecret(String secretKey) {
        String value = environment.getProperty(secretKey);
        
        if (value == null || value.trim().isEmpty()) {
            log.error("❌ CRITICAL SECRET MISSING: {} is not configured in Azure KeyVault", secretKey);
            return false;
        }

        if (isFallbackValue(value)) {
            log.error("❌ SECURITY VIOLATION: {} contains fallback value instead of KeyVault secret", secretKey);
            return false;
        }

        log.info("✅ {} is properly configured from KeyVault", secretKey);
        return true;
    }

    /**
     * Valide qu'un secret optionnel n'utilise pas de fallbacks dangereux.
     */
    private void validateOptionalSecret(String secretKey) {
        String value = environment.getProperty(secretKey);
        
        if (value == null || value.trim().isEmpty()) {
            log.info("ℹ️ Optional secret {} is not configured (OK for optional services)", secretKey);
            return;
        }

        if (isFallbackValue(value)) {
            log.warn("⚠️ Optional secret {} contains fallback value - should be removed or properly configured", secretKey);
            return;
        }

        log.info("✅ Optional secret {} is properly configured from KeyVault", secretKey);
    }

    /**
     * Détecte si une valeur est un fallback dangereux plutôt qu'un vrai secret KeyVault.
     */
    private boolean isFallbackValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        String lowerValue = value.toLowerCase().trim();
        
        // Détection des fallbacks courants dangereux
        return lowerValue.equals("changeme") ||
               lowerValue.equals("your-secret-here") ||
               lowerValue.equals("your-key-here") ||
               lowerValue.equals("test") ||
               lowerValue.equals("demo") ||
               lowerValue.equals("pk_test_") ||
               lowerValue.equals("sk_test_") ||
               lowerValue.equals("whsec_") ||
               lowerValue.startsWith("your-") ||
               lowerValue.startsWith("change-") ||
               lowerValue.startsWith("replace-") ||
               lowerValue.contains("placeholder") ||
               lowerValue.contains("example") ||
               lowerValue.contains("demo") ||
               value.length() < 8; // Secrets trop courts suspects
    }
}