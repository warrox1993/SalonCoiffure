package com.jb.afrostyle.core.util;

import com.jb.afrostyle.core.constants.ValidationConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Utilitaires numériques pour AfroStyle
 * Fournit des méthodes pour la manipulation, validation et formatage des nombres
 * Intègre avec les constantes de validation pour les règles métier
 * 
 * @version 1.0
 * @since Java 21
 */
public final class NumberUtils {
    
    // ==================== CONSTANTES ====================
    
    public static final BigDecimal ZERO = BigDecimal.ZERO;
    public static final BigDecimal ONE = BigDecimal.ONE;
    public static final BigDecimal TEN = BigDecimal.TEN;
    public static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    
    // Formats communs
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("#0.00%");
    private static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("#,##0");
    
    // Formats localisés
    private static final NumberFormat EURO_FORMAT = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    private static final NumberFormat US_DOLLAR_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    
    /**
     * Constructeur privé pour classe utilitaire
     */
    private NumberUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // ==================== VÉRIFICATIONS DE BASE ====================
    
    /**
     * Vérifie si un nombre est null
     * @param number Nombre à vérifier
     * @return true si null
     */
    public static boolean isNull(Number number) {
        return number == null;
    }
    
    /**
     * Vérifie si un nombre n'est pas null
     * @param number Nombre à vérifier
     * @return true si non null
     */
    public static boolean isNotNull(Number number) {
        return number != null;
    }
    
    /**
     * Vérifie si un nombre est zéro
     * @param number Nombre à vérifier
     * @return true si zéro
     */
    public static boolean isZero(Number number) {
        if (number == null) {
            return false;
        }
        
        return switch (number) {
            case BigDecimal bd -> bd.compareTo(ZERO) == 0;
            case Double d -> Double.compare(d, 0.0) == 0;
            case Float f -> Float.compare(f, 0.0f) == 0;
            case Long l -> l == 0L;
            case Integer i -> i == 0;
            default -> number.doubleValue() == 0.0;
        };
    }
    
    /**
     * Vérifie si un nombre est positif (> 0)
     * @param number Nombre à vérifier
     * @return true si positif
     */
    public static boolean isPositive(Number number) {
        if (number == null) {
            return false;
        }
        
        return switch (number) {
            case BigDecimal bd -> bd.compareTo(ZERO) > 0;
            case Double d -> d > 0.0;
            case Float f -> f > 0.0f;
            case Long l -> l > 0L;
            case Integer i -> i > 0;
            default -> number.doubleValue() > 0.0;
        };
    }
    
    /**
     * Vérifie si un nombre est négatif (< 0)
     * @param number Nombre à vérifier
     * @return true si négatif
     */
    public static boolean isNegative(Number number) {
        if (number == null) {
            return false;
        }
        
        return switch (number) {
            case BigDecimal bd -> bd.compareTo(ZERO) < 0;
            case Double d -> d < 0.0;
            case Float f -> f < 0.0f;
            case Long l -> l < 0L;
            case Integer i -> i < 0;
            default -> number.doubleValue() < 0.0;
        };
    }
    
    /**
     * Vérifie si un nombre est positif ou zéro (>= 0)
     * @param number Nombre à vérifier
     * @return true si positif ou zéro
     */
    public static boolean isPositiveOrZero(Number number) {
        return isZero(number) || isPositive(number);
    }
    
    // ==================== CONVERSION SÉCURISÉE ====================
    
