package com.jb.afrostyle.core.mapper;

import com.jb.afrostyle.core.constants.ValidationConstants;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Utilitaires MapStruct pour la validation et la transformation de données
 * Centralise toutes les validations communes utilisées dans les mappers
 * Intègre avec ValidationConstants pour les règles métier
 * 
 * @version 1.0
 * @since Java 21
 */
@Mapper(componentModel = "spring")
@Component
public class ValidationMapperUtils {
    
    // ==================== PATTERNS DE VALIDATION COMPILÉS ====================
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(ValidationConstants.EMAIL_PATTERN);
    private static final Pattern PHONE_PATTERN = Pattern.compile(ValidationConstants.PHONE_PATTERN);
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(ValidationConstants.STRONG_PASSWORD_PATTERN);
    
    // ==================== VALIDATION D'EMAIL ====================
    
    /**
     * Valide et normalise un email
     * @param email Email à valider
     * @return Email normalisé ou null si invalide
     */
    @Named("validateAndNormalizeEmail")
    public String validateAndNormalizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        String normalizedEmail = email.trim().toLowerCase();
        
        if (EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            return normalizedEmail;
        }
        
        return null; // Email invalide
    }
    
    /**
     * Vérifie si un email est valide
     * @param email Email à vérifier
     * @return true si email valide
     */
    @Named("isValidEmail")
    public Boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        return EMAIL_PATTERN.matcher(email.trim().toLowerCase()).matches();
    }
    
    /**
     * Masque un email pour l'affichage (ex: j***@domain.com)
     * @param email Email à masquer
     * @return Email masqué ou null
     */
    @Named("maskEmail")
    public String maskEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        String normalizedEmail = email.trim().toLowerCase();
        
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            return "***@***.***"; // Email invalide masqué
        }
        
        String[] parts = normalizedEmail.split("@");
        if (parts.length != 2) {
            return "***@***.***";
        }
        
        String localPart = parts[0];
        String domainPart = parts[1];
        
        // Masquer la partie locale (garder premier caractère)
        String maskedLocal = localPart.length() > 1 ? 
            localPart.substring(0, 1) + "***" : 
            "***";
        
        return maskedLocal + "@" + domainPart;
    }
    
    // ==================== VALIDATION DE TÉLÉPHONE ====================
    
    /**
     * Valide et normalise un numéro de téléphone
     * @param phone Numéro à valider
     * @return Numéro normalisé ou null si invalide
     */
    @Named("validateAndNormalizePhone")
    public String validateAndNormalizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        
        // Supprimer tous les caractères non numériques sauf + au début
        String cleanPhone = phone.trim().replaceAll("[^+\\d]", "");
        
        if (PHONE_PATTERN.matcher(cleanPhone).matches()) {
            return cleanPhone;
        }
        
        return null; // Téléphone invalide
    }
    
    /**
     * Vérifie si un numéro de téléphone est valide
     * @param phone Numéro à vérifier
     * @return true si numéro valide
     */
    @Named("isValidPhone")
    public Boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        
        String cleanPhone = phone.trim().replaceAll("[^+\\d]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }
    
    /**
     * Formate un numéro de téléphone pour l'affichage
     * Ex: +32123456789 -> +32 1 23 45 67 89
     * @param phone Numéro à formater
     * @return Numéro formaté ou original si impossible
     */
    @Named("formatPhoneForDisplay")
    public String formatPhoneForDisplay(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        
        String cleanPhone = phone.trim().replaceAll("[^+\\d]", "");
        
        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            return phone; // Retourner original si invalide
        }
        
        // Format simple pour les numéros belges/français
        if (cleanPhone.startsWith("+32") && cleanPhone.length() == 12) {
            // Format belge: +32 X XX XX XX XX
            return cleanPhone.substring(0, 3) + " " + 
                   cleanPhone.substring(3, 4) + " " +
                   cleanPhone.substring(4, 6) + " " +
                   cleanPhone.substring(6, 8) + " " +
                   cleanPhone.substring(8, 10) + " " +
                   cleanPhone.substring(10, 12);
        }
        
        if (cleanPhone.startsWith("+33") && cleanPhone.length() == 12) {
            // Format français: +33 X XX XX XX XX
            return cleanPhone.substring(0, 3) + " " + 
                   cleanPhone.substring(3, 4) + " " +
                   cleanPhone.substring(4, 6) + " " +
                   cleanPhone.substring(6, 8) + " " +
                   cleanPhone.substring(8, 10) + " " +
                   cleanPhone.substring(10, 12);
        }
        
        return cleanPhone; // Retourner propre si format non reconnu
    }
    
    // ==================== VALIDATION DE MOT DE PASSE ====================
    
    /**
     * Vérifie si un mot de passe est fort
     * @param password Mot de passe à vérifier
     * @return true si mot de passe fort
     */
    @Named("isStrongPassword")
    public Boolean isStrongPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        return STRONG_PASSWORD_PATTERN.matcher(password).matches() &&
               password.length() >= ValidationConstants.MIN_PASSWORD_LENGTH;
    }
    
    /**
     * Évalue la force d'un mot de passe (0-4)
     * @param password Mot de passe à évaluer
     * @return Score de force (0=très faible, 4=très fort)
     */
    @Named("evaluatePasswordStrength")
    public Integer evaluatePasswordStrength(String password) {
        if (password == null || password.trim().isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // Longueur minimale
        if (password.length() >= ValidationConstants.MIN_PASSWORD_LENGTH) {
            score++;
        }
        
        // Contient minuscules
        if (password.matches(".*[a-z].*")) {
            score++;
        }
        
        // Contient majuscules
        if (password.matches(".*[A-Z].*")) {
            score++;
        }
        
        // Contient chiffres
        if (password.matches(".*[0-9].*")) {
            score++;
        }
        
        // Contient caractères spéciaux
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            score++;
        }
        
        return Math.min(score, 4); // Maximum 4
    }
    
    /**
     * Masque un mot de passe pour l'affichage
     * @param password Mot de passe à masquer
     * @return Mot de passe masqué
     */
    @Named("maskPassword")
    public String maskPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return null;
        }
        
        return "*".repeat(Math.min(password.length(), 8));
    }
    
    // ==================== VALIDATION DE MONTANTS ====================
    
    /**
     * Valide et normalise un montant monétaire
     * @param amount Montant à valider
     * @return Montant normalisé ou null si invalide
     */
    @Named("validateAndNormalizeAmount")
    public BigDecimal validateAndNormalizeAmount(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        
        // Arrondir à 2 décimales
        BigDecimal rounded = amount.setScale(2, BigDecimal.ROUND_HALF_UP);
        
        // Vérifier que le montant est positif et dans les limites
        if (rounded.compareTo(BigDecimal.ZERO) < 0) {
            return null; // Montant négatif invalide
        }
        
        if (rounded.compareTo(ValidationConstants.MAX_PAYMENT_AMOUNT) > 0) {
            return null; // Montant trop élevé
        }
        
        return rounded;
    }
    
    /**
     * Vérifie si un montant est valide
     * @param amount Montant à vérifier
     * @return true si montant valide
     */
    @Named("isValidAmount")
    public Boolean isValidAmount(BigDecimal amount) {
        return validateAndNormalizeAmount(amount) != null;
    }
    
    /**
     * Formate un montant pour l'affichage avec devise
     * @param amount Montant à formater
     * @param currency Code devise (EUR, USD, etc.)
     * @return Montant formaté avec devise
     */
    @Named("formatAmountWithCurrency")
    public String formatAmountWithCurrency(BigDecimal amount, String currency) {
        if (amount == null) {
            return null;
        }
        
        BigDecimal normalizedAmount = validateAndNormalizeAmount(amount);
        if (normalizedAmount == null) {
            return "Montant invalide";
        }
        
        String currencySymbol = switch (currency != null ? currency.toUpperCase() : "EUR") {
            case "EUR" -> "€";
            case "USD" -> "$";
            case "GBP" -> "£";
            default -> currency != null ? currency : "€";
        };
        
        return String.format("%.2f %s", normalizedAmount, currencySymbol);
    }
    
    // ==================== VALIDATION DE TEXTE ====================
    
    /**
     * Nettoie et valide un nom (prénom, nom de famille)
     * @param name Nom à valider
     * @return Nom nettoyé ou null si invalide
     */
    @Named("validateAndCleanName")
    public String validateAndCleanName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        String cleanName = name.trim();
        
        // Supprimer les caractères non alphabétiques sauf espaces, tirets et apostrophes
        cleanName = cleanName.replaceAll("[^a-zA-ZÀ-ÿ\\s\\-']", "");
        
        // Vérifier longueur minimale et maximale
        if (cleanName.length() < ValidationConstants.MIN_NAME_LENGTH || 
            cleanName.length() > ValidationConstants.MAX_NAME_LENGTH) {
            return null;
        }
        
        // Capitaliser chaque mot
        return capitalizeWords(cleanName);
    }
    
    /**
     * Capitalise chaque mot dans une chaîne
     * @param text Texte à capitaliser
     * @return Texte capitalisé
     */
    @Named("capitalizeWords")
    public String capitalizeWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        StringBuilder capitalized = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c) || c == '-' || c == '\'') {
                capitalizeNext = true;
                capitalized.append(c);
            } else if (capitalizeNext) {
                capitalized.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                capitalized.append(Character.toLowerCase(c));
            }
        }
        
        return capitalized.toString();
    }
    
    /**
     * Nettoie et valide une description
     * @param description Description à valider
     * @return Description nettoyée ou null si invalide
     */
    @Named("validateAndCleanDescription")
    public String validateAndCleanDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }
        
        String cleanDescription = description.trim();
        
        // Vérifier longueur maximale
        if (cleanDescription.length() > ValidationConstants.MAX_DESCRIPTION_LENGTH) {
            cleanDescription = cleanDescription.substring(0, ValidationConstants.MAX_DESCRIPTION_LENGTH);
        }
        
        // Nettoyer les caractères dangereux (basique)
        cleanDescription = cleanDescription.replaceAll("[<>\"']", "");
        
        return cleanDescription.trim().isEmpty() ? null : cleanDescription;
    }
    
    // ==================== UTILITAIRES DE TRANSFORMATION ====================
    
    /**
     * Convertit une chaîne en slug URL-friendly
     * @param text Texte à convertir
     * @return Slug URL-friendly
     */
    @Named("textToSlug")
    public String textToSlug(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        String slug = text.trim()
                         .toLowerCase()
                         .replaceAll("[àáâãäå]", "a")
                         .replaceAll("[èéêë]", "e")
                         .replaceAll("[ìíîï]", "i")
                         .replaceAll("[òóôõö]", "o")
                         .replaceAll("[ùúûü]", "u")
                         .replaceAll("[ç]", "c")
                         .replaceAll("[ñ]", "n")
                         .replaceAll("[^a-z0-9\\s\\-]", "") // Garder seulement lettres, chiffres, espaces et tirets
                         .replaceAll("\\s+", "-") // Remplacer espaces par tirets
                         .replaceAll("-+", "-") // Supprimer tirets multiples
                         .replaceAll("^-|-$", ""); // Supprimer tirets au début/fin
        
        return slug.isEmpty() ? null : slug;
    }
    
    /**
     * Tronque un texte à une longueur maximale avec ellipse
     * @param text Texte à tronquer
     * @param maxLength Longueur maximale
     * @return Texte tronqué avec "..." si nécessaire
     */
    @Named("truncateText")
    public String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        
        if (maxLength <= 3) {
            return "...";
        }
        
        return text.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Valide et nettoie une URL
     * @param url URL à valider
     * @return URL nettoyée ou null si invalide
     */
    @Named("validateAndCleanUrl")
    public String validateAndCleanUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        
        String cleanUrl = url.trim();
        
        // Ajouter https:// si pas de protocole
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://" + cleanUrl;
        }
        
        // Validation basique d'URL
        try {
            new java.net.URL(cleanUrl);
            return cleanUrl;
        } catch (java.net.MalformedURLException e) {
            return null;
        }
    }
}