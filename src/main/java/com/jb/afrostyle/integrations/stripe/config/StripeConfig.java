package com.jb.afrostyle.integrations.stripe.config;

import com.stripe.Stripe;
import com.jb.afrostyle.security.config.SecuritySecretsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuration Stripe pour initialiser la clé API
 */
@Configuration
public class StripeConfig {

    private static final Logger log = LoggerFactory.getLogger(StripeConfig.class);

    private final SecuritySecretsProperties securityProperties;

    public StripeConfig(SecuritySecretsProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @PostConstruct
    public void initStripe() {
        String stripeApiKey = securityProperties.getStripe().getSecretKey();
        String stripePublishableKey = securityProperties.getStripe().getPublishableKey();
        log.info("🔧 STRIPE CONFIGURATION INITIALIZATION");
        log.info("   - Secret Key configured: {}", stripeApiKey != null && !stripeApiKey.trim().isEmpty() ? "✅ YES" : "❌ NO");
        log.info("   - Publishable Key configured: {}", stripePublishableKey != null && !stripePublishableKey.trim().isEmpty() ? "✅ YES" : "❌ NO");
        
        if (stripeApiKey == null || stripeApiKey.trim().isEmpty() || !stripeApiKey.startsWith("sk_")) {
            log.error("❌ STRIPE API KEY NOT CONFIGURED PROPERLY!");
            log.error("   - Current value: [REDACTED FOR SECURITY]");
            log.error("   - Please verify Azure KeyVault configuration");
            log.error("   - Payment functionality will be limited.");
            return;
        }

        try {
            Stripe.apiKey = stripeApiKey;
            log.info("✅ STRIPE API INITIALIZED SUCCESSFULLY!");
            log.info("   - API Key set in Stripe SDK");
            log.info("   - Publishable key status: {}", stripePublishableKey != null && !stripePublishableKey.trim().isEmpty() ? "CONFIGURED" : "NOT SET");
        } catch (Exception e) {
            log.error("❌ FAILED TO INITIALIZE STRIPE API!");
            log.error("   - Error: {}", e.getMessage());
            log.error("   - Stack trace:", e);
        }
    }

    public String getPublishableKey() {
        return securityProperties.getStripe().getPublishableKey();
    }

    public String getApiKey() {
        return securityProperties.getStripe().getSecretKey();
    }
    
    public String getWebhookSecret() {
        return securityProperties.getStripe().getWebhookSecret();
    }
}