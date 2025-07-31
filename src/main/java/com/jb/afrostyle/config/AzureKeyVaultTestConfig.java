package com.jb.afrostyle.config;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * Test de connexion Azure Key Vault
 */
@Configuration
public class AzureKeyVaultTestConfig {

    private static final Logger log = LoggerFactory.getLogger(AzureKeyVaultTestConfig.class);

    @Autowired(required = false)
    private SecretClient secretClient;

    @EventListener(ApplicationReadyEvent.class)
    public void testAzureKeyVaultConnection() {
        log.info("🔍 Testing Azure Key Vault connection...");
        
        if (secretClient == null) {
            log.error("❌ SecretClient is null - Azure Key Vault not configured properly");
            return;
        }

        try {
            // Test de récupération du secret JWT-SECRET
            KeyVaultSecret secret = secretClient.getSecret("JWT-SECRET");
            if (secret != null && secret.getValue() != null) {
                String value = secret.getValue();
                log.info("✅ Secret JWT-SECRET found in Azure Key Vault!");
                log.info("   - Secret length: {} characters", value.length());
                log.info("   - Secret preview: [REDACTED FOR SECURITY]");
            } else {
                log.warn("⚠️ Secret JWT-SECRET exists but has no value");
            }
        } catch (Exception e) {
            log.error("❌ Failed to retrieve JWT-SECRET from Azure Key Vault: {}", e.getMessage());
            log.error("   - Error type: {}", e.getClass().getSimpleName());
            if (e.getCause() != null) {
                log.error("   - Cause: {}", e.getCause().getMessage());
            }
        }

        // Test de listage des secrets
        try {
            log.info("🔍 Listing secrets in Azure Key Vault...");
            secretClient.listPropertiesOfSecrets().forEach(secretProperties -> {
                log.info("   - Found secret: {}", secretProperties.getName());
            });
        } catch (Exception e) {
            log.error("❌ Failed to list secrets: {}", e.getMessage());
        }
    }
}