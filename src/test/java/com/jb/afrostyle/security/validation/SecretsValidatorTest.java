package com.jb.afrostyle.security.validation;

import com.jb.afrostyle.security.config.SecuritySecretsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le validateur de secrets
 * 
 * Ces tests vérifient que l'application refuse de démarrer avec des secrets invalides
 * et accepte uniquement des secrets sécurisés.
 */
@ExtendWith(MockitoExtension.class)
class SecretsValidatorTest {

    private SecretsValidator secretsValidator;
    
    @Mock
    private SecuritySecretsProperties secretsProperties;
    
    @Mock
    private Environment environment;

    @BeforeEach
    void setUp() {
        secretsValidator = new SecretsValidator();
        
        // Injection manuelle pour les tests
        secretsValidator.secretsProperties = secretsProperties;
        secretsValidator.environment = environment;
    }

    @Test
    void shouldFailWithoutJwtSecret() {
        // Given
        when(environment.getProperty("app.jwt.secret")).thenReturn(null);
        
        // When & Then
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            secretsValidator.validateAllSecrets();
        });
        
        assertTrue(exception.getMessage().contains("JWT SECRET is missing"));
    }

    @Test
    void shouldFailWithShortJwtSecret() {
        // Given - Secret trop court (moins de 32 bytes)
        when(environment.getProperty("app.jwt.secret")).thenReturn("short");
        
        // When & Then
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            secretsValidator.validateAllSecrets();
        });
        
        assertTrue(exception.getMessage().contains("JWT SECRET too short"));
    }

    @Test
    void shouldFailWithDangerousJwtSecret() {
        // Given - Secret dangereux
        when(environment.getProperty("app.jwt.secret")).thenReturn("MyVerySecure");
        
        // When & Then
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            secretsValidator.validateAllSecrets();
        });
        
        assertTrue(exception.getMessage().contains("JWT SECRET uses dangerous default value"));
    }

    @Test
    void shouldPassWithValidJwtSecret() {
        // Given - Secret valide (32+ bytes)
        String validSecret = "this-is-a-very-long-and-secure-jwt-secret-for-production-use-only";
        when(environment.getProperty("app.jwt.secret")).thenReturn(validSecret);
        
        // When & Then - Ne devrait pas lever d'exception
        assertDoesNotThrow(() -> {
            secretsValidator.validateAllSecrets();
        });
    }

    @Test
    void shouldFailWithIncompleteStripeConfig() {
        // Given
        String validSecret = "this-is-a-very-long-and-secure-jwt-secret-for-production-use-only";
        when(environment.getProperty("app.jwt.secret")).thenReturn(validSecret);
        when(environment.getProperty("app.security.stripe.secret-key")).thenReturn("sk_test_valid_key");
        when(environment.getProperty("app.security.stripe.publishable-key")).thenReturn(null); // Manquant
        
        // When & Then
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            secretsValidator.validateAllSecrets();
        });
        
        assertTrue(exception.getMessage().contains("Stripe secret key present but publishable key missing"));
    }

    @Test
    void shouldFailWithInvalidStripeKeyFormat() {
        // Given
        String validSecret = "this-is-a-very-long-and-secure-jwt-secret-for-production-use-only";
        when(environment.getProperty("app.jwt.secret")).thenReturn(validSecret);
        when(environment.getProperty("app.security.stripe.secret-key")).thenReturn("invalid_stripe_key"); // Format invalide
        when(environment.getProperty("app.security.stripe.publishable-key")).thenReturn("pk_test_valid_key");
        
        // When & Then
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            secretsValidator.validateAllSecrets();
        });
        
        assertTrue(exception.getMessage().contains("Stripe secret key invalid format"));
    }

    @Test
    void shouldPassWithValidStripeConfig() {
        // Given
        String validSecret = "this-is-a-very-long-and-secure-jwt-secret-for-production-use-only";
        when(environment.getProperty("app.jwt.secret")).thenReturn(validSecret);
        when(environment.getProperty("app.security.stripe.secret-key")).thenReturn("sk_test_valid_stripe_secret_key");
        when(environment.getProperty("app.security.stripe.publishable-key")).thenReturn("pk_test_valid_stripe_publishable_key");
        
        // When & Then - Ne devrait pas lever d'exception
        assertDoesNotThrow(() -> {
            secretsValidator.validateAllSecrets();
        });
    }

    @Test
    void shouldPassWithoutOptionalSecrets() {
        // Given - Seulement JWT (obligatoire), pas de Stripe/Twilio/Google
        String validSecret = "this-is-a-very-long-and-secure-jwt-secret-for-production-use-only";
        when(environment.getProperty("app.jwt.secret")).thenReturn(validSecret);
        
        // When & Then - Devrait passer car seul JWT est obligatoire
        assertDoesNotThrow(() -> {
            secretsValidator.validateAllSecrets();
        });
    }

    @Test
    void shouldFailWithIncompleteTwilioConfig() {
        // Given
        String validSecret = "this-is-a-very-long-and-secure-jwt-secret-for-production-use-only";
        when(environment.getProperty("app.jwt.secret")).thenReturn(validSecret);
        when(environment.getProperty("twilio.account-sid")).thenReturn("AC123456789");
        when(environment.getProperty("twilio.auth-token")).thenReturn(null); // Manquant
        
        // When & Then
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            secretsValidator.validateAllSecrets();
        });
        
        assertTrue(exception.getMessage().contains("Twilio account SID present but auth token missing"));
    }

    /**
     * Test critique : l'application DOIT échouer si des secrets critiques sont manquants
     */
    @Test
    void criticalTest_ApplicationMustFailWithoutCriticalSecrets() {
        // Given - Configuration vide ou dangereuse
        when(environment.getProperty("app.jwt.secret")).thenReturn("CHANGE_ME_IN_PRODUCTION");
        
        // When & Then - L'application DOIT échouer
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            secretsValidator.validateAllSecrets();
        });
        
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Critical secrets validation failed"));
        
        // Ce test garantit que l'application ne peut PAS démarrer avec des secrets non sécurisés
    }

    /**
     * Test de régression : vérifier que les secrets générés par openssl sont acceptés
     */
    @Test
    void shouldAcceptOpenSSLGeneratedSecrets() {
        // Given - Secret généré par: openssl rand -base64 64
        String opensslSecret = "Hj6+gOrpcURhsTGoz6TbGQ5ynHOQCMBuucHk466uh3555urr7U+p9RLApPP7zY8qbPuTIxES66K+qBJUgrvLew==";
        when(environment.getProperty("app.jwt.secret")).thenReturn(opensslSecret);
        
        // When & Then - Devrait passer
        assertDoesNotThrow(() -> {
            secretsValidator.validateAllSecrets();
        });
    }
}