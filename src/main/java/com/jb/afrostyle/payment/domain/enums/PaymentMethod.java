package com.jb.afrostyle.payment.domain.enums;

/**
 * Méthodes de paiement supportées
 */
public enum PaymentMethod {
    /**
     * Carte de crédit/débit
     */
    CARD("Credit/Debit Card"),

    /**
     * Virement bancaire
     */
    BANK_TRANSFER("Bank Transfer"),

    /**
     * PayPal
     */
    PAYPAL("PayPal"),

    /**
     * Apple Pay
     */
    APPLE_PAY("Apple Pay"),

    /**
     * Google Pay
     */
    GOOGLE_PAY("Google Pay"),

    /**
     * Espèces (paiement sur place)
     */
    CASH("Cash");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isOnlineMethod() {
        return this != CASH;
    }
}