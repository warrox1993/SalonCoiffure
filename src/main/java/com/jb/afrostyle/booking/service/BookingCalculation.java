package com.jb.afrostyle.booking.service;

import java.math.BigDecimal;

/**
 * Résultat des calculs d'une réservation
 * Contient durée et prix calculés
 * Migré vers Java Record pour réduire le boilerplate
 */
public record BookingCalculation(
    int totalDuration, // en minutes
    BigDecimal totalPrice,
    int totalServices
) {
    /**
     * Factory method pour créer un calcul
     */
    public static BookingCalculation create(int duration, BigDecimal price, int servicesCount) {
        return new BookingCalculation(duration, price, servicesCount);
    }
}