package com.hbms.booking.client;

import com.hbms.booking.dto.BookingDtos.ProcessPaymentRequest;
import com.hbms.booking.dto.BookingDtos.ProcessPaymentResponse;
import com.hbms.booking.dto.BookingDtos.RefundResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

// Booking Service -> Payment Service : "Initiate payment" / "Process Refund" (Figure 4 step 11-12, Figure 3)
@Component
public class PaymentServiceClient {

    private final WebClient webClient;

    public PaymentServiceClient(WebClient.Builder builder, @Value("${services.payment.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public ProcessPaymentResponse processPayment(ProcessPaymentRequest request) {
        return webClient.post()
                .uri("/api/payments/process")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ProcessPaymentResponse.class)
                .onErrorReturn(new ProcessPaymentResponse(null, "FAILED"))
                .block();
    }

    public RefundResponse refund(String bookingId, String paymentId) {
        return webClient.post()
                .uri("/api/payments/{paymentId}/refund", paymentId)
                .retrieve()
                .bodyToMono(RefundResponse.class)
                .onErrorReturn(new RefundResponse("FAILED", java.math.BigDecimal.ZERO))
                .block();
    }
}
