package com.hbms.payment.controller;

import com.hbms.payment.dto.PaymentDtos.ProcessPaymentRequest;
import com.hbms.payment.dto.PaymentDtos.ProcessPaymentResponse;
import com.hbms.payment.dto.PaymentDtos.RefundResponse;
import com.hbms.payment.model.Payment;
import com.hbms.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // POST api/payments/process -> Process Payment() -> Output: Payment Success (BR4)
    @PostMapping("/process")
    public ResponseEntity<ProcessPaymentResponse> processPayment(@RequestBody ProcessPaymentRequest request) {
        ProcessPaymentResponse response = paymentService.processPayment(request);
        HttpStatus status = "SUCCESS".equals(response.getStatus()) ? HttpStatus.OK : HttpStatus.PAYMENT_REQUIRED;
        return ResponseEntity.status(status).body(response);
    }

    // POST api/payments/{paymentId}/refund -> Refund Payment() -> Output: Refund Status
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<?> refund(@PathVariable String paymentId) {
        try {
            RefundResponse response = paymentService.refundPayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }

    // GET api/payments/booking/{bookingId}
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getByBooking(@PathVariable String bookingId) {
        try {
            Payment payment = paymentService.getByBookingId(bookingId);
            return ResponseEntity.ok(payment);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }
}
