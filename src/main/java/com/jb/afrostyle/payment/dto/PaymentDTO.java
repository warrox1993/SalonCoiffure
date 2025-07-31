package com.jb.afrostyle.payment.dto;

import com.jb.afrostyle.payment.domain.enums.PaymentMethod;
import com.jb.afrostyle.payment.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Payment DTO migré vers Java Record pour réduire le boilerplate
 * Implémente le Builder Pattern Context7 pour une construction fluide et validation métier
 * 
 * <p>Exemple d'utilisation :</p>
 * <pre>{@code
 * PaymentDTO payment = PaymentDTO.builder()
 *     .bookingId(123L)
 *     .customerId(456L)
 *     .amount(new BigDecimal("25.00"))
 *     .paymentMethod(PaymentMethod.CARD)
 *     .description("Payment for Hair Braiding service")
 *     .build();
 * }</pre>
 */
public record PaymentDTO(
    Long id,
    Long bookingId,
    Long customerId,
    BigDecimal amount,
    String currency,
    PaymentMethod paymentMethod,
    PaymentStatus status,
    String transactionId,
    String description,
    String failureReason,
    BigDecimal refundAmount,
    String refundReason,
    LocalDateTime createdAt,
    LocalDateTime paidAt,
    LocalDateTime refundedAt
) {
    
    /**
     * Crée une nouvelle instance du Builder pour PaymentDTO
     * @return Une nouvelle instance du Builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder Pattern Context7 pour PaymentDTO
     * Fournit une API fluide avec validation métier intégrée
     * 
     * <p>Fonctionnalités :</p>
     * <ul>
     *   <li>Validation métier automatique dans build()</li>
     *   <li>Valeurs par défaut intelligentes (EUR, PENDING)</li>
     *   <li>API fluide chainable</li>
     *   <li>Calculs automatiques (timestamps, etc.)</li>
     *   <li>Type safety avec validation des enums</li>
     * </ul>
     */
    public static class Builder {
        private Long id;
        private Long bookingId;
        private Long customerId;
        private BigDecimal amount;
        private String currency = "EUR"; // Valeur par défaut
        private PaymentMethod paymentMethod;
        private PaymentStatus status = PaymentStatus.PENDING; // Valeur par défaut
        private String transactionId;
        private String description;
        private String failureReason;
        private BigDecimal refundAmount;
        private String refundReason;
        private LocalDateTime createdAt;
        private LocalDateTime paidAt;
        private LocalDateTime refundedAt;
        
        /**
         * Définit l'ID du paiement
         * @param id L'identifiant unique du paiement
         * @return Le builder pour chaînage
         */
        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        
        /**
         * Définit l'ID de la réservation associée (requis)
         * @param bookingId L'identifiant de la réservation
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si bookingId est null ou négatif
         */
        public Builder bookingId(Long bookingId) {
            if (bookingId != null && bookingId <= 0) {
                throw new IllegalArgumentException("Booking ID must be positive");
            }
            this.bookingId = bookingId;
            return this;
        }
        
        /**
         * Définit l'ID du client (requis)
         * @param customerId L'identifiant du client
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si customerId est null ou négatif
         */
        public Builder customerId(Long customerId) {
            if (customerId != null && customerId <= 0) {
                throw new IllegalArgumentException("Customer ID must be positive");
            }
            this.customerId = customerId;
            return this;
        }
        
        /**
         * Définit le montant du paiement (requis)
         * @param amount Le montant à payer
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si le montant est négatif ou dépasse 10000
         */
        public Builder amount(BigDecimal amount) {
            if (amount != null) {
                if (amount.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Payment amount cannot be negative");
                }
                if (amount.compareTo(new BigDecimal("10000.00")) > 0) {
                    throw new IllegalArgumentException("Payment amount cannot exceed 10000.00");
                }
            }
            this.amount = amount;
            return this;
        }
        
        /**
         * Définit la devise (par défaut EUR)
         * @param currency Code devise ISO 4217
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si la devise est invalide
         */
        public Builder currency(String currency) {
            if (currency != null && (currency.length() != 3 || !currency.matches("[A-Z]{3}"))) {
                throw new IllegalArgumentException("Currency must be a valid 3-letter ISO 4217 code");
            }
            this.currency = currency != null ? currency : "EUR";
            return this;
        }
        
        /**
         * Définit la méthode de paiement (requis)
         * @param paymentMethod La méthode de paiement
         * @return Le builder pour chaînage
         */
        public Builder paymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }
        
        /**
         * Définit le statut du paiement (par défaut PENDING)
         * @param status Le statut du paiement
         * @return Le builder pour chaînage
         */
        public Builder status(PaymentStatus status) {
            this.status = status != null ? status : PaymentStatus.PENDING;
            return this;
        }
        
        /**
         * Définit l'ID de transaction (généré automatiquement si non fourni)
         * @param transactionId L'identifiant de transaction
         * @return Le builder pour chaînage
         */
        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }
        
        /**
         * Définit la description du paiement
         * @param description Description du paiement (max 200 caractères)
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si la description dépasse 200 caractères
         */
        public Builder description(String description) {
            if (description != null && description.length() > 200) {
                throw new IllegalArgumentException("Payment description cannot exceed 200 characters");
            }
            this.description = description;
            return this;
        }
        
        /**
         * Définit la raison d'échec du paiement
         * @param failureReason La raison d'échec
         * @return Le builder pour chaînage
         */
        public Builder failureReason(String failureReason) {
            this.failureReason = failureReason;
            return this;
        }
        
        /**
         * Définit le montant remboursé
         * @param refundAmount Le montant remboursé
         * @return Le builder pour chaînage
         * @throws IllegalArgumentException si le montant de remboursement est invalide
         */
        public Builder refundAmount(BigDecimal refundAmount) {
            if (refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Refund amount cannot be negative");
            }
            this.refundAmount = refundAmount;
            return this;
        }
        
        /**
         * Définit la raison du remboursement
         * @param refundReason La raison du remboursement
         * @return Le builder pour chaînage
         */
        public Builder refundReason(String refundReason) {
            this.refundReason = refundReason;
            return this;
        }
        
        /**
         * Définit la date de création (automatique si non fournie)
         * @param createdAt La date de création
         * @return Le builder pour chaînage
         */
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        /**
         * Définit la date de paiement
         * @param paidAt La date de paiement
         * @return Le builder pour chaînage
         */
        public Builder paidAt(LocalDateTime paidAt) {
            this.paidAt = paidAt;
            return this;
        }
        
        /**
         * Définit la date de remboursement
         * @param refundedAt La date de remboursement
         * @return Le builder pour chaînage
         */
        public Builder refundedAt(LocalDateTime refundedAt) {
            this.refundedAt = refundedAt;
            return this;
        }
        
        /**
         * Construit et valide l'instance PaymentDTO
         * Applique la validation métier Context7 et génère les valeurs automatiques
         * 
         * @return Une nouvelle instance PaymentDTO validée
         * @throws IllegalStateException si les champs requis sont manquants ou invalides
         */
        public PaymentDTO build() {
            // Validation des champs requis
            if (bookingId == null) {
                throw new IllegalStateException("Booking ID is required for payment creation");
            }
            if (customerId == null) {
                throw new IllegalStateException("Customer ID is required for payment creation");
            }
            if (amount == null) {
                throw new IllegalStateException("Payment amount is required");
            }
            if (paymentMethod == null) {
                throw new IllegalStateException("Payment method is required");
            }
            
            // Validation métier avancée
            if (refundAmount != null && amount != null && refundAmount.compareTo(amount) > 0) {
                throw new IllegalStateException("Refund amount cannot exceed original payment amount");
            }
            
            // Cohérence des statuts avec les données
            if (status == PaymentStatus.REFUNDED && refundAmount == null) {
                throw new IllegalStateException("Refunded status requires a refund amount");
            }
            if (status == PaymentStatus.FAILED && failureReason == null) {
                throw new IllegalStateException("Failed status requires a failure reason");
            }
            
            // Génération automatique des valeurs par défaut
            if (transactionId == null) {
                transactionId = generateTransactionId();
            }
            if (createdAt == null) {
                createdAt = LocalDateTime.now();
            }
            
            // Cohérence des timestamps
            if (paidAt != null && createdAt != null && paidAt.isBefore(createdAt)) {
                throw new IllegalStateException("Payment date cannot be before creation date");
            }
            if (refundedAt != null && paidAt != null && refundedAt.isBefore(paidAt)) {
                throw new IllegalStateException("Refund date cannot be before payment date");
            }
            
            return new PaymentDTO(
                id, bookingId, customerId, amount, currency, paymentMethod, status,
                transactionId, description, failureReason, refundAmount, refundReason,
                createdAt, paidAt, refundedAt
            );
        }
        
        /**
         * Génère un ID de transaction unique
         * Format : TXN_[timestamp]_[hash]
         * @return Un ID de transaction unique
         */
        private String generateTransactionId() {
            long timestamp = System.currentTimeMillis();
            String hash = Integer.toHexString(Objects.hash(bookingId, customerId, amount, timestamp))
                .toUpperCase();
            return String.format("TXN_%d_%s", timestamp, hash);
        }
    }
}