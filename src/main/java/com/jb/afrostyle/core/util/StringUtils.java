package com.jb.afrostyle.core.util;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utilitaires de manipulation de chaînes de caractères pour AfroStyle
 * Fournit des méthodes communes pour le traitement de texte
 * Intègre des fonctionnalités de validation, formatage et transformation
 * 
 * @version 1.0
 * @since Java 21
 */
public final class StringUtils {
    
    // ==================== CONSTANTES ====================
    
    private static final String EMPTY = "";
    private static final String SPACE = " ";
    private static final String UNDERSCORE = "_";
    private static final String HYPHEN = "-";
    
    // Patterns compilés pour performance
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern NON_ALPHANUMERIC_PATTERN = Pattern.compile("[^a-zA-Z0-9]");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    
    /**
     * Constructeur privé pour classe utilitaire
     */
    private StringUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // ==================== VÉRIFICATIONS DE BASE ====================
    
    /**
     * Vérifie si une chaîne est null ou vide
     * @param str Chaîne à vérifier
     * @return true si null ou vide
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
    
    /**
     * Vérifie si une chaîne n'est pas null et pas vide
     * @param str Chaîne à vérifier
     * @return true si non null et non vide
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * Vérifie si une chaîne est null, vide ou ne contient que des espaces
     * @param str Chaîne à vérifier
     * @return true si blank
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Vérifie si une chaîne n'est pas blank
     * @param str Chaîne à vérifier
     * @return true si non blank
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    /**
     * Vérifie si toutes les chaînes sont non blank
     * @param strings Chaînes à vérifier
     * @return true si toutes non blank
     */
    public static boolean areAllNotBlank(String... strings) {
        if (strings == null || strings.length == 0) {
            return false;
        }
        
        for (String str : strings) {
            if (isBlank(str)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Vérifie si au moins une chaîne est non blank
     * @param strings Chaînes à vérifier
     * @return true si au moins une non blank
     */
    public static boolean anyNotBlank(String... strings) {
        if (strings == null || strings.length == 0) {
            return false;
        }
        
        for (String str : strings) {
            if (isNotBlank(str)) {
                return true;
            }
        }
        return false;
    }
    
    // ==================== OPÉRATIONS DE BASE ====================
    
    /**
     * Retourne une chaîne par défaut si la chaîne est null ou vide
     * @param str Chaîne à vérifier
     * @param defaultStr Chaîne par défaut
     * @return Chaîne originale ou par défaut
     */
    public static String defaultIfEmpty(String str, String defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }
    
    /**
     * Retourne une chaîne par défaut si la chaîne est blank
     * @param str Chaîne à vérifier
     * @param defaultStr Chaîne par défaut
     * @return Chaîne originale ou par défaut
     */
    public static String defaultIfBlank(String str, String defaultStr) {
        return isBlank(str) ? defaultStr : str;
    }
    
    /**
     * Convertit null en chaîne vide
     * @param str Chaîne à convertir
     * @return Chaîne ou vide si null
     */
    public static String nullToEmpty(String str) {
        return str == null ? EMPTY : str;
    }
    
    /**
     * Convertit chaîne vide en null
     * @param str Chaîne à convertir
     * @return Chaîne ou null si vide
     */
    public static String emptyToNull(String str) {
        return isEmpty(str) ? null : str;
    }
    
    /**
     * Tronque une chaîne à la longueur maximale
     * @param str Chaîne à tronquer
     * @param maxLength Longueur maximale
     * @return Chaîne tronquée
     */
    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength);
    }
    
    /**
     * Tronque une chaîne avec ellipse
     * @param str Chaîne à tronquer
     * @param maxLength Longueur maximale (incluant ellipse)
     * @return Chaîne tronquée avec "..."
     */
    public static String truncateWithEllipsis(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        
        if (maxLength <= 3) {
            return "...";
        }
        
        return str.substring(0, maxLength - 3) + "...";
    }
    
    // ==================== FORMATAGE ET CASSE ====================
    
    /**
     * Capitalise la première lettre d'une chaîne
     * @param str Chaîne à capitaliser
     * @return Chaîne capitalisée
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        if (str.length() == 1) {
            return str.toUpperCase();
        }
        
        return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
    }
    
    /**
     * Capitalise chaque mot d'une chaîne
     * @param str Chaîne à capitaliser
     * @return Chaîne avec chaque mot capitalisé
     */
    public static String capitalizeWords(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        StringBuilder result = new StringBuilder(str.length());
        boolean capitalizeNext = true;
        
        for (char c : str.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        
        return result.toString();
    }
    
    /**
     * Convertit une chaîne en camelCase
     * @param str Chaîne à convertir
     * @return Chaîne en camelCase
     */
    public static String toCamelCase(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        String[] words = str.toLowerCase().split("[\\s_-]+");
        StringBuilder result = new StringBuilder(words[0]);
        
        for (int i = 1; i < words.length; i++) {
            result.append(capitalize(words[i]));
        }
        
        return result.toString();
    }
    
    /**
     * Convertit une chaîne en PascalCase
     * @param str Chaîne à convertir
     * @return Chaîne en PascalCase
     */
    public static String toPascalCase(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        return capitalize(toCamelCase(str));
    }
    
    /**
     * Convertit une chaîne en snake_case
     * @param str Chaîne à convertir
     * @return Chaîne en snake_case
     */
    public static String toSnakeCase(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        return str.toLowerCase()
                  .replaceAll("\\s+", UNDERSCORE)
                  .replaceAll("-+", UNDERSCORE)
                  .replaceAll("[^a-z0-9_]", EMPTY);
    }
    
    /**
     * Convertit une chaîne en kebab-case
     * @param str Chaîne à convertir
     * @return Chaîne en kebab-case
     */
    public static String toKebabCase(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        return str.toLowerCase()
                  .replaceAll("\\s+", HYPHEN)
                  .replaceAll("_+", HYPHEN)
                  .replaceAll("[^a-z0-9-]", EMPTY);
    }
    
    // ==================== NETTOYAGE ET NORMALISATION ====================
    
    /**
     * Supprime les espaces en début et fin, et normalise les espaces internes
     * @param str Chaîne à nettoyer
     * @return Chaîne nettoyée
     */
    public static String clean(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        return WHITESPACE_PATTERN.matcher(str.trim()).replaceAll(SPACE);
    }
    
    /**
     * Supprime tous les espaces d'une chaîne
     * @param str Chaîne à traiter
     * @return Chaîne sans espaces
     */
    public static String removeWhitespace(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        return str.replaceAll("\\s", EMPTY);
    }
    
    /**
     * Supprime les caractères non alphanumériques
     * @param str Chaîne à traiter
     * @return Chaîne alphanumérique seulement
     */
    public static String removeNonAlphanumeric(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        return NON_ALPHANUMERIC_PATTERN.matcher(str).replaceAll(EMPTY);
    }
    
    /**
     * Normalise les caractères accentués
     * @param str Chaîne à normaliser
     * @return Chaîne sans accents
     */
    public static String removeAccents(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        return Normalizer.normalize(str, Normalizer.Form.NFD)
                        .replaceAll("[\\p{InCombiningDiacriticalMarks}]", EMPTY);
    }
    
    /**
     * Convertit une chaîne en slug URL-friendly
     * @param str Chaîne à convertir
     * @return Slug URL-friendly
     */
    public static String toSlug(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        return removeAccents(str)
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", EMPTY)
                .replaceAll("\\s+", HYPHEN)
                .replaceAll("-+", HYPHEN)
                .replaceAll("^-|-$", EMPTY);
    }
    
    // ==================== VALIDATION DE FORMATS ====================
    
    /**
     * Vérifie si une chaîne est un email valide
     * @param email Email à vérifier
     * @return true si email valide
     */
    public static boolean isValidEmail(String email) {
        return isNotBlank(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Vérifie si une chaîne est un numéro de téléphone valide
     * @param phone Numéro à vérifier
     * @return true si numéro valide
     */
    public static boolean isValidPhone(String phone) {
        if (isBlank(phone)) {
            return false;
        }
        
        String cleanPhone = phone.replaceAll("[^+\\d]", EMPTY);
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }
    
    /**
     * Vérifie si une chaîne est un UUID valide
     * @param uuid UUID à vérifier
     * @return true si UUID valide
     */
    public static boolean isValidUUID(String uuid) {
        return isNotBlank(uuid) && UUID_PATTERN.matcher(uuid).matches();
    }
    
    /**
     * Vérifie si une chaîne ne contient que des chiffres
     * @param str Chaîne à vérifier
     * @return true si numérique seulement
     */
    public static boolean isNumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }
        
        return str.chars().allMatch(Character::isDigit);
    }
    
    /**
     * Vérifie si une chaîne ne contient que des lettres
     * @param str Chaîne à vérifier
     * @return true si alphabétique seulement
     */
    public static boolean isAlpha(String str) {
        if (isEmpty(str)) {
            return false;
        }
        
        return str.chars().allMatch(Character::isLetter);
    }
    
    /**
     * Vérifie si une chaîne ne contient que des lettres et chiffres
     * @param str Chaîne à vérifier
     * @return true si alphanumérique seulement
     */
    public static boolean isAlphanumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }
        
        return str.chars().allMatch(Character::isLetterOrDigit);
    }
    
