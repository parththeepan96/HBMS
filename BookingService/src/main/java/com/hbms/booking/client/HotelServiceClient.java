package com.hbms.booking.client;

import com.hbms.booking.dto.BookingDtos.AvailabilityResponse;
import com.hbms.booking.dto.BookingDtos.RoomPriceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;

// Booking Service -> Hotel Service : "check room availability" / "Reserve a room" (Figure 4, steps 3, 9-10)
@Component
public class HotelServiceClient {

    private final WebClient webClient;

    public HotelServiceClient(WebClient.Builder builder, @Value("${services.hotel.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public boolean checkAvailability(String hotelId, String roomId) {
        AvailabilityResponse response = webClient.get()
                .uri("/api/hotels/{hotelId}/rooms/{roomId}/availability", hotelId, roomId)
                .retrieve()
                .bodyToMono(AvailabilityResponse.class)
                .onErrorReturn(new AvailabilityResponse(false))
                .block();
        return response != null && response.isAvailable();
    }

    public BigDecimal getRoomPrice(String hotelId, String roomId) {
        RoomPriceResponse response = webClient.get()
                .uri("/api/hotels/{hotelId}/rooms/{roomId}/price", hotelId, roomId)
                .retrieve()
                .bodyToMono(RoomPriceResponse.class)
                .onErrorReturn(new RoomPriceResponse(BigDecimal.ZERO))
                .block();
        return response != null ? response.getPricePerNight() : BigDecimal.ZERO;
    }

    // Marks room unavailable (reserve) or available again (release on cancellation)
    public void updateRoomAvailability(String hotelId, String roomId, boolean isAvailable) {
        webClient.put()
                .uri("/api/hotels/{hotelId}/rooms/{roomId}/availability", hotelId, roomId)
                .bodyValue(Map.of("isAvailable", isAvailable))
                .retrieve()
                .toBodilessEntity()
                .onErrorComplete()
                .block();
    }
}
