package com.jb.afrostyle.core.util;

import com.jb.afrostyle.core.constants.BusinessConstants;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Générateur d'identifiants centralisé pour AfroStyle
 * Fournit des méthodes pour générer différents types d'IDs uniques
 * Intègre avec les constantes métier pour les formats standardisés
 * 
 * @version 1.0
 * @since Java 21
 */
@Component
public class IDGenerator {
    
    // ==================== CONSTANTES ====================
    
    private static final String TRANSACTION_PREFIX = "TXN_";
    private static final String BOOKING_PREFIX = "BKG_";
    private static final String PAYMENT_PREFIX = "PAY_";
    private static final String SESSION_PREFIX = "SES_";
    private static final String ORDER_PREFIX = "ORD_";
    private static final String INVOICE_PREFIX = "INV_";
    private static final String REFUND_PREFIX = "REF_";
    
    // Générateur sécurisé pour les IDs sensibles
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    // Compteur atomique pour les séquences
    private static final AtomicLong SEQUENCE_COUNTER = new AtomicLong(1);
    
    // Format date pour les IDs
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    // ==================== GÉNÉRATEURS D'IDS MÉTIER ====================
    
    /**
     * Génère un ID de transaction unique (ex: TXN_20241231235959_A1B2C3D4)
     * @return ID de transaction
     */
    public String generateTransactionId() {
        return TRANSACTION_PREFIX + 
               System.currentTimeMillis() + "_" +
               generateRandomAlphaNumeric(8).toUpperCase();
    }
    
    /**
     * Génère un ID de réservation unique (ex: BKG_20241231_001234_X1Y2)
     * @return ID de réservation
     */
    public String generateBookingId() {
        return BOOKING_PREFIX + 
               LocalDateTime.now().format(DATE_FORMAT) + "_" +
               String.format("%06d", SEQUENCE_COUNTER.getAndIncrement()) + "_" +
               generateRandomAlphaNumeric(4).toUpperCase();
    }
    
