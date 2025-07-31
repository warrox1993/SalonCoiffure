package com.jb.afrostyle.core.constants;

import java.util.Set;

/**
 * Constantes de validation centralisées pour AfroStyle
 * Centralise tous les patterns regex, formats et règles de validation
 * 
 * @version 1.0
 * @since Java 21
 */
public final class ValidationConstants {
    
    private ValidationConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // ==================== PATTERNS REGEX ====================
    
    /** Pattern pour validation email RFC 5322 compliant */
    public static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    
    /** Pattern pour numéro de téléphone international */
    public static final String PHONE_PATTERN = 
        "^\\+?[1-9]\\d{1,14}$";
    
    /** Pattern pour numéro de téléphone belge */
    public static final String BELGIAN_PHONE_PATTERN = 
        "^(\\+32|0)[1-9]\\d{7,8}$";
    
    /** Pattern pour mot de passe fort */
    public static final String STRONG_PASSWORD_PATTERN = 
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    
    /** Pattern pour nom d'utilisateur */
    public static final String USERNAME_PATTERN = 
        "^[a-zA-Z0-9._-]{3,30}$";
    
    /** Pattern pour nom/prénom */
    public static final String NAME_PATTERN = 
        "^[a-zA-ZÀ-ÿ\\s\\-']{2,50}$";
    
    /** Pattern pour code postal belge */
    public static final String BELGIAN_POSTAL_CODE_PATTERN = 
        "^[1-9]\\d{3}$";
    
    /** Pattern pour IBAN */
    public static final String IBAN_PATTERN = 
        "^[A-Z]{2}[0-9]{2}[A-Z0-9]{4}[0-9]{7}([A-Z0-9]?){0,16}$";
    
    /** Pattern pour UUID */
    public static final String UUID_PATTERN = 
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    
    /** Pattern pour URL */
    public static final String URL_PATTERN = 
        "^https?://(?:[-\\w.])+(?::[0-9]+)?(?:/(?:[\\w/_.])*(?:\\?(?:[\\w&=%.])*)?(?:#(?:[\\w.])*)?)?$";
    
    /** Pattern pour slug (URL-friendly) */
    public static final String SLUG_PATTERN = 
        "^[a-z0-9]+(?:-[a-z0-9]+)*$";
    
    /** Pattern pour hexadecimal */
    public static final String HEX_COLOR_PATTERN = 
        "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
    
    // ==================== FORMATS DE DATE/HEURE ====================
    
    /** Format de date ISO 8601 */
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
    
    /** Format de date et heure ISO 8601 */
    public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    
    /** Format de date et heure avec timezone */
    public static final String ISO_DATETIME_WITH_TIMEZONE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    
    /** Format d'heure 24h */
    public static final String TIME_24H_FORMAT = "HH:mm";
    
    /** Format d'heure avec secondes */
    public static final String TIME_WITH_SECONDS_FORMAT = "HH:mm:ss";
    
    // ==================== MESSAGES D'ERREUR STANDARDISÉS ====================
    
    /** Messages d'erreur pour email */
    public static final String INVALID_EMAIL_MESSAGE = "Email address format is invalid";
    public static final String EMPTY_EMAIL_MESSAGE = "Email address is required";
    public static final String EMAIL_TOO_LONG_MESSAGE = "Email address cannot exceed 100 characters";
    
    /** Messages d'erreur pour téléphone */
    public static final String INVALID_PHONE_MESSAGE = "Phone number format is invalid";
    public static final String EMPTY_PHONE_MESSAGE = "Phone number is required";
    
    /** Messages d'erreur pour mot de passe */
    public static final String WEAK_PASSWORD_MESSAGE = 
        "Password must contain at least 8 characters with uppercase, lowercase, digit and special character";
    public static final String EMPTY_PASSWORD_MESSAGE = "Password is required";
    public static final String PASSWORD_TOO_SHORT_MESSAGE = "Password must be at least 8 characters long";
    public static final String PASSWORD_TOO_LONG_MESSAGE = "Password cannot exceed 128 characters";
    
