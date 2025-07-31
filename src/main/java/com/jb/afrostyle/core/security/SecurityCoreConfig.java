package com.jb.afrostyle.core.security;

import com.jb.afrostyle.core.constants.SecurityConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.AuthenticationFailureLockedEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.context.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Configuration centralisée de sécurité pour le système /core
 * Fournit des beans et configurations partagés pour tous les modules
 * Intègre logging de sécurité et monitoring des événements d'authentification
 * 
 * @version 1.0
 * @since Java 21
 */
@Configuration
public class SecurityCoreConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityCoreConfig.class);
    
    // Statistiques de sécurité en mémoire (pour monitoring simple)
    private final Map<String, Object> securityStats = new ConcurrentHashMap<>();
    
    // Compteurs d'événements
    private volatile long successfulLogins = 0;
    private volatile long failedLogins = 0;
    private volatile long lockedAccounts = 0;
    
    public SecurityCoreConfig() {
        logger.info("Initializing Security Core Configuration");
        initializeSecurityStats();
    }
    
    // ==================== CONFIGURATION BEANS ====================
    
    /**
     * Bean pour les statistiques de sécurité
     * @return Map des statistiques de sécurité
     */
    @Bean
    public Map<String, Object> securityStatistics() {
        return securityStats;
    }
    
    // ==================== EVENT LISTENERS ====================
    
    /**
     * Écoute les événements de connexion réussie
     * @param event Événement d'authentification réussie
     */
    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        logger.info("✅ Successful authentication for user: {}", username);
        
        successfulLogins++;
        updateSecurityStats();
        
        // Log sécurisé pour audit
        auditLog("AUTHENTICATION_SUCCESS", username, null);
    }
    
    /**
     * Écoute les événements de connexion échouée (mauvais credentials)
     * @param event Événement d'échec d'authentification
     */
    @EventListener
    public void handleAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        logger.warn("❌ Failed authentication for user: {} - Bad credentials", username);
        
        failedLogins++;
        updateSecurityStats();
        
        // Log sécurisé pour audit
        auditLog("AUTHENTICATION_FAILURE", username, "bad_credentials");
    }
    
    /**
     * Écoute les événements de compte verrouillé
     * @param event Événement de compte verrouillé
     */
    @EventListener
    public void handleAccountLocked(AuthenticationFailureLockedEvent event) {
        String username = event.getAuthentication().getName();
        logger.error("🔒 Account locked for user: {}", username);
        
        lockedAccounts++;
        updateSecurityStats();
        
        // Log sécurisé pour audit
        auditLog("ACCOUNT_LOCKED", username, "account_locked");
    }
    
    /**
     * Écoute tous les événements d'authentification pour logging centralisé
     * @param event Événement d'authentification
     */
    @EventListener
    public void handleAuthenticationEvent(AbstractAuthenticationEvent event) {
        String eventType = event.getClass().getSimpleName();
        String username = event.getAuthentication().getName();
        
        logger.debug("🔐 Authentication event: {} for user: {}", eventType, username);
        
        // Mise à jour du timestamp de dernière activité
        securityStats.put("lastAuthenticationEvent", LocalDateTime.now());
        securityStats.put("lastAuthenticationEventType", eventType);
        securityStats.put("lastAuthenticationUser", username);
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Initialise les statistiques de sécurité
     */
    private void initializeSecurityStats() {
        securityStats.put("startupTime", LocalDateTime.now());
        securityStats.put("successfulLogins", 0L);
        securityStats.put("failedLogins", 0L);
        securityStats.put("lockedAccounts", 0L);
        securityStats.put("lastUpdate", LocalDateTime.now());
        securityStats.put("securityConstantsLoaded", SecurityConstants.JWT_EXPIRATION != null);
        
        logger.debug("Security statistics initialized");
    }
    
    /**
     * Met à jour les statistiques de sécurité
     */
    private void updateSecurityStats() {
        securityStats.put("successfulLogins", successfulLogins);
        securityStats.put("failedLogins", failedLogins);
        securityStats.put("lockedAccounts", lockedAccounts);
        securityStats.put("lastUpdate", LocalDateTime.now());
        
        // Calcul de métriques dérivées
        long totalAttempts = successfulLogins + failedLogins;
        if (totalAttempts > 0) {
            double successRate = (double) successfulLogins / totalAttempts * 100.0;
            securityStats.put("successRate", Math.round(successRate * 100.0) / 100.0);
        }
        
        logger.debug("Security statistics updated - Success: {}, Failed: {}, Locked: {}", 
                    successfulLogins, failedLogins, lockedAccounts);
    }
    
    /**
     * Log d'audit sécurisé
     * @param action Action effectuée
     * @param username Nom d'utilisateur (peut être null)
     * @param details Détails supplémentaires
     */
    private void auditLog(String action, String username, String details) {
        // Format standard pour audit de sécurité
        String auditMessage = String.format(
            "SECURITY_AUDIT: action=%s, user=%s, timestamp=%s, details=%s",
            action,
            username != null ? username : "unknown",
            LocalDateTime.now(),
            details != null ? details : "none"
        );
        
        // Log dans un logger spécial pour audit (configurable via logback)
        Logger auditLogger = LoggerFactory.getLogger("SECURITY_AUDIT");
        auditLogger.info(auditMessage);
    }
    
    // ==================== MÉTHODES PUBLIQUES ====================
    
    /**
     * Obtient les statistiques de sécurité actuelles
     * @return Map des statistiques
     */
    public Map<String, Object> getCurrentSecurityStats() {
        return Map.copyOf(securityStats);
    }
    
    /**
     * Remet à zéro les compteurs de sécurité
     */
    public void resetSecurityStats() {
        logger.info("Resetting security statistics");
        
        successfulLogins = 0;
        failedLogins = 0;
        lockedAccounts = 0;
        
        initializeSecurityStats();
        auditLog("STATS_RESET", "system", "security_stats_reset");
    }
    
    /**
     * Obtient le taux de succès des authentifications
     * @return Taux de succès en pourcentage
     */
    public double getAuthenticationSuccessRate() {
        long totalAttempts = successfulLogins + failedLogins;
        if (totalAttempts == 0) {
            return 0.0;
        }
        return (double) successfulLogins / totalAttempts * 100.0;
    }
    
    /**
     * Vérifie si le système a des problèmes de sécurité
     * @return true s'il y a des alertes de sécurité
     */
    public boolean hasSecurityAlerts() {
        // Critères d'alerte simples
        double successRate = getAuthenticationSuccessRate();
        long totalAttempts = successfulLogins + failedLogins;
        
        // Alerte si taux de succès très bas avec suffisamment de tentatives
        if (totalAttempts >= 10 && successRate < 50.0) {
            return true;
        }
        
        // Alerte si trop de comptes verrouillés
        if (lockedAccounts > 0) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Obtient un rapport de sécurité détaillé
     * @return String avec rapport de sécurité
     */
    public String getSecurityReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== SECURITY REPORT ===\n");
        report.append(String.format("Successful Logins: %d\n", successfulLogins));
        report.append(String.format("Failed Logins: %d\n", failedLogins));
        report.append(String.format("Locked Accounts: %d\n", lockedAccounts));
        report.append(String.format("Success Rate: %.2f%%\n", getAuthenticationSuccessRate()));
        report.append(String.format("Has Alerts: %s\n", hasSecurityAlerts() ? "YES" : "NO"));
        report.append(String.format("Last Update: %s\n", securityStats.get("lastUpdate")));
        report.append("========================");
        
        return report.toString();
    }
    
    /**
     * Valide la configuration de sécurité
     * @return true si configuration valide
     */
    public boolean validateSecurityConfiguration() {
        boolean valid = true;
        
        // Vérifier que les constantes de sécurité sont chargées
        if (SecurityConstants.JWT_EXPIRATION == null) {
            logger.error("SecurityConstants not properly loaded");
            valid = false;
        }
        
        // Vérifier les endpoints publics
        if (SecurityConstants.PUBLIC_ENDPOINTS.length == 0) {
            logger.warn("No public endpoints defined - may cause authentication issues");
        }
        
        // Vérifier les endpoints admin
        if (SecurityConstants.ADMIN_ENDPOINTS.length == 0) {
            logger.warn("No admin endpoints defined");
        }
        
        if (valid) {
            logger.info("✅ Security configuration validation passed");
        } else {
            logger.error("❌ Security configuration validation failed");
        }
        
        return valid;
    }
}