package com.jb.afrostyle.integrations.stripe.controller;

import com.jb.afrostyle.integrations.stripe.config.StripeConfig;
import com.jb.afrostyle.payment.dto.PaymentRequest;
import com.jb.afrostyle.payment.dto.PaymentResponse;
import com.jb.afrostyle.payment.service.PaymentService;
import com.jb.afrostyle.integrations.stripe.service.StripeCheckoutService;
import com.jb.afrostyle.core.security.AuthenticationUtil;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour Stripe Checkout
 * REFACTORISÉ : Délègue la logique métier à PaymentService
 */
@RestController
@RequestMapping("/api/payments/checkout")
@RequiredArgsConstructor
// CORS géré globalement dans SecurityConfig - @CrossOrigin supprimé
public class StripeCheckoutController {

    private static final Logger log = LoggerFactory.getLogger(StripeCheckoutController.class);

    private final StripeCheckoutService stripeCheckoutService;
    private final PaymentService paymentService;
    private final StripeConfig stripeConfig;
    private final AuthenticationUtil authenticationUtil;

    /**
     * Crée une session Stripe Checkout
     * REFACTORISÉ : Délègue toute la logique métier à PaymentService
     */
    @PostMapping("/create-session")
    public ResponseEntity<?> createCheckoutSession(
            @Valid @RequestBody PaymentRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("🚀 STRIPE CHECKOUT SESSION CREATION");
        
        try {
            // ÉTAPE 1: Authentification avec AuthenticationUtil
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long customerId = authenticationUtil.extractUserIdFromAuth(auth);
            
            if (customerId == null) {
                log.error("❌ User authentication failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(PaymentResponse.error("User authentication required"));
            }
            
            // ÉTAPE 2: Déléguer TOUTE la création à PaymentService
            PaymentResponse paymentResponse = paymentService.createPayment(request, customerId);
            
            // ÉTAPE 3: Adapter la réponse pour Stripe Checkout
            if (paymentResponse.clientSecret() != null) {
                // C'est une session Stripe (pas un paiement CASH)
                Map<String, Object> response = new HashMap<>();
                response.put("sessionId", paymentResponse.clientSecret()); // clientSecret = sessionId
                response.put("url", "https://checkout.stripe.com/c/pay/" + paymentResponse.clientSecret());
                response.put("paymentId", paymentResponse.paymentId());
                response.put("transactionId", paymentResponse.transactionId());
                response.put("publishableKey", stripeConfig.getPublishableKey());
                return ResponseEntity.ok(response);
            } else {
                // Paiement CASH
                return ResponseEntity.ok(paymentResponse);
            }
            
        } catch (Exception e) {
            log.error("❌ Unexpected error creating checkout session: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.error("Failed to create payment session: " + e.getMessage()));
        }
    }

    /**
     * Traite les webhooks Stripe
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleCheckoutWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature) {
        
        log.info("🔔 Received Stripe webhook");
        log.info("📝 Payload length: {}", payload != null ? payload.length() : 0);
        log.info("🔑 Signature present: {}", signature != null);
        
        try {
            stripeCheckoutService.processCheckoutWebhook(payload, signature);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("❌ Error processing webhook: {}", e.getMessage());
            e.printStackTrace(); // Pour voir la stack trace complète
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook processing failed: " + e.getMessage());
        }
    }

    /**
     * Récupère les détails d'une session
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<?> getSessionDetails(@PathVariable String sessionId) {
        try {
            Session session = stripeCheckoutService.retrieveSession(sessionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", session.getId());
            response.put("status", session.getStatus());
            response.put("paymentStatus", session.getPaymentStatus());
            response.put("amountTotal", session.getAmountTotal());
            response.put("currency", session.getCurrency());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving session: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Session not found"));
        }
    }

    /**
     * Expire une session
     */
    @PostMapping("/session/{sessionId}/expire")
    public ResponseEntity<?> expireSession(@PathVariable String sessionId) {
        try {
            Session session = stripeCheckoutService.expireSession(sessionId);
            return ResponseEntity.ok(Map.of("status", "expired", "sessionId", session.getId()));
        } catch (Exception e) {
            log.error("Error expiring session: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to expire session"));
        }
    }
}