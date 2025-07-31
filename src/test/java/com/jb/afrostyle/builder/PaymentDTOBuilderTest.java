package com.jb.afrostyle.builder;

import com.jb.afrostyle.payment.dto.PaymentDTO;
import com.jb.afrostyle.payment.domain.enums.PaymentMethod;
import com.jb.afrostyle.payment.domain.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour PaymentDTO Builder Pattern Context7
 * Valide la construction fluide et la validation métier
 */
@DisplayName("PaymentDTO Builder Pattern Tests")
class PaymentDTOBuilderTest {

    @Test
    @DisplayName("Should create valid PaymentDTO with all required fields")
    void shouldCreateValidPaymentDTO() {
        // Given
        Long bookingId = 123L;
        Long customerId = 456L;
        BigDecimal amount = new BigDecimal("25.00");
        
        // When
        PaymentDTO payment = PaymentDTO.builder()
            .bookingId(bookingId)
            .customerId(customerId)
            .amount(amount)
            .paymentMethod(PaymentMethod.CARD)
            .description("Payment for Hair Braiding service")
            .build();
        
        // Then
        assertNotNull(payment);
        assertEquals(bookingId, payment.bookingId());
        assertEquals(customerId, payment.customerId());
        assertEquals(amount, payment.amount());
        assertEquals(PaymentMethod.CARD, payment.paymentMethod());
        assertEquals(PaymentStatus.PENDING, payment.status()); // Default value
        assertEquals("EUR", payment.currency()); // Default value
        assertNotNull(payment.transactionId()); // Auto-generated
        assertNotNull(payment.createdAt()); // Auto-generated
        assertTrue(payment.transactionId().startsWith("TXN_"));
    }

    @Test
    @DisplayName("Should auto-generate transaction ID with correct format")
    void shouldAutoGenerateTransactionId() {
        // When
        PaymentDTO payment = PaymentDTO.builder()
            .bookingId(123L)
            .customerId(456L)
            .amount(new BigDecimal("25.00"))
            .paymentMethod(PaymentMethod.CARD)
            .build();
        
        // Then
        String transactionId = payment.transactionId();
        assertNotNull(transactionId);
        assertTrue(transactionId.matches("TXN_\\d+_[A-F0-9]+"));
    }

    @Test
    @DisplayName("Should throw exception when booking ID is missing")
    void shouldThrowExceptionWhenBookingIdMissing() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            PaymentDTO.builder()
                .customerId(456L)
                .amount(new BigDecimal("25.00"))
                .paymentMethod(PaymentMethod.CARD)
                .build()
        );
        
        assertEquals("Booking ID is required for payment creation", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when customer ID is missing")
    void shouldThrowExceptionWhenCustomerIdMissing() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            PaymentDTO.builder()
                .bookingId(123L)
                .amount(new BigDecimal("25.00"))
                .paymentMethod(PaymentMethod.CARD)
                .build()
        );
        
        assertEquals("Customer ID is required for payment creation", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when amount is missing")
    void shouldThrowExceptionWhenAmountMissing() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            PaymentDTO.builder()
                .bookingId(123L)
                .customerId(456L)
                .paymentMethod(PaymentMethod.CARD)
                .build()
        );
        
        assertEquals("Payment amount is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when payment method is missing")
    void shouldThrowExceptionWhenPaymentMethodMissing() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            PaymentDTO.builder()
                .bookingId(123L)
                .customerId(456L)
                .amount(new BigDecimal("25.00"))
                .build()
        );
        
        assertEquals("Payment method is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when amount is negative")
    void shouldThrowExceptionWhenAmountNegative() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PaymentDTO.builder()
                .bookingId(123L)
                .customerId(456L)
                .amount(new BigDecimal("-5.00"))
                .paymentMethod(PaymentMethod.CARD)
                .build()
        );
        
        assertEquals("Payment amount cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when amount exceeds maximum")
    void shouldThrowExceptionWhenAmountTooHigh() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PaymentDTO.builder()
                .amount(new BigDecimal("10001.00"))
        );
        
        assertEquals("Payment amount cannot exceed 10000.00", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate currency format")
    void shouldValidateCurrencyFormat() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PaymentDTO.builder()
                .currency("INVALID")
        );
        
        assertEquals("Currency must be a valid 3-letter ISO 4217 code", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate refund amount against original amount")
    void shouldValidateRefundAmount() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            PaymentDTO.builder()
                .bookingId(123L)
                .customerId(456L)
                .amount(new BigDecimal("25.00"))
                .paymentMethod(PaymentMethod.CARD)
                .refundAmount(new BigDecimal("30.00"))
                .build()
        );
        
        assertEquals("Refund amount cannot exceed original payment amount", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate refunded status consistency")
    void shouldValidateRefundedStatusConsistency() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            PaymentDTO.builder()
                .bookingId(123L)
                .customerId(456L)
                .amount(new BigDecimal("25.00"))
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.REFUNDED)
                .build()
        );
        
        assertEquals("Refunded status requires a refund amount", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate failed status consistency")
    void shouldValidateFailedStatusConsistency() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            PaymentDTO.builder()
                .bookingId(123L)
                .customerId(456L)
                .amount(new BigDecimal("25.00"))
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.FAILED)
                .build()
        );
        
        assertEquals("Failed status requires a failure reason", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate timestamp consistency")
    void shouldValidateTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minusHours(1);
        
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            PaymentDTO.builder()
                .bookingId(123L)
                .customerId(456L)
                .amount(new BigDecimal("25.00"))
                .paymentMethod(PaymentMethod.CARD)
                .createdAt(now)
                .paidAt(past)
                .build()
        );
        
        assertEquals("Payment date cannot be before creation date", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate description length")
    void shouldValidateDescriptionLength() {
        String longDescription = "a".repeat(201);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PaymentDTO.builder()
                .description(longDescription)
        );
        
        assertEquals("Payment description cannot exceed 200 characters", exception.getMessage());
    }

    @Test
    @DisplayName("Should create payment with custom transaction ID")
    void shouldCreatePaymentWithCustomTransactionId() {
        // Given
        String customTransactionId = "CUSTOM_TXN_123";
        
        // When
        PaymentDTO payment = PaymentDTO.builder()
            .bookingId(123L)
            .customerId(456L)
            .amount(new BigDecimal("25.00"))
            .paymentMethod(PaymentMethod.CARD)
            .transactionId(customTransactionId)
            .build();
        
        // Then
        assertEquals(customTransactionId, payment.transactionId());
    }
}