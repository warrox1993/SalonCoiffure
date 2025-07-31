package com.jb.afrostyle.builder;

import com.jb.afrostyle.booking.dto.BookingRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour BookingRequest Builder Pattern Context7
 * Valide la construction fluide et validation avancée des créneaux horaires
 */
@DisplayName("BookingRequest Builder Pattern Tests")
class BookingRequestBuilderTest {

    @Test
    @DisplayName("Should create valid BookingRequest with all validations")
    void shouldCreateValidBookingRequest() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endTime = startTime.plusMinutes(90);
        
        // When
        BookingRequest request = BookingRequest.builder()
            .startTime(startTime)
            .endTime(endTime)
            .serviceIds(Set.of(123L, 124L))
            .build();
        
        // Then
        assertNotNull(request);
        assertEquals(startTime, request.startTime());
        assertEquals(endTime, request.endTime());
        assertEquals(Set.of(123L, 124L), request.serviceIds());
    }

    @Test
    @DisplayName("Should create BookingRequest using timeSlot helper")
    void shouldCreateBookingRequestUsingTimeSlot() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        
        // When
        BookingRequest request = BookingRequest.builder()
            .timeSlot(startTime, 120) // 2 hours
            .serviceIds(Set.of(123L))
            .build();
        
        // Then
        assertEquals(startTime, request.startTime());
        assertEquals(startTime.plusMinutes(120), request.endTime());
    }

    @Test
    @DisplayName("Should create BookingRequest using todaySlot helper")
    void shouldCreateBookingRequestUsingTodaySlot() {
        // When
        BookingRequest request = BookingRequest.builder()
            .todaySlot(14, 30, 90) // Today at 14:30 for 90 minutes
            .serviceIds(Set.of(123L))
            .build();
        
        // Then
        assertNotNull(request.startTime());
        assertEquals(14, request.startTime().getHour());
        assertEquals(30, request.startTime().getMinute());
        assertEquals(request.startTime().plusMinutes(90), request.endTime());
    }

    @Test
    @DisplayName("Should add individual service IDs")
    void shouldAddIndividualServiceIds() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        
        // When
        BookingRequest request = BookingRequest.builder()
            .startTime(startTime)
            .endTime(startTime.plusMinutes(60))
            .addServiceId(123L)
            .addServiceId(124L)
            .build();
        
        // Then
        assertEquals(2, request.serviceIds().size());
        assertTrue(request.serviceIds().contains(123L));
        assertTrue(request.serviceIds().contains(124L));
    }

    @Test
    @DisplayName("Should throw exception when start time is missing")
    void shouldThrowExceptionWhenStartTimeMissing() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            BookingRequest.builder()
                .endTime(LocalDateTime.now().plusHours(2))
                .serviceIds(Set.of(123L))
                .build()
        );
        
        assertEquals("Start time is required for booking request", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when end time is missing")
    void shouldThrowExceptionWhenEndTimeMissing() {
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            BookingRequest.builder()
                .startTime(LocalDateTime.now().plusHours(2))
                .serviceIds(Set.of(123L))
                .build()
        );
        
        assertEquals("End time is required for booking request", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when no services selected")
    void shouldThrowExceptionWhenNoServicesSelected() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            BookingRequest.builder()
                .startTime(startTime)
                .endTime(startTime.plusMinutes(60))
                .serviceIds(Set.of()) // Empty set
                .build()
        );
        
        assertEquals("At least one service must be selected", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when start time is too far in past")
    void shouldThrowExceptionWhenStartTimeTooFarInPast() {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            BookingRequest.builder()
                .startTime(pastTime)
        );
        
        assertEquals("Start time must be in the future (5 min tolerance)", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when start time is outside business hours")
    void shouldThrowExceptionWhenStartTimeOutsideBusinessHours() {
        LocalDateTime earlyTime = LocalDateTime.now().plusDays(1).withHour(7).withMinute(0);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            BookingRequest.builder()
                .startTime(earlyTime)
        );
        
        assertEquals("Start time must be within business hours (8:00-20:00)", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when start time is not on 15-minute intervals")
    void shouldThrowExceptionWhenStartTimeNotOn15MinuteIntervals() {
        LocalDateTime invalidTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(7); // Not 15-min interval
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            BookingRequest.builder()
                .startTime(invalidTime)
        );
        
        assertEquals("Start time must be on 15-minute intervals", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when end time is after business hours")
    void shouldThrowExceptionWhenEndTimeAfterBusinessHours() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        LocalDateTime lateEndTime = LocalDateTime.now().plusDays(1).withHour(21).withMinute(0);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            BookingRequest.builder()
                .startTime(startTime)
                .endTime(lateEndTime)
        );
        
        assertEquals("End time must be before 20:00", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when end time is not on 15-minute intervals")
    void shouldThrowExceptionWhenEndTimeNotOn15MinuteIntervals() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime invalidEndTime = LocalDateTime.now().plusDays(1).withHour(12).withMinute(7);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            BookingRequest.builder()
                .startTime(startTime)
                .endTime(invalidEndTime)
        );
        
        assertEquals("End time must be on 15-minute intervals", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when end time is before start time")
    void shouldThrowExceptionWhenEndTimeBeforeStartTime() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0);
        LocalDateTime endTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            BookingRequest.builder()
                .startTime(startTime)
                .endTime(endTime)
        );
        
        assertEquals("End time must be after start time", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when duration is too short")
    void shouldThrowExceptionWhenDurationTooShort() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime endTime = startTime.plusMinutes(10); // Too short
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            BookingRequest.builder()
                .startTime(startTime)
                .endTime(endTime)
        );
        
        assertEquals("Booking duration must be at least 15 minutes", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when duration exceeds 8 hours")
    void shouldThrowExceptionWhenDurationTooLong() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);
        LocalDateTime endTime = startTime.plusMinutes(500); // > 8 hours
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            BookingRequest.builder()
                .startTime(startTime)
                .endTime(endTime)
        );
        
        assertEquals("Booking duration cannot exceed 8 hours", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for too many services")
    void shouldThrowExceptionForTooManyServices() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            BookingRequest.builder()
                .serviceIds(Set.of(1L, 2L, 3L, 4L, 5L, 6L)) // 6 services (> 5 max)
        );
        
        assertEquals("Cannot select more than 5 services per booking", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when adding too many services individually")
    void shouldThrowExceptionWhenAddingTooManyServicesIndividually() {
        BookingRequest.Builder builder = BookingRequest.builder()
            .addServiceId(1L)
            .addServiceId(2L)
            .addServiceId(3L)
            .addServiceId(4L)
            .addServiceId(5L);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            builder.addServiceId(6L) // 6th service
        );
        
        assertEquals("Cannot select more than 5 services per booking", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for invalid service IDs")
    void shouldThrowExceptionForInvalidServiceIds() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            BookingRequest.builder()
                .serviceIds(Set.of(123L, -1L)) // Negative ID
        );
        
        assertEquals("All service IDs must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when adding invalid service ID")
    void shouldThrowExceptionWhenAddingInvalidServiceId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            BookingRequest.builder()
                .addServiceId(-1L)
        );
        
        assertEquals("Service ID must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for Sunday bookings")
    void shouldThrowExceptionForSundayBookings() {
        // Find next Sunday
        LocalDateTime searchDate = LocalDateTime.now().plusDays(1);
        while (searchDate.getDayOfWeek().getValue() != 7) {
            searchDate = searchDate.plusDays(1);
        }
        final LocalDateTime sunday = searchDate.withHour(10).withMinute(0).withSecond(0).withNano(0);
        
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            BookingRequest.builder()
                .startTime(sunday)
                .endTime(sunday.plusMinutes(60))
                .serviceIds(Set.of(123L))
                .build()
        );
        
        assertEquals("Bookings are not allowed on Sundays", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when duration too short for number of services")
    void shouldThrowExceptionWhenDurationTooShortForServices() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endTime = startTime.plusMinutes(45); // 45 minutes for 2 services = 22.5 min/service (< 30 min minimum)
        
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            BookingRequest.builder()
                .startTime(startTime)
                .endTime(endTime)
                .serviceIds(Set.of(123L, 124L)) // 2 services
                .build()
        );
        
        assertTrue(exception.getMessage().contains("Duration too short for 2 services"));
        assertTrue(exception.getMessage().contains("minimum 30 min/service"));
    }

    @Test
    @DisplayName("Should throw exception when booking too far in advance")
    void shouldThrowExceptionWhenBookingTooFarInAdvance() {
        LocalDateTime futureTime = LocalDateTime.now().plusMonths(7).withHour(10).withMinute(0);
        
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            BookingRequest.builder()
                .startTime(futureTime)
                .endTime(futureTime.plusMinutes(60))
                .serviceIds(Set.of(123L))
                .build()
        );
        
        assertEquals("Cannot book more than 6 months in advance", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate timeSlot duration parameters")
    void shouldValidateTimeSlotDurationParameters() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        
        // When & Then - Duration too short
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () ->
            BookingRequest.builder()
                .timeSlot(startTime, 10) // < 15 minutes
        );
        assertEquals("Duration must be at least 15 minutes", exception1.getMessage());
        
        // When & Then - Duration too long
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () ->
            BookingRequest.builder()
                .timeSlot(startTime, 500) // > 8 hours
        );
        assertEquals("Duration cannot exceed 8 hours (480 minutes)", exception2.getMessage());
        
        // When & Then - Duration not in 15-minute increments
        IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class, () ->
            BookingRequest.builder()
                .timeSlot(startTime, 37) // Not multiple of 15
        );
        assertEquals("Duration must be in 15-minute increments", exception3.getMessage());
    }

    @Test
    @DisplayName("Should validate all build() time constraints")
    void shouldValidateAllBuildTimeConstraints() {
        final LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        final LocalDateTime endTime = startTime.plusMinutes(480); // Exactly 8 hours (valid)
        
        // When - Valid 8-hour booking
        BookingRequest request = BookingRequest.builder()
            .startTime(startTime)
            .endTime(endTime)
            .serviceIds(Set.of(123L))
            .build();
        
        // Then
        assertNotNull(request);
        assertEquals(startTime, request.startTime());
        assertEquals(endTime, request.endTime());
    }
}