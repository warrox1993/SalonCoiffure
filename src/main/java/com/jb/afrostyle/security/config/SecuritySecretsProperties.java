package com.jb.afrostyle.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties pour les secrets de sécurité
 * 
 * Gère la liaison entre les variables d'environnement et les propriétés application.
 * Ordre de résolution : Environment Variables > application-{profile}.properties > valeurs par défaut
 * 
 * @author AfroStyle Security Team
 */
@Component
@ConfigurationProperties(prefix = "app.security")
public class SecuritySecretsProperties {

    /**
     * Configuration JWT
     */
    private Jwt jwt = new Jwt();

    /**
     * Configuration Stripe
     */
    private Stripe stripe = new Stripe();

    /**
     * Configuration Email
     */
    private Email email = new Email();

    /**
     * Configuration Twilio SMS
     */
    private Twilio twilio = new Twilio();

    /**
     * Configuration Google
     */
    private Google google = new Google();

    // Getters et Setters
    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }

    public Stripe getStripe() { return stripe; }
    public void setStripe(Stripe stripe) { this.stripe = stripe; }

    public Email getEmail() { return email; }
    public void setEmail(Email email) { this.email = email; }

    public Twilio getTwilio() { return twilio; }
    public void setTwilio(Twilio twilio) { this.twilio = twilio; }

    public Google getGoogle() { return google; }
    public void setGoogle(Google google) { this.google = google; }

    /**
     * Configuration JWT
     */
    public static class Jwt {
        private String secret;
        private long expirationMs = 86400000L; // 24h par défaut
        private long refreshExpirationMs = 2592000000L; // 30 jours par défaut

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }

        public long getExpirationMs() { return expirationMs; }
        public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }

        public long getRefreshExpirationMs() { return refreshExpirationMs; }
        public void setRefreshExpirationMs(long refreshExpirationMs) { this.refreshExpirationMs = refreshExpirationMs; }
    }

    /**
     * Configuration Stripe
     */
    public static class Stripe {
        private String secretKey;
        private String publishableKey;
        private String webhookSecret;

        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

        public String getPublishableKey() { return publishableKey; }
        public void setPublishableKey(String publishableKey) { this.publishableKey = publishableKey; }

        public String getWebhookSecret() { return webhookSecret; }
        public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
    }

    /**
     * Configuration Email
     */
    public static class Email {
        private String password;
        private String username;

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    /**
     * Configuration Twilio
     */
    public static class Twilio {
        private String accountSid;
        private String authToken;
        private String phoneNumber;

        public String getAccountSid() { return accountSid; }
        public void setAccountSid(String accountSid) { this.accountSid = accountSid; }

        public String getAuthToken() { return authToken; }
        public void setAuthToken(String authToken) { this.authToken = authToken; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    }

    /**
     * Configuration Google
     */
    public static class Google {
        private String clientSecret;
        private String mapsApiKey;

        public String getClientSecret() { return clientSecret; }
        public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

        public String getMapsApiKey() { return mapsApiKey; }
        public void setMapsApiKey(String mapsApiKey) { this.mapsApiKey = mapsApiKey; }
    }
}