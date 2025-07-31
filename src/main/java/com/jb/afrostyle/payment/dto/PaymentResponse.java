package com.jb.afrostyle.payment.dto;

import com.jb.afrostyle.payment.domain.enums.PaymentStatus;
import java.math.BigDecimal;

public record PaymentResponse(
        Long id,
        Long paymentId,
        String transactionId,
        PaymentStatus status,
        String clientSecret,
        String paymentUrl,
        String message,
        BigDecimal amount,
        String currency
) {
    public static PaymentResponse success(Long paymentId, String transactionId, String clientSecret) {
        return new PaymentResponse(paymentId, paymentId, transactionId, PaymentStatus.PENDING, clientSecret, null, "Payment created successfully", null, null);
    }
    
    public static PaymentResponse success(Long paymentId, String transactionId, String clientSecret, BigDecimal amount, String currency) {
        return new PaymentResponse(paymentId, paymentId, transactionId, PaymentStatus.PENDING, clientSecret, null, "Payment created successfully", amount, currency);
    }

    public static PaymentResponse error(String message) {
        return new PaymentResponse(null, null, null, PaymentStatus.FAILED, null, null, message, null, null);
    }
}