package com.jb.afrostyle.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

/**
 * Configuration pour Azure Key Vault
 * Vérifie que la connexion est établie au démarrage
 */
@Configuration
public class AzureKeyVaultConfig {

    private static final Logger log = LoggerFactory.getLogger(AzureKeyVaultConfig.class);
    private final Environment environment;

    public AzureKeyVaultConfig(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void verifyKeyVaultConnection() {
        log.info("🔐 Verifying Azure Key Vault connection...");
        
        // Test de récupération d'un secret
        String jwtSecret = environment.getProperty("app.security.jwt.secret");
        
        if (jwtSecret != null && !jwtSecret.equals("${JWT-SECRET}")) {
            log.info("✅ Azure Key Vault connection successful - Secrets are being loaded");
        } else {
            log.warn("⚠️ Azure Key Vault might not be properly configured - JWT secret not loaded");
            log.warn("⚠️ Make sure you have:");
            log.warn("   1. Created the secret 'JWT-SECRET' in Azure Key Vault");
            log.warn("   2. Replaced 'YOUR-VAULT-NAME' with your actual vault name in application.properties");
            log.warn("   3. Given proper permissions to your App Registration");
        }
    }
}