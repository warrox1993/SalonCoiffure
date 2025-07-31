package com.jb.afrostyle.core.constants;

import com.jb.afrostyle.core.config.properties.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Set;

/**
 * Constantes métier centralisées pour AfroStyle
 * 
 * MIGRATION VERS @ConfigurationProperties COMPLÉTÉE !
 * Cette classe est maintenant un facade/adapter pour maintenir la compatibilité
 * ascendante avec le code existant qui utilise ces constantes statiques.
 * 
 * Les vraies valeurs sont maintenant externalisées dans application.yml
 * et injectées via @ConfigurationProperties avec validation automatique.
 * 
 * @version 2.0
 * @since Java 21
 * @deprecated Utiliser directement les @ConfigurationProperties injectées
 */
@Component
@Deprecated(since = "2.0", forRemoval = false)
public final class BusinessConstants {
    
    private static AfroStyleProperties properties;
    
    @Autowired
    public BusinessConstants(AfroStyleProperties properties) {
        BusinessConstants.properties = properties;
    }
    
    private BusinessConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // ==================== HEURES D'OUVERTURE ====================
    // Valeurs externalisées dans application.yml -> afrostyle.business.hours.*
    
    /** Heure d'ouverture du salon */
    public static LocalTime getBusinessOpenTime() {
        return properties != null ? properties.businessHours().openTime() : LocalTime.of(8, 0);
    }
    
    /** Heure de fermeture du salon */
    public static LocalTime getBusinessCloseTime() {
        return properties != null ? properties.businessHours().closeTime() : LocalTime.of(20, 0);
    }
    
    /** Intervalles de créneaux en minutes */
    public static int getTimeSlotIntervalMinutes() {
        return properties != null ? properties.businessHours().timeSlotIntervalMinutes() : 15;
    }
    
    /** Jours de fermeture (dimanche = 7) */
    public static Set<Integer> getClosedDays() {
        return properties != null ? properties.businessHours().closedDays() : Set.of(7);
    }
    
    // Compatibilité ascendante - DEPRECATED
    @Deprecated(since = "2.0", forRemoval = true)
    public static final LocalTime BUSINESS_OPEN_TIME = LocalTime.of(8, 0);
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final LocalTime BUSINESS_CLOSE_TIME = LocalTime.of(20, 0);
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int TIME_SLOT_INTERVAL_MINUTES = 15;
    
    // Constantes manquantes pour compatibilité
    @Deprecated(since = "2.0", forRemoval = true)
    public static final Duration MIN_CANCELLATION_NOTICE = Duration.ofHours(24);
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int SLOT_INTERVAL_MINUTES = 15;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final Set<Integer> CLOSED_DAYS = Set.of(7);
    
    // Constantes additionnelles manquantes
    @Deprecated(since = "2.0", forRemoval = true)
    public static final Duration MIN_BOOKING_ADVANCE = Duration.ofMinutes(30);
    
    @Deprecated(since = "2.0", forRemoval = true)  
    public static final int MIN_CANCELLATION_NOTICE_HOURS = 24;
    
    // ==================== RÉSERVATIONS ====================
    // Valeurs externalisées dans application.yml -> afrostyle.booking.*
    
    /** Durée minimum d'une réservation en minutes */
    public static int getMinBookingDurationMinutes() {
        return properties != null ? properties.booking().minDurationMinutes() : 15;
    }
    
    /** Durée maximum d'une réservation en minutes */
    public static int getMaxBookingDurationMinutes() {
        return properties != null ? properties.booking().maxDurationMinutes() : 480;
    }
    
    /** Nombre maximum de services par réservation */
    public static int getMaxServicesPerBooking() {
        return properties != null ? properties.booking().maxServicesPerBooking() : 5;
    }
    
    /** Durée minimum par service en minutes */
    public static int getMinDurationPerServiceMinutes() {
        return properties != null ? properties.booking().minDurationPerServiceMinutes() : 30;
    }
    
    /** Délai d'annulation en heures */
    public static int getCancellationDeadlineHours() {
        return properties != null ? properties.booking().cancellationDeadlineHours() : 24;
    }
    
