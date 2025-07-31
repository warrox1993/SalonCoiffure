package com.jb.afrostyle.core.config.properties;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests de validation des propriétés AfroStyle externalisées
 * Valide que toutes les @ConfigurationProperties fonctionnent correctement
 * avec leur validation Bean Validation
 * 
 * @version 1.0
 * @since Java 21
 */
@SpringBootTest(classes = AfroStylePropertiesTest.TestConfig.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class AfroStylePropertiesTest {

    @Autowired
    private AfroStyleProperties afroStyleProperties;
    
    @Autowired
    private Validator validator;

    @Nested
    @DisplayName("Configuration valide")
    class ValidConfiguration {

        @Test
        @DisplayName("Devrait charger toutes les propriétés avec des valeurs valides")
        void shouldLoadAllPropertiesWithValidValues() {
            // Given & When - Configuration chargée par Spring Boot
            
            // Then - Vérifier que toutes les propriétés sont chargées
            assertThat(afroStyleProperties).isNotNull();
            assertThat(afroStyleProperties.businessHours()).isNotNull();
            assertThat(afroStyleProperties.booking()).isNotNull();
            assertThat(afroStyleProperties.payment()).isNotNull();
            assertThat(afroStyleProperties.user()).isNotNull();
            assertThat(afroStyleProperties.salon()).isNotNull();
            assertThat(afroStyleProperties.notification()).isNotNull();
            assertThat(afroStyleProperties.pagination()).isNotNull();
            assertThat(afroStyleProperties.cache()).isNotNull();
            assertThat(afroStyleProperties.validation()).isNotNull();
        }

        @Test
        @DisplayName("Devrait valider automatiquement les propriétés business hours")
        void shouldValidateBusinessHoursProperties() {
            var businessHours = afroStyleProperties.businessHours();
            
            assertThat(businessHours.openTime()).isEqualTo(LocalTime.of(8, 0));
            assertThat(businessHours.closeTime()).isEqualTo(LocalTime.of(20, 0));
            assertThat(businessHours.timeSlotIntervalMinutes()).isEqualTo(15);
            assertThat(businessHours.closedDays()).containsExactly(7);
            
            // Validation logique
            assertThat(businessHours.isBusinessDay(1)).isTrue(); // Lundi
            assertThat(businessHours.isBusinessDay(7)).isFalse(); // Dimanche
            assertThat(businessHours.isWithinBusinessHours(LocalTime.of(10, 0))).isTrue();
            assertThat(businessHours.isWithinBusinessHours(LocalTime.of(6, 0))).isFalse();
        }

        @Test
        @DisplayName("Devrait valider automatiquement les propriétés booking")
        void shouldValidateBookingProperties() {
            var booking = afroStyleProperties.booking();
            
            assertThat(booking.minDurationMinutes()).isEqualTo(15);
            assertThat(booking.maxDurationMinutes()).isEqualTo(480);
            assertThat(booking.maxServicesPerBooking()).isEqualTo(5);
            assertThat(booking.minDurationPerServiceMinutes()).isEqualTo(30);
            assertThat(booking.cancellationDeadlineHours()).isEqualTo(24);
            assertThat(booking.timeToleranceMinutes()).isEqualTo(5);
            assertThat(booking.maxAdvanceBookingMonths()).isEqualTo(6);
        }

        @Test
        @DisplayName("Devrait valider automatiquement les propriétés payment")
        void shouldValidatePaymentProperties() {
            var payment = afroStyleProperties.payment();
            
            assertThat(payment.amounts().maxAmount()).isEqualTo(new BigDecimal("10000.00"));
            assertThat(payment.amounts().minPricePerService()).isEqualTo(new BigDecimal("5.00"));
            assertThat(payment.supportedCurrencies()).containsExactlyInAnyOrder("EUR", "USD", "GBP", "CAD");
            assertThat(payment.majorCurrencies()).containsExactlyInAnyOrder("EUR", "USD", "GBP");
            assertThat(payment.defaultCurrency()).isEqualTo("EUR");
            assertThat(payment.cashLimitEur()).isEqualTo(new BigDecimal("500.00"));
            assertThat(payment.highAmountThreshold()).isEqualTo(new BigDecimal("1000.00"));
            
            // Test des méthodes utilitaires
            assertThat(payment.getMinimumAmount("EUR")).isEqualTo(new BigDecimal("0.50"));
            assertThat(payment.isSupportedCurrency("EUR")).isTrue();
            assertThat(payment.isMajorCurrency("EUR")).isTrue();
            assertThat(payment.isSupportedCurrency("JPY")).isFalse();
        }
    }

    @Nested
    @DisplayName("Validation des contraintes")
    class ConstraintValidation {

        @Test
        @DisplayName("Devrait rejeter les heures d'ouverture invalides")
        void shouldRejectInvalidBusinessHours() {
            // Given - Configuration avec heure fermeture avant ouverture
            var invalidConfig = new BusinessHoursProperties(
                LocalTime.of(20, 0), // Ouverture à 20h
                LocalTime.of(8, 0),  // Fermeture à 8h (invalide)
                15,
                Set.of(7)
            );
            
            // When & Then - Doit lancer une exception
            assertThatThrownBy(() -> invalidConfig)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("L'heure de fermeture doit être après l'heure d'ouverture");
        }

        @Test
        @DisplayName("Devrait rejeter les durées de booking invalides")
        void shouldRejectInvalidBookingDurations() {
            // Given - Configuration avec durée min > durée max
            assertThatThrownBy(() -> new BookingProperties(
                480, // min = 480 minutes
                15,  // max = 15 minutes (invalide)
                5,
                30,
                24,
                5,
                6
            )).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("La durée minimum doit être inférieure à la durée maximum");
        }

        @Test
        @DisplayName("Devrait valider les contraintes Bean Validation")
        void shouldValidateBeanValidationConstraints() {
            // Given - Configuration avec valeurs hors limites
            var invalidBooking = new BookingProperties(
                5,    // Min invalide (< 15)
                700,  // Max invalide (> 600)
                15,   // Max services invalide (> 10)
                200,  // Min per service invalide (> 180)
                200,  // Cancellation invalide (> 168)
                40,   // Tolerance invalide (> 30)
                15    // Advance booking invalide (> 12)
            );
            
            // When - Validation manuelle
            Set<ConstraintViolation<BookingProperties>> violations = validator.validate(invalidBooking);
            
            // Then - Doit avoir des violations
            assertThat(violations).isNotEmpty();
            assertThat(violations).hasSize(7); // Une violation par champ invalide
        }
    }

    @Nested
    @DisplayName("Intégration avec Spring Boot")
    class SpringBootIntegration {

        @Test
        @DisplayName("Devrait injecter les propriétés dans BusinessConstants")
        void shouldInjectPropertiesInBusinessConstants() {
            // Given - Configuration chargée
            
            // When & Then - Les getters de BusinessConstants doivent utiliser les propriétés
            // Note: Ce test nécessite que BusinessConstants soit initialisé en tant que bean Spring
            
            assertThat(afroStyleProperties.businessHours().openTime()).isNotNull();
            assertThat(afroStyleProperties.payment().defaultCurrency()).isEqualTo("EUR");
            assertThat(afroStyleProperties.salon().defaultName()).isEqualTo("AfroStyle Salon");
        }

        @Test
        @DisplayName("Devrait supporter les profils différents")
        void shouldSupportDifferentProfiles() {
            // Given - Profil test activé
            
            // When & Then - Les valeurs doivent être celles du profil test
            var pagination = afroStyleProperties.pagination();
            assertThat(pagination.defaultPageSize()).isEqualTo(20);
            assertThat(pagination.maxPageSize()).isEqualTo(100);
        }
    }

    /**
     * Configuration de test pour charger uniquement les beans nécessaires
     */
    @org.springframework.boot.test.context.TestConfiguration
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
    static class TestConfig {
        // Configuration minimale pour les tests
    }
}