package com.jb.afrostyle.payment.repository;

import com.jb.afrostyle.payment.domain.enums.PaymentStatus;
import com.jb.afrostyle.payment.domain.enums.PaymentMethod;
import com.jb.afrostyle.payment.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Trouve tous les paiements d'un client
     */
    List<Payment> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    /**
     * Trouve tous les paiements
     */
    List<Payment> findAllByOrderByCreatedAtDesc();

    /**
     * Trouve tous les paiements d'une réservation
     */
    List<Payment> findByBookingIdOrderByCreatedAtDesc(Long bookingId);


    /**
     * Trouve un paiement par son transaction ID
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Trouve un paiement par son Stripe Session ID
     */
    Optional<Payment> findByStripeSessionId(String stripeSessionId);

    /**
     * Trouve tous les paiements avec un statut donné
     */
    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);


    /**
     * Trouve tous les paiements dans une période donnée
     */
    List<Payment> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);


    /**
     * Calcule le total des paiements réussis
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'SUCCEEDED'")
    BigDecimal getTotalEarnings();

    /**
     * Calcule le total des remboursements
     */
    @Query("SELECT COALESCE(SUM(p.refundAmount), 0) FROM Payment p WHERE p.refundAmount > 0")
    BigDecimal getTotalRefunds();


    /**
     * Trouve les paiements en attente de confirmation
     */
    @Query("SELECT p FROM Payment p WHERE p.status IN ('PENDING', 'REQUIRES_CONFIRMATION') AND p.createdAt < :cutoffTime")
    List<Payment> findStalePayments(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Vérifie s'il existe déjà un paiement réussi pour une réservation
     */
    boolean existsByBookingIdAndStatus(Long bookingId, PaymentStatus status);

    /**
     * Compte le nombre de paiements d'un client après une date donnée
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.customerId = :customerId AND p.createdAt > :date")
    Long countByCustomerIdAndCreatedAtAfter(@Param("customerId") Long customerId, @Param("date") LocalDateTime date);

    /**
     * Trouve un paiement par son Stripe Charge ID
     */
    Optional<Payment> findByStripeChargeId(String stripeChargeId);

    /**
     * Trouve un paiement par son Stripe PaymentIntent ID
     */
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    /**
     * Compte le nombre de paiements par statut
     */
    Long countByStatus(PaymentStatus status);

    /**
     * Trouve tous les paiements par méthode de paiement
     */
    List<Payment> findByPaymentMethodOrderByCreatedAtDesc(PaymentMethod paymentMethod);

    /**
     * Trouve tous les paiements d'un client avec un statut donné
     */
    List<Payment> findByCustomerIdAndStatusOrderByCreatedAtDesc(Long customerId, PaymentStatus status);
}