    /** Tolérance pour réservations (minutes) */
    public static int getBookingTimeToleranceMinutes() {
        return properties != null ? properties.booking().timeToleranceMinutes() : 5;
    }
    
    /** Maximum de mois pour réserver à l'avance */
    public static int getMaxAdvanceBookingMonths() {
        return properties != null ? properties.booking().maxAdvanceBookingMonths() : 6;
    }
    
    // Compatibilité ascendante - DEPRECATED
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int MIN_BOOKING_DURATION_MINUTES = 15;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int MAX_BOOKING_DURATION_MINUTES = 480;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int MAX_SERVICES_PER_BOOKING = 5;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int MIN_DURATION_PER_SERVICE_MINUTES = 30;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int CANCELLATION_DEADLINE_HOURS = 24;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int BOOKING_TIME_TOLERANCE_MINUTES = 5;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int MAX_ADVANCE_BOOKING_MONTHS = 6;
    
    // ==================== PAIEMENTS ====================
    // Valeurs externalisées dans application.yml -> afrostyle.payment.*
    
    /** Montant maximum de paiement */
    public static BigDecimal getMaxPaymentAmount() {
        return properties != null ? properties.payment().amounts().maxAmount() : new BigDecimal("10000.00");
    }
    
    /** Limite pour paiements en espèces (EUR uniquement) */
    public static BigDecimal getCashPaymentLimitEur() {
        return properties != null ? properties.payment().cashLimitEur() : new BigDecimal("500.00");
    }
    
    /** Seuil pour devises majeures obligatoires */
    public static BigDecimal getHighAmountThreshold() {
        return properties != null ? properties.payment().highAmountThreshold() : new BigDecimal("1000.00");
    }
    
    /** Prix minimum par service */
    public static BigDecimal getMinPricePerService() {
        return properties != null ? properties.payment().amounts().minPricePerService() : new BigDecimal("5.00");
    }
    
    // Compatibilité ascendante - DEPRECATED
    @Deprecated(since = "2.0", forRemoval = true)
    public static final BigDecimal MIN_PAYMENT_AMOUNT_EUR = new BigDecimal("0.50");
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final BigDecimal MIN_PAYMENT_AMOUNT_USD = new BigDecimal("0.50");
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final BigDecimal MIN_PAYMENT_AMOUNT_GBP = new BigDecimal("0.30");
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final BigDecimal MIN_PAYMENT_AMOUNT_CAD = new BigDecimal("0.50");
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final BigDecimal MAX_PAYMENT_AMOUNT = new BigDecimal("10000.00");
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final BigDecimal CASH_PAYMENT_LIMIT_EUR = new BigDecimal("500.00");
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("1000.00");
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final BigDecimal MIN_PRICE_PER_SERVICE = new BigDecimal("5.00");
    
    // ==================== DEVISES SUPPORTÉES ====================
    // Valeurs externalisées dans application.yml -> afrostyle.payment.*
    
    /** Devises supportées */
    public static Set<String> getSupportedCurrencies() {
        return properties != null ? properties.payment().supportedCurrencies() : Set.of("EUR", "USD", "GBP", "CAD");
    }
    
    /** Devises majeures */
    public static Set<String> getMajorCurrencies() {
        return properties != null ? properties.payment().majorCurrencies() : Set.of("EUR", "USD", "GBP");
    }
    
    /** Devise par défaut */
    public static String getDefaultCurrency() {
        return properties != null ? properties.payment().defaultCurrency() : "EUR";
    }
    
    // Compatibilité ascendante - DEPRECATED
    @Deprecated(since = "2.0", forRemoval = true)
    public static final Set<String> SUPPORTED_CURRENCIES = Set.of("EUR", "USD", "GBP", "CAD");
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final Set<String> MAJOR_CURRENCIES = Set.of("EUR", "USD", "GBP");
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final String DEFAULT_CURRENCY = "EUR";
    
    // ==================== UTILISATEURS ====================
    // Valeurs externalisées dans application.yml -> afrostyle.user.*
    