    /**
     * Génère un ID de paiement unique (ex: PAY_20241231235959_UUID8)
     * @return ID de paiement
     */
    public String generatePaymentId() {
        return PAYMENT_PREFIX + 
               System.currentTimeMillis() + "_" +
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Génère un ID de session unique (ex: SES_timestamp_random)
     * @return ID de session
     */
    public String generateSessionId() {
        return SESSION_PREFIX + 
               System.currentTimeMillis() + "_" +
               generateSecureRandom(16);
    }
    
    /**
     * Génère un numéro de commande (ex: ORD_2024_001234)
     * @return Numéro de commande
     */
    public String generateOrderNumber() {
        return ORDER_PREFIX + 
               LocalDateTime.now().getYear() + "_" +
               String.format("%06d", SEQUENCE_COUNTER.getAndIncrement());
    }
    
    /**
     * Génère un numéro de facture (ex: INV_2024001234)
     * @return Numéro de facture
     */
    public String generateInvoiceNumber() {
        return INVOICE_PREFIX + 
               LocalDateTime.now().getYear() +
               String.format("%06d", SEQUENCE_COUNTER.getAndIncrement());
    }
    
    /**
     * Génère un ID de remboursement (ex: REF_timestamp_UUID8)
     * @return ID de remboursement
     */
    public String generateRefundId() {
        return REFUND_PREFIX + 
               System.currentTimeMillis() + "_" +
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    // ==================== GÉNÉRATEURS UUID ====================
    
    /**
     * Génère un UUID standard
     * @return UUID sous forme de string
     */
    public String generateUUID() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Génère un UUID court (8 caractères)
     * @return UUID court
     */
    public String generateShortUUID() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Génère un UUID sans tirets
     * @return UUID sans tirets
     */
    public String generateCompactUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * Génère un UUID sécurisé avec SecureRandom
     * @return UUID sécurisé
     */
    public String generateSecureUUID() {
        byte[] randomBytes = new byte[16];
        SECURE_RANDOM.nextBytes(randomBytes);
        
        // Construire UUID depuis bytes
        long mostSigBits = 0;
        long leastSigBits = 0;
        
        for (int i = 0; i < 8; i++) {
            mostSigBits = (mostSigBits << 8) | (randomBytes[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            leastSigBits = (leastSigBits << 8) | (randomBytes[i] & 0xff);
        }
        
        return new UUID(mostSigBits, leastSigBits).toString();
    }
    
    // ==================== GÉNÉRATEURS ALPHANUMÉRIQUES ====================
    
    /**
     * Génère une chaîne alphanumérique aléatoire
     * @param length Longueur souhaitée
     * @return Chaîne alphanumérique
     */
    public String generateRandomAlphaNumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        
        return result.toString();
    }
    
    /**
     * Génère une chaîne alphabétique aléatoire (lettres seulement)
     * @param length Longueur souhaitée
     * @return Chaîne alphabétique
     */
    public String generateRandomAlpha(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        
        return result.toString();
    }
    
    /**
     * Génère une chaîne numérique aléatoire
     * @param length Longueur souhaitée
     * @return Chaîne numérique
     */
    public String generateRandomNumeric(int length) {
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            result.append(SECURE_RANDOM.nextInt(10));
        }
        
        return result.toString();
    }
    
    /**
     * Génère une chaîne hexadécimale aléatoire
     * @param length Longueur souhaitée
     * @return Chaîne hexadécimale
     */
    public String generateRandomHex(int length) {
        String chars = "0123456789ABCDEF";
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        
        return result.toString();
    }
    
    // ==================== GÉNÉRATEURS SÉCURISÉS ====================
    
    /**
     * Génère une chaîne aléatoire sécurisée pour les tokens
     * @param length Longueur souhaitée
     * @return Token sécurisé
     */
    public String generateSecureToken(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        
        return result.toString();
    }
    
    /**
     * Génère une chaîne aléatoire sécurisée (bytes + base64)
     * @param byteLength Nombre de bytes aléatoires
     * @return String base64 sécurisée
     */
    public String generateSecureRandom(int byteLength) {
        byte[] randomBytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(randomBytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    /**
     * Génère un code de vérification numérique
     * @param digits Nombre de chiffres
     * @return Code de vérification
     */
    public String generateVerificationCode(int digits) {
        int min = (int) Math.pow(10, digits - 1);
        int max = (int) Math.pow(10, digits) - 1;
        return String.valueOf(SECURE_RANDOM.nextInt(max - min + 1) + min);
    }
    
    // ==================== GÉNÉRATEURS SPÉCIALISÉS ====================
    
    /**
     * Génère un code promo aléatoire (ex: SAVE20XYZ)
     * @return Code promo
     */
    public String generatePromoCode() {
        String prefix = generateRandomAlpha(4);
        String suffix = generateRandomNumeric(2) + generateRandomAlpha(3);
        return prefix + suffix;
    }
    
    /**
     * Génère un nom de fichier unique avec extension
     * @param originalName Nom original du fichier
     * @return Nom de fichier unique
     */
    public String generateUniqueFileName(String originalName) {
        if (originalName == null || originalName.trim().isEmpty()) {
            return "file_" + System.currentTimeMillis() + "_" + generateRandomAlphaNumeric(6);
        }
        
        // Extraire l'extension
        String extension = "";
        int lastDot = originalName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = originalName.substring(lastDot);
            originalName = originalName.substring(0, lastDot);
        }
        
        // Nettoyer le nom original
        String cleanName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        return cleanName + "_" + System.currentTimeMillis() + "_" + generateRandomAlphaNumeric(4) + extension;
    }
    
    /**
     * Génère un slug URL-friendly unique
     * @param baseText Texte de base
     * @return Slug unique
     */
    public String generateUniqueSlug(String baseText) {
        if (baseText == null || baseText.trim().isEmpty()) {
            return "item-" + System.currentTimeMillis();
        }
        
        // Convertir en slug basique
        String slug = baseText.toLowerCase()
                             .trim()
                             .replaceAll("[àáâãäå]", "a")
                             .replaceAll("[èéêë]", "e")
                             .replaceAll("[ìíîï]", "i")
                             .replaceAll("[òóôõö]", "o")
                             .replaceAll("[ùúûü]", "u")
                             .replaceAll("[ç]", "c")
                             .replaceAll("[ñ]", "n")
                             .replaceAll("[^a-z0-9\\s-]", "")
                             .replaceAll("\\s+", "-")
                             .replaceAll("-+", "-")
                             .replaceAll("^-|-$", "");
        
        // Ajouter suffixe unique
        return slug + "-" + System.currentTimeMillis();
    }
    
    // ==================== UTILITAIRES DE VALIDATION ====================
    
    /**
     * Vérifie si un ID de transaction est valide
     * @param transactionId ID à vérifier
     * @return true si format valide
     */
    public boolean isValidTransactionId(String transactionId) {
        if (transactionId == null || !transactionId.startsWith(TRANSACTION_PREFIX)) {
            return false;
        }
        
        String[] parts = transactionId.substring(TRANSACTION_PREFIX.length()).split("_");
        return parts.length == 2 && 
               parts[0].matches("\\d+") && 
               parts[1].matches("[A-Z0-9]{8}");
    }
    
    /**
     * Vérifie si un ID de réservation est valide
     * @param bookingId ID à vérifier
     * @return true si format valide
     */
    public boolean isValidBookingId(String bookingId) {
        if (bookingId == null || !bookingId.startsWith(BOOKING_PREFIX)) {
            return false;
        }
        
        String[] parts = bookingId.substring(BOOKING_PREFIX.length()).split("_");
        return parts.length == 3 && 
               parts[0].matches("\\d{14}") && 
               parts[1].matches("\\d{6}") && 
               parts[2].matches("[A-Z0-9]{4}");
    }
    
    /**
     * Vérifie si un UUID est valide
     * @param uuid UUID à vérifier
     * @return true si format valide
     */
    public boolean isValidUUID(String uuid) {
        if (uuid == null) {
            return false;
        }
        
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    // ==================== MÉTHODES D'INFORMATION ====================
    
    /**
     * Extrait le timestamp d'un ID de transaction
     * @param transactionId ID de transaction
     * @return Timestamp ou null si invalide
     */
    public Long extractTimestampFromTransactionId(String transactionId) {
        if (!isValidTransactionId(transactionId)) {
            return null;
        }
        
        String timestampStr = transactionId.substring(TRANSACTION_PREFIX.length()).split("_")[0];
        try {
            return Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Obtient les statistiques du générateur
     * @return Map avec statistiques
     */
    public java.util.Map<String, Object> getGeneratorStatistics() {
        return java.util.Map.of(
            "sequenceCounter", SEQUENCE_COUNTER.get(),
            "secureRandomClass", SECURE_RANDOM.getClass().getSimpleName(),
            "prefixes", java.util.Map.of(
                "transaction", TRANSACTION_PREFIX,
                "booking", BOOKING_PREFIX,
                "payment", PAYMENT_PREFIX,
                "session", SESSION_PREFIX,
                "order", ORDER_PREFIX,
                "invoice", INVOICE_PREFIX,
                "refund", REFUND_PREFIX
            )
        );
    }
    
    /**
     * Réinitialise le compteur de séquence (pour testing)
     */
    public void resetSequenceCounter() {
        SEQUENCE_COUNTER.set(1);
    }
}