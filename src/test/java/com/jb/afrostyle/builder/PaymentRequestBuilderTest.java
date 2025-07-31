package com.jb.afrostyle.builder;

import com.jb.afrostyle.payment.dto.PaymentRequest;
import com.jb.afrostyle.payment.domain.enums.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour PaymentRequest Builder Pattern Context7
 * Valide la construction fluide et validation avancée des paiements
 */
@DisplayName("PaymentRequest Builder Pattern Tests")
class PaymentRequestBuilderTest {

    @Test
    @DisplayName("Should create valid PaymentRequest with all features")
    void shouldCreateValidPaymentRequest() {
        // When
        PaymentRequest request = PaymentRequest.builder()
            .bookingId(123L)
            .amount(new BigDecimal("25.00"))
            .paymentMethod(PaymentMethod.CARD)
            .currency("EUR")
            .description("Payment for Hair Braiding service")
            .returnUrl("https://afrostyle.be/payment/success")
            .cancelUrl("https://afrostyle.be/payment/cancel")
            .build();
        
        // Then
        assertNotNull(request);
        assertEquals(123L, request.bookingId());
        assertEquals(new BigDecimal("25.00"), request.amount());
        assertEquals(PaymentMethod.CARD, request.paymentMethod());
        assertEquals("EUR", request.currency());
        assertEquals("Payment for Hair Braiding service", request.description());
        assertEquals("https://afrostyle.be/payment/success", request.returnUrl());
        assertEquals("https://afrostyle.be/payment/cancel", request.cancelUrl());
    }

    @Test
    @DisplayName("Should create PaymentRequest with auto-generated description")
    void shouldCreatePaymentRequestWithAutoDescription() {
        // When
        PaymentRequest request = PaymentRequest.builder()
            .bookingId(123L)
            .amount(new BigDecimal("25.00"))
            .paymentMethod(PaymentMethod.CARD)
            .build();
        
        // Then
        assertEquals("Payment of 25.00 EUR for booking #123", request.description());
    }

    @Test
    @DisplayName("Should create PaymentRequest with test mode configuration")
    void shouldCreatePaymentRequestWithTestMode() {
        // When
        PaymentRequest request = PaymentRequest.builder()
            .bookingId(123L)
            .amount(new BigDecimal("25.00"))
            .paymentMethod(PaymentMethod.CARD)
            .testMode()
            .build();
        
        // Then
        assertEquals("http://localhost:4200/payment/success", request.returnUrl());
        assertEquals("http://localhost:4200/payment/cancel", request.cancelUrl());
        assertEquals("Test payment - AfroStyle", request.description());
    }

    @Test
    @DisplayName("Should create PaymentRequest with production mode configuration")
    void shouldCreatePaymentRequestWithProductionMode() {
        // When
        PaymentRequest request = PaymentRequest.builder()
            .bookingId(123L)
            .amount(new BigDecimal("25.00"))
            .paymentMethod(PaymentMethod.CARD)
            .productionMode()
            .build();
        
        // Then
        assertEquals("https://afrostyle.be/payment/success", request.returnUrl());
        assertEquals("https://afrostyle.be/payment/cancel", request.cancelUrl());
    }

    @Test
    @DisplayName("Should accept amount as string")
    void shouldAcceptAmountAsString() {
        // When
        PaymentRequest request = PaymentRequest.builder()
            .bookingId(123L)
            .amount("25.50")
            .paymentMethod(PaymentMethod.CARD)
            .build();
        
        // Then
        assertEquals(new BigDecimal("25.50"), request.amount());
    }

    @Test
    @DisplayName("Should accept amount as double")
    void shouldAcceptAmountAsDouble() {
        // When
        PaymentRequest request = PaymentRequest.builder()
            .bookingId(123L)
            .amount(25.75)
            .paymentMethod(PaymentMethod.CARD)
            .build();
        
        // Then
        assertEquals(BigDecimal.valueOf(25.75), request.amount());
    }