    /** Longueur minimum du mot de passe */
    public static int getMinPasswordLength() {
        return properties != null ? properties.user().password().minLength() : 8;
    }
    
    /** Longueur maximum du mot de passe */
    public static int getMaxPasswordLength() {
        return properties != null ? properties.user().password().maxLength() : 128;
    }
    
    /** Longueur maximum du nom d'utilisateur */
    public static int getMaxUsernameLength() {
        return properties != null ? properties.user().profile().maxUsernameLength() : 50;
    }
    
    /** Longueur maximum de l'email */
    public static int getMaxEmailLength() {
        return properties != null ? properties.user().profile().maxEmailLength() : 100;
    }
    
    /** Longueur maximum du nom complet */
    public static int getMaxFullNameLength() {
        return properties != null ? properties.user().profile().maxFullNameLength() : 100;
    }
    
    /** Longueur maximum de la description de paiement */
    public static int getMaxPaymentDescriptionLength() {
        return properties != null ? properties.user().profile().maxPaymentDescriptionLength() : 200;
    }
    
    // Compatibilité ascendante - DEPRECATED
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int MIN_PASSWORD_LENGTH = 8;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int MAX_PASSWORD_LENGTH = 128;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int MAX_USERNAME_LENGTH = 50;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int MAX_EMAIL_LENGTH = 100;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int MAX_FULL_NAME_LENGTH = 100;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int MAX_PAYMENT_DESCRIPTION_LENGTH = 200;
    
    // ==================== SALONS ====================
    // Valeurs externalisées dans application.yml -> afrostyle.salon.*
    
    /** Nom du salon par défaut */
    public static String getDefaultSalonName() {
        return properties != null ? properties.salon().defaultName() : "AfroStyle Salon";
    }
    
    /** Adresse du salon par défaut */
    public static String getDefaultSalonAddress() {
        return properties != null ? properties.salon().defaultAddress() : "Brussels, Belgium";
    }
    
    /** Nombre maximum de salons par propriétaire */
    public static int getMaxSalonsPerOwner() {
        return properties != null ? properties.salon().maxSalonsPerOwner() : 10;
    }
    
    // Compatibilité ascendante - DEPRECATED
    @Deprecated(since = "2.0", forRemoval = true)
    public static final String DEFAULT_SALON_NAME = "AfroStyle Salon";
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final String DEFAULT_SALON_ADDRESS = "Brussels, Belgium";
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int MAX_SALONS_PER_OWNER = 10;
    
    // ==================== NOTIFICATIONS ====================
    // Valeurs externalisées dans application.yml -> afrostyle.notification.*
    
    /** Délai avant envoi de notification de rappel (heures) */
    public static int getReminderNotificationHours() {
        return properties != null ? properties.notification().reminderHours() : 24;
    }
    
    /** Nombre maximum de tentatives d'envoi */
    public static int getMaxNotificationAttempts() {
        return properties != null ? properties.notification().maxAttempts() : 5;
    }
    
    /** Délai entre tentatives d'envoi (minutes) */
    public static int getNotificationRetryDelayMinutes() {
        return properties != null ? properties.notification().retryDelayMinutes() : 15;
    }
    
    // Compatibilité ascendante - DEPRECATED
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int REMINDER_NOTIFICATION_HOURS = 24;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int MAX_NOTIFICATION_ATTEMPTS = 5;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int NOTIFICATION_RETRY_DELAY_MINUTES = 15;
    
    // ==================== PAGINATION ====================
    // Valeurs externalisées dans application.yml -> afrostyle.pagination.*
    
    /** Taille de page par défaut */
    public static int getDefaultPageSize() {
        return properties != null ? properties.pagination().defaultPageSize() : 20;
    }
    
    /** Taille de page maximum */
    public static int getMaxPageSize() {
        return properties != null ? properties.pagination().maxPageSize() : 100;
    }
    
    /** Page par défaut */
    public static int getDefaultPageNumber() {
        return properties != null ? properties.pagination().defaultPageNumber() : 0;
    }
    
    // Compatibilité ascendante - DEPRECATED
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int DEFAULT_PAGE_SIZE = 20;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int MAX_PAGE_SIZE = 100;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int DEFAULT_PAGE_NUMBER = 0;
    