    /**
     * Convertit une chaîne en Integer de manière sécurisée
     * @param str Chaîne à convertir
     * @return Integer ou null si conversion impossible
     */
    public static Integer toIntegerSafe(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        
        try {
            return Integer.valueOf(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Convertit une chaîne en Long de manière sécurisée
     * @param str Chaîne à convertir
     * @return Long ou null si conversion impossible
     */
    public static Long toLongSafe(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        
        try {
            return Long.valueOf(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Convertit une chaîne en Double de manière sécurisée
     * @param str Chaîne à convertir
     * @return Double ou null si conversion impossible
     */
    public static Double toDoubleSafe(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        
        try {
            // Remplacer virgule par point pour format français
            String normalized = str.trim().replace(',', '.');
            return Double.valueOf(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Convertit une chaîne en BigDecimal de manière sécurisée
     * @param str Chaîne à convertir
     * @return BigDecimal ou null si conversion impossible
     */
    public static BigDecimal toBigDecimalSafe(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        
        try {
            // Remplacer virgule par point et supprimer espaces
            String normalized = str.trim().replace(',', '.').replace(" ", "");
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    // ==================== CONVERSION AVEC VALEURS PAR DÉFAUT ====================
    
    /**
     * Convertit en Integer avec valeur par défaut
     * @param str Chaîne à convertir
     * @param defaultValue Valeur par défaut
     * @return Integer ou valeur par défaut
     */
    public static Integer toInteger(String str, Integer defaultValue) {
        Integer result = toIntegerSafe(str);
        return result != null ? result : defaultValue;
    }
    
    /**
     * Convertit en Long avec valeur par défaut
     * @param str Chaîne à convertir
     * @param defaultValue Valeur par défaut
     * @return Long ou valeur par défaut
     */
    public static Long toLong(String str, Long defaultValue) {
        Long result = toLongSafe(str);
        return result != null ? result : defaultValue;
    }
    
    /**
     * Convertit en Double avec valeur par défaut
     * @param str Chaîne à convertir
     * @param defaultValue Valeur par défaut
     * @return Double ou valeur par défaut
     */
    public static Double toDouble(String str, Double defaultValue) {
        Double result = toDoubleSafe(str);
        return result != null ? result : defaultValue;
    }
    
    /**
     * Convertit en BigDecimal avec valeur par défaut
     * @param str Chaîne à convertir
     * @param defaultValue Valeur par défaut
     * @return BigDecimal ou valeur par défaut
     */
    public static BigDecimal toBigDecimal(String str, BigDecimal defaultValue) {
        BigDecimal result = toBigDecimalSafe(str);
        return result != null ? result : defaultValue;
    }
    
    // ==================== OPÉRATIONS BIGDECIMAL ====================
    
    /**
     * Addition sécurisée de BigDecimal (gère les null)
     * @param a Premier nombre
     * @param b Deuxième nombre
     * @return Somme ou null si les deux sont null
     */
    public static BigDecimal addSafe(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.add(b);
    }
    
    /**
     * Soustraction sécurisée de BigDecimal
     * @param a Premier nombre
     * @param b Deuxième nombre
     * @return Différence ou null si a est null
     */
    public static BigDecimal subtractSafe(BigDecimal a, BigDecimal b) {
        if (a == null) {
            return null;
        }
        if (b == null) {
            return a;
        }
        return a.subtract(b);
    }
    
    /**
     * Multiplication sécurisée de BigDecimal
     * @param a Premier nombre
     * @param b Deuxième nombre
     * @return Produit ou null si l'un est null
     */
    public static BigDecimal multiplySafe(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return null;
        }
        return a.multiply(b);
    }
    
    /**
     * Division sécurisée de BigDecimal
     * @param a Dividende
     * @param b Diviseur
     * @param scale Nombre de décimales
     * @param roundingMode Mode d'arrondi
     * @return Quotient ou null si erreur
     */
    public static BigDecimal divideSafe(BigDecimal a, BigDecimal b, int scale, RoundingMode roundingMode) {
        if (a == null || b == null || isZero(b)) {
            return null;
        }
        
        try {
            return a.divide(b, scale, roundingMode);
        } catch (ArithmeticException e) {
            return null;
        }
    }
    
    /**
     * Division sécurisée avec paramètres par défaut (2 décimales, HALF_UP)
     * @param a Dividende
     * @param b Diviseur
     * @return Quotient ou null si erreur
     */
    public static BigDecimal divideSafe(BigDecimal a, BigDecimal b) {
        return divideSafe(a, b, 2, RoundingMode.HALF_UP);
    }
    
    // ==================== ARRONDI ET FORMATAGE ====================
    
    /**
     * Arrondit un BigDecimal à n décimales
     * @param number Nombre à arrondir
     * @param scale Nombre de décimales
     * @param roundingMode Mode d'arrondi
     * @return Nombre arrondi
     */
    public static BigDecimal round(BigDecimal number, int scale, RoundingMode roundingMode) {
        if (number == null) {
            return null;
        }
        return number.setScale(scale, roundingMode);
    }
    
    /**
     * Arrondit à 2 décimales (mode HALF_UP)
     * @param number Nombre à arrondir
     * @return Nombre arrondi
     */
    public static BigDecimal roundToTwoDecimals(BigDecimal number) {
        return round(number, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Arrondit à l'entier le plus proche
     * @param number Nombre à arrondir  
     * @return Nombre arrondi
     */
    public static BigDecimal roundToInteger(BigDecimal number) {
        return round(number, 0, RoundingMode.HALF_UP);
    }
    
    // ==================== FORMATAGE POUR AFFICHAGE ====================
    
    /**
     * Formate un nombre comme monnaie (ex: 1 234,56)
     * @param number Nombre à formater
     * @return String formatée
     */
    public static String formatAsCurrency(BigDecimal number) {
        if (number == null) {
            return null;
        }
        return CURRENCY_FORMAT.format(number);
    }
    
    /**
     * Formate un nombre avec devise (ex: 1 234,56 €)
     * @param number Nombre à formater
     * @param currency Code devise (EUR, USD, etc.)
     * @return String formatée avec devise
     */
    public static String formatWithCurrency(BigDecimal number, String currency) {
        if (number == null) {
            return null;
        }
        
        NumberFormat formatter = switch (currency != null ? currency.toUpperCase() : "EUR") {
            case "EUR" -> EURO_FORMAT;
            case "USD" -> US_DOLLAR_FORMAT;
            default -> CURRENCY_FORMAT;
        };
        
        return switch (currency != null ? currency.toUpperCase() : "EUR") {
            case "EUR", "USD" -> formatter.format(number);
            default -> CURRENCY_FORMAT.format(number) + " " + currency;
        };
    }
    
    /**
     * Formate un nombre comme pourcentage (ex: 15,25%)
     * @param number Nombre à formater (0.1525 → 15,25%)
     * @return String formatée
     */
    public static String formatAsPercentage(BigDecimal number) {
        if (number == null) {
            return null;
        }
        return PERCENTAGE_FORMAT.format(number);
    }
    
    /**
     * Formate un entier avec séparateurs de milliers (ex: 1 234)
     * @param number Nombre à formater
     * @return String formatée
     */
    public static String formatAsInteger(Long number) {
        if (number == null) {
            return null;
        }
        return INTEGER_FORMAT.format(number);
    }
    
    /**
     * Formate un nombre avec un nombre fixe de décimales
     * @param number Nombre à formater
     * @param decimals Nombre de décimales
     * @return String formatée
     */
    public static String formatWithDecimals(BigDecimal number, int decimals) {
        if (number == null) {
            return null;
        }
        
        String pattern = "#,##0." + "0".repeat(Math.max(0, decimals));
        DecimalFormat formatter = new DecimalFormat(pattern);
        return formatter.format(number);
    }
    
    // ==================== VALIDATION MÉTIER ====================
    
    /**
     * Valide un montant selon les règles métier
     * @param amount Montant à valider
     * @return true si montant valide
     */
    public static boolean isValidAmount(BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        
        return amount.compareTo(ZERO) >= 0 && 
               amount.compareTo(ValidationConstants.MAX_PAYMENT_AMOUNT) <= 0;
    }
    
    /**
     * Valide un prix selon les règles métier
     * @param price Prix à valider
     * @return true si prix valide
     */
    public static boolean isValidPrice(BigDecimal price) {
        if (price == null) {
            return false;
        }
        
        return price.compareTo(ZERO) > 0 && 
               price.compareTo(ValidationConstants.MAX_PAYMENT_AMOUNT) <= 0 &&
               price.scale() <= 2;
    }
    
    /**
     * Valide un pourcentage (0-100)
     * @param percentage Pourcentage à valider
     * @return true si pourcentage valide
     */
    public static boolean isValidPercentage(BigDecimal percentage) {
        if (percentage == null) {
            return false;
        }
        
        return percentage.compareTo(ZERO) >= 0 && 
               percentage.compareTo(HUNDRED) <= 0;
    }
    
    /**
     * Valide une durée en minutes
     * @param minutes Durée à valider
     * @return true si durée valide
     */
    public static boolean isValidDurationMinutes(Integer minutes) {
        if (minutes == null) {
            return false;
        }
        
        return minutes > 0 && minutes <= 720; // Maximum 12 heures
    }
    
    // ==================== CALCULS MÉTIER ====================
    
    /**
     * Calcule une remise
     * @param originalPrice Prix original
     * @param discountPercentage Pourcentage de remise
     * @return Prix après remise
     */
    public static BigDecimal calculateDiscount(BigDecimal originalPrice, BigDecimal discountPercentage) {
        if (originalPrice == null || discountPercentage == null) {
            return originalPrice;
        }
        
        if (!isValidPrice(originalPrice) || !isValidPercentage(discountPercentage)) {
            return originalPrice;
        }
        
        BigDecimal discountAmount = originalPrice.multiply(discountPercentage).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        return originalPrice.subtract(discountAmount);
    }
    
    /**
     * Calcule la TVA
     * @param priceHT Prix hors taxes
     * @param vatRate Taux de TVA (ex: 21 pour 21%)
     * @return Montant de la TVA
     */
    public static BigDecimal calculateVAT(BigDecimal priceHT, BigDecimal vatRate) {
        if (priceHT == null || vatRate == null) {
            return ZERO;
        }
        
        if (!isValidPrice(priceHT) || !isValidPercentage(vatRate)) {
            return ZERO;
        }
        
        return priceHT.multiply(vatRate).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calcule le prix TTC à partir du prix HT
     * @param priceHT Prix hors taxes
     * @param vatRate Taux de TVA
     * @return Prix toutes taxes comprises
     */
    public static BigDecimal calculatePriceWithVAT(BigDecimal priceHT, BigDecimal vatRate) {
        BigDecimal vat = calculateVAT(priceHT, vatRate);
        return addSafe(priceHT, vat);
    }
    
    /**
     * Calcule un pourcentage
     * @param part Partie
     * @param total Total
     * @return Pourcentage (0-100)
     */
    public static BigDecimal calculatePercentage(BigDecimal part, BigDecimal total) {
        if (part == null || total == null || isZero(total)) {
            return ZERO;
        }
        
        return part.multiply(HUNDRED).divide(total, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calcule la moyenne d'une liste de nombres
     * @param numbers Nombres à moyenner
     * @return Moyenne ou null si liste vide
     */
    public static BigDecimal calculateAverage(java.util.List<BigDecimal> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return null;
        }
        
        BigDecimal sum = numbers.stream()
                               .filter(Objects::nonNull)
                               .reduce(ZERO, BigDecimal::add);
        
        long count = numbers.stream().filter(Objects::nonNull).count();
        
        return count > 0 ? sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : null;
    }
    
    // ==================== UTILITAIRES DE COMPARAISON ====================
    
    /**
     * Obtient le minimum de deux BigDecimal (gère les null)
     * @param a Premier nombre
     * @param b Deuxième nombre
     * @return Minimum ou null si les deux sont null
     */
    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.min(b);
    }
    
    /**
     * Obtient le maximum de deux BigDecimal (gère les null)
     * @param a Premier nombre
     * @param b Deuxième nombre
     * @return Maximum ou null si les deux sont null
     */
    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.max(b);
    }
    
    /**
     * Compare deux BigDecimal (gère les null, null < non-null)
     * @param a Premier nombre
     * @param b Deuxième nombre
     * @return Résultat de comparaison (-1, 0, 1)
     */
    public static int compareWithNull(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        return a.compareTo(b);
    }
    
    /**
     * Vérifie si deux nombres sont approximativement égaux
     * @param a Premier nombre
     * @param b Deuxième nombre
     * @param tolerance Tolérance
     * @return true si approximativement égaux
     */
    public static boolean approximatelyEquals(BigDecimal a, BigDecimal b, BigDecimal tolerance) {
        if (a == null || b == null || tolerance == null) {
            return false;
        }
        
        BigDecimal diff = a.subtract(b).abs();
        return diff.compareTo(tolerance) <= 0;
    }
}