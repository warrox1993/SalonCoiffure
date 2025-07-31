package com.jb.afrostyle.serviceoffering.payload.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour les services du salon unique.
 * 
 * MIGRATION MONO-SALON :
 * - Suppression salonId (plus nécessaire)
 * - Amélioration types : int → BigDecimal pour price, int → Integer pour duration
 * - Ajout validations
 * - Migré vers Java Record avec méthodes utilitaires
 * 
 * @author AfroStyle Team
 * @since 2.0 (Migration mono-salon)
 */
public record ServiceDTO(
    Long id,

    @NotBlank(message = "Le nom du service est obligatoire")
    String name,

    @NotBlank(message = "La description du service est obligatoire")
    String description,

    @NotNull(message = "Le prix du service est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    @DecimalMax(value = "9999.99", message = "Le prix ne peut pas dépasser 9999.99€")
    BigDecimal price,

    @NotNull(message = "La durée du service est obligatoire")
    @Min(value = 5, message = "La durée minimale est de 5 minutes")
    @Max(value = 600, message = "La durée maximale est de 600 minutes (10h)")
    Integer duration,

    String images,

    Boolean active,

    Integer displayOrder,

    String tags,

    LocalDateTime createdAt,

    LocalDateTime updatedAt
) {
    // =========================
    // MÉTHODES UTILITAIRES
    // =========================

    /**
     * Retourne le prix formaté en euros.
     */
    public String getFormattedPrice() {
        return price != null ? String.format("%.2f€", price) : "0.00€";
    }

    /**
     * Retourne la durée formatée.
     */
    public String getFormattedDuration() {
        if (duration == null) return "0min";
        
        int hours = duration / 60;
        int minutes = duration % 60;
        
        if (hours > 0 && minutes > 0) {
            return String.format("%dh %02dmin", hours, minutes);
        } else if (hours > 0) {
            return String.format("%dh", hours);
        } else {
            return String.format("%dmin", minutes);
        }
    }

    /**
     * Compact constructor pour valeurs par défaut
     */
    public ServiceDTO {
        if (active == null) active = true;
        if (displayOrder == null) displayOrder = 0;
    }
}