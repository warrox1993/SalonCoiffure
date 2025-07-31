package com.jb.afrostyle.core.util;

import com.jb.afrostyle.core.constants.ValidationConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Utilitaire centralisé pour tous les Pattern compilés dans AfroStyle
 * Évite la recompilation répétée des patterns regex et centralise leur gestion
 * Utilise un cache pour optimiser les performances
 * 
 * @version 1.0
 * @since Java 21
 */
public final class PatternUtils {
    
    // Cache pour les patterns compilés
    private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();
    
    // ==================== PATTERNS VALIDATION ====================
    
    public static final Pattern EMAIL_PATTERN = getCachedPattern("email", ValidationConstants.EMAIL_PATTERN);
    public static final Pattern PHONE_PATTERN = getCachedPattern("phone", ValidationConstants.PHONE_PATTERN);
    public static final Pattern BELGIAN_PHONE_PATTERN = getCachedPattern("belgian_phone", ValidationConstants.BELGIAN_PHONE_PATTERN);
    public static final Pattern STRONG_PASSWORD_PATTERN = getCachedPattern("strong_password", ValidationConstants.STRONG_PASSWORD_PATTERN);
    public static final Pattern USERNAME_PATTERN = getCachedPattern("username", ValidationConstants.USERNAME_PATTERN);
    public static final Pattern NAME_PATTERN = getCachedPattern("name", ValidationConstants.NAME_PATTERN);
    public static final Pattern URL_PATTERN = getCachedPattern("url", ValidationConstants.URL_PATTERN);
    
    // ==================== PATTERNS UTILS ====================
    
    public static final Pattern WHITESPACE_PATTERN = getCachedPattern("whitespace", "\\s+");
    public static final Pattern NON_ALPHANUMERIC_PATTERN = getCachedPattern("non_alphanumeric", "[^a-zA-Z0-9]");
    public static final Pattern UUID_PATTERN = getCachedPattern("uuid", 
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    
    // ==================== PATTERNS STRIPE ====================
    
    public static final Pattern STRIPE_SESSION_ID_PATTERN = getCachedPattern("stripe_session_id", 
        "\"id\"\\s*:\\s*\"(cs_[^\"]+)\"");
    public static final Pattern STRIPE_BOOKING_ID_PATTERN = getCachedPattern("stripe_booking_id", 
        "\"booking_id\"\\s*:\\s*\"([^\"]+)\"");
    public static final Pattern STRIPE_CUSTOMER_ID_PATTERN = getCachedPattern("stripe_customer_id", 
        "\"customer_id\"\\s*:\\s*\"([^\"]+)\"");
    
    // ==================== PATTERNS BELGIQUE ====================
    
    // Pattern pour numéros belges: +32 475 20 65 25 ou 0475 20 65 25
    public static final Pattern BELGIAN_MOBILE_PATTERN = getCachedPattern("belgian_mobile",
        "^(\\+32\\s?|0)([1-9]\\d{1,2})(\\s?\\d{2}){3}$");
    
    // Pattern général pour numéros internationaux: +XX XXX XXX XXX
    public static final Pattern INTERNATIONAL_PHONE_PATTERN = getCachedPattern("international_phone",
        "^\\+[1-9]\\d{1,14}$");
    
    /**
     * Constructeur privé pour classe utilitaire
     */
    private PatternUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Obtient un pattern compilé depuis le cache ou le compile s'il n'existe pas
     * @param key Clé du pattern dans le cache
     * @param regex Expression régulière à compiler
     * @return Pattern compilé
     */
    public static Pattern getCachedPattern(String key, String regex) {
        return PATTERN_CACHE.computeIfAbsent(key, k -> Pattern.compile(regex));
    }
    
    /**
     * Compile un pattern avec des flags spécifiques
     * @param key Clé du pattern dans le cache
     * @param regex Expression régulière à compiler
     * @param flags Flags de compilation (ex: Pattern.CASE_INSENSITIVE)
     * @return Pattern compilé
     */
    public static Pattern getCachedPattern(String key, String regex, int flags) {
        String cacheKey = key + "_" + flags;
        return PATTERN_CACHE.computeIfAbsent(cacheKey, k -> Pattern.compile(regex, flags));
    }
    
    /**
     * Vide le cache des patterns (utile pour les tests)
     */
    public static void clearCache() {
        PATTERN_CACHE.clear();
    }
    
    /**
     * Retourne la taille du cache
     * @return Nombre de patterns en cache
     */
    public static int getCacheSize() {
        return PATTERN_CACHE.size();
    }
    
    /**
     * Vérifie si un pattern est en cache
     * @param key Clé du pattern
     * @return true si le pattern est en cache
     */
    public static boolean isPatternCached(String key) {
        return PATTERN_CACHE.containsKey(key);
    }
}