    // ==================== MANIPULATION DE COLLECTIONS ====================
    
    /**
     * Joint une collection de chaînes avec un délimiteur
     * @param collection Collection à joindre
     * @param delimiter Délimiteur
     * @return Chaîne jointe
     */
    public static String join(Collection<String> collection, String delimiter) {
        if (collection == null || collection.isEmpty()) {
            return EMPTY;
        }
        
        return collection.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(delimiter));
    }
    
    /**
     * Joint un tableau de chaînes avec un délimiteur
     * @param array Tableau à joindre
     * @param delimiter Délimiteur
     * @return Chaîne jointe
     */
    public static String join(String[] array, String delimiter) {
        if (array == null || array.length == 0) {
            return EMPTY;
        }
        
        return Arrays.stream(array)
                     .filter(Objects::nonNull)
                     .collect(Collectors.joining(delimiter));
    }
    
    /**
     * Divise une chaîne et filtre les éléments vides
     * @param str Chaîne à diviser
     * @param delimiter Délimiteur
     * @return Liste des éléments non vides
     */
    public static List<String> splitAndFilter(String str, String delimiter) {
        if (isEmpty(str)) {
            return Collections.emptyList();
        }
        
        return Arrays.stream(str.split(Pattern.quote(delimiter)))
                     .map(String::trim)
                     .filter(StringUtils::isNotEmpty)
                     .collect(Collectors.toList());
    }
    
    // ==================== COMPARAISON ET RECHERCHE ====================
    
    /**
     * Compare deux chaînes en ignorant la casse
     * @param str1 Première chaîne
     * @param str2 Deuxième chaîne
     * @return true si égales (ignore casse)
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == str2) {
            return true;
        }
        
        if (str1 == null || str2 == null) {
            return false;
        }
        
        return str1.equalsIgnoreCase(str2);
    }
    
    /**
     * Vérifie si une chaîne contient une sous-chaîne (ignore casse)
     * @param str Chaîne principale
     * @param searchStr Sous-chaîne à chercher
     * @return true si contient
     */
    public static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        
        return str.toLowerCase().contains(searchStr.toLowerCase());
    }
    
    /**
     * Vérifie si une chaîne commence par une autre (ignore casse)
     * @param str Chaîne principale
     * @param prefix Préfixe à vérifier
     * @return true si commence par
     */
    public static boolean startsWithIgnoreCase(String str, String prefix) {
        if (str == null || prefix == null) {
            return false;
        }
        
        if (str.length() < prefix.length()) {
            return false;
        }
        
        return str.substring(0, prefix.length()).equalsIgnoreCase(prefix);
    }
    
    /**
     * Vérifie si une chaîne finit par une autre (ignore casse)
     * @param str Chaîne principale
     * @param suffix Suffixe à vérifier
     * @return true si finit par
     */
    public static boolean endsWithIgnoreCase(String str, String suffix) {
        if (str == null || suffix == null) {
            return false;
        }
        
        if (str.length() < suffix.length()) {
            return false;
        }
        
        return str.substring(str.length() - suffix.length()).equalsIgnoreCase(suffix);
    }
    
    // ==================== MASQUAGE ET SÉCURITÉ ====================
    
    /**
     * Masque une chaîne en gardant les premiers et derniers caractères
     * @param str Chaîne à masquer
     * @param visibleChars Nombre de caractères visibles au début et à la fin
     * @param maskChar Caractère de masquage
     * @return Chaîne masquée
     */
    public static String mask(String str, int visibleChars, char maskChar) {
        if (isEmpty(str) || str.length() <= visibleChars * 2) {
            return str;
        }
        
        String start = str.substring(0, visibleChars);
        String end = str.substring(str.length() - visibleChars);
        String middle = String.valueOf(maskChar).repeat(str.length() - visibleChars * 2);
        
        return start + middle + end;
    }
    
    /**
     * Masque un email (ex: j***@domain.com)
     * @param email Email à masquer
     * @return Email masqué
     */
    public static String maskEmail(String email) {
        if (!isValidEmail(email)) {
            return "***@***.***";
        }
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];
        
        String maskedLocal = localPart.length() > 1 ? 
            localPart.charAt(0) + "***" : 
            "***";
        
        return maskedLocal + "@" + domainPart;
    }
    
    /**
     * Masque un numéro de téléphone
     * @param phone Numéro à masquer
     * @return Numéro masqué
     */
    public static String maskPhone(String phone) {
        if (isEmpty(phone)) {
            return phone;
        }
        
        if (phone.length() <= 4) {
            return "***";
        }
        
        return phone.substring(0, 2) + "***" + phone.substring(phone.length() - 2);
    }
    
    // ==================== UTILITAIRES AVANCÉS ====================
    
    /**
     * Calcule la distance de Levenshtein entre deux chaînes
     * @param str1 Première chaîne
     * @param str2 Deuxième chaîne
     * @return Distance de Levenshtein
     */
    public static int levenshteinDistance(String str1, String str2) {
        if (str1 == null) str1 = EMPTY;
        if (str2 == null) str2 = EMPTY;
        
        int len1 = str1.length();
        int len2 = str2.length();
        
        int[][] dp = new int[len1 + 1][len2 + 1];
        
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }
        
        return dp[len1][len2];
    }
    
    /**
     * Calcule la similarité entre deux chaînes (0.0 à 1.0)
     * @param str1 Première chaîne
     * @param str2 Deuxième chaîne
     * @return Similarité (1.0 = identiques, 0.0 = complètement différentes)
     */
    public static double similarity(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return 1.0;
        }
        
        if (str1 == null || str2 == null) {
            return 0.0;
        }
        
        int maxLength = Math.max(str1.length(), str2.length());
        if (maxLength == 0) {
            return 1.0;
        }
        
        int distance = levenshteinDistance(str1, str2);
        return 1.0 - (double) distance / maxLength;
    }
    
    /**
     * Génère un hash simple d'une chaîne
     * @param str Chaîne à hasher
     * @return Hash de la chaîne
     */
    public static int simpleHash(String str) {
        if (isEmpty(str)) {
            return 0;
        }
        
        int hash = 0;
        for (char c : str.toCharArray()) {
            hash = 31 * hash + c;
        }
        return hash;
    }
}