    @Test
    @DisplayName("Should throw exception when booking ID is missing")
    void shouldThrowExceptionWhenBookingIdMissing() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            PaymentRequest.builder()
                .amount(new BigDecimal("25.00"))
                .paymentMethod(PaymentMethod.CARD)
                .build()
        );
        
        assertEquals("Booking ID is required for payment request", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when amount is missing")
    void shouldThrowExceptionWhenAmountMissing() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            PaymentRequest.builder()
                .bookingId(123L)
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
            PaymentRequest.builder()
                .bookingId(123L)
                .amount(new BigDecimal("25.00"))
                .build()
        );
        
        assertEquals("Payment method is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when amount is zero or negative")
    void shouldThrowExceptionWhenAmountInvalid() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PaymentRequest.builder()
                .amount(BigDecimal.ZERO)
        );
        
        assertEquals("Payment amount must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when amount exceeds maximum")
    void shouldThrowExceptionWhenAmountTooHigh() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PaymentRequest.builder()
                .amount(new BigDecimal("10001.00"))
        );
        
        assertEquals("Payment amount cannot exceed 10000.00", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when amount has too many decimal places")
    void shouldThrowExceptionWhenAmountTooManyDecimals() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PaymentRequest.builder()
                .amount(new BigDecimal("25.123"))
        );
        
        assertEquals("Payment amount cannot have more than 2 decimal places", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when amount string is invalid")
    void shouldThrowExceptionWhenAmountStringInvalid() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PaymentRequest.builder()
                .amount("invalid-amount")
        );
        
        assertEquals("Invalid amount format: invalid-amount", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate currency format")
    void shouldValidateCurrencyFormat() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PaymentRequest.builder()
                .currency("INVALID")
        );
        
        assertEquals("Currency must be a valid 3-letter ISO 4217 code", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for unsupported currency")
    void shouldThrowExceptionForUnsupportedCurrency() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PaymentRequest.builder()
                .currency("XYZ")
        );
        
        assertTrue(exception.getMessage().contains("Currency not supported: XYZ"));
    }

    @Test
    @DisplayName("Should validate minimum amount per currency")
    void shouldValidateMinimumAmountPerCurrency() {
        // When & Then - EUR minimum is 0.50
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PaymentRequest.builder()
                .amount(new BigDecimal("0.25"))
                .currency("EUR")
        );
        
        assertEquals("Minimum amount for EUR is 0.50", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate Apple Pay currency restrictions")
    void shouldValidateApplePayCurrencyRestrictions() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PaymentRequest.builder()
                .paymentMethod(PaymentMethod.APPLE_PAY)
                .currency("CAD")
        );
        
        assertEquals("Apple Pay only supports major currencies (EUR, USD, GBP)", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate cash payment currency restrictions")
    void shouldValidateCashPaymentCurrencyRestrictions() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PaymentRequest.builder()
                .paymentMethod(PaymentMethod.CASH)
                .currency("USD")
        );
        
        assertEquals("Cash payments only accepted in EUR", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate cash payment amount limit")
    void shouldValidateCashPaymentAmountLimit() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            PaymentRequest.builder()
                .bookingId(123L)
                .amount(new BigDecimal("501.00"))
                .paymentMethod(PaymentMethod.CASH)
                .currency("EUR")
                .build()
        );
        
        assertEquals("Cash payments cannot exceed 500.00 EUR", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate high amount currency restrictions")
    void shouldValidateHighAmountCurrencyRestrictions() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            PaymentRequest.builder()
                .bookingId(123L)
                .amount(new BigDecimal("1500.00"))
                .paymentMethod(PaymentMethod.CARD)
                .currency("CAD")
                .build()
        );
        
        assertEquals("Payments over 1000.00 must use major currencies (EUR, USD, GBP)", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate description length")
    void shouldValidateDescriptionLength() {
        String longDescription = "a".repeat(201);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PaymentRequest.builder()
                .description(longDescription)
        );
        
        assertEquals("Payment description cannot exceed 200 characters", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for empty description")
    void shouldThrowExceptionForEmptyDescription() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PaymentRequest.builder()
                .description("   ")
        );
        
        assertEquals("Payment description cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate URL format")
    void shouldValidateUrlFormat() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PaymentRequest.builder()
                .returnUrl("invalid-url")
        );
        
        assertTrue(exception.getMessage().contains("Return URL is not a valid URL"));
    }

    @Test
    @DisplayName("Should validate URL protocol")
    void shouldValidateUrlProtocol() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PaymentRequest.builder()
                .returnUrl("ftp://example.com/payment")
        );
        
        assertEquals("Return URL must use HTTP or HTTPS protocol", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate HTTPS for production URLs")
    void shouldValidateHttpsForProductionUrls() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            PaymentRequest.builder()
                .returnUrl("http://example.com/payment")
        );
        
        assertEquals("Return URL must use HTTPS in production", exception.getMessage());
    }

    @Test
    @DisplayName("Should accept HTTP for localhost URLs")
    void shouldAcceptHttpForLocalhostUrls() {
        // When
        PaymentRequest request = PaymentRequest.builder()
            .bookingId(123L)
            .amount(new BigDecimal("25.00"))
            .paymentMethod(PaymentMethod.CARD)
            .returnUrl("http://localhost:4200/payment/success")
            .build();
        
        // Then
        assertEquals("http://localhost:4200/payment/success", request.returnUrl());
    }

    @Test
    @DisplayName("Should auto-generate URLs with base domain")
    void shouldAutoGenerateUrlsWithBaseDomain() {
        // When
        PaymentRequest request = PaymentRequest.builder()
            .bookingId(123L)
            .amount(new BigDecimal("25.00"))
            .paymentMethod(PaymentMethod.CARD)
            .autoUrls("https://afrostyle.be/")
            .build();
        
        // Then
        assertEquals("https://afrostyle.be/payment/success", request.returnUrl());
        assertEquals("https://afrostyle.be/payment/cancel", request.cancelUrl());
    }

    @Test
    @DisplayName("Should use auto-description when not provided")
    void shouldUseAutoDescriptionWhenNotProvided() {
        // When
        PaymentRequest request = PaymentRequest.builder()
            .bookingId(123L)
            .amount(new BigDecimal("25.00"))
            .paymentMethod(PaymentMethod.CARD)
            .autoDescription()
            .build();
        
        // Then
        assertEquals("Payment of 25.00 EUR for booking #123", request.description());
    }
}