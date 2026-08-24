package com.hbms.booking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;

// Booking Service -> Notification Service : "initiate notification event" (Figure 4, step 14-15)
@Component
public class NotificationServiceClient {

    private final WebClient webClient;

    public NotificationServiceClient(WebClient.Builder builder,
                                      @Value("${services.notification.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public void sendConfirmation(String bookingId, String email, String customerName,
                                  String hotelName, String roomType, BigDecimal amountPaid) {
        webClient.post()
                .uri("/api/notifications/confirmation")
                .bodyValue(Map.of(
                        "bookingId", bookingId,
                        "recipientEmail", email,
                        "customerName", customerName,
                        "hotelName", hotelName,
                        "roomType", roomType,
                        "amountPaid", amountPaid
                ))
                .retrieve()
                .toBodilessEntity()
                // Notification failures must not break the booking flow (report section 1 & 10)
                .onErrorComplete()
                .block();
    }

    public void sendCancellation(String bookingId, String email, String customerName, BigDecimal refundAmount) {
        webClient.post()
                .uri("/api/notifications/cancellation")
                .bodyValue(Map.of(
                        "bookingId", bookingId,
                        "recipientEmail", email,
                        "customerName", customerName,
                        "refundAmount", refundAmount
                ))
                .retrieve()
                .toBodilessEntity()
                .onErrorComplete()
                .block();
    }
}
