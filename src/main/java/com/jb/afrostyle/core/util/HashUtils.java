package com.jb.afrostyle.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilitaires de hachage et cryptographie pour AfroStyle
 * Fournit des méthodes sécurisées pour le hachage, les signatures et la génération de tokens
 * Utilise des algorithmes standard avec bonnes pratiques de sécurité
 * 
 * @version 1.0
 * @since Java 21
 */
public final class HashUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(HashUtils.class);
    
    // ==================== CONSTANTES ====================
    
    // Algorithmes de hachage
    public static final String MD5 = "MD5";
    public static final String SHA1 = "SHA-1";
    public static final String SHA256 = "SHA-256";
    public static final String SHA512 = "SHA-512";
    
    // Algorithmes HMAC
    public static final String HMAC_SHA256 = "HmacSHA256";
    public static final String HMAC_SHA512 = "HmacSHA512";
    
    // Générateur sécurisé pour les sels
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    // Longueur par défaut des sels
    private static final int DEFAULT_SALT_LENGTH = 32;
    
    /**
     * Constructeur privé pour classe utilitaire
     */
    private HashUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // ==================== HACHAGE SIMPLE ====================
    
    /**
     * Hash MD5 (à éviter pour la sécurité, utilisé pour checksums)
     * @param input Données à hasher
     * @return Hash MD5 en hexadécimal
     */
    public static String md5(String input) {
        return hash(input, MD5);
    }
    
    /**
     * Hash SHA-1 (déprécié pour la sécurité)
     * @param input Données à hasher
     * @return Hash SHA-1 en hexadécimal
     */
    public static String sha1(String input) {
        return hash(input, SHA1);
    }
    
    /**
     * Hash SHA-256 (recommandé)
     * @param input Données à hasher
     * @return Hash SHA-256 en hexadécimal
     */
    public static String sha256(String input) {
        return hash(input, SHA256);
    }
    
    /**
     * Hash SHA-512 (recommandé pour haute sécurité)
     * @param input Données à hasher
     * @return Hash SHA-512 en hexadécimal
     */
    public static String sha512(String input) {
        return hash(input, SHA512);
    }
    
    /**
     * Hash avec algorithme spécifié
     * @param input Données à hasher
     * @param algorithm Algorithme de hachage
     * @return Hash en hexadécimal ou null si erreur
     */
    public static String hash(String input, String algorithm) {
        if (StringUtils.isBlank(input) || StringUtils.isBlank(algorithm)) {
            return null;
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Hash algorithm not supported: {}", algorithm, e);
            return null;
        }
    }
    
    /**
     * Hash des bytes avec algorithme spécifié
     * @param input Bytes à hasher
     * @param algorithm Algorithme de hachage
     * @return Hash en hexadécimal ou null si erreur
     */
    public static String hash(byte[] input, String algorithm) {
        if (input == null || StringUtils.isBlank(algorithm)) {
            return null;
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashBytes = digest.digest(input);
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Hash algorithm not supported: {}", algorithm, e);
            return null;
        }
    }
    
    // ==================== HACHAGE AVEC SEL ====================
    
    /**
     * Génère un sel aléatoire sécurisé
     * @return Sel en base64
     */
    public static String generateSalt() {
        return generateSalt(DEFAULT_SALT_LENGTH);
    }
    
    /**
     * Génère un sel aléatoire sécurisé de longueur spécifiée  
     * @param length Longueur en bytes
     * @return Sel en base64
     */
    public static String generateSalt(int length) {
        byte[] salt = new byte[length];
        SECURE_RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Hash avec sel SHA-256
     * @param input Données à hasher
     * @param salt Sel à utiliser
     * @return Hash salé en hexadécimal
     */
    public static String sha256WithSalt(String input, String salt) {
        return hashWithSalt(input, salt, SHA256);
    }
    
    /**
     * Hash avec sel SHA-512  
     * @param input Données à hasher
     * @param salt Sel à utiliser
     * @return Hash salé en hexadécimal
     */
    public static String sha512WithSalt(String input, String salt) {
        return hashWithSalt(input, salt, SHA512);
    }
    
    /**
     * Hash avec sel et algorithme spécifié
     * @param input Données à hasher
     * @param salt Sel à utiliser
     * @param algorithm Algorithme de hachage
     * @return Hash salé en hexadécimal ou null si erreur
     */
    public static String hashWithSalt(String input, String salt, String algorithm) {
        if (StringUtils.isBlank(input) || StringUtils.isBlank(salt) || StringUtils.isBlank(algorithm)) {
            return null;
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            
            // Ajouter le sel avant le hash
            digest.update(Base64.getDecoder().decode(salt));
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            logger.error("Error hashing with salt using {}: {}", algorithm, e.getMessage());
            return null;
        }
    }
    
    /**
     * Génère un hash avec un nouveau sel
     * @param input Données à hasher
     * @param algorithm Algorithme de hachage
     * @return SaltedHash contenant le hash et le sel
     */
    public static SaltedHash generateSaltedHash(String input, String algorithm) {
        if (StringUtils.isBlank(input) || StringUtils.isBlank(algorithm)) {
            return null;
        }
        
        String salt = generateSalt();
        String hash = hashWithSalt(input, salt, algorithm);
        
        return hash != null ? new SaltedHash(hash, salt) : null;
    }
    
    /**
     * Génère un hash SHA-256 avec un nouveau sel
     * @param input Données à hasher
     * @return SaltedHash contenant le hash et le sel
     */
    public static SaltedHash generateSaltedSha256(String input) {
        return generateSaltedHash(input, SHA256);
    }
    
    /**
     * Vérifie un hash salé
     * @param input Données originales
     * @param expectedHash Hash attendu
     * @param salt Sel utilisé
     * @param algorithm Algorithme de hachage
     * @return true si hash correspond
     */
    public static boolean verifySaltedHash(String input, String expectedHash, String salt, String algorithm) {
        String computedHash = hashWithSalt(input, salt, algorithm);
        return computedHash != null && computedHash.equals(expectedHash);
    }
    
    // ==================== HMAC ====================
    
    /**
     * Calcule HMAC-SHA256
     * @param data Données à signer
     * @param key Clé secrète
     * @return HMAC en hexadécimal
     */
    public static String hmacSha256(String data, String key) {
        return hmac(data, key, HMAC_SHA256);
    }
    
    /**
     * Calcule HMAC-SHA512
     * @param data Données à signer
     * @param key Clé secrète
     * @return HMAC en hexadécimal
     */
    public static String hmacSha512(String data, String key) {
        return hmac(data, key, HMAC_SHA512);
    }
    
    /**
     * Calcule HMAC avec algorithme spécifié
     * @param data Données à signer
     * @param key Clé secrète
     * @param algorithm Algorithme HMAC
     * @return HMAC en hexadécimal ou null si erreur
     */
    public static String hmac(String data, String key, String algorithm) {
        if (StringUtils.isBlank(data) || StringUtils.isBlank(key) || StringUtils.isBlank(algorithm)) {
            return null;
        }
        
        try {
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
            mac.init(secretKeySpec);
            
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmacBytes);
        } catch (Exception e) {
            logger.error("Error calculating HMAC with {}: {}", algorithm, e.getMessage());
            return null;
        }
    }
    
    /**
     * Calcule HMAC et retourne en base64
     * @param data Données à signer
     * @param key Clé secrète
     * @param algorithm Algorithme HMAC
     * @return HMAC en base64 ou null si erreur
     */
    public static String hmacBase64(String data, String key, String algorithm) {
        if (StringUtils.isBlank(data) || StringUtils.isBlank(key) || StringUtils.isBlank(algorithm)) {
            return null;
        }
        
        try {
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
            mac.init(secretKeySpec);
            
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (Exception e) {
            logger.error("Error calculating HMAC base64 with {}: {}", algorithm, e.getMessage());
            return null;
        }
    }
    
    /**
     * Vérifie une signature HMAC
     * @param data Données originales
     * @param signature Signature à vérifier
     * @param key Clé secrète
     * @param algorithm Algorithme HMAC
     * @return true si signature valide
     */
    public static boolean verifyHmac(String data, String signature, String key, String algorithm) {
        String computedHmac = hmac(data, key, algorithm);
        return computedHmac != null && constantTimeEquals(computedHmac, signature);
    }
    
    // ==================== HACHAGE DE MOTS DE PASSE ====================
    
    /**
     * Hash un mot de passe avec salt généré automatiquement (SHA-512)
     * Recommandé pour les nouveaux mots de passe
     * @param password Mot de passe à hasher
     * @return SaltedHash contenant le hash et le sel
     */
    public static SaltedHash hashPassword(String password) {
        return generateSaltedSha512(password);
    }
    
    /**
     * Génère un hash SHA-512 avec un nouveau sel
     * @param input Données à hasher
     * @return SaltedHash contenant le hash et le sel
     */
    public static SaltedHash generateSaltedSha512(String input) {
        return generateSaltedHash(input, SHA512);
    }
    
    /**
     * Vérifie un mot de passe hashé
     * @param password Mot de passe en clair
     * @param hashedPassword Hash stocké
     * @param salt Sel utilisé
     * @return true si mot de passe correct
     */
    public static boolean verifyPassword(String password, String hashedPassword, String salt) {
        return verifySaltedHash(password, hashedPassword, salt, SHA512);
    }
    
    /**
     * Hash un mot de passe avec algorithme spécifique (pour compatibilité)
     * @param password Mot de passe à hasher
     * @param algorithm Algorithme à utiliser
     * @return SaltedHash contenant le hash et le sel
     */
    public static SaltedHash hashPasswordWithAlgorithm(String password, String algorithm) {
        return generateSaltedHash(password, algorithm);
    }
    
    // ==================== UTILITAIRES ====================
    
    /**
     * Convertit des bytes en représentation hexadécimale
     * @param bytes Bytes à convertir
     * @return String hexadécimale
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    /**
     * Convertit une string hexadécimale en bytes
     * @param hex String hexadécimale
     * @return Bytes correspondants
     */
    public static byte[] hexToBytes(String hex) {
        if (StringUtils.isBlank(hex) || hex.length() % 2 != 0) {
            return null;
        }
        
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) +
                                   Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }
    
    /**
     * Comparaison à temps constant pour éviter les attaques timing
     * @param a Première chaîne
     * @param b Deuxième chaîne
     * @return true si chaînes égales
     */
    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        
        return result == 0;
    }
    
    /**
     * Génère un token sécurisé aléatoire
     * @param length Longueur en bytes
     * @return Token en base64
     */
    public static String generateSecureToken(int length) {
        byte[] token = new byte[length];
        SECURE_RANDOM.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }
    
    /**
     * Génère un token sécurisé de 32 bytes
     * @return Token en base64
     */
    public static String generateSecureToken() {
        return generateSecureToken(32);
    }
    
    /**
     * Hash pour checksum simple (MD5)
     * @param input Données à hasher
     * @return Checksum MD5
     */
    public static String checksum(String input) {
        return md5(input);
    }
    
    /**
     * Hash pour checksum sécurisé (SHA-256)
     * @param input Données à hasher
     * @return Checksum SHA-256
     */
    public static String secureChecksum(String input) {
        return sha256(input);
    }
    
    /**
     * Vérifie l'intégrité d'un fichier avec checksum
     * @param content Contenu du fichier
     * @param expectedChecksum Checksum attendu
     * @param algorithm Algorithme utilisé
     * @return true si intégrité vérifiée
     */
    public static boolean verifyIntegrity(String content, String expectedChecksum, String algorithm) {
        String computedChecksum = hash(content, algorithm);
        return computedChecksum != null && computedChecksum.equals(expectedChecksum);
    }
    
    // ==================== CLASSE INTERNE ====================
    
    /**
     * Classe pour stocker un hash avec son sel
     */
    public static class SaltedHash {
        private final String hash;
        private final String salt;
        
        public SaltedHash(String hash, String salt) {
            this.hash = hash;
            this.salt = salt;
        }
        
        public String getHash() {
            return hash;
        }
        
        public String getSalt() {
            return salt;
        }
        
        @Override
        public String toString() {
            return "SaltedHash{hash='" + (hash != null ? hash.substring(0, Math.min(hash.length(), 8)) + "..." : "null") + 
                   "', salt='" + (salt != null ? salt.substring(0, Math.min(salt.length(), 8)) + "..." : "null") + "'}";
        }
        
        /**
         * Combine hash et salt en une seule string pour stockage
         * Format: {algorithm}${salt}${hash}
         * @param algorithm Algorithme utilisé
         * @return String combinée
         */
        public String toCombinedString(String algorithm) {
            return String.format("%s$%s$%s", algorithm, salt, hash);
        }
        
        /**
         * Parse une string combinée en SaltedHash
         * @param combinedString String au format {algorithm}${salt}${hash}
         * @return SaltedHash ou null si format invalide
         */
        public static SaltedHash fromCombinedString(String combinedString) {
            if (StringUtils.isBlank(combinedString)) {
                return null;
            }
            
            String[] parts = combinedString.split("\\$");
            if (parts.length != 3) {
                return null;
            }
            
            return new SaltedHash(parts[2], parts[1]);
        }
        
        /**
         * Obtient l'algorithme depuis une string combinée
         * @param combinedString String au format {algorithm}${salt}${hash}
         * @return Algorithme ou null si format invalide
         */
        public static String getAlgorithmFromCombinedString(String combinedString) {
            if (StringUtils.isBlank(combinedString)) {
                return null;
            }
            
            String[] parts = combinedString.split("\\$");
            return parts.length >= 1 ? parts[0] : null;
        }
    }
}