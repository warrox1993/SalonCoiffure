package com.jb.afrostyle.core.validation;

import com.jb.afrostyle.core.constants.BusinessConstants;
import com.jb.afrostyle.core.constants.ValidationConstants;
import com.jb.afrostyle.core.enums.EntityType;
import com.jb.afrostyle.core.enums.Operation;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.Collection;
import java.util.regex.Pattern;
import com.jb.afrostyle.core.util.PatternUtils;
import java.util.Set;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Utilitaires de validation centralisés avec Java 21 Pattern Matching
 * Version centralisée dans /core avec intégration des constantes et enums
 * Remplace tous les validateurs éparpillés dans les modules
 * 
 * @version 2.0 - Centralisée dans /core
 * @since Java 21
 */
public final class ValidationUtils {
    
    // ==================== PATTERNS COMPILÉS ====================
    
    // Patterns importés depuis PatternUtils centralisé
    private static final Pattern EMAIL_PATTERN = PatternUtils.EMAIL_PATTERN;
    private static final Pattern PHONE_PATTERN = PatternUtils.PHONE_PATTERN;
    private static final Pattern BELGIAN_PHONE_PATTERN = PatternUtils.BELGIAN_PHONE_PATTERN;
    private static final Pattern STRONG_PASSWORD_PATTERN = PatternUtils.STRONG_PASSWORD_PATTERN;
    private static final Pattern USERNAME_PATTERN = PatternUtils.USERNAME_PATTERN;
    private static final Pattern NAME_PATTERN = PatternUtils.NAME_PATTERN;
    private static final Pattern URL_PATTERN = PatternUtils.URL_PATTERN;
    
    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // ==================== VALIDATIONS STRING DE BASE ====================
    
    /**
     * Valide si une chaîne n'est pas null et non vide (JAVA 21 Pattern Matching)
     * @param value Valeur à valider
     * @param fieldName Nom du champ pour les messages d'erreur
     * @return ValidationResult avec la valeur trimée ou erreur
     */
    public static ValidationResult<String> validateNotNullOrEmpty(String value, String fieldName) {
        return switch (value) {
            case null -> ValidationResult.error(fieldName + " cannot be null");
            case String s when s.trim().isEmpty() -> 
                ValidationResult.error(fieldName + " cannot be empty");
            case String s -> ValidationResult.success(s.trim());
        };
    }
    
    /**
     * Valide une chaîne avec longueur minimale et maximale
     * @param value Valeur à valider
     * @param fieldName Nom du champ
     * @param minLength Longueur minimale
     * @param maxLength Longueur maximale
     * @return ValidationResult avec validation de longueur
     */
    public static ValidationResult<String> validateStringLength(
            String value, String fieldName, int minLength, int maxLength) {
        return switch (validateNotNullOrEmpty(value, fieldName)) {
            case ValidationResult.Error<String> error -> error;
            case ValidationResult.Success<String>(var trimmedValue) -> {
                int len = trimmedValue.length();
                if (len < minLength) {
                    yield ValidationResult.error(
                        fieldName + " must be at least " + minLength + " characters",
                        "LENGTH_TOO_SHORT"
                    );
                } else if (len > maxLength) {
                    yield ValidationResult.error(
                        fieldName + " must not exceed " + maxLength + " characters",
                        "LENGTH_TOO_LONG"
                    );
                } else {
                    yield ValidationResult.success(trimmedValue);
                }
            }
        };
    }
    
    // ==================== VALIDATIONS EMAIL ET TÉLÉPHONE ====================
    
    /**
     * Validation d'email avec Pattern Matching Java 21
     * @param email Email à valider
     * @return ValidationResult avec email normalisé ou erreur
     */
    public static ValidationResult<String> validateEmail(String email) {
        return switch (validateNotNullOrEmpty(email, "Email")) {
            case ValidationResult.Error<String> error -> error;
            case ValidationResult.Success<String>(var validEmail) -> {
                if (validEmail.length() > BusinessConstants.MAX_EMAIL_LENGTH) {
                    yield ValidationResult.error(ValidationConstants.EMAIL_TOO_LONG_MESSAGE);
                } else if (EMAIL_PATTERN.matcher(validEmail).matches()) {
                    yield ValidationResult.success(validEmail.toLowerCase());
                } else {
                    yield ValidationResult.error(
                        ValidationConstants.INVALID_EMAIL_MESSAGE + ": " + validEmail,
                        "INVALID_EMAIL_FORMAT"
                    );
                }
            }
        };
    }
    
