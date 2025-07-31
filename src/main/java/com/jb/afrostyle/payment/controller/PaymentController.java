package com.jb.afrostyle.payment.controller;

import com.jb.afrostyle.payment.domain.entity.Payment;
import com.jb.afrostyle.payment.dto.PaymentDTO;
import com.jb.afrostyle.payment.dto.PaymentRequest;
import com.jb.afrostyle.payment.dto.PaymentResponse;
import com.jb.afrostyle.payment.dto.RefundRequest;
import com.jb.afrostyle.payment.service.PaymentService;
import com.jb.afrostyle.payment.service.PaymentStats;
import com.jb.afrostyle.payment.mapper.PaymentMapper;
import com.jb.afrostyle.core.validation.ValidationUtils;
import com.jb.afrostyle.core.exception.ExceptionUtils;
import com.jb.afrostyle.core.response.ResponseFactory;
import com.jb.afrostyle.core.validation.ValidationResult;
import com.jb.afrostyle.core.security.AuthenticationUtil;
import com.jb.afrostyle.core.enums.EntityType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Contrôleur principal pour les paiements
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor

// CORS géré globalement dans SecurityConfig
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    private final AuthenticationUtil authenticationUtil;

    /**
     * Créer un nouveau paiement
     */
    @PostMapping
    public ResponseEntity<?> createPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            // Extraction de l'ID utilisateur avec AuthenticationUtil
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long customerId = authenticationUtil.extractUserIdFromAuth(auth);
            
            if (customerId == null) {
                return ResponseFactory.unauthorized("Authentication required to create payment");
            }
            
            PaymentResponse response = paymentService.createPayment(request, customerId);
            return ResponseFactory.success(response);
            
        } catch (Exception e) {
            log.error("Unexpected error creating payment: {}", e.getMessage());
            return ResponseFactory.errorFromException(e);
        }
    }


    /**
     * Confirmer un paiement (callback Stripe)
     */
    @PostMapping("/confirm/{sessionId}")
    public ResponseEntity<?> confirmPayment(@PathVariable String sessionId) {
        try {
            // PATTERN MIGRÉ : Validation avec ValidationUtils
            var validationResult = ValidationUtils.validateNotNullOrEmpty(sessionId, "Session ID");
            return switch (validationResult) {
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> 
                    ResponseFactory.badRequest(message);
                case ValidationResult.Success(var validSessionId) -> {
                    try {
                        Payment payment = paymentService.confirmPayment(validSessionId);
                        PaymentDTO dto = PaymentMapper.INSTANCE.toDTO(payment);
                        yield ResponseFactory.success(dto);
                    } catch (Exception e) {
                        log.error("Error confirming payment: {}", e.getMessage());
                        yield ResponseFactory.errorFromException(e);
                    }
                }
            };
        } catch (Exception e) {
            log.error("Unexpected error confirming payment: {}", e.getMessage());
            return ResponseFactory.errorFromException(e);
        }
    }

    /**
     * Obtenir un paiement par ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPaymentById(@PathVariable Long paymentId) {
        try {
            // PATTERN MIGRÉ : Validation avec ValidationUtils
            var validationResult = ValidationUtils.validatePositiveId(paymentId, EntityType.PAYMENT);
            return switch (validationResult) {
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> 
                    ResponseFactory.badRequest(message);
                case ValidationResult.Success(var validId) -> {
                    try {
                        Payment payment = paymentService.getPaymentById(validId);
                        PaymentDTO dto = PaymentMapper.INSTANCE.toDTO(payment);
                        yield ResponseFactory.success(dto);
                    } catch (Exception e) {
                        log.error("Error getting payment: {}", e.getMessage());
                        yield ResponseFactory.notFound("Payment not found with ID: " + validId);
                    }
                }
            };
        } catch (Exception e) {
            log.error("Unexpected error getting payment: {}", e.getMessage());
            return ResponseFactory.errorFromException(e);
        }
    }

    /**
     * Obtenir les paiements d'un client
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getPaymentsByCustomer(@PathVariable Long customerId) {
        try {
            // PATTERN MIGRÉ : Validation avec ValidationUtils
            var validationResult = ValidationUtils.validatePositiveId(customerId, EntityType.USER);
            return switch (validationResult) {
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> 
                    ResponseFactory.badRequest(message);
                case ValidationResult.Success(var validId) -> {
                    List<Payment> payments = paymentService.getPaymentsByCustomer(validId);
                    List<PaymentDTO> dtos = payments.stream()
                            .map(PaymentMapper.INSTANCE::toDTO)
                            .collect(Collectors.toList());
                    yield ResponseFactory.success(dtos);
                }
            };
        } catch (Exception e) {
            log.error("Error getting payments for customer: {}", e.getMessage());
            return ResponseFactory.errorFromException(e);
        }
    }

    /**
     * Obtenir tous les paiements du salon
     * MONO-SALON : Tous les paiements appartiennent au salon unique
     */
    @GetMapping("/salon")
    public ResponseEntity<?> getAllSalonPayments() {
        try {
            List<Payment> payments = paymentService.getAllPayments();
            List<PaymentDTO> dtos = payments.stream()
                    .map(PaymentMapper.INSTANCE::toDTO)
                    .collect(Collectors.toList());
            return ResponseFactory.success(dtos);
        } catch (Exception e) {
            log.error("Error getting all salon payments: {}", e.getMessage());
            return ResponseFactory.errorFromException(e);
        }
    }

    /**
     * Obtenir les paiements d'une réservation
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getPaymentsByBooking(@PathVariable Long bookingId) {
        try {
            // PATTERN MIGRÉ : Validation avec ValidationUtils
            var validationResult = ValidationUtils.validatePositiveId(bookingId, EntityType.BOOKING);
            return switch (validationResult) {
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> 
                    ResponseFactory.badRequest(message);
                case ValidationResult.Success(var validId) -> {
                    List<Payment> payments = paymentService.getPaymentsByBooking(validId);
                    List<PaymentDTO> dtos = payments.stream()
                            .map(PaymentMapper.INSTANCE::toDTO)
                            .collect(Collectors.toList());
                    yield ResponseFactory.success(dtos);
                }
            };
        } catch (Exception e) {
            log.error("Error getting payments for booking: {}", e.getMessage());
            return ResponseFactory.errorFromException(e);
        }
    }

    /**
     * Rembourser un paiement
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<?> refundPayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody RefundRequest refundRequest) {
        try {
            // PATTERN MIGRÉ : Validation avec ValidationUtils
            var validationResult = ValidationUtils.validatePositiveId(paymentId, EntityType.PAYMENT);
            return switch (validationResult) {
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> 
                    ResponseFactory.badRequest(message);
                case ValidationResult.Success(var validId) -> {
                    try {
                        Payment payment = paymentService.refundPayment(validId, refundRequest);
                        PaymentDTO dto = PaymentMapper.INSTANCE.toDTO(payment);
                        yield ResponseFactory.paymentResponse(ResponseFactory.PaymentResult.REFUND_PROCESSED, validId);
                    } catch (Exception e) {
                        log.error("Error refunding payment: {}", e.getMessage());
                        yield ResponseFactory.errorFromException(e);
                    }
                }
            };
        } catch (Exception e) {
            log.error("Unexpected error refunding payment: {}", e.getMessage());
            return ResponseFactory.errorFromException(e);
        }
    }

    /**
     * Annuler un paiement
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<?> cancelPayment(@PathVariable Long paymentId) {
        try {
            // PATTERN MIGRÉ : Validation avec ValidationUtils
            var validationResult = ValidationUtils.validatePositiveId(paymentId, EntityType.PAYMENT);
            return switch (validationResult) {
                case ValidationResult.Error(var message, var cause, ignored1, ignored2, ignored3) -> 
                    ResponseFactory.badRequest(message);
                case ValidationResult.Success(var validId) -> {
                    try {
                        Payment payment = paymentService.cancelPayment(validId);
                        PaymentDTO dto = PaymentMapper.INSTANCE.toDTO(payment);
                        yield ResponseFactory.success(dto);
                    } catch (Exception e) {
                        log.error("Error cancelling payment: {}", e.getMessage());
                        yield ResponseFactory.errorFromException(e);
                    }
                }
            };
        } catch (Exception e) {
            log.error("Unexpected error cancelling payment: {}", e.getMessage());
            return ResponseFactory.errorFromException(e);
        }
    }

    /**
     * Obtenir les statistiques de paiement du salon
     * MONO-SALON : Toutes les statistiques concernent le salon unique
     */
    @GetMapping("/salon/stats")
    public ResponseEntity<?> getSalonPaymentStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : LocalDateTime.now().minusMonths(1);
            LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : LocalDateTime.now();
            
            PaymentStats stats = paymentService.getPaymentStats(start, end);
            return ResponseFactory.success(stats);
        } catch (Exception e) {
            log.error("Error getting payment stats: {}", e.getMessage());
            return ResponseFactory.errorFromException(e);
        }
    }


}