package com.hbms.payment.service;

import com.hbms.payment.dto.PaymentDtos.ProcessPaymentRequest;
import com.hbms.payment.dto.PaymentDtos.ProcessPaymentResponse;
import com.hbms.payment.dto.PaymentDtos.RefundResponse;
import com.hbms.payment.model.Payment;
import com.hbms.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

// Called by Booking Service (Figure 4, step 11-12: "Initiate payment" / "Confirmed Payment")
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // Process Payment() - Input: Booking ID, Amount -> Output: Payment Success (BR4)
    public ProcessPaymentResponse processPayment(ProcessPaymentRequest request) {
        Payment payment = new Payment();
        payment.setBookingId(request.getBookingId());
        payment.setAmount(request.getAmount());
        payment.setMaskedCardNumber(maskCard(request.getCardNumber()));
        payment.setTransactionReference("TXN-" + UUID.randomUUID());
        payment.setTransactionDate(LocalDateTime.now());

        // Simulated gateway call: in this design, a well-formed card number and a
        // positive amount always succeed. A real integration (Stripe/PayPal/etc.)
        // would replace this block without changing the service's public contract.
        boolean success = request.getAmount() != null
                && request.getAmount().compareTo(BigDecimal.ZERO) > 0
                && request.getCardNumber() != null
                && request.getCardNumber().replaceAll("\\s", "").length() >= 12;

        payment.setStatus(success ? Payment.PaymentStatus.SUCCESS : Payment.PaymentStatus.FAILED);
        payment = paymentRepository.save(payment);

        return new ProcessPaymentResponse(
                payment.getId(),
                payment.getStatus().name(),
                success ? "Payment processed successfully." : "Payment declined."
        );
    }

    // Refund Payment() - Input: Booking ID / Payment ID -> Output: Refund Status (BR3 cancellation flow)
    public RefundResponse refundPayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoSuchElementException("Payment not found: " + paymentId));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            return new RefundResponse("Not eligible for refund", BigDecimal.ZERO);
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setRefundedAmount(payment.getAmount());
        payment.setRefundDate(LocalDateTime.now());
        paymentRepository.save(payment);

        return new RefundResponse("Refunded", payment.getRefundedAmount());
    }

    public Payment getByBookingId(String bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new NoSuchElementException("No payment found for booking: " + bookingId));
    }

    private String maskCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