    // ==================== CACHE ====================
    // Valeurs externalisées dans application.yml -> afrostyle.cache.*
    
    /** Durée de cache pour données utilisateur (minutes) */
    public static int getUserCacheDurationMinutes() {
        return properties != null ? properties.cache().userCacheDurationMinutes() : 30;
    }
    
    /** Durée de cache pour services (heures) */
    public static int getServiceCacheDurationHours() {
        return properties != null ? properties.cache().serviceCacheDurationHours() : 2;
    }
    
    /** Durée de cache pour salons (heures) */
    public static int getSalonCacheDurationHours() {
        return properties != null ? properties.cache().salonCacheDurationHours() : 4;
    }
    
    // Compatibilité ascendante - DEPRECATED
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int USER_CACHE_DURATION_MINUTES = 30;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int SERVICE_CACHE_DURATION_HOURS = 2;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int SALON_CACHE_DURATION_HOURS = 4;
    
    // ==================== VALIDATION ====================
    // Valeurs externalisées dans application.yml -> afrostyle.validation.*
    
    /** Nombre maximum d'erreurs de validation affichées */
    public static int getMaxValidationErrors() {
        return properties != null ? properties.validation().maxErrors() : 10;
    }
    
    /** Longueur minimum des descriptions */
    public static int getMinDescriptionLength() {
        return properties != null ? properties.validation().description().minLength() : 5;
    }
    
    /** Longueur maximum des descriptions */
    public static int getMaxDescriptionLength() {
        return properties != null ? properties.validation().description().maxLength() : 500;
    }
    
    // Compatibilité ascendante - DEPRECATED
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int MAX_VALIDATION_ERRORS = 10;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int MIN_DESCRIPTION_LENGTH = 5;
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    // ==================== MÉTHODES UTILITAIRES MIGRÉES ====================
    
    /**
     * Vérifie si un jour est ouvert
     * @param dayOfWeek Jour de la semaine (1=Lundi, 7=Dimanche)
     * @return true si ouvert, false si fermé
     */
    public static boolean isBusinessDay(int dayOfWeek) {
        return properties != null ? properties.businessHours().isBusinessDay(dayOfWeek) : !Set.of(7).contains(dayOfWeek);
    }
    
    /**
     * Vérifie si une heure est dans les heures d'ouverture
     * @param time Heure à vérifier
     * @return true si dans les heures d'ouverture
     */
    public static boolean isWithinBusinessHours(LocalTime time) {
        return properties != null ? properties.businessHours().isWithinBusinessHours(time) : 
               !time.isBefore(LocalTime.of(8, 0)) && !time.isAfter(LocalTime.of(20, 0));
    }
    
    /**
     * Obtient le montant minimum pour une devise
     * @param currency Code devise (EUR, USD, etc.)
     * @return Montant minimum ou EUR par défaut
     */
    public static BigDecimal getMinimumAmount(String currency) {
        return properties != null ? properties.payment().getMinimumAmount(currency) : 
               switch (currency.toUpperCase()) {
                   case "EUR" -> new BigDecimal("0.50");
                   case "USD" -> new BigDecimal("0.50");
                   case "GBP" -> new BigDecimal("0.30");
                   case "CAD" -> new BigDecimal("0.50");
                   default -> new BigDecimal("0.50");
               };
    }
    
    /**
     * Vérifie si une devise est supportée
     * @param currency Code devise
     * @return true si supportée
     */
    public static boolean isSupportedCurrency(String currency) {
        return properties != null ? properties.payment().isSupportedCurrency(currency) : 
               Set.of("EUR", "USD", "GBP", "CAD").contains(currency.toUpperCase());
    }
    
    /**
     * Vérifie si une devise est majeure
     * @param currency Code devise
     * @return true si majeure
     */
    public static boolean isMajorCurrency(String currency) {
        return properties != null ? properties.payment().isMajorCurrency(currency) : 
               Set.of("EUR", "USD", "GBP").contains(currency.toUpperCase());
    }
}