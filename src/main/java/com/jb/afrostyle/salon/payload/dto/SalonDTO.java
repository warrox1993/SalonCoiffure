package com.jb.afrostyle.salon.payload.dto;

import jakarta.validation.constraints.*;

import java.time.LocalTime;
import java.util.List;

/**
 * Salon DTO migré vers Java Record pour réduire le boilerplate
 */
public record SalonDTO(
    Long id,

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    String name,

    List<String> images,

    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    String address,

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone must be a valid phone number")
    String phone,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    String email,

    @NotBlank(message = "City is required")
    @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters")
    String city,

    @NotNull(message = "Open time is required")
    LocalTime openTime,

    @NotNull(message = "Close time is required")
    LocalTime closeTime,

    // GPS coordinates
    Double latitude,
    Double longitude,
    String googlePlaceId,
    String formattedAddress
) {}