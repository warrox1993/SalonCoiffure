package com.jb.afrostyle.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO pour la demande de remboursement
 * Utilisé pour traiter les remboursements partiels ou complets
 */
public record RefundRequest(
    /**
     * Montant à rembourser
     * Si null, remboursement total
     * Doit être positif et ne pas dépasser le montant original
     */
    @DecimalMin(value = "0.01", message = "Refund amount must be at least 0.01")
    @DecimalMax(value = "10000.00", message = "Refund amount cannot exceed 10000.00")
    BigDecimal amount,

    /**
     * Raison du remboursement
     * Obligatoire pour la traçabilité
     */
    @NotBlank(message = "Refund reason is required")
    @Size(min = 5, max = 500, message = "Refund reason must be between 5 and 500 characters")
    String reason
) {}