    /** Messages d'erreur pour nom */
    public static final String INVALID_NAME_MESSAGE = "Name can only contain letters, spaces, hyphens and apostrophes";
    public static final String EMPTY_NAME_MESSAGE = "Name is required";
    public static final String NAME_TOO_SHORT_MESSAGE = "Name must be at least 2 characters long";
    public static final String NAME_TOO_LONG_MESSAGE = "Name cannot exceed 50 characters";
    
    /** Messages d'erreur pour username */
    public static final String INVALID_USERNAME_MESSAGE = "Username can only contain letters, numbers, dots, hyphens and underscores";
    public static final String USERNAME_TOO_SHORT_MESSAGE = "Username must be at least 3 characters long";
    public static final String USERNAME_TOO_LONG_MESSAGE = "Username cannot exceed 30 characters";
    
    /** Messages d'erreur pour URL */
    public static final String INVALID_URL_MESSAGE = "URL format is invalid";
    public static final String EMPTY_URL_MESSAGE = "URL is required";
    
    /** Messages d'erreur pour devise */
    public static final String INVALID_CURRENCY_MESSAGE = "Currency must be a valid 3-letter ISO 4217 code";
    public static final String UNSUPPORTED_CURRENCY_MESSAGE = "Currency is not supported";
    
    /** Messages d'erreur pour montant */
    public static final String INVALID_AMOUNT_MESSAGE = "Amount must be a positive number";
    public static final String AMOUNT_TOO_SMALL_MESSAGE = "Amount is below minimum required";
    public static final String AMOUNT_TOO_LARGE_MESSAGE = "Amount exceeds maximum allowed";
    public static final String INVALID_DECIMAL_PLACES_MESSAGE = "Amount cannot have more than 2 decimal places";
    
    // ==================== VALIDATION MÉTIER ====================
    
    /** Codes de pays supportés */
    public static final Set<String> SUPPORTED_COUNTRY_CODES = Set.of(
        "BE", "FR", "DE", "NL", "LU", "GB", "US", "CA"
    );
    
    /** Langues supportées */
    public static final Set<String> SUPPORTED_LANGUAGES = Set.of(
        "fr", "en", "nl", "de"
    );
    
