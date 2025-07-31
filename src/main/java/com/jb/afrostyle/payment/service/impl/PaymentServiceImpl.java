package com.jb.afrostyle.payment.service.impl;

import com.jb.afrostyle.booking.service.BookingService;
import com.jb.afrostyle.booking.domain.entity.Booking;
import com.jb.afrostyle.user.service.UserService;
import com.jb.afrostyle.user.domain.entity.User;
import com.jb.afrostyle.payment.domain.enums.PaymentMethod;
import com.jb.afrostyle.payment.domain.enums.PaymentStatus;
import com.jb.afrostyle.payment.domain.entity.Payment;
import com.jb.afrostyle.payment.dto.PaymentRequest;
import com.jb.afrostyle.payment.dto.PaymentResponse;
import com.jb.afrostyle.payment.dto.RefundRequest;
import com.jb.afrostyle.payment.repository.PaymentRepository;
import com.jb.afrostyle.payment.service.PaymentService;
import com.jb.afrostyle.payment.service.PaymentStats;
import com.jb.afrostyle.integrations.stripe.service.StripeCheckoutService;
import com.jb.afrostyle.service.email.EmailService;
import com.jb.afrostyle.config.FrontendUrlProperties;
import com.jb.afrostyle.core.validation.ValidationUtils;
import com.jb.afrostyle.core.validation.ValidationResult;
import com.jb.afrostyle.core.enums.EntityType;
import com.jb.afrostyle.core.util.IDGenerator;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.jb.afrostyle.core.util.IDGenerator;
import com.jb.afrostyle.core.enums.EntityType;
import com.jb.afrostyle.payment.validation.PaymentValidation;
import com.jb.afrostyle.core.validation.ValidationResult;
import com.jb.afrostyle.core.validation.ValidationUtils;
import com.jb.afrostyle.core.exception.ExceptionUtils;


