package com.hbms.payment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Maps to Table 1 - Payment Service: Process Payment(), Refund Payment()
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    private String bookingId;
    private BigDecimal amount;

    // SUCCESS | FAILED | REFUNDED
    private PaymentStatus status;

    private String maskedCardNumber; // only last 4 digits stored, never full card data
    private String transactionReference;

    private BigDecimal refundedAmount = BigDecimal.ZERO;

    private LocalDateTime transactionDate = LocalDateTime.now();
    private LocalDateTime refundDate;

    public enum PaymentStatus {
        SUCCESS,
        FAILED,
        REFUNDED
    }
}
