package com.jb.afrostyle.builder;

import com.jb.afrostyle.booking.dto.BookingDTO;
import com.jb.afrostyle.booking.dto.BookingRequest;
import com.jb.afrostyle.booking.domain.enums.BookingStatus;
import com.jb.afrostyle.payment.dto.PaymentDTO;
import com.jb.afrostyle.payment.dto.PaymentRequest;
import com.jb.afrostyle.payment.domain.enums.PaymentMethod;
import com.jb.afrostyle.payment.domain.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour valider tous les Builders Context7
 * Démontre l'utilisation fluide et les fonctionnalités avancées
 */
@DisplayName("Builder Pattern Context7 Integration Tests")
class BuilderIntegrationTest {

    @Test
    @DisplayName("PaymentDTO Builder - Creation and Validation Success")
    void paymentDTOBuilderSuccessScenario() {
        // When
        PaymentDTO payment = PaymentDTO.builder()
            .bookingId(123L)
            .customerId(456L)
            .amount(new BigDecimal("25.00"))
            .paymentMethod(PaymentMethod.CARD)
            .description("Payment for Hair Braiding service")
            .build();
        
        // Then
        assertNotNull(payment);
        assertEquals(123L, payment.bookingId());
        assertEquals(456L, payment.customerId());
        assertEquals(new BigDecimal("25.00"), payment.amount());
        assertEquals(PaymentMethod.CARD, payment.paymentMethod());
        assertEquals(PaymentStatus.PENDING, payment.status()); // Default
        assertEquals("EUR", payment.currency()); // Default
        assertNotNull(payment.transactionId()); // Auto-generated
        assertNotNull(payment.createdAt()); // Auto-generated
        assertTrue(payment.transactionId().startsWith("TXN_"));
    }

    @Test
    @DisplayName("PaymentDTO Builder - Validation Failures")
    void paymentDTOBuilderValidationFailures() {
        // Test missing required fields
        assertThrows(IllegalStateException.class, () ->
            PaymentDTO.builder()
                .customerId(456L)
                .amount(new BigDecimal("25.00"))
                .paymentMethod(PaymentMethod.CARD)
                .build()
        );
        
        // Test invalid amount
        assertThrows(IllegalArgumentException.class, () ->
            PaymentDTO.builder()
                .amount(new BigDecimal("-5.00"))
        );
        
        // Test invalid currency
        assertThrows(IllegalArgumentException.class, () ->
            PaymentDTO.builder()
                .currency("INVALID")
        );
    }

    @Test
    @DisplayName("BookingDTO Builder - Creation with Auto-Calculations")
    void bookingDTOBuilderWithCalculations() {
        // Given - Future booking time
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endTime = tomorrow.plusMinutes(150); // 2h30
        
        // When
        BookingDTO booking = BookingDTO.builder()
            .customerId(456L)
            .startTime(tomorrow)
            .endTime(endTime)
            .serviceIds(Set.of(123L, 124L))
            .totalPrice(new BigDecimal("75.00"))
            .userName("Marie Dupont")
            .salonName("AfroStyle Salon")
            .build();
        
        // Then
        assertNotNull(booking);
        assertEquals(456L, booking.customerId());
        assertEquals(tomorrow, booking.startTime());
        assertEquals(endTime, booking.endTime());
        assertEquals(Set.of(123L, 124L), booking.serviceIds());
        assertEquals(BookingStatus.PENDING, booking.status()); // Default
        assertEquals(Integer.valueOf(150), booking.totalDuration()); // Auto-calculated
        assertEquals(Integer.valueOf(2), booking.totalServices()); // Auto-calculated
        assertEquals(123L, booking.serviceId()); // First service as main
        assertNotNull(booking.bookingDate()); // Auto-generated
    }

    @Test
    @DisplayName("BookingRequest Builder - Time Slot Validation")
    void bookingRequestBuilderValidation() {
        // Given - Valid future time on a weekday (not Sunday)
        LocalDateTime nextWeekday = getNextWeekday().withHour(14).withMinute(0).withSecond(0).withNano(0);
        
        // When
        BookingRequest request = BookingRequest.builder()
            .startTime(nextWeekday)
            .endTime(nextWeekday.plusMinutes(90))
            .serviceIds(Set.of(123L, 124L))
            .build();
        
        // Then
        assertNotNull(request);
        assertEquals(nextWeekday, request.startTime());
        assertEquals(nextWeekday.plusMinutes(90), request.endTime());
        assertEquals(Set.of(123L, 124L), request.serviceIds());
    }