    /** Types de fichiers autorisés pour upload */
    public static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    
    /** Extensions de fichiers autorisées */
    public static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "webp"
    );
    
    /** Taille maximum des fichiers uploadés (5MB) */
    public static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
    
    /** Montant maximum pour un paiement (10000.00 EUR) */
    public static final java.math.BigDecimal MAX_PAYMENT_AMOUNT = new java.math.BigDecimal("10000.00");
    
    // ==================== VALIDATION BOOKING ====================
    
    /** Messages d'erreur pour réservations */
    public static final String INVALID_BOOKING_DATE_MESSAGE = "Booking date must be in the future";
    public static final String INVALID_BUSINESS_HOURS_MESSAGE = "Booking must be within business hours (8:00-20:00)";
    public static final String INVALID_TIME_SLOT_MESSAGE = "Booking time must be on 15-minute intervals";
    public static final String BOOKING_TOO_SHORT_MESSAGE = "Booking duration must be at least 15 minutes";
    public static final String BOOKING_TOO_LONG_MESSAGE = "Booking duration cannot exceed 8 hours";
    public static final String SUNDAY_BOOKING_MESSAGE = "Bookings are not allowed on Sundays";
    public static final String TOO_MANY_SERVICES_MESSAGE = "Cannot select more than 5 services per booking";
    public static final String NO_SERVICES_MESSAGE = "At least one service must be selected";
    public static final String INSUFFICIENT_DURATION_PER_SERVICE_MESSAGE = 
        "Minimum 30 minutes required per service";
    
    // ==================== VALIDATION PAIEMENT ====================
    
    /** Messages d'erreur pour paiements */
    public static final String CASH_LIMIT_EXCEEDED_MESSAGE = "Cash payments cannot exceed 500.00 EUR";
    public static final String CASH_CURRENCY_RESTRICTION_MESSAGE = "Cash payments only accepted in EUR";
    public static final String APPLE_PAY_CURRENCY_RESTRICTION_MESSAGE = 
        "Apple Pay only supports major currencies (EUR, USD, GBP)";
    public static final String HIGH_AMOUNT_CURRENCY_RESTRICTION_MESSAGE = 
        "Payments over 1000.00 must use major currencies (EUR, USD, GBP)";
    
    // ==================== VALIDATION SALON ====================
    
    /** Messages d'erreur pour salons */
    public static final String SALON_NAME_REQUIRED_MESSAGE = "Salon name is required";
    public static final String SALON_ADDRESS_REQUIRED_MESSAGE = "Salon address is required";
    public static final String INVALID_COORDINATES_MESSAGE = "Invalid GPS coordinates";
    public static final String MAX_SALONS_EXCEEDED_MESSAGE = "Cannot own more than 10 salons";
    
    // ==================== LIMITES DE LONGUEUR ====================
    
    /** Longueurs minimales */
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MIN_DESCRIPTION_LENGTH = 10;
    
    /** Longueurs maximales */
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_USERNAME_LENGTH = 30;
    public static final int MAX_PASSWORD_LENGTH = 128;
    public static final int MAX_EMAIL_LENGTH = 100;
    public static final int MAX_PHONE_LENGTH = 20;
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final int MAX_TITLE_LENGTH = 100;
    public static final int MAX_ADDRESS_LENGTH = 200;
    public static final int MAX_CITY_LENGTH = 50;
    public static final int MAX_COUNTRY_LENGTH = 2; // ISO 3166-1 alpha-2
    public static final int MAX_POSTAL_CODE_LENGTH = 10;
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Valide un email selon le pattern RFC 5322
     * @param email Email à valider
     * @return true si valide
     */
    public static boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_PATTERN);
    }
    
    /**
     * Valide un numéro de téléphone
     * @param phone Numéro à valider
     * @return true si valide
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches(PHONE_PATTERN);
    }
    
    /**
     * Valide un mot de passe fort
     * @param password Mot de passe à valider
     * @return true si fort
     */
    public static boolean isStrongPassword(String password) {
        return password != null && password.matches(STRONG_PASSWORD_PATTERN);
    }
    
    /**
     * Valide une URL
     * @param url URL à valider
     * @return true si valide
     */
    public static boolean isValidUrl(String url) {
        return url != null && url.matches(URL_PATTERN);
    }
    
    /**
     * Valide un nom d'utilisateur
     * @param username Username à valider
     * @return true si valide
     */
    public static boolean isValidUsername(String username) {
        return username != null && username.matches(USERNAME_PATTERN);
    }
    
    /**
     * Valide un nom/prénom
     * @param name Nom à valider
     * @return true si valide
     */
    public static boolean isValidName(String name) {
        return name != null && name.matches(NAME_PATTERN);
    }
    
    /**
     * Obtient le message d'erreur approprié pour un type de validation
     * @param validationType Type de validation
     * @param fieldName Nom du champ
     * @return Message d'erreur formaté
     */
    public static String getValidationMessage(String validationType, String fieldName) {
        return switch (validationType.toUpperCase()) {
            case "EMAIL_INVALID" -> INVALID_EMAIL_MESSAGE;
            case "EMAIL_EMPTY" -> EMPTY_EMAIL_MESSAGE;
            case "PHONE_INVALID" -> INVALID_PHONE_MESSAGE;
            case "PASSWORD_WEAK" -> WEAK_PASSWORD_MESSAGE;
            case "NAME_INVALID" -> INVALID_NAME_MESSAGE;
            case "USERNAME_INVALID" -> INVALID_USERNAME_MESSAGE;
            case "URL_INVALID" -> INVALID_URL_MESSAGE;
            case "CURRENCY_INVALID" -> INVALID_CURRENCY_MESSAGE;
            case "AMOUNT_INVALID" -> INVALID_AMOUNT_MESSAGE;
            default -> fieldName + " validation failed";
        };
    }
    
    /**
     * Vérifie si un type de fichier est autorisé
     * @param mimeType Type MIME du fichier
     * @return true si autorisé
     */
    public static boolean isAllowedImageType(String mimeType) {
        return ALLOWED_IMAGE_TYPES.contains(mimeType);
    }
    
    /**
     * Vérifie si une extension de fichier est autorisée
     * @param extension Extension du fichier (sans le point)
     * @return true si autorisée
     */
    public static boolean isAllowedImageExtension(String extension) {
        return ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase());
    }
}