package com.jb.afrostyle.builder;

import com.jb.afrostyle.booking.dto.BookingDTO;
import com.jb.afrostyle.booking.domain.enums.BookingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour BookingDTO Builder Pattern Context7
 * Valide la construction fluide, calculs automatiques et validation métier
 */
@DisplayName("BookingDTO Builder Pattern Tests")
class BookingDTOBuilderTest {

    @Test
    @DisplayName("Should create valid BookingDTO with automatic calculations")
    void shouldCreateValidBookingDTOWithCalculations() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2024, 3, 15, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 3, 15, 12, 30);
        Set<Long> serviceIds = Set.of(123L, 124L);
        
        // When
        BookingDTO booking = BookingDTO.builder()
            .customerId(456L)
            .startTime(startTime)
            .endTime(endTime)
            .serviceIds(serviceIds)
            .totalPrice(new BigDecimal("75.00"))
            .userName("Marie Dupont")
            .salonName("AfroStyle Salon")
            .build();
        
        // Then
        assertNotNull(booking);
        assertEquals(456L, booking.customerId());
        assertEquals(startTime, booking.startTime());
        assertEquals(endTime, booking.endTime());
        assertEquals(serviceIds, booking.serviceIds());
        assertEquals(BookingStatus.PENDING, booking.status()); // Default
        assertEquals(Integer.valueOf(150), booking.totalDuration()); // Auto-calculated: 2h30 = 150 min
        assertEquals(Integer.valueOf(2), booking.totalServices()); // Auto-calculated
        assertEquals(123L, booking.serviceId()); // First service as main
        assertNotNull(booking.bookingDate()); // Auto-generated
    }

    @Test
    @DisplayName("Should throw exception when customer ID is missing")
    void shouldThrowExceptionWhenCustomerIdMissing() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            BookingDTO.builder()
                .startTime(LocalDateTime.of(2024, 3, 15, 10, 0))
                .endTime(LocalDateTime.of(2024, 3, 15, 12, 0))
                .serviceIds(Set.of(123L))
                .build()
        );
        
        assertEquals("Customer ID is required for booking creation", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when start time is missing")
    void shouldThrowExceptionWhenStartTimeMissing() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            BookingDTO.builder()
                .customerId(456L)
                .endTime(LocalDateTime.of(2024, 3, 15, 12, 0))
                .serviceIds(Set.of(123L))
                .build()
        );
        
        assertEquals("Start time is required for booking creation", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when end time is missing")
    void shouldThrowExceptionWhenEndTimeMissing() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            BookingDTO.builder()
                .customerId(456L)
                .startTime(LocalDateTime.of(2024, 3, 15, 10, 0))
                .serviceIds(Set.of(123L))
                .build()
        );
        
        assertEquals("End time is required for booking creation", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when no services selected")
    void shouldThrowExceptionWhenNoServicesSelected() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            BookingDTO.builder()
                .customerId(456L)
                .startTime(LocalDateTime.of(2024, 3, 15, 10, 0))
                .endTime(LocalDateTime.of(2024, 3, 15, 12, 0))
                .serviceIds(Set.of()) // Empty set
                .build()
        );
        
        assertEquals("At least one service must be selected", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when end time is before start time")
    void shouldThrowExceptionWhenEndTimeBeforeStartTime() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            BookingDTO.builder()
                .customerId(456L)
                .startTime(LocalDateTime.of(2024, 3, 15, 12, 0))
                .endTime(LocalDateTime.of(2024, 3, 15, 10, 0)) // Before start time
                .serviceIds(Set.of(123L))
                .build()
        );
        
        assertEquals("End time must be after start time", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when duration exceeds 8 hours")
    void shouldThrowExceptionWhenDurationTooLong() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            BookingDTO.builder()
                .customerId(456L)
                .startTime(LocalDateTime.of(2024, 3, 15, 8, 0))
                .endTime(LocalDateTime.of(2024, 3, 15, 17, 0)) // 9 hours
                .serviceIds(Set.of(123L))
                .build()
        );
        
        assertEquals("Booking duration cannot exceed 8 hours", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when duration is too short")
    void shouldThrowExceptionWhenDurationTooShort() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            BookingDTO.builder()
                .customerId(456L)
                .startTime(LocalDateTime.of(2024, 3, 15, 10, 0))
                .endTime(LocalDateTime.of(2024, 3, 15, 10, 10)) // 10 minutes
                .serviceIds(Set.of(123L))
                .build()
        );
        
        assertEquals("Booking duration must be at least 15 minutes", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when start time is outside business hours")
    void shouldThrowExceptionWhenStartTimeOutsideBusinessHours() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            BookingDTO.builder()
                .customerId(456L)
                .startTime(LocalDateTime.of(2024, 3, 15, 7, 0)) // Before 8:00
                .endTime(LocalDateTime.of(2024, 3, 15, 9, 0))
                .serviceIds(Set.of(123L))
                .build()
        );
        
        assertEquals("Booking must be within business hours (8:00-20:00)", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when end time is after business hours")
    void shouldThrowExceptionWhenEndTimeAfterBusinessHours() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            BookingDTO.builder()
                .customerId(456L)
                .startTime(LocalDateTime.of(2024, 3, 15, 19, 0))
                .endTime(LocalDateTime.of(2024, 3, 15, 21, 0)) // After 20:00
                .serviceIds(Set.of(123L))
                .build()
        );
        
        assertEquals("Booking must end before 20:00", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate price per service minimum")
    void shouldValidatePricePerServiceMinimum() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            BookingDTO.builder()
                .customerId(456L)
                .startTime(LocalDateTime.of(2024, 3, 15, 10, 0))
                .endTime(LocalDateTime.of(2024, 3, 15, 12, 0))
                .serviceIds(Set.of(123L, 124L)) // 2 services
                .totalPrice(new BigDecimal("8.00")) // 4€ per service (< 5€ minimum)
                .build()
        );
        
        assertEquals("Price per service seems too low (< 5.00 EUR)", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when total price exceeds maximum")
    void shouldThrowExceptionWhenPriceTooHigh() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            BookingDTO.builder()
                .totalPrice(new BigDecimal("1001.00"))
        );
        
        assertEquals("Total price cannot exceed 1000.00 EUR", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate user name length")
    void shouldValidateUserNameLength() {
        String longName = "a".repeat(101);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            BookingDTO.builder()
                .userName(longName)
        );
        
        assertEquals("User name cannot exceed 100 characters", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate service name length")
    void shouldValidateServiceNameLength() {
        String longServiceName = "a".repeat(201);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            BookingDTO.builder()
                .serviceName(longServiceName)
        );
        
        assertEquals("Service name cannot exceed 200 characters", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate notes length")
    void shouldValidateNotesLength() {
        String longNotes = "a".repeat(501);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            BookingDTO.builder()
                .notes(longNotes)
        );
        
        assertEquals("Notes cannot exceed 500 characters", exception.getMessage());
    }

    @Test
    @DisplayName("Should add individual service IDs")
    void shouldAddIndividualServiceIds() {
        // When
        BookingDTO booking = BookingDTO.builder()
            .customerId(456L)
            .startTime(LocalDateTime.of(2024, 3, 15, 10, 0))
            .endTime(LocalDateTime.of(2024, 3, 15, 12, 0))
            .addServiceId(123L)
            .addServiceId(124L)
            .addServiceId(125L)
            .build();
        
        // Then
        assertEquals(3, booking.serviceIds().size());
        assertTrue(booking.serviceIds().contains(123L));
        assertTrue(booking.serviceIds().contains(124L));
        assertTrue(booking.serviceIds().contains(125L));
        assertEquals(Integer.valueOf(3), booking.totalServices());
    }

    @Test
    @DisplayName("Should validate booking date consistency")
    void shouldValidateBookingDateConsistency() {
        LocalDateTime startTime = LocalDateTime.of(2024, 3, 15, 10, 0);
        LocalDateTime bookingDate = LocalDateTime.of(2024, 3, 16, 9, 0); // After start time
        
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            BookingDTO.builder()
                .customerId(456L)
                .startTime(startTime)
                .endTime(LocalDateTime.of(2024, 3, 15, 12, 0))
                .serviceIds(Set.of(123L))
                .bookingDate(bookingDate)
                .build()
        );
        
        assertEquals("Booking date cannot be after start time", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate service IDs are positive")
    void shouldValidateServiceIdsPositive() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            BookingDTO.builder()
                .serviceIds(Set.of(123L, -1L)) // Negative ID
        );
        
        assertEquals("All service IDs must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate start time not too far in past")
    void shouldValidateStartTimeNotTooFarInPast() {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            BookingDTO.builder()
                .startTime(pastTime)
        );
        
        assertEquals("Start time cannot be more than 15 minutes in the past", exception.getMessage());
    }
}