    /**
     * Validation de numéro de téléphone avec support belge
     * @param phone Numéro de téléphone à valider
     * @param preferBelgian true pour privilégier le format belge
     * @return ValidationResult avec numéro nettoyé ou erreur
     */
    public static ValidationResult<String> validatePhoneNumber(String phone, boolean preferBelgian) {
        return switch (validateNotNullOrEmpty(phone, "Phone number")) {
            case ValidationResult.Error<String> error -> error;
            case ValidationResult.Success<String>(var validPhone) -> {
                String cleanPhone = validPhone.replaceAll("[\\s\\-\\(\\)\\.]", "");
                
                boolean isValid = preferBelgian 
                    ? BELGIAN_PHONE_PATTERN.matcher(cleanPhone).matches()
                    : PHONE_PATTERN.matcher(cleanPhone).matches();
                    
                if (isValid) {
                    yield ValidationResult.success(cleanPhone);
                } else {
                    yield ValidationResult.error(
                        ValidationConstants.INVALID_PHONE_MESSAGE + ": " + phone,
                        "INVALID_PHONE_FORMAT"
                    );
                }
            }
        };
    }
    
    /**
     * Validation de numéro de téléphone (format international par défaut)
     * @param phone Numéro de téléphone
     * @return ValidationResult avec numéro validé
     */
    public static ValidationResult<String> validatePhoneNumber(String phone) {
        return validatePhoneNumber(phone, false);
    }
    
    // ==================== VALIDATIONS MOT DE PASSE ====================
    
    /**
     * Validation de mot de passe fort avec critères stricts
     * @param password Mot de passe à valider
     * @return ValidationResult avec mot de passe validé ou erreur détaillée
     */
    public static ValidationResult<String> validatePassword(String password) {
        return switch (validateNotNullOrEmpty(password, "Password")) {
            case ValidationResult.Error<String> error -> error;
            case ValidationResult.Success<String>(var validPassword) -> {
                if (validPassword.length() < BusinessConstants.MIN_PASSWORD_LENGTH) {
                    yield ValidationResult.error(ValidationConstants.PASSWORD_TOO_SHORT_MESSAGE);
                } else if (validPassword.length() > BusinessConstants.MAX_PASSWORD_LENGTH) {
                    yield ValidationResult.error(ValidationConstants.PASSWORD_TOO_LONG_MESSAGE);
                } else if (!STRONG_PASSWORD_PATTERN.matcher(validPassword).matches()) {
                    yield ValidationResult.error(
                        ValidationConstants.WEAK_PASSWORD_MESSAGE,
                        "WEAK_PASSWORD"
                    );
                } else {
                    yield ValidationResult.success(validPassword);
                }
            }
        };
    }
    
    /**
     * Validation de nom d'utilisateur
     * @param username Nom d'utilisateur à valider
     * @return ValidationResult avec username validé
     */
    public static ValidationResult<String> validateUsername(String username) {
        return switch (validateStringLength(username, "Username", 3, BusinessConstants.MAX_USERNAME_LENGTH)) {
            case ValidationResult.Error<String> error -> error;
            case ValidationResult.Success<String>(var validUsername) -> {
                if (USERNAME_PATTERN.matcher(validUsername).matches()) {
                    yield ValidationResult.success(validUsername);
                } else {
                    yield ValidationResult.error(
                        ValidationConstants.INVALID_USERNAME_MESSAGE,
                        "INVALID_USERNAME_FORMAT"
                    );
                }
            }
        };
    }
    
    /**
     * Validation de nom/prénom
     * @param name Nom à valider
     * @param fieldName Nom du champ
     * @return ValidationResult avec nom validé
     */
    public static ValidationResult<String> validateName(String name, String fieldName) {
        return switch (validateStringLength(name, fieldName, 2, BusinessConstants.MAX_FULL_NAME_LENGTH)) {
            case ValidationResult.Error<String> error -> error;
            case ValidationResult.Success<String>(var validName) -> {
                if (NAME_PATTERN.matcher(validName).matches()) {
                    yield ValidationResult.success(validName);
                } else {
                    yield ValidationResult.error(
                        ValidationConstants.INVALID_NAME_MESSAGE,
                        "INVALID_NAME_FORMAT"
                    );
                }
            }
        };
    }
    
    // ==================== VALIDATIONS NUMÉRIQUES ====================
    
