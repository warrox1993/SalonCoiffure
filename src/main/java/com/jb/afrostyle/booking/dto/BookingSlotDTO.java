package com.jb.afrostyle.booking.dto;

import java.time.LocalDateTime;

public record BookingSlotDTO(
        LocalDateTime startTime,
        LocalDateTime endTime
) {}