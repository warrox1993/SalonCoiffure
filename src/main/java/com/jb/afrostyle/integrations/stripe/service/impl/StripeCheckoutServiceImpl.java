package com.jb.afrostyle.integrations.stripe.service.impl;

import com.jb.afrostyle.booking.domain.enums.BookingStatus;
import com.jb.afrostyle.config.BusinessConstants;
import com.jb.afrostyle.payment.domain.enums.PaymentStatus;
import com.jb.afrostyle.payment.domain.entity.Payment;
import com.jb.afrostyle.payment.repository.PaymentRepository;
import com.jb.afrostyle.integrations.stripe.service.StripeCheckoutService;
import com.jb.afrostyle.integrations.stripe.service.WebhookEventService;
import com.jb.afrostyle.integrations.stripe.service.WebhookVersionManager;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeError;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.LineItem;
import com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StripeCheckoutServiceImpl implements StripeCheckoutService {

    private static final Logger log = LoggerFactory.getLogger(StripeCheckoutServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final BusinessConstants businessConstants;
    private final WebhookVersionManager versionManager;
    private final WebhookEventService eventService;

    @Value("${app.security.stripe.webhook-secret}")
    private String webhookSecret;

    @Override
    public Session createCheckoutSession(
            BigDecimal amount,
            Long bookingId,
            Long customerId,
            String description,
            String successUrl,
            String cancelUrl
    ) throws StripeException {
        
        log.info("🚀 CREATING STRIPE CHECKOUT SESSION");
        log.info("   - Amount: {} EUR", amount);
        log.info("   - Booking ID: {}", bookingId);
        log.info("   - Customer ID: {}", customerId);
        log.info("   - Description: {}", description);

        // Convertir le montant en centimes
        Long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

        // Créer les paramètres de la session Checkout
        SessionCreateParams params = SessionCreateParams.builder()
                // URLs de redirection
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                
                // Mode de paiement unique
                .setMode(SessionCreateParams.Mode.PAYMENT)
                
                // Métadonnées pour le suivi - MONO-SALON : Plus besoin de salon_id
                .putMetadata("booking_id", bookingId.toString())
                .putMetadata("customer_id", customerId.toString())
                
                // Ligne d'article avec prix dynamique
                .addLineItem(
                    LineItem.builder()
                        .setPriceData(
                            PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount(amountInCents)
                                .setProductData(
                                    PriceData.ProductData.builder()
                                        .setName("Réservation AfroStyle")
                                        .setDescription(description)
                                        .build()
                                )
                                .build()
                        )
                        .setQuantity(1L)
                        .build()
                )
                
                // Options de paiement
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.BANCONTACT)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.IDEAL)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.SEPA_DEBIT)
                
                // Collecte automatique de l'email
                .setCustomerEmail(null) // Sera fourni par le frontend si nécessaire
                
                // Expiration configurée via BusinessConstants (plus de valeur hardcodée)
                .setExpiresAt(System.currentTimeMillis() / 1000L + (businessConstants.getStripe().getSessionExpiresMinutes() * 60))
                
                // Locale pour l'interface
                .setLocale(SessionCreateParams.Locale.FR)
                
                // Options de facturation
                .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.AUTO)
                
                .build();

        log.info("📤 Creating Checkout Session with Stripe API...");
        
        try {
            Session session = Session.create(params);
            
            log.info("✅ CHECKOUT SESSION CREATED SUCCESSFULLY!");
            log.info("   - Session ID: {}", session.getId());
            log.info("   - URL: {}", session.getUrl());
            log.info("   - Status: {}", session.getStatus());
            log.info("   - Payment Status: {}", session.getPaymentStatus());
            
            return session;
            
        } catch (StripeException e) {
            log.error("❌ STRIPE CHECKOUT SESSION CREATION FAILED!");
            log.error("   - Error Code: {}", e.getCode());
            log.error("   - Error Message: {}", e.getMessage());
            log.error("   - Request ID: {}", e.getRequestId());
            throw e;
        }
    }

    @Override
    public Session retrieveSession(String sessionId) throws StripeException {
        log.info("Retrieving Stripe Checkout Session: {}", sessionId);
        return Session.retrieve(sessionId);
    }

    @Override
    public Session expireSession(String sessionId) throws StripeException {
        log.info("Expiring Stripe Checkout Session: {}", sessionId);
        Session session = Session.retrieve(sessionId);
        return session.expire();
    }

    @Override
    @Transactional
    public void processCheckoutWebhook(String payload, String signature) throws StripeException {
        processCheckoutWebhookWithVersion(payload, signature, null);
    }
    
    /**
     * Version améliorée du processeur webhook avec gestion des versions.
     */
    public void processCheckoutWebhookWithVersion(String payload, String signature, String versionParam) throws StripeException {
        log.info("🔄 Processing Stripe Checkout webhook with version management");
        log.info("📝 Payload: {}", payload != null ? payload.substring(0, Math.min(200, payload.length())) + "..." : "NULL");
        log.info("🔑 Signature: {}", signature != null ? signature.substring(0, Math.min(50, signature.length())) + "..." : "NULL");
        log.info("🏷️ Version param: {}", versionParam);

        // Vérification de la signature webhook avec le secret configuré
        if (webhookSecret == null || webhookSecret.trim().isEmpty()) {
            log.error("❌ Webhook secret not configured!");
            throw new RuntimeException("Webhook secret not configured");
        }

        if (signature == null || signature.trim().isEmpty()) {
            log.error("❌ Stripe-Signature header missing!");
            throw new RuntimeException("Stripe signature header missing");
        }

        log.info("🔐 Webhook secret configured: {}", webhookSecret.substring(0, 10) + "...");
        log.info("🔑 Signature received: {}", signature.substring(0, 20) + "...");

        try {
            Event event = Webhook.constructEvent(payload, signature, webhookSecret);
            log.info("✅ Webhook signature verified successfully");
            
            // Gestion des versions et idempotence
            String eventId = event.getId();
            String apiVersion = event.getApiVersion();
            
            log.info("🆔 Event ID: {}", eventId);
            log.info("🏷️ API Version: {}", apiVersion);
            
            // Vérifier si l'événement a déjà été traité (idempotence)
            if (eventService.hasEventBeenProcessed(eventId)) {
                log.info("🔄 Event {} already processed, returning 200", eventId);
                return; // Retourner 200 pour éviter les retries
            }
            
            // Déterminer l'action à prendre selon la version
            WebhookVersionManager.WebhookAction action = versionManager.determineAction(apiVersion, versionParam);
            
            switch (action) {
                case PROCESS:
                    log.info("✅ Processing event {} with version {}", eventId, apiVersion);
                    processEventWithIdempotency(event);
                    break;
                    
                case REJECT_LEGACY:
                    log.warn("❌ Rejecting legacy event {} (version {})", eventId, apiVersion);
                    throw new RuntimeException("Legacy API version deprecated - please retry");
                    
                case IGNORE_NEW:
                    log.info("⏳ Ignoring new event {} during migration preparation", eventId);
                    return; // Retourner 200 sans traitement
                    
                case UNSUPPORTED:
                    log.error("❌ Unsupported API version {} for event {}", apiVersion, eventId);
                    throw new RuntimeException("Unsupported API version: " + apiVersion);
                    
                default:
                    log.error("❌ Unknown webhook action: {}", action);
                    throw new RuntimeException("Unknown webhook action");
            }
            
        } catch (SignatureVerificationException e) {
            log.error("❌ Invalid Stripe webhook signature: {}", e.getMessage());
            throw new RuntimeException("Invalid webhook signature");
        } catch (Exception e) {
            log.error("💥 UNEXPECTED ERROR in webhook processing: {}", e.getMessage());
            log.error("💥 Exception type: {}", e.getClass().getSimpleName());
            log.error("💥 Stack trace: ", e);
            throw new RuntimeException("Webhook processing failed: " + e.getMessage());
        }
    }
    
    /**
     * Traite un événement avec gestion de l'idempotence.
     */
    private void processEventWithIdempotency(Event event) {
        String eventId = event.getId();
        
        // Marquer comme en cours de traitement
        if (!eventService.markEventAsProcessing(eventId)) {
            log.warn("⚠️ Event {} is already being processed by another instance", eventId);
            return;
        }
        
        try {
            log.info("🎯 About to call handleStripeEvent for event {}...", eventId);
            handleStripeEvent(event);
            
            // Marquer comme traité avec succès
            eventService.markEventAsCompleted(eventId);
            log.info("✅ Event {} processed and marked as completed", eventId);
            
        } catch (Exception e) {
            // Marquer comme échoué
            eventService.markEventAsFailed(eventId, e.getMessage());
            log.error("❌ Event {} failed and marked as failed", eventId);
            throw e; // Re-lancer pour que l'erreur remonte
        }
    }

    @Override
    public void handleStripeEvent(Event event) {
        log.info("🔄 Processing Stripe event: {} - {}", event.getType(), event.getId());

        try {
            log.info("🔍 Entering switch statement for event type: '{}'", event.getType());
            
            switch (event.getType()) {
                case "checkout.session.completed":
                    log.info("✅ MATCHED: checkout.session.completed - calling handler");
                    handleCheckoutSessionCompleted(event);
                    log.info("✅ Handler checkout.session.completed executed");
                    break;
                    
                case "checkout.session.expired":
                    log.info("✅ MATCHED: checkout.session.expired - calling handler");
                    handleCheckoutSessionExpired(event);
                    break;
                    
                case "checkout.session.async_payment_succeeded":
                    log.info("✅ MATCHED: checkout.session.async_payment_succeeded - calling handler");
                    handleAsyncPaymentSucceeded(event);
                    break;
                    
                case "checkout.session.async_payment_failed":
                    log.info("✅ MATCHED: checkout.session.async_payment_failed - calling handler");
                    handleAsyncPaymentFailed(event);
                    break;
                    
                case "payment_intent.succeeded":
                    log.info("✅ MATCHED: payment_intent.succeeded - calling handler");
                    handlePaymentIntentSucceeded(event);
                    break;
                    
                case "payment_intent.payment_failed":
                    log.info("✅ MATCHED: payment_intent.payment_failed - calling handler");
                    handlePaymentIntentFailed(event);
                    break;
                    
                case "charge.succeeded":
                    log.info("✅ MATCHED: charge.succeeded - calling handler");
                    handleChargeSucceeded(event);
                    break;
                    
                default:
                    log.info("❌ UNHANDLED: Stripe event type: '{}'", event.getType());
            }
        } catch (Exception e) {
            log.error("💥 ERROR processing Stripe event {}: {}", event.getType(), e.getMessage());
            log.error("💥 Exception type: {}", e.getClass().getSimpleName());
            log.error("💥 Full stack trace: ", e);
            // Ne pas relancer l'exception pour éviter de casser le webhook
        }
    }

    private void processWebhookEvent(String payload) {
        try {
            Event event = Event.GSON.fromJson(payload, Event.class);
            handleStripeEvent(event);
        } catch (Exception e) {
            log.error("Error parsing webhook payload: {}", e.getMessage());
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {
        log.info("🎯 HANDLING CHECKOUT SESSION COMPLETED - TRYING 5 ROBUST SOLUTIONS");
        
        // Essayer les 5 solutions dans l'ordre de robustesse
        if (handleCheckoutSessionCompleted_Solution1(event)) return;
        if (handleCheckoutSessionCompleted_Solution2(event)) return;
        if (handleCheckoutSessionCompleted_Solution3(event)) return;
        if (handleCheckoutSessionCompleted_Solution4(event)) return;
        if (handleCheckoutSessionCompleted_Solution5(event)) return;
        
        log.error("❌ ALL 5 SOLUTIONS FAILED - Could not extract metadata from checkout.session.completed event");
        log.error("❌ Event data: {}", event.getData().toJson());
    }

    /**
     * SOLUTION 1: EventDataObjectDeserializer (Recommandée Stripe)
     */
    private boolean handleCheckoutSessionCompleted_Solution1(Event event) {
        log.info("🎯 SOLUTION 1: Using EventDataObjectDeserializer");
        
        try {
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            
            if (dataObjectDeserializer.getObject().isPresent()) {
                Session session = (Session) dataObjectDeserializer.getObject().get();
                
                String sessionId = session.getId();
                String bookingId = session.getMetadata().get("booking_id");
                String customerId = session.getMetadata().get("customer_id");
                
                log.info("✅ SOLUTION 1 SUCCESS - Session: {}, Booking: {}, Customer: {}", 
                        sessionId, bookingId, customerId);
                
                if (sessionId != null && bookingId != null && customerId != null) {
                    updatePaymentFromWebhookData(sessionId, bookingId, customerId);
                    return true;
                }
            }
            
            // Essayer deserializeUnsafe en cas de mismatch d'API
            Session session = (Session) dataObjectDeserializer.deserializeUnsafe();
            if (session != null && session.getMetadata() != null) {
                String sessionId = session.getId();
                String bookingId = session.getMetadata().get("booking_id");
                String customerId = session.getMetadata().get("customer_id");
                
                log.info("✅ SOLUTION 1 (unsafe) SUCCESS - Session: {}, Booking: {}, Customer: {}", 
                        sessionId, bookingId, customerId);
                
                if (sessionId != null && bookingId != null && customerId != null) {
                    updatePaymentFromWebhookData(sessionId, bookingId, customerId);
                    return true;
                }
            }
            
        } catch (Exception e) {
            log.warn("⚠️ SOLUTION 1 failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * SOLUTION 2: Jackson ObjectMapper avec Map Conversion
     */
    private boolean handleCheckoutSessionCompleted_Solution2(Event event) {
        log.info("🎯 SOLUTION 2: Using Jackson ObjectMapper");
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            
            java.util.Map<String, Object> eventDataMap = mapper.convertValue(event.getData(), java.util.Map.class);
            Object dataObject = eventDataMap.get("object");
            java.util.Map<String, Object> sessionMap = mapper.convertValue(dataObject, java.util.Map.class);
            
            String sessionId = (String) sessionMap.get("id");
            java.util.Map<String, String> metadata = (java.util.Map<String, String>) sessionMap.get("metadata");
            
            if (metadata != null) {
                String bookingId = metadata.get("booking_id");
                String customerId = metadata.get("customer_id");
                
                log.info("✅ SOLUTION 2 SUCCESS - Session: {}, Booking: {}, Customer: {}", 
                        sessionId, bookingId, customerId);
                
                if (sessionId != null && bookingId != null && customerId != null) {
                    updatePaymentFromWebhookData(sessionId, bookingId, customerId);
                    return true;
                }
            }
            
        } catch (Exception e) {
            log.warn("⚠️ SOLUTION 2 failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * SOLUTION 3: Raw JSON avec getRawJson()
     */
    private boolean handleCheckoutSessionCompleted_Solution3(Event event) {
        log.info("🎯 SOLUTION 3: Using getRawJson()");
        
        try {
            String rawJson = event.getDataObjectDeserializer().getRawJson();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(rawJson);
            
            String sessionId = rootNode.path("id").asText(null);
            com.fasterxml.jackson.databind.JsonNode metadataNode = rootNode.path("metadata");
            String bookingId = metadataNode.path("booking_id").asText(null);
            String customerId = metadataNode.path("customer_id").asText(null);
            
            log.info("✅ SOLUTION 3 SUCCESS - Session: {}, Booking: {}, Customer: {}", 
                    sessionId, bookingId, customerId);
            
            if (sessionId != null && bookingId != null && customerId != null && 
                !sessionId.isEmpty() && !bookingId.isEmpty() && !customerId.isEmpty()) {
                updatePaymentFromWebhookData(sessionId, bookingId, customerId);
                return true;
            }
            
        } catch (Exception e) {
            log.warn("⚠️ SOLUTION 3 failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * SOLUTION 4: Regex Pattern Matching
     */
    private boolean handleCheckoutSessionCompleted_Solution4(Event event) {
        log.info("🎯 SOLUTION 4: Using regex patterns");
        
        try {
            String jsonData = event.getData().toJson();
            
            java.util.regex.Pattern sessionIdPattern = java.util.regex.Pattern.compile("\"id\"\\s*:\\s*\"(cs_[^\"]+)\"");
            java.util.regex.Pattern bookingIdPattern = java.util.regex.Pattern.compile("\"booking_id\"\\s*:\\s*\"([^\"]+)\"");
            java.util.regex.Pattern customerIdPattern = java.util.regex.Pattern.compile("\"customer_id\"\\s*:\\s*\"([^\"]+)\"");
            
            String sessionId = extractWithRegex(jsonData, sessionIdPattern);
            String bookingId = extractWithRegex(jsonData, bookingIdPattern);
            String customerId = extractWithRegex(jsonData, customerIdPattern);
            
            log.info("✅ SOLUTION 4 SUCCESS - Session: {}, Booking: {}, Customer: {}", 
                    sessionId, bookingId, customerId);
            
            if (sessionId != null && bookingId != null && customerId != null) {
                updatePaymentFromWebhookData(sessionId, bookingId, customerId);
                return true;
            }
            
        } catch (Exception e) {
            log.warn("⚠️ SOLUTION 4 failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * SOLUTION 5: Fallback avec Session.retrieve()
     */
    private boolean handleCheckoutSessionCompleted_Solution5(Event event) {
        log.info("🎯 SOLUTION 5: Using Session.retrieve() fallback");
        
        try {
            String sessionId = extractSessionIdFromEventData(event);
            
            if (sessionId != null) {
                Session fullSession = Session.retrieve(sessionId);
                
                if (fullSession != null && fullSession.getMetadata() != null) {
                    String bookingId = fullSession.getMetadata().get("booking_id");
                    String customerId = fullSession.getMetadata().get("customer_id");
                    
                    log.info("✅ SOLUTION 5 SUCCESS - Session: {}, Booking: {}, Customer: {}", 
                            sessionId, bookingId, customerId);
                    
                    if (bookingId != null && customerId != null) {
                        updatePaymentFromWebhookData(sessionId, bookingId, customerId);
                        return true;
                    }
                }
            }
            
        } catch (Exception e) {
            log.warn("⚠️ SOLUTION 5 failed: {}", e.getMessage());
        }
        return false;
    }

    private String extractWithRegex(String jsonData, java.util.regex.Pattern pattern) {
        java.util.regex.Matcher matcher = pattern.matcher(jsonData);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private void handleCheckoutSessionExpired(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session != null) {
            log.info("Checkout session expired: {}", session.getId());
            updatePaymentStatus(session.getId(), PaymentStatus.CANCELLED);
        }
    }

    private void handleAsyncPaymentSucceeded(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session != null) {
            log.info("Async payment succeeded for session: {}", session.getId());
            updatePaymentFromCheckoutSession(session);
        }
    }

    private void handleAsyncPaymentFailed(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session != null) {
            log.info("Async payment failed for session: {}", session.getId());
            updatePaymentStatus(session.getId(), PaymentStatus.FAILED);
        }
    }

    private void updatePaymentFromCheckoutSession(Session session) {
        Optional<Payment> optionalPayment = paymentRepository.findByStripeSessionId(session.getId());
        
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            
            // Mettre à jour avec les informations de la session
            payment.setStripeSessionId(session.getId());
            payment.setStripeChargeId(session.getPaymentIntent()); // PaymentIntent ID from completed session
            
            // Mettre à jour le statut
            if ("paid".equals(session.getPaymentStatus())) {
                payment.setStatus(PaymentStatus.SUCCEEDED);
                payment.setPaidAt(LocalDateTime.now());
            } else if ("unpaid".equals(session.getPaymentStatus())) {
                payment.setStatus(PaymentStatus.FAILED);
            }
            
            paymentRepository.save(payment);
            
            log.info("Payment {} updated from Checkout session - Status: {}", 
                    payment.getId(), payment.getStatus());
        } else {
            log.warn("Payment not found for Checkout session: {}", session.getId());
        }
    }

    private void updatePaymentStatus(String sessionId, PaymentStatus status) {
        Optional<Payment> optionalPayment = paymentRepository.findByStripeSessionId(sessionId);
        
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            payment.setStatus(status);
            
            if (status == PaymentStatus.FAILED || status == PaymentStatus.CANCELLED) {
                payment.setFailureReason("Session " + status.name().toLowerCase());
            }
            
            paymentRepository.save(payment);
            log.info("Payment {} status updated to: {}", payment.getId(), status);
        }
    }

    /**
     * Extrait l'ID de session depuis les données d'événement
     */
    private String extractSessionIdFromEventData(Event event) {
        try {
            // Essayer de parser le JSON pour trouver l'ID
            String jsonData = event.getData().toJson();
            if (jsonData.contains("\"id\":")) {
                // Pattern simple pour extraire l'ID
                String[] parts = jsonData.split("\"id\":");
                if (parts.length > 1) {
                    String idPart = parts[1].split(",")[0].replace("\"", "").trim();
                    if (idPart.startsWith("cs_")) {
                        return idPart;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error extracting session ID from event data: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extrait une métadonnée spécifique depuis les données d'événement
     */
    private String extractMetadataFromEventData(Event event, String metadataKey) {
        try {
            String jsonData = event.getData().toJson();
            log.debug("Searching for metadata '{}' in JSON data", metadataKey);
            
            // Essayer plusieurs patterns pour trouver la clé
            String[] patterns = {
                "\"" + metadataKey + "\":\"",  // Pattern standard
                "\"" + metadataKey + "\": \"", // Avec espace
                metadataKey + "\":\"" // Sans guillemets de début (fallback)
            };
            
            for (String pattern : patterns) {
                int keyStart = jsonData.indexOf(pattern);
                
                if (keyStart != -1) {
                    int valueStart = keyStart + pattern.length();
                    int valueEnd = jsonData.indexOf("\"", valueStart);
                    
                    if (valueEnd != -1) {
                        String value = jsonData.substring(valueStart, valueEnd);
                        log.info("Found metadata {} = {}", metadataKey, value);
                        return value;
                    }
                }
            }
            
            log.warn("Could not find metadata key: {}", metadataKey);
            
        } catch (Exception e) {
            log.error("💥 Error extracting metadata '{}' from event data: {}", metadataKey, e.getMessage());
        }
        return null;
    }

    /**
     * Met à jour le paiement avec les données extraites du webhook
     */
    private void updatePaymentFromWebhookData(String sessionId, String bookingId, String customerId) {
        try {
            log.info("🔍 Searching for payment with sessionId: {}", sessionId);
            
            // Rechercher le paiement par session_id (équivalent stripe_session_id)
            Optional<Payment> paymentOpt = paymentRepository.findByStripeSessionId(sessionId);
            
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                log.info("✅ Found payment with ID: {}", payment.getId());
                
                // Mettre à jour le statut à SUCCEEDED
                payment.setStatus(PaymentStatus.SUCCEEDED);
                payment.setUpdatedAt(LocalDateTime.now());
                
                // Sauvegarder
                paymentRepository.save(payment);
                log.info("✅ Payment status updated to SUCCEEDED for payment ID: {}", payment.getId());
                
                // Mettre à jour le booking associé
                updateBookingStatus(Long.valueOf(bookingId), BookingStatus.CONFIRMED);
                
            } else {
                log.warn("⚠️ No payment found with sessionId: {}", sessionId);
            }
            
        } catch (Exception e) {
            log.error("💥 Error updating payment for sessionId {}: {}", sessionId, e.getMessage());
        }
    }

    /**
     * Met à jour le statut du booking
     */
    private void updateBookingStatus(Long bookingId, BookingStatus status) {
        try {
            // Cette méthode devrait être dans BookingService, mais on fait simple pour le webhook
            log.info("🔄 Updating booking {} status to {}", bookingId, status);
            // TODO: Appeler BookingService pour mettre à jour le statut
            log.info("✅ Booking status updated (placeholder)");
        } catch (Exception e) {
            log.error("💥 Error updating booking status: {}", e.getMessage());
        }
    }

    /**
     * Handler pour payment_intent.succeeded
     * Déclenché quand un PaymentIntent est complété avec succès
     */
    private void handlePaymentIntentSucceeded(Event event) {
        try {
            log.info("💳 Processing payment_intent.succeeded event");
            
            // Extraire le PaymentIntent de l'événement
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            PaymentIntent paymentIntent = null;
            
            if (dataObjectDeserializer.getObject().isPresent()) {
                paymentIntent = (PaymentIntent) dataObjectDeserializer.getObject().get();
            } else {
                // Fallback avec deserializeUnsafe
                paymentIntent = (PaymentIntent) dataObjectDeserializer.deserializeUnsafe();
            }
            
            if (paymentIntent != null) {
                log.info("✅ PaymentIntent succeeded: {}", paymentIntent.getId());
                log.info("   - Amount: {} {}", paymentIntent.getAmount() / 100.0, paymentIntent.getCurrency());
                log.info("   - Customer: {}", paymentIntent.getCustomer());
                log.info("   - Payment Method: {}", paymentIntent.getPaymentMethod());
                
                // Extraire les métadonnées
                String bookingId = null;
                String customerId = null;
                if (paymentIntent.getMetadata() != null) {
                    bookingId = paymentIntent.getMetadata().get("booking_id");
                    customerId = paymentIntent.getMetadata().get("customer_id");
                    log.info("   - Booking ID: {}", bookingId);
                    log.info("   - Customer ID: {}", customerId);
                }
                
                // Rechercher le paiement par PaymentIntent ID
                Optional<Payment> paymentOpt = paymentRepository.findByStripePaymentIntentId(paymentIntent.getId());
                
                if (paymentOpt.isPresent()) {
                    Payment payment = paymentOpt.get();
                    payment.setStatus(PaymentStatus.SUCCEEDED);
                    payment.setStripeChargeId(paymentIntent.getLatestCharge());
                    payment.setPaidAt(LocalDateTime.now());
                    payment.setUpdatedAt(LocalDateTime.now());
                    
                    paymentRepository.save(payment);
                    log.info("✅ Payment {} updated from PaymentIntent", payment.getId());
                    
                    // Mettre à jour le booking si nécessaire
                    if (bookingId != null) {
                        updateBookingStatus(Long.valueOf(bookingId), BookingStatus.CONFIRMED);
                    }
                } else {
                    log.warn("⚠️ Payment not found for PaymentIntent: {}", paymentIntent.getId());
                }
            }
        } catch (Exception e) {
            log.error("💥 Error handling payment_intent.succeeded: {}", e.getMessage(), e);
        }
    }

    /**
     * Handler pour payment_intent.payment_failed
     * Déclenché quand un PaymentIntent échoue
     */
    private void handlePaymentIntentFailed(Event event) {
        try {
            log.info("❌ Processing payment_intent.payment_failed event");
            
            // Extraire le PaymentIntent de l'événement
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            PaymentIntent paymentIntent = null;
            
            if (dataObjectDeserializer.getObject().isPresent()) {
                paymentIntent = (PaymentIntent) dataObjectDeserializer.getObject().get();
            } else {
                // Fallback avec deserializeUnsafe
                paymentIntent = (PaymentIntent) dataObjectDeserializer.deserializeUnsafe();
            }
            
            if (paymentIntent != null) {
                log.error("❌ PaymentIntent failed: {}", paymentIntent.getId());
                
                // Extraire les détails de l'erreur
                String errorMessage = null;
                String errorCode = null;
                if (paymentIntent.getLastPaymentError() != null) {
                    StripeError error = paymentIntent.getLastPaymentError();
                    errorMessage = error.getMessage();
                    errorCode = error.getCode();
                    log.error("   - Error: {} ({})", errorMessage, errorCode);
                }
                
                // Rechercher le paiement
                Optional<Payment> paymentOpt = paymentRepository.findByStripePaymentIntentId(paymentIntent.getId());
                
                if (paymentOpt.isPresent()) {
                    Payment payment = paymentOpt.get();
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setFailureReason(errorMessage != null ? errorMessage : "Payment failed");
                    payment.setUpdatedAt(LocalDateTime.now());
                    
                    paymentRepository.save(payment);
                    log.info("✅ Payment {} marked as failed", payment.getId());
                    
                    // Mettre à jour le booking
                    if (payment.getBookingId() != null) {
                        updateBookingStatus(payment.getBookingId(), BookingStatus.CANCELLED);
                    }
                } else {
                    log.warn("⚠️ Payment not found for PaymentIntent: {}", paymentIntent.getId());
                }
            }
        } catch (Exception e) {
            log.error("💥 Error handling payment_intent.payment_failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Handler pour charge.succeeded
     * Déclenché quand un paiement (charge) est réussi
     */
    private void handleChargeSucceeded(Event event) {
        try {
            log.info("💰 Processing charge.succeeded event");
            
            // Extraire le Charge de l'événement
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            Charge charge = null;
            
            if (dataObjectDeserializer.getObject().isPresent()) {
                charge = (Charge) dataObjectDeserializer.getObject().get();
            } else {
                // Fallback avec deserializeUnsafe
                charge = (Charge) dataObjectDeserializer.deserializeUnsafe();
            }
            
            if (charge != null) {
                log.info("✅ Charge succeeded: {}", charge.getId());
                log.info("   - Amount: {} {}", charge.getAmount() / 100.0, charge.getCurrency());
                log.info("   - PaymentIntent: {}", charge.getPaymentIntent());
                log.info("   - Paid: {}", charge.getPaid());
                log.info("   - Receipt URL: {}", charge.getReceiptUrl());
                
                // Si nous avons un PaymentIntent ID, mettre à jour le paiement
                if (charge.getPaymentIntent() != null) {
                    Optional<Payment> paymentOpt = paymentRepository.findByStripePaymentIntentId(charge.getPaymentIntent());
                    
                    if (paymentOpt.isPresent()) {
                        Payment payment = paymentOpt.get();
                        
                        // Mettre à jour avec les infos du charge
                        payment.setStripeChargeId(charge.getId());
                        
                        // Si le statut n'est pas déjà SUCCEEDED, le mettre à jour
                        if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
                            payment.setStatus(PaymentStatus.SUCCEEDED);
                            payment.setPaidAt(LocalDateTime.now());
                        }
                        
                        payment.setUpdatedAt(LocalDateTime.now());
                        paymentRepository.save(payment);
                        
                        log.info("✅ Payment {} updated from Charge", payment.getId());
                    } else {
                        log.warn("⚠️ Payment not found for PaymentIntent: {}", charge.getPaymentIntent());
                    }
                }
                
                // Extraire les métadonnées si disponibles
                if (charge.getMetadata() != null) {
                    String bookingId = charge.getMetadata().get("booking_id");
                    if (bookingId != null) {
                        log.info("   - Booking ID from metadata: {}", bookingId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("💥 Error handling charge.succeeded: {}", e.getMessage(), e);
        }
    }
}