    /**
     * Valide un ID positif avec Pattern Matching
     * @param id ID à valider
     * @param entityType Type d'entité pour contexte
     * @return ValidationResult avec ID validé
     */
    public static ValidationResult<Long> validatePositiveId(Long id, EntityType entityType) {
        return switch (id) {
            case null -> ValidationResult.error(
                entityType.getDisplayName() + " ID cannot be null",
                "NULL_ID",
                entityType,
                Operation.VALIDATE
            );
            case Long idValue when idValue <= 0 -> ValidationResult.error(
                entityType.getDisplayName() + " ID must be positive, got: " + idValue,
                "INVALID_ID",
                entityType,
                Operation.VALIDATE
            );
            case Long validId -> ValidationResult.success(validId);
        };
    }
    
    /**
     * Valide un montant avec limites par devise
     * @param amount Montant à valider
     * @param currency Devise
     * @param fieldName Nom du champ
     * @return ValidationResult avec montant validé
     */
    public static ValidationResult<BigDecimal> validateAmount(
            BigDecimal amount, String currency, String fieldName) {
        return switch (amount) {
            case null -> ValidationResult.error(fieldName + " cannot be null");
            case BigDecimal amt when amt.compareTo(BigDecimal.ZERO) <= 0 -> 
                ValidationResult.error(
                    ValidationConstants.INVALID_AMOUNT_MESSAGE + ", got: " + amt,
                    "AMOUNT_NOT_POSITIVE"
                );
            case BigDecimal amt when amt.scale() > 2 -> 
                ValidationResult.error(
                    ValidationConstants.INVALID_DECIMAL_PLACES_MESSAGE,
                    "TOO_MANY_DECIMALS"
                );
            case BigDecimal amt when amt.compareTo(BusinessConstants.MAX_PAYMENT_AMOUNT) > 0 -> 
                ValidationResult.error(
                    ValidationConstants.AMOUNT_TOO_LARGE_MESSAGE,
                    "AMOUNT_TOO_LARGE"
                );
            case BigDecimal amt -> {
                BigDecimal minAmount = BusinessConstants.getMinimumAmount(currency);
                if (amt.compareTo(minAmount) < 0) {
                    yield ValidationResult.error(
                        "Minimum amount for " + currency + " is " + minAmount,
                        "AMOUNT_TOO_SMALL"
                    );
                } else {
                    yield ValidationResult.success(amt);
                }
            }
        };
    }
    
    // ==================== VALIDATIONS DE COLLECTION ====================
    
    /**
     * Valide qu'une collection n'est pas null ou vide
     * @param collection Collection à valider
     * @param fieldName Nom du champ
     * @return ValidationResult avec collection validée
     */
    public static <T> ValidationResult<Collection<T>> validateNotEmptyCollection(
            Collection<T> collection, String fieldName) {
        return switch (collection) {
            case null -> ValidationResult.error(fieldName + " cannot be null");
            case Collection<T> coll when coll.isEmpty() -> 
                ValidationResult.error(fieldName + " cannot be empty");
            case Collection<T> validCollection -> ValidationResult.success(validCollection);
        };
    }
    
    /**
     * Valide la taille d'une collection
     * @param collection Collection à valider
     * @param fieldName Nom du champ
     * @param minSize Taille minimale
     * @param maxSize Taille maximale
     * @return ValidationResult avec collection validée
     */
    public static <T> ValidationResult<Collection<T>> validateCollectionSize(
            Collection<T> collection, String fieldName, int minSize, int maxSize) {
        return switch (validateNotEmptyCollection(collection, fieldName)) {
            case ValidationResult.Error<Collection<T>> error -> error;
            case ValidationResult.Success<Collection<T>>(var validCollection) -> {
                int size = validCollection.size();
                if (size < minSize) {
                    yield ValidationResult.error(
                        fieldName + " must contain at least " + minSize + " items"
                    );
                } else if (size > maxSize) {
                    yield ValidationResult.error(
                        fieldName + " cannot contain more than " + maxSize + " items"
                    );
                } else {
                    yield ValidationResult.success(validCollection);
                }
            }
        };
    }
    
    // ==================== VALIDATIONS DATE/HEURE ====================
    
    /**
     * Valide qu'une date/heure est dans le futur avec tolérance
     * @param dateTime Date/heure à valider
     * @param fieldName Nom du champ
     * @return ValidationResult avec date validée
     */
    public static ValidationResult<LocalDateTime> validateFutureDateTime(
            LocalDateTime dateTime, String fieldName) {
        return switch (dateTime) {
            case null -> ValidationResult.error(fieldName + " cannot be null");
            case LocalDateTime dt when dt.isBefore(
                LocalDateTime.now().minusMinutes(BusinessConstants.BOOKING_TIME_TOLERANCE_MINUTES)
            ) -> ValidationResult.error(
                fieldName + " must be in the future (5 min tolerance)",
                "DATETIME_IN_PAST"
            );
            case LocalDateTime validDateTime -> ValidationResult.success(validDateTime);
        };
    }
    
    /**
     * Valide qu'une heure est dans les heures d'ouverture
     * @param time Heure à valider
     * @param fieldName Nom du champ
     * @return ValidationResult avec heure validée
     */
    public static ValidationResult<LocalTime> validateBusinessHours(LocalTime time, String fieldName) {
        return switch (time) {
            case null -> ValidationResult.error(fieldName + " cannot be null");
            case LocalTime t when t.isBefore(BusinessConstants.BUSINESS_OPEN_TIME) -> 
                ValidationResult.error(
                    fieldName + " must be within business hours (8:00-20:00)",
                    "OUTSIDE_BUSINESS_HOURS"
                );
            case LocalTime t when t.isAfter(BusinessConstants.BUSINESS_CLOSE_TIME) -> 
                ValidationResult.error(
                    fieldName + " must be within business hours (8:00-20:00)",
                    "OUTSIDE_BUSINESS_HOURS"
                );
            case LocalTime validTime -> ValidationResult.success(validTime);
        };
    }
    
    /**
     * Valide qu'une heure est sur des intervalles de 15 minutes
     * @param time Heure à valider
     * @param fieldName Nom du champ
     * @return ValidationResult avec heure validée
     */
    public static ValidationResult<LocalTime> validateTimeSlotInterval(LocalTime time, String fieldName) {
        return switch (time) {
            case null -> ValidationResult.error(fieldName + " cannot be null");
            case LocalTime t when t.getMinute() % BusinessConstants.TIME_SLOT_INTERVAL_MINUTES != 0 -> 
                ValidationResult.error(
                    fieldName + " must be on 15-minute intervals",
                    "INVALID_TIME_INTERVAL"
                );
            case LocalTime validTime -> ValidationResult.success(validTime);
        };
    }
    
    /**
     * Valide qu'un jour est un jour ouvrable
     * @param dateTime Date/heure à valider
     * @return ValidationResult avec date validée
     */
    public static ValidationResult<LocalDateTime> validateBusinessDay(LocalDateTime dateTime) {
        return switch (dateTime) {
            case null -> ValidationResult.error("Date cannot be null");
            case LocalDateTime dt when !BusinessConstants.isBusinessDay(dt.getDayOfWeek().getValue()) -> 
                ValidationResult.error(
                    "Bookings are not allowed on Sundays",
                    "CLOSED_DAY"
                );
            case LocalDateTime validDateTime -> ValidationResult.success(validDateTime);
        };
    }
    
    // ==================== VALIDATIONS MÉTIER ====================
    
    /**
     * Valide une durée de réservation
     * @param startTime Heure de début
     * @param endTime Heure de fin
     * @return ValidationResult avec durée validée
     */
    public static ValidationResult<Integer> validateBookingDuration(
            LocalDateTime startTime, LocalDateTime endTime) {
        return switch (startTime) {
            case null -> ValidationResult.error("Start time cannot be null");
            case LocalDateTime start -> switch (endTime) {
                case null -> ValidationResult.error("End time cannot be null");
                case LocalDateTime end when !end.isAfter(start) -> 
                    ValidationResult.error("End time must be after start time");
                case LocalDateTime end -> {
                    int durationMinutes = (int) java.time.Duration.between(start, end).toMinutes();
                    if (durationMinutes < BusinessConstants.MIN_BOOKING_DURATION_MINUTES) {
                        yield ValidationResult.error(
                            "Booking duration must be at least " + 
                            BusinessConstants.MIN_BOOKING_DURATION_MINUTES + " minutes"
                        );
                    } else if (durationMinutes > BusinessConstants.MAX_BOOKING_DURATION_MINUTES) {
                        yield ValidationResult.error(
                            "Booking duration cannot exceed " + 
                            BusinessConstants.MAX_BOOKING_DURATION_MINUTES + " minutes"
                        );
                    } else {
                        yield ValidationResult.success(durationMinutes);
                    }
                }
            };
        };
    }
    
    /**
     * Valide une devise
     * @param currency Code devise à valider
     * @return ValidationResult avec devise validée
     */
    public static ValidationResult<String> validateCurrency(String currency) {
        return switch (validateNotNullOrEmpty(currency, "Currency")) {
            case ValidationResult.Error<String> error -> error;
            case ValidationResult.Success<String>(var validCurrency) -> {
                String upperCurrency = validCurrency.toUpperCase();
                if (upperCurrency.length() != 3) {
                    yield ValidationResult.error(
                        ValidationConstants.INVALID_CURRENCY_MESSAGE,
                        "INVALID_CURRENCY_FORMAT"
                    );
                } else if (!BusinessConstants.isSupportedCurrency(upperCurrency)) {
                    yield ValidationResult.error(
                        ValidationConstants.UNSUPPORTED_CURRENCY_MESSAGE + ": " + upperCurrency,
                        "UNSUPPORTED_CURRENCY"
                    );
                } else {
                    yield ValidationResult.success(upperCurrency);
                }
            }
        };
    }
    
    /**
     * Valide une URL
     * @param url URL à valider
     * @param fieldName Nom du champ
     * @param requireHttps true pour exiger HTTPS
     * @return ValidationResult avec URL validée
     */
    public static ValidationResult<String> validateUrl(String url, String fieldName, boolean requireHttps) {
        return switch (validateNotNullOrEmpty(url, fieldName)) {
            case ValidationResult.Error<String> error -> error;
            case ValidationResult.Success<String>(var validUrl) -> {
                try {
                    URL urlObj = new URL(validUrl);
                    String protocol = urlObj.getProtocol().toLowerCase();
                    
                    if (!protocol.equals("http") && !protocol.equals("https")) {
                        yield ValidationResult.error(
                            fieldName + " must use HTTP or HTTPS protocol",
                            "INVALID_URL_PROTOCOL"
                        );
                    } else if (requireHttps && !protocol.equals("https") && 
                               !urlObj.getHost().equals("localhost") && 
                               !urlObj.getHost().startsWith("127.")) {
                        yield ValidationResult.error(
                            fieldName + " must use HTTPS in production",
                            "HTTPS_REQUIRED"
                        );
                    } else {
                        yield ValidationResult.success(validUrl);
                    }
                } catch (MalformedURLException e) {
                    yield ValidationResult.error(
                        fieldName + " is not a valid URL: " + validUrl,
                        "MALFORMED_URL"
                    );
                }
            }
        };
    }
    
    // ==================== VALIDATIONS COMPOSITES ====================
    
    /**
     * Validation composite pour utilisateur complet
     * @param username Nom d'utilisateur
     * @param email Email
     * @param phone Téléphone
     * @param password Mot de passe
     * @param fullName Nom complet
     * @return ValidationResult avec UserValidation ou erreurs
     */
    public static ValidationResult<UserValidation> validateUser(
            String username, String email, String phone, String password, String fullName) {
        
        var usernameResult = validateUsername(username);
        var emailResult = validateEmail(email);
        var phoneResult = validatePhoneNumber(phone);
        var passwordResult = validatePassword(password);
        var fullNameResult = validateName(fullName, "Full name");
        
        // Utilisation de la méthode combine de ValidationResult
        return usernameResult
            .combine(emailResult, (u, e) -> new UserValidation(u, e, null, null, null))
            .combine(phoneResult, (partial, p) -> new UserValidation(
                partial.username(), partial.email(), p, null, null))
            .combine(passwordResult, (partial, pw) -> new UserValidation(
                partial.username(), partial.email(), partial.phone(), pw, null))
            .combine(fullNameResult, (partial, fn) -> new UserValidation(
                partial.username(), partial.email(), partial.phone(), 
                partial.password(), fn));
    }
    
    // ==================== RECORDS DE VALIDATION ====================
    
    /**
     * Record pour les données utilisateur validées
     */
    public record UserValidation(
        String username,
        String email, 
        String phone,
        String password,
        String fullName
    ) {}
    
    /**
     * Record pour les données de réservation validées
     */
    public record BookingValidation(
        Long customerId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Set<Long> serviceIds,
        Integer durationMinutes
    ) {}
    
    /**
     * Record pour les données de paiement validées
     */
    public record PaymentValidation(
        Long bookingId,
        BigDecimal amount,
        String currency,
        String description
    ) {}
    
    // ==================== VALIDATIONS HORAIRES ====================
    
    /**
     * Valide une plage horaire
     * @param startTime Heure de début
     * @param endTime Heure de fin
     * @return ValidationResult avec plage validée
     */
    public static ValidationResult<TimeRange> validateTimeRange(LocalTime startTime, LocalTime endTime) {
        return switch (startTime) {
            case null -> ValidationResult.error("Start time cannot be null");
            case LocalTime start -> switch (endTime) {
                case null -> ValidationResult.error("End time cannot be null");
                case LocalTime end when !end.isAfter(start) -> 
                    ValidationResult.error("End time must be after start time");
                case LocalTime end -> ValidationResult.success(new TimeRange(start, end));
            };
        };
    }
    
    /**
     * Record pour une plage horaire validée
     */
    public record TimeRange(
        LocalTime startTime,
        LocalTime endTime
    ) {}
}