    @Test
    @DisplayName("PaymentRequest Builder - Multi-Currency and URL Validation")
    void paymentRequestBuilderAdvancedFeatures() {
        // When - Test mode with auto URLs
        PaymentRequest testRequest = PaymentRequest.builder()
            .bookingId(123L)
            .amount("25.50")
            .paymentMethod(PaymentMethod.CARD)
            .testMode()
            .build();
        
        // Then
        assertEquals("http://localhost:4200/payment/success", testRequest.returnUrl());
        assertEquals("http://localhost:4200/payment/cancel", testRequest.cancelUrl());
        assertEquals("Test payment - AfroStyle", testRequest.description());
        
        // When - Production mode
        PaymentRequest prodRequest = PaymentRequest.builder()
            .bookingId(123L)
            .amount(25.75)
            .paymentMethod(PaymentMethod.CARD)
            .currency("USD")
            .productionMode()
            .build();
        
        // Then
        assertEquals("https://afrostyle.be/payment/success", prodRequest.returnUrl());
        assertEquals("https://afrostyle.be/payment/cancel", prodRequest.cancelUrl());
        assertEquals("USD", prodRequest.currency());
    }

    @Test
    @DisplayName("Builder Pattern - Complex Validation Scenarios")
    void complexValidationScenarios() {
        // Test PaymentDTO refund validation
        assertThrows(IllegalStateException.class, () ->
            PaymentDTO.builder()
                .bookingId(123L)
                .customerId(456L)
                .amount(new BigDecimal("25.00"))
                .paymentMethod(PaymentMethod.CARD)
                .refundAmount(new BigDecimal("30.00")) // Exceeds original
                .build()
        );
        
        // Test PaymentRequest currency restrictions
        assertThrows(IllegalArgumentException.class, () ->
            PaymentRequest.builder()
                .paymentMethod(PaymentMethod.APPLE_PAY)
                .currency("CAD") // Not supported for Apple Pay
        );
        
        // Test BookingRequest service duration validation
        LocalDateTime nextWeekday = getNextWeekday().withHour(10).withMinute(0).withSecond(0).withNano(0);
        assertThrows(IllegalStateException.class, () ->
            BookingRequest.builder()
                .startTime(nextWeekday)
                .endTime(nextWeekday.plusMinutes(45)) // 45 min for 2 services = 22.5 min/service (< 30 min minimum)
                .serviceIds(Set.of(123L, 124L))
                .build()
        );
    }

    @Test
    @DisplayName("Builder Pattern - Fluent API Demonstration")
    void fluentAPIDemo() {
        // Demonstrate chaining and auto-calculations
        LocalDateTime nextWeekday = getNextWeekday().withHour(10).withMinute(0).withSecond(0).withNano(0);
        
        // Complex BookingRequest with method chaining
        BookingRequest request = BookingRequest.builder()
            .startTime(nextWeekday)
            .endTime(nextWeekday.plusMinutes(120))
            .addServiceId(123L)
            .addServiceId(124L)
            .build();
        
        assertNotNull(request);
        assertEquals(2, request.serviceIds().size());
        
        // Complex PaymentRequest with auto-description
        PaymentRequest payment = PaymentRequest.builder()
            .bookingId(123L)
            .amount("75.50")
            .paymentMethod(PaymentMethod.CARD)
            .currency("EUR")
            .autoDescription()
            .autoUrls("http://localhost:4200")
            .build();
        
        assertNotNull(payment);
        assertTrue(payment.description().contains("75.50"));
        assertTrue(payment.description().contains("123"));
        assertEquals("http://localhost:4200/payment/success", payment.returnUrl());
    }

    @Test
    @DisplayName("Builder Pattern - Edge Cases and Boundaries")
    void edgeCasesAndBoundaries() {
        // Test minimum amounts per currency
        PaymentRequest eurMinimum = PaymentRequest.builder()
            .bookingId(123L)
            .amount("0.50") // EUR minimum
            .paymentMethod(PaymentMethod.CARD)
            .currency("EUR")
            .build();
        
        assertEquals(new BigDecimal("0.50"), eurMinimum.amount());
        
        // Test maximum valid booking duration (exactly 8 hours)
        LocalDateTime nextWeekday = getNextWeekday().withHour(8).withMinute(0).withSecond(0).withNano(0);
        BookingRequest maxDuration = BookingRequest.builder()
            .startTime(nextWeekday)
            .endTime(nextWeekday.plusMinutes(480)) // Exactly 8 hours
            .serviceIds(Set.of(123L))
            .build();
        
        assertNotNull(maxDuration);
        assertEquals(nextWeekday.plusMinutes(480), maxDuration.endTime());
        
        // Test maximum services (5 services)
        BookingRequest maxServices = BookingRequest.builder()
            .startTime(nextWeekday)
            .endTime(nextWeekday.plusMinutes(300)) // 5 hours for 5 services = 60 min/service (valid)
            .serviceIds(Set.of(1L, 2L, 3L, 4L, 5L))
            .build();
        
        assertEquals(5, maxServices.serviceIds().size());
    }

    /**
     * Helper method to get next weekday (Monday-Saturday, not Sunday)
     */
    private LocalDateTime getNextWeekday() {
        LocalDateTime date = LocalDateTime.now().plusDays(1);
        while (date.getDayOfWeek().getValue() == 7) { // Skip Sunday
            date = date.plusDays(1);
        }
        return date;
    }
}