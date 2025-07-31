package com.jb.afrostyle.payment.service;

import com.jb.afrostyle.payment.domain.enums.PaymentStatus;
import com.jb.afrostyle.payment.domain.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment statistics data class
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStats {

    /**
     * Total number of payments
     */
    private Long totalPayments;

    /**
     * Total number of successful payments
     */
    private Long successfulPayments;

    /**
     * Total number of pending payments
     */
    private Long pendingPayments;

    /**
     * Total number of failed payments
     */
    private Long failedPayments;

    /**
     * Total number of cancelled payments
     */
    private Long cancelledPayments;

    /**
     * Total number of refunded payments
     */
    private Long refundedPayments;

    /**
     * Total revenue amount
     */
    private BigDecimal totalRevenue;

    /**
     * Total refund amount
     */
    private BigDecimal totalRefunds;

    /**
     * Net revenue (total revenue - total refunds)
     */
    private BigDecimal netRevenue;

    /**
     * Average payment amount
     */
    private BigDecimal averagePaymentAmount;

    /**
     * Success rate (percentage of successful payments)
     */
    private Double successRate;

    /**
     * Refund rate (percentage of refunded payments)
     */
    private Double refundRate;

    /**
     * Statistics calculation period start
     */
    private LocalDateTime periodStart;

    /**
     * Statistics calculation period end
     */
    private LocalDateTime periodEnd;

    /**
     * Last updated timestamp
     */
    private LocalDateTime lastUpdated;

    /**
     * Calculate derived statistics
     */
    public void calculateDerivedStats() {
        // Calculate net revenue
        if (totalRevenue != null && totalRefunds != null) {
            netRevenue = totalRevenue.subtract(totalRefunds);
        }

        // Calculate success rate
        if (totalPayments != null && totalPayments > 0 && successfulPayments != null) {
            successRate = (successfulPayments.doubleValue() / totalPayments.doubleValue()) * 100;
        }

        // Calculate refund rate
        if (totalPayments != null && totalPayments > 0 && refundedPayments != null) {
            refundRate = (refundedPayments.doubleValue() / totalPayments.doubleValue()) * 100;
        }

        // Calculate average payment amount
        if (totalPayments != null && totalPayments > 0 && totalRevenue != null) {
            averagePaymentAmount = totalRevenue.divide(new BigDecimal(totalPayments), 2, java.math.RoundingMode.HALF_UP);
        }

        // Set last updated timestamp
        lastUpdated = LocalDateTime.now();
    }

    /**
     * Create PaymentStats from a list of payments
     * @param payments List of payments
     * @return PaymentStats
     */
    public static PaymentStats fromPayments(List<Payment> payments) {
        PaymentStats stats = new PaymentStats();
        stats.totalPayments = (long) payments.size();
        stats.successfulPayments = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCEEDED)
                .count();
        stats.pendingPayments = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .count();
        stats.failedPayments = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.FAILED)
                .count();
        stats.cancelledPayments = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.CANCELLED)
                .count();
        stats.refundedPayments = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.REFUNDED)
                .count();
        
        stats.totalRevenue = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCEEDED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        stats.totalRefunds = payments.stream()
                .filter(p -> p.getRefundAmount() != null)
                .map(Payment::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        stats.calculateDerivedStats();
        return stats;
    }
}