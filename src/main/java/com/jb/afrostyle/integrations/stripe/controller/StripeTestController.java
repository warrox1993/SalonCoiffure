package com.jb.afrostyle.integrations.stripe.controller;

import com.jb.afrostyle.config.FrontendUrlProperties;
import com.jb.afrostyle.integrations.stripe.config.StripeConfig;
import com.jb.afrostyle.integrations.stripe.service.StripeCheckoutService;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur de test pour Stripe (uniquement en mode développement)
 * Permet de tester l'intégration Stripe sans passer par tout le système
 */
@RestController
@RequestMapping("/api/payments/test")
@RequiredArgsConstructor
@Profile({"dev", "test"}) // Actif seulement en dev/test
public class StripeTestController {

    private static final Logger log = LoggerFactory.getLogger(StripeTestController.class);

    private final StripeCheckoutService stripeCheckoutService;
    private final StripeConfig stripeConfig;
    private final FrontendUrlProperties frontendUrlProperties;

    /**
     * Test de connexion Stripe
     */
    @GetMapping("/stripe-status")
    public ResponseEntity<Map<String, Object>> getStripeStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            String publishableKey = stripeConfig.getPublishableKey();

            status.put("status", "connected");
            status.put("publishableKey", publishableKey != null ?
                    publishableKey.substring(0, 12) + "..." : "not configured");
            status.put("message", "Stripe is properly configured");

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("Stripe status check failed: {}", e.getMessage());
            status.put("status", "error");
            status.put("message", "Stripe configuration error: " + e.getMessage());
            return ResponseEntity.status(500).body(status);
        }
    }

    /**
     * Test de création de session Stripe simple
     */
    @PostMapping("/create-test-session")
    public ResponseEntity<Map<String, Object>> createTestSession(
            @RequestParam(defaultValue = "10.00") BigDecimal amount) {
        
        log.info("Creating test Stripe session for amount: {}", amount);
        
        try {
            Session session = stripeCheckoutService.createCheckoutSession(
                    amount,
                    999L, // Test booking ID
                    1L,   // Test customer ID
                    "Test payment - AfroStyle",
                    frontendUrlProperties.getPayment().getSuccessUrl(),
                    frontendUrlProperties.getPayment().getCancelUrl()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", session.getId());
            response.put("sessionUrl", session.getUrl());
            response.put("status", session.getStatus());
            response.put("amount", amount);
            response.put("currency", "EUR");
            response.put("publishableKey", stripeConfig.getPublishableKey());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Test session creation failed: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to create test session: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Test de récupération de session
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getTestSession(@PathVariable String sessionId) {
        try {
            Session session = stripeCheckoutService.retrieveSession(sessionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", session.getId());
            response.put("status", session.getStatus());
            response.put("paymentStatus", session.getPaymentStatus());
            response.put("amountTotal", session.getAmountTotal());
            response.put("currency", session.getCurrency());
            response.put("url", session.getUrl());
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Test session retrieval failed: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to retrieve session: " + e.getMessage());
            return ResponseEntity.status(404).body(error);
        }
    }

    /**
     * Endpoint pour la clé publique Stripe
     */
    @GetMapping("/publishable-key")
    public ResponseEntity<Map<String, String>> getPublishableKey() {
        Map<String, String> response = new HashMap<>();
        response.put("publishableKey", stripeConfig.getPublishableKey());
        return ResponseEntity.ok(response);
    }
}