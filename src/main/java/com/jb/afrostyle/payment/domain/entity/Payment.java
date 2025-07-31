package com.jb.afrostyle.payment.domain.entity;

import com.jb.afrostyle.payment.domain.enums.PaymentStatus;
import com.jb.afrostyle.payment.domain.enums.PaymentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Paiement pour le salon unique.
 * 
 * MODÈLE MONO-SALON :
 * - Plus de salonId (tous les paiements pour le salon unique)
 * - Simplification de la logique de paiement
 * - Plus de validation ownership salon
 * 
 * MIGRATION MULTI-SALON → MONO-SALON :
 * - Suppression salonId (champ obligatoire avant)
 * - Mise à jour constructeurs
 * - Ajout validations
 * 
 * @author AfroStyle Team
 * @since 2.0 (Migration mono-salon)
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_booking_id", columnList = "bookingId"),
    @Index(name = "idx_payments_customer_id", columnList = "customerId"),
    @Index(name = "idx_payments_status", columnList = "status"),
    @Index(name = "idx_payments_stripe_session_id", columnList = "stripeSessionId"),
    @Index(name = "idx_payments_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    @NotNull(message = "L'ID de réservation est obligatoire")
    private Long bookingId;

    @Column(name = "customer_id", nullable = false)
    @NotNull(message = "L'ID du client est obligatoire")
    private Long customerId;

    // HACK TEMPORAIRE : Ignorer salon_id existant en DB sans l'insérer/modifier
    @Column(name = "salon_id", insertable = false, updatable = false)
    private Long legacySalonId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Le montant du paiement est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Size(min = 3, max = 3, message = "Le code devise doit faire 3 caractères")
    private String currency = "EUR";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    @NotNull(message = "La méthode de paiement est obligatoire")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    // =========================
    // INTÉGRATION STRIPE
    // =========================

    @Column(name = "stripe_charge_id")
    private String stripeChargeId;

    @Column(name = "stripe_session_id", unique = true)
    private String stripeSessionId;

    @Column(name = "stripe_payment_intent_id", unique = true)
    private String stripePaymentIntentId;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    // =========================
    // INFORMATIONS MÉTIER
    // =========================

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_reason")
    private String refundReason;

    // =========================
    // MÉTADONNÉES TEMPORELLES
    // =========================

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    // =========================
    // CONSTRUCTEURS
    // =========================

    /**
     * Constructeur pour créer un paiement (sans salonId).
     * MIGRATION MONO-SALON : salonId supprimé du constructeur.
     */
    public Payment(Long bookingId, Long customerId, BigDecimal amount, PaymentMethod paymentMethod, String description) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.description = description;
        this.status = PaymentStatus.PENDING;
        this.currency = "EUR";
    }

    // =========================
    // MÉTHODES UTILITAIRES
    // =========================

    /**
     * Retourne le montant formaté en euros.
     * 
     * @return Montant formaté (ex: "45.50€")
     */
    public String getFormattedAmount() {
        return amount != null ? String.format("%.2f€", amount) : "0.00€";
    }

    /**
     * Vérifie si le paiement est réussi.
     * 
     * @return true si le statut est SUCCEEDED
     */
    public boolean isSucceeded() {
        return PaymentStatus.SUCCEEDED.equals(status);
    }

    /**
     * Vérifie si le paiement a échoué.
     * 
     * @return true si le statut est FAILED
     */
    public boolean isFailed() {
        return PaymentStatus.FAILED.equals(status);
    }

    /**
     * Vérifie si le paiement peut être remboursé.
     * 
     * @return true si le paiement est réussi et pas déjà remboursé
     */
    public boolean canBeRefunded() {
        return isSucceeded() && (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) == 0);
    }
}