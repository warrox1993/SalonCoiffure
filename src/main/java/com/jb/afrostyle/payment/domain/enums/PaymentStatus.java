package com.jb.afrostyle.payment.domain.enums;

/**
 * Statuts possibles pour un paiement
 */
public enum PaymentStatus {
    /**
     * Paiement en attente
     */
    PENDING("Pending"),

    /**
     * Paiement en cours de traitement
     */
    PROCESSING("Processing"),

    /**
     * Paiement réussi
     */
    SUCCEEDED("Succeeded"),

    /**
     * Paiement échoué
     */
    FAILED("Failed"),

    /**
     * Paiement annulé
     */
    CANCELLED("Cancelled"),

    /**
     * Paiement remboursé (partiellement ou totalement)
     */
    REFUNDED("Refunded"),

    /**
     * Paiement partiellement remboursé
     */
    PARTIALLY_REFUNDED("Partially Refunded"),

    /**
     * Paiement en litige
     */
    DISPUTED("Disputed"),

    /**
     * Paiement en attente de confirmation
     */
    REQUIRES_CONFIRMATION("Requires Confirmation");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}