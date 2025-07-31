package com.jb.afrostyle.payment.validation;

import com.jb.afrostyle.booking.domain.entity.Booking;
import com.jb.afrostyle.user.domain.entity.User;
import com.jb.afrostyle.payment.dto.PaymentRequest;
import com.jb.afrostyle.core.validation.ValidationResult;

/**
 * Records et patterns pour la validation des paiements
 * Utilise Java 21 Pattern Matching pour une approche moderne
 */
public class PaymentValidation {
    
    /**
     * Record pour encapsuler les données validées d'un paiement
     */
    public record ValidatedPaymentData(
        User user,
        Booking booking,
        PaymentRequest request
    ) {}
    
    /**
     * États de validation pour Pattern Matching
     */
    public sealed interface PaymentValidationState 
        permits PaymentValidationState.Valid, 
                PaymentValidationState.InvalidUser,
                PaymentValidationState.InvalidBooking,
                PaymentValidationState.UnauthorizedBooking,
                PaymentValidationState.AlreadyPaid,
                PaymentValidationState.InvalidRequest {
        
        record Valid(ValidatedPaymentData data) implements PaymentValidationState {}
        record InvalidUser(String message) implements PaymentValidationState {}
        record InvalidBooking(String message) implements PaymentValidationState {}
        record UnauthorizedBooking(Long customerId, Long bookingCustomerId) implements PaymentValidationState {}
        record AlreadyPaid(Long bookingId) implements PaymentValidationState {}
        record InvalidRequest(String message) implements PaymentValidationState {}
    }
    
    /**
     * Convertit un état de validation en ValidationResult
     * JAVA 21 PATTERN MATCHING
     */
    public static ValidationResult<ValidatedPaymentData> toValidationResult(PaymentValidationState state) {
        return switch (state) {
            case PaymentValidationState.Valid(var data) -> 
                ValidationResult.success(data);
                
            case PaymentValidationState.InvalidUser(var message) -> 
                ValidationResult.error("Invalid user: " + message);
                
            case PaymentValidationState.InvalidBooking(var message) -> 
                ValidationResult.error("Invalid booking: " + message);
                
            case PaymentValidationState.UnauthorizedBooking(var customerId, var bookingCustomerId) -> 
                ValidationResult.error(String.format(
                    "Unauthorized: Customer %d cannot pay for booking owned by customer %d", 
                    customerId, bookingCustomerId));
                    
            case PaymentValidationState.AlreadyPaid(var bookingId) -> 
                ValidationResult.error("Booking " + bookingId + " has already been paid");
                
            case PaymentValidationState.InvalidRequest(var message) -> 
                ValidationResult.error("Invalid payment request: " + message);
        };
    }
    
    /**
     * Valide une requête de paiement avec Pattern Matching
     */
    public static PaymentValidationState validatePaymentRequest(
            PaymentRequest request, Long customerId, User user, Booking booking, boolean alreadyPaid) {
        
        return switch (validateBasicRequest(request, customerId)) {
            case PaymentValidationState.InvalidRequest invalid -> invalid;
            
            case PaymentValidationState.Valid(_) -> switch (validateUserAndBooking(user, booking, customerId, alreadyPaid)) {
                case PaymentValidationState.InvalidUser invalidUser -> invalidUser;
                case PaymentValidationState.InvalidBooking invalidBooking -> invalidBooking;
                case PaymentValidationState.UnauthorizedBooking unauthorized -> unauthorized;
                case PaymentValidationState.AlreadyPaid alreadyPaid_ -> alreadyPaid_;
                case PaymentValidationState.Valid(_) -> 
                    new PaymentValidationState.Valid(new ValidatedPaymentData(user, booking, request));
                default -> new PaymentValidationState.InvalidRequest("Unexpected validation state");
            };
            
            default -> new PaymentValidationState.InvalidRequest("Unexpected validation state");
        };
    }
    
    /**
     * Validation basique de la requête
     */
    private static PaymentValidationState validateBasicRequest(PaymentRequest request, Long customerId) {
        return switch (request) {
            case null -> new PaymentValidationState.InvalidRequest("Payment request cannot be null");
            case PaymentRequest req when req.bookingId() == null -> 
                new PaymentValidationState.InvalidRequest("Booking ID is required");
            case PaymentRequest req when req.amount() == null || req.amount().doubleValue() <= 0 -> 
                new PaymentValidationState.InvalidRequest("Valid amount is required");  
            case PaymentRequest req when req.paymentMethod() == null -> 
                new PaymentValidationState.InvalidRequest("Payment method is required");
            case PaymentRequest req when customerId == null -> 
                new PaymentValidationState.InvalidRequest("Customer ID is required");
            default -> new PaymentValidationState.Valid(null); // Sera replacé plus tard
        };
    }
    
    /**
     * Validation de l'utilisateur et de la réservation
     */
    private static PaymentValidationState validateUserAndBooking(
            User user, Booking booking, Long customerId, boolean alreadyPaid) {
        
        return switch (user) {
            case null -> new PaymentValidationState.InvalidUser("User not found with ID: " + customerId);
            case User u -> switch (booking) {
                case null -> new PaymentValidationState.InvalidBooking("Booking not found");
                case Booking b when !b.getCustomerId().equals(customerId) -> 
                    new PaymentValidationState.UnauthorizedBooking(customerId, b.getCustomerId());
                case Booking b when alreadyPaid -> 
                    new PaymentValidationState.AlreadyPaid(b.getId());
                case Booking b -> new PaymentValidationState.Valid(null); // Sera replacé plus tard
            };
        };
    }
}