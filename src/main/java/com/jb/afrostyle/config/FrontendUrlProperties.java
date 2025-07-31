package com.jb.afrostyle.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Configuration properties for frontend URLs - URLS EXTERNALISÉES
 * 
 * Cette classe centralise TOUTES les URLs du frontend pour éviter les valeurs
 * hardcodées dans le code. Critical pour le déploiement multi-environnements.
 * 
 * CONFIGURATION REQUISE dans application.properties :
 * app.frontend.base-url=${FRONTEND_BASE_URL:http://localhost:4200}
 * app.frontend.payment.success-url=${PAYMENT_SUCCESS_URL:${app.frontend.base-url}/payment/success}
 * app.frontend.payment.cancel-url=${PAYMENT_CANCEL_URL:${app.frontend.base-url}/payment/cancel}
 * 
 * USAGE :
 * - Redirections Stripe après paiement
 * - Génération de liens dans les emails
 * - Configuration CORS
 * 
 * @author AfroStyle Team  
 * @since 1.0
 */
@ConfigurationProperties(prefix = "app.frontend")
@Component
@Data
public class FrontendUrlProperties {
    
    /**
     * URL de base de l'application frontend.
     * 
     * CONFIGURATION : app.frontend.base-url dans application.properties
     * 
     * EXEMPLES :
     * - Développement : http://localhost:4200
     * - Staging : https://staging.afrostyle.be
     * - Production : https://afrostyle.be
     * 
     * IMPORTANTE : Cette URL est utilisée pour construire toutes les autres URLs
     * du frontend si elles ne sont pas explicitement configurées.
     */
    private String baseUrl;
    
    /**
     * URLs liées aux paiements (Stripe redirections).
     */
    private PaymentUrls payment = new PaymentUrls();
    
    @Data
    public static class PaymentUrls {
        /**
         * URL de redirection après un paiement réussi.
         * 
         * CONFIGURATION : app.frontend.payment.success-url dans application.properties
         * 
         * USAGE : Stripe redirige vers cette URL après un paiement confirmé.
         * L'URL peut contenir des paramètres comme ?session_id={CHECKOUT_SESSION_ID}
         * 
         * EXEMPLE : https://afrostyle.be/payment/success
         */
        private String successUrl;
        
        /**
         * URL de redirection après un paiement annulé.
         * 
         * CONFIGURATION : app.frontend.payment.cancel-url dans application.properties
         * 
         * USAGE : Stripe redirige vers cette URL si l'utilisateur annule le paiement.
         * 
         * EXEMPLE : https://afrostyle.be/payment/cancel
         */
        private String cancelUrl;
    }
    
    /**
     * Initialise les URLs de paiement basées sur l'URL de base si elles ne sont pas configurées.
     * 
     * Cette méthode est appelée automatiquement après l'injection des propriétés.
     * Elle fournit des valeurs par défaut sûres basées sur baseUrl.
     * 
     * ATTENTION : En production, il est recommandé de configurer explicitement
     * toutes les URLs pour éviter les surprises.
     */
    @PostConstruct
    public void init() {
        if (payment.successUrl == null && baseUrl != null) {
            payment.successUrl = baseUrl + "/payment/success";
        }
        if (payment.cancelUrl == null && baseUrl != null) {
            payment.cancelUrl = baseUrl + "/payment/cancel";
        }
    }
}