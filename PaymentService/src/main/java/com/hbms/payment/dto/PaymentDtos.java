package com.hbms.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class PaymentDtos {

    // Input: Booking ID, Amount -> Process Payment() -> Output: Payment Success
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessPaymentRequest {
        private String bookingId;
        private BigDecimal amount;
        private String cardNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessPaymentResponse {
        private String paymentId;
        private String status; // "SUCCESS" | "FAILED"  -> "Payment Success"
        private String message;
    }

    // Input: Booking ID -> Refund Payment() -> Output: Refund Status
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundResponse {
        private String refundStatus;
        private BigDecimal refundAmount;
    }
}
