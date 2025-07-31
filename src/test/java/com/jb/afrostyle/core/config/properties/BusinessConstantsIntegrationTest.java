package com.jb.afrostyle.core.config.properties;

import com.jb.afrostyle.core.constants.BusinessConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests d'intégration entre BusinessConstants et les propriétés externalisées
 * Valide que la migration est complète et que la compatibilité ascendante est assurée
 * 
 * @version 1.0
 * @since Java 21
 */
@SpringBootTest(classes = BusinessConstantsIntegrationTest.TestConfig.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class BusinessConstantsIntegrationTest {

    @Autowired
    private AfroStyleProperties afroStyleProperties;

    @Test
    @DisplayName("Devrait migrer correctement les heures d'ouverture")
    void shouldMigrateBusinessHoursCorrectly() {
        // When - Utilisation des nouvelles méthodes
        LocalTime openTime = BusinessConstants.getBusinessOpenTime();
        LocalTime closeTime = BusinessConstants.getBusinessCloseTime();
        int interval = BusinessConstants.getTimeSlotIntervalMinutes();
        
        // Then - Les valeurs doivent venir des propriétés externalisées
        assertThat(openTime).isEqualTo(afroStyleProperties.businessHours().openTime());
        assertThat(closeTime).isEqualTo(afroStyleProperties.businessHours().closeTime());
        assertThat(interval).isEqualTo(afroStyleProperties.businessHours().timeSlotIntervalMinutes());
        
        // Validation des méthodes utilitaires
        assertThat(BusinessConstants.isBusinessDay(1)).isTrue(); // Lundi
        assertThat(BusinessConstants.isBusinessDay(7)).isFalse(); // Dimanche fermé
        assertThat(BusinessConstants.isWithinBusinessHours(LocalTime.of(10, 0))).isTrue();
        assertThat(BusinessConstants.isWithinBusinessHours(LocalTime.of(22, 0))).isFalse();
    }

    @Test
    @DisplayName("Devrait migrer correctement les propriétés de réservation")
    void shouldMigrateBookingPropertiesCorrectly() {
        // When - Utilisation des nouvelles méthodes
        int minDuration = BusinessConstants.getMinBookingDurationMinutes();
        int maxDuration = BusinessConstants.getMaxBookingDurationMinutes();
        int maxServices = BusinessConstants.getMaxServicesPerBooking();
        int cancellationHours = BusinessConstants.getCancellationDeadlineHours();
        
        // Then - Les valeurs doivent venir des propriétés externalisées
        assertThat(minDuration).isEqualTo(afroStyleProperties.booking().minDurationMinutes());
        assertThat(maxDuration).isEqualTo(afroStyleProperties.booking().maxDurationMinutes());
        assertThat(maxServices).isEqualTo(afroStyleProperties.booking().maxServicesPerBooking());
        assertThat(cancellationHours).isEqualTo(afroStyleProperties.booking().cancellationDeadlineHours());
    }

    @Test
    @DisplayName("Devrait migrer correctement les propriétés de paiement")
    void shouldMigratePaymentPropertiesCorrectly() {
        // When - Utilisation des nouvelles méthodes
        BigDecimal maxAmount = BusinessConstants.getMaxPaymentAmount();
        BigDecimal minPrice = BusinessConstants.getMinPricePerService();
        String defaultCurrency = BusinessConstants.getDefaultCurrency();
        
        // Then - Les valeurs doivent venir des propriétés externalisées
        assertThat(maxAmount).isEqualTo(afroStyleProperties.payment().amounts().maxAmount());
        assertThat(minPrice).isEqualTo(afroStyleProperties.payment().amounts().minPricePerService());
        assertThat(defaultCurrency).isEqualTo(afroStyleProperties.payment().defaultCurrency());
        
        // Test des méthodes utilitaires de devises
        assertThat(BusinessConstants.getMinimumAmount("EUR")).isEqualTo(new BigDecimal("0.50"));
        assertThat(BusinessConstants.isSupportedCurrency("EUR")).isTrue();
        assertThat(BusinessConstants.isMajorCurrency("EUR")).isTrue();
        assertThat(BusinessConstants.isSupportedCurrency("JPY")).isFalse();
    }

    @Test
    @DisplayName("Devrait migrer correctement les propriétés utilisateur")
    void shouldMigrateUserPropertiesCorrectly() {
        // When - Utilisation des nouvelles méthodes
        int minPasswordLength = BusinessConstants.getMinPasswordLength();
        int maxPasswordLength = BusinessConstants.getMaxPasswordLength();
        int maxUsernameLength = BusinessConstants.getMaxUsernameLength();
        int maxEmailLength = BusinessConstants.getMaxEmailLength();
        
        // Then - Les valeurs doivent venir des propriétés externalisées
        assertThat(minPasswordLength).isEqualTo(afroStyleProperties.user().password().minLength());
        assertThat(maxPasswordLength).isEqualTo(afroStyleProperties.user().password().maxLength());
        assertThat(maxUsernameLength).isEqualTo(afroStyleProperties.user().profile().maxUsernameLength());
        assertThat(maxEmailLength).isEqualTo(afroStyleProperties.user().profile().maxEmailLength());
    }

    @Test
    @DisplayName("Devrait migrer correctement les propriétés salon")
    void shouldMigrateSalonPropertiesCorrectly() {
        // When - Utilisation des nouvelles méthodes
        String defaultName = BusinessConstants.getDefaultSalonName();
        String defaultAddress = BusinessConstants.getDefaultSalonAddress();
        int maxSalons = BusinessConstants.getMaxSalonsPerOwner();
        
        // Then - Les valeurs doivent venir des propriétés externalisées
        assertThat(defaultName).isEqualTo(afroStyleProperties.salon().defaultName());
        assertThat(defaultAddress).isEqualTo(afroStyleProperties.salon().defaultAddress());
        assertThat(maxSalons).isEqualTo(afroStyleProperties.salon().maxSalonsPerOwner());
    }

    @Test
    @DisplayName("Devrait migrer correctement les propriétés de pagination")
    void shouldMigratePaginationPropertiesCorrectly() {
        // When - Utilisation des nouvelles méthodes
        int defaultPageSize = BusinessConstants.getDefaultPageSize();
        int maxPageSize = BusinessConstants.getMaxPageSize();
        int defaultPageNumber = BusinessConstants.getDefaultPageNumber();
        
        // Then - Les valeurs doivent venir des propriétés externalisées
        assertThat(defaultPageSize).isEqualTo(afroStyleProperties.pagination().defaultPageSize());
        assertThat(maxPageSize).isEqualTo(afroStyleProperties.pagination().maxPageSize());
        assertThat(defaultPageNumber).isEqualTo(afroStyleProperties.pagination().defaultPageNumber());
    }

    @Test
    @DisplayName("Devrait migrer correctement les propriétés de cache")
    void shouldMigrateCachePropertiesCorrectly() {
        // When - Utilisation des nouvelles méthodes
        int userCacheDuration = BusinessConstants.getUserCacheDurationMinutes();
        int serviceCacheDuration = BusinessConstants.getServiceCacheDurationHours();
        int salonCacheDuration = BusinessConstants.getSalonCacheDurationHours();
        
        // Then - Les valeurs doivent venir des propriétés externalisées
        assertThat(userCacheDuration).isEqualTo(afroStyleProperties.cache().userCacheDurationMinutes());
        assertThat(serviceCacheDuration).isEqualTo(afroStyleProperties.cache().serviceCacheDurationHours());
        assertThat(salonCacheDuration).isEqualTo(afroStyleProperties.cache().salonCacheDurationHours());
    }

    @Test
    @DisplayName("Devrait migrer correctement les propriétés de validation")
    void shouldMigrateValidationPropertiesCorrectly() {
        // When - Utilisation des nouvelles méthodes
        int maxErrors = BusinessConstants.getMaxValidationErrors();
        int minDescriptionLength = BusinessConstants.getMinDescriptionLength();
        int maxDescriptionLength = BusinessConstants.getMaxDescriptionLength();
        
        // Then - Les valeurs doivent venir des propriétés externalisées
        assertThat(maxErrors).isEqualTo(afroStyleProperties.validation().maxErrors());
        assertThat(minDescriptionLength).isEqualTo(afroStyleProperties.validation().description().minLength());
        assertThat(maxDescriptionLength).isEqualTo(afroStyleProperties.validation().description().maxLength());
    }

    @Test
    @DisplayName("Devrait supporter les constantes deprecated pour compatibilité ascendante")
    void shouldSupportDeprecatedConstantsForBackwardCompatibility() {
        // When & Then - Les anciennes constantes doivent encore être accessibles mais deprecated
        
        // Note: Ces assertions vérifient que les constantes deprecated existent encore
        // En production, on devrait voir des warnings de compilation pour leur utilisation
        
        @SuppressWarnings("deprecation")
        var oldOpenTime = BusinessConstants.BUSINESS_OPEN_TIME;
        assertThat(oldOpenTime).isEqualTo(LocalTime.of(8, 0));
        
        @SuppressWarnings("deprecation")
        var oldMaxAmount = BusinessConstants.MAX_PAYMENT_AMOUNT;
        assertThat(oldMaxAmount).isEqualTo(new BigDecimal("10000.00"));
        
        @SuppressWarnings("deprecation")
        var oldDefaultName = BusinessConstants.DEFAULT_SALON_NAME;
        assertThat(oldDefaultName).isEqualTo("AfroStyle Salon");
    }

    /**
     * Configuration de test pour charger BusinessConstants comme bean Spring
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
    @ComponentScan(basePackages = "com.jb.afrostyle.core.constants")
    static class TestConfig {
        // Configuration pour scanner BusinessConstants en tant que @Component
    }
}