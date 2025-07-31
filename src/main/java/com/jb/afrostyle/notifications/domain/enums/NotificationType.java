package com.jb.afrostyle.notifications.domain.enums;

public enum NotificationType {
    // Événements liés aux réservations
    BOOKING_CONFIRMATION("Confirmation de réservation"),
    BOOKING_REMINDER("Rappel de rendez-vous"),
    BOOKING_CANCELLED("Réservation annulée"),
    BOOKING_RESCHEDULED("Réservation reportée"),
    
    // Événements liés aux disponibilités
    NEW_AVAILABILITY("Nouveau créneau disponible"),
    AVAILABILITY_CANCELLED("Créneau annulé"),
    
    // Événements liés au salon
    SALON_PROMOTION("Promotion du salon"),
    SALON_NEWS("Actualités du salon"),
    
    // Événements système
    PAYMENT_CONFIRMATION("Confirmation de paiement"),
    PAYMENT_FAILED("Échec de paiement"),
    
    // Marketing
    NEWSLETTER("Newsletter"),
    BIRTHDAY_OFFER("Offre anniversaire");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // Méthodes utilitaires pour catégoriser les notifications
    public boolean isBookingRelated() {
        return this == BOOKING_CONFIRMATION || this == BOOKING_REMINDER || 
               this == BOOKING_CANCELLED || this == BOOKING_RESCHEDULED;
    }

    public boolean isAvailabilityRelated() {
        return this == NEW_AVAILABILITY || this == AVAILABILITY_CANCELLED;
    }

    public boolean isUrgent() {
        return this == BOOKING_REMINDER || this == BOOKING_CANCELLED || 
               this == PAYMENT_FAILED;
    }

    public boolean isMarketing() {
        return this == SALON_PROMOTION || this == SALON_NEWS || 
               this == NEWSLETTER || this == BIRTHDAY_OFFER;
    }
}