@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final StripeCheckoutService stripeCheckoutService;
    private final BookingService bookingService;
    private final UserService userService;
    private final FrontendUrlProperties frontendUrlProperties;
    private final IDGenerator idGenerator;
    private final EmailService emailService;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request, Long customerId) throws Exception {
        log.info("Creating payment for customer {} and booking {}", customerId, request.bookingId());

        // JAVA 21 PATTERN MATCHING - Validation moderne avec ValidationResult
        var validationResult = validatePaymentCreation(request, customerId);
        
        return switch (validationResult) {
            case ValidationResult.Success(var validatedData) -> 
                processValidPayment(validatedData);
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> 
                throw new Exception("Payment validation failed: " + message, cause);
        };
    }
    
    /**
     * Valide la création d'un paiement avec Pattern Matching moderne
     */
    private ValidationResult<PaymentValidation.ValidatedPaymentData> validatePaymentCreation(
            PaymentRequest request, Long customerId) {
        
        try {
            // Récupération des données avec gestion d'erreurs
            User user = null;
            Booking booking = null;
            boolean alreadyPaid = false;
            
            try {
                user = userService.getUserById(customerId);
            } catch (Exception e) {
                return ValidationResult.error("User not found: " + e.getMessage(), e);
            }
            
            try {
                booking = bookingService.getBookingById(request.bookingId());
                alreadyPaid = paymentRepository.existsByBookingIdAndStatus(
                    request.bookingId(), PaymentStatus.SUCCEEDED);
            } catch (Exception e) {
                return ValidationResult.error("Booking validation failed: " + e.getMessage(), e);
            }
            
            // Validation avec Pattern Matching
            var state = PaymentValidation.validatePaymentRequest(request, customerId, user, booking, alreadyPaid);
            return PaymentValidation.toValidationResult(state);
            
        } catch (Exception e) {
            log.error("Unexpected error during payment validation", e);
            return ValidationResult.error("Payment validation failed", e);
        }
    }
    
    /**
     * Traite un paiement validé avec Pattern Matching pour les méthodes de paiement
     */
    private PaymentResponse processValidPayment(PaymentValidation.ValidatedPaymentData validatedData) throws Exception {
        var request = validatedData.request();
        var booking = validatedData.booking();
        var user = validatedData.user();
        
        // ÉTAPE 1 : Créer le paiement en base
        Payment payment = new Payment(
                booking.getId(),
                user.getId(),
                request.amount(),
                request.paymentMethod(),
                request.description()
        );
        payment.setCurrency(request.currency());
        payment.setTransactionId(idGenerator.generateTransactionId());

        payment = paymentRepository.save(payment);

        // JAVA 21 PATTERN MATCHING - Traitement des méthodes de paiement
        return switch (request.paymentMethod()) {
            case PaymentMethod.CASH -> {
                log.info("Processing cash payment for booking {}", booking.getId());
                payment.setStatus(PaymentStatus.PENDING);
                payment = paymentRepository.save(payment);
                yield PaymentResponse.success(
                    payment.getId(), 
                    payment.getTransactionId(), 
                    null, 
                    payment.getAmount(), 
                    payment.getCurrency()
                );
            }
            
            case PaymentMethod.CARD -> {
                log.info("Processing card payment via Stripe for booking {}", booking.getId());
                try {
                    String checkoutSessionId = stripeCheckoutService.createCheckoutSession(
                            request.amount(),
                            request.bookingId(),
                            user.getId(),
                            request.description(),
                            frontendUrlProperties.getPayment().getSuccessUrl(),
                            frontendUrlProperties.getPayment().getCancelUrl()
                    ).getId();

                    payment.setStripeSessionId(checkoutSessionId);
                    payment = paymentRepository.save(payment);

                    yield PaymentResponse.success(
                            payment.getId(),
                            payment.getTransactionId(),
                            checkoutSessionId,
                            payment.getAmount(),
                            payment.getCurrency()
                    );
                } catch (StripeException e) {
                    log.error("Stripe error while creating payment: {}", e.getMessage());
                    throw new Exception("Stripe payment creation failed: " + e.getMessage(), e);
                }
            }
            
            case null -> throw new Exception("Payment method cannot be null");
            
            default -> throw new Exception("Unsupported payment method: " + request.paymentMethod());
        };
    }

    @Override
    @Transactional
    public Payment confirmPayment(String sessionId) throws Exception {
        log.info("Confirming payment for Checkout Session: {}", sessionId);

        try {
            // Récupérer le paiement depuis la base
            Payment payment = paymentRepository.findByStripeSessionId(sessionId)
                    .orElseThrow(() -> new Exception("Payment not found for Session: " + sessionId));

            // Vérifier le statut de la session via Stripe
            var session = stripeCheckoutService.retrieveSession(sessionId);
            
            if ("complete".equals(session.getStatus()) && "paid".equals(session.getPaymentStatus())) {
                payment.setStatus(PaymentStatus.SUCCEEDED);
                payment.setPaidAt(LocalDateTime.now());
                payment.setStripeChargeId(session.getPaymentIntent()); // L'ID du PaymentIntent créé automatiquement
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Payment not completed or not paid");
            }

            payment = paymentRepository.save(payment);

            log.info("Payment confirmed successfully: {}", payment.getId());
            
            // Envoyer l'email de confirmation de paiement si le paiement a réussi
            if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
                sendPaymentConfirmationEmail(payment);
            }
            
            return payment;

        } catch (StripeException e) {
            log.error("Stripe error while confirming payment: {}", e.getMessage());
            throw new Exception("Payment confirmation failed: " + e.getMessage());
        }
    }

    @Override
    public void processStripeWebhook(String payload, String signature) throws Exception {
        try {
            log.info("Processing Stripe webhook...");
            // Les webhooks sont maintenant gérés par StripeCheckoutService
            // via StripeCheckoutController.handleCheckoutWebhook()
            log.info("Webhooks are handled by StripeCheckoutController");
        } catch (Exception e) {
            log.error("Error processing Stripe webhook: {}", e.getMessage());
            throw new Exception("Webhook processing failed: " + e.getMessage());
        }
    }

    @Override
    public Payment getPaymentById(Long paymentId) throws Exception {
        // PATTERN MIGRÉ : Validation avec ValidationUtils
        var validationResult = ValidationUtils.validatePositiveId(paymentId, EntityType.PAYMENT);
        return switch (validationResult) {
            case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> 
                throw new Exception(message, cause);
            case ValidationResult.Success(var validId) -> 
                paymentRepository.findById(validId)
                    .orElseThrow(() -> ExceptionUtils.createNotFoundException(
                        EntityType.PAYMENT, validId
                    ));
        };
    }

    @Override
    public List<Payment> getPaymentsByCustomer(Long customerId) {
        return paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<Payment> getPaymentsByBooking(Long bookingId) {
        return paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
    }

    @Override
    @Transactional
    public Payment refundPayment(Long paymentId, RefundRequest refundRequest) throws Exception {
        log.info("Processing refund for payment: {}", paymentId);

        try {
            Payment payment = getPaymentById(paymentId);

            // Vérifier que le paiement peut être remboursé
            if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
                throw new Exception("Only successful payments can be refunded");
            }

            if (payment.getStripeChargeId() == null) {
                throw new Exception("Cannot refund payment without Stripe Charge ID");
            }

            // Déterminer le montant du remboursement
            BigDecimal refundAmount = refundRequest.amount();
            if (refundAmount == null || refundAmount.compareTo(payment.getAmount()) >= 0) {
                refundAmount = payment.getAmount(); // Remboursement total
            }

            // Effectuer le remboursement via Stripe Refund API
            // Note: Avec Checkout Sessions, les remboursements se font via l'API Refund standard
            var refund = com.stripe.model.Refund.create(
                    com.stripe.param.RefundCreateParams.builder()
                            .setCharge(payment.getStripeChargeId())
                            .setAmount(refundAmount.multiply(BigDecimal.valueOf(100)).longValue()) // Convert to cents
                            .setReason(com.stripe.param.RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                            .build()
            );

            // Mettre à jour le paiement
            payment.setRefundAmount(refundAmount);
            payment.setRefundReason(refundRequest.reason());
            payment.setRefundedAt(LocalDateTime.now());
            payment.setStatus(PaymentStatus.REFUNDED);

            payment = paymentRepository.save(payment);

            log.info("Payment refunded successfully: {} - Amount: {}", paymentId, refundAmount);
            return payment;

        } catch (StripeException e) {
            log.error("Stripe error while refunding payment: {}", e.getMessage());
            throw new Exception("Refund failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Payment cancelPayment(Long paymentId) throws Exception {
        log.info("Cancelling payment: {}", paymentId);

        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            throw new Exception("Cannot cancel a successful payment. Use refund instead.");
        }

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new Exception("Payment is already cancelled");
        }

        try {
            // Avec Checkout Sessions, pas besoin d'annuler côté Stripe
            // Les sessions expirent automatiquement après 24h
            // On marque simplement le paiement comme annulé en base
            
            payment.setStatus(PaymentStatus.CANCELLED);
            payment = paymentRepository.save(payment);

            log.info("Payment cancelled successfully: {}", paymentId);
            return payment;

        } catch (Exception e) {
            log.error("Error while cancelling payment: {}", e.getMessage());
            throw new Exception("Payment cancellation failed: " + e.getMessage());
        }
    }

    @Override
    public BigDecimal getTotalEarnings() {
        return paymentRepository.getTotalEarnings();
    }

    @Override
    public BigDecimal getTotalRefunds() {
        return paymentRepository.getTotalRefunds();
    }

    @Override
    public PaymentStats getPaymentStats(LocalDateTime startDate, LocalDateTime endDate) {
        // Récupérer les paiements dans la période
        List<Payment> payments = paymentRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(
                startDate, endDate);

        // Calculer les statistiques
        return PaymentStats.fromPayments(payments);
    }

    
    /**
     * Envoie un email de confirmation de paiement au client.
     * 
     * Cette méthode récupère les informations nécessaires (utilisateur, réservation)
     * et envoie un email de confirmation contenant le reçu de paiement électronique.
     * 
     * L'email est envoyé de manière asynchrone et les erreurs sont loggées
     * sans faire échouer le processus de paiement.
     * 
     * @param payment Le paiement confirmé
     */
    private void sendPaymentConfirmationEmail(Payment payment) {
        try {
            log.info("📧 Sending payment confirmation email for payment {}", payment.getId());
            
            // Récupérer les informations utilisateur
            User user = userService.getUserById(payment.getCustomerId());
            
            // Récupérer les informations de réservation
            Booking booking = bookingService.getBookingById(payment.getBookingId());
            
            // Formater les détails du service pour l'email
            String serviceDetails = formatServiceDetailsForEmail(booking);
            
            // Envoyer l'email de confirmation (asynchrone)
            emailService.sendPaymentConfirmationEmail(
                user.getEmail(),
                user.getFullName(),
                payment.getAmount().toString(),
                payment.getTransactionId(),
                serviceDetails
            );
            
            log.info("✅ Payment confirmation email queued for user {}", user.getEmail());
            
        } catch (Exception e) {
            // L'email est un service non-critique : on log l'erreur mais on ne fait pas échouer le paiement
            log.error("❌ Failed to send payment confirmation email for payment {}: {}", payment.getId(), e.getMessage());
        }
    }
    
    /**
     * Formate les détails des services pour l'affichage dans l'email de confirmation de paiement.
     * 
     * Crée un résumé des services payés basé sur les informations de la réservation.
     * 
     * @param booking La réservation associée au paiement
     * @return HTML formaté des détails des services
     */
    private String formatServiceDetailsForEmail(Booking booking) {
        StringBuilder details = new StringBuilder();
        
        details.append("<p><strong>Réservation :</strong> #").append(booking.getId()).append("</p>");
        details.append("<p><strong>Date du rendez-vous :</strong> ").append(booking.getStartTime().toLocalDate()).append("</p>");
        details.append("<p><strong>Horaires :</strong> ").append(booking.getStartTime().toLocalTime())
               .append(" - ").append(booking.getEndTime().toLocalTime()).append("</p>");
        details.append("<p><strong>Nombre de services :</strong> ").append(booking.getTotalServices()).append("</p>");
        
        return details.toString();
    }
}