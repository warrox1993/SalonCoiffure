package com.jb.afrostyle.payment.service;

import com.jb.afrostyle.payment.domain.entity.Payment;
import com.jb.afrostyle.payment.dto.PaymentRequest;
import com.jb.afrostyle.payment.dto.PaymentResponse;
import com.jb.afrostyle.payment.dto.RefundRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for payment operations
 */
public interface PaymentService {

    /**
     * Create a new payment
     * @param request Payment request details
     * @param customerId Customer ID
     * @return Payment response
     * @throws Exception if payment creation fails
     */
    PaymentResponse createPayment(PaymentRequest request, Long customerId) throws Exception;

    /**
     * Confirm a payment
     * @param sessionId Stripe Checkout Session ID
     * @return Updated payment
     * @throws Exception if confirmation fails
     */
    Payment confirmPayment(String sessionId) throws Exception;

    /**
     * Process Stripe webhook
     * @param payload Webhook payload
     * @param signature Webhook signature
     * @throws Exception if webhook processing fails
     */
    void processStripeWebhook(String payload, String signature) throws Exception;

    /**
     * Get payment by ID
     * @param paymentId Payment ID
     * @return Payment entity
     * @throws Exception if payment not found
     */
    Payment getPaymentById(Long paymentId) throws Exception;

    /**
     * Get payments by customer ID
     * @param customerId Customer ID
     * @return List of payments
     */
    List<Payment> getPaymentsByCustomer(Long customerId);

    /**
     * Get all payments
     * @return List of payments
     */
    List<Payment> getAllPayments();

    /**
     * Get payments by booking ID
     * @param bookingId Booking ID
     * @return List of payments
     */
    List<Payment> getPaymentsByBooking(Long bookingId);

    /**
     * Process a refund
     * @param paymentId Payment ID
     * @param refundRequest Refund request details
     * @return Updated payment
     * @throws Exception if refund fails
     */
    Payment refundPayment(Long paymentId, RefundRequest refundRequest) throws Exception;

    /**
     * Cancel a payment
     * @param paymentId Payment ID
     * @return Updated payment
     * @throws Exception if cancellation fails
     */
    Payment cancelPayment(Long paymentId) throws Exception;

    /**
     * Get total earnings
     * @return Total earnings
     */
    BigDecimal getTotalEarnings();

    /**
     * Get total refunds
     * @return Total refunds
     */
    BigDecimal getTotalRefunds();

    /**
     * Get payment statistics by date range
     * @param startDate Start date
     * @param endDate End date
     * @return Payment statistics
     */
    PaymentStats getPaymentStats(LocalDateTime startDate, LocalDateTime endDate);
}