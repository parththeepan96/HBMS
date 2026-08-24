package com.hbms.booking.client;

import com.hbms.booking.dto.BookingDtos.ValidateUserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

// Booking Service -> User Service : "Validate user" (Figure 4, step 7-8)
@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder builder, @Value("${services.user.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public ValidateUserResponse validateUser(String userId) {
        return webClient.get()
                .uri("/api/users/{userId}/validate", userId)
                .retrieve()
                .bodyToMono(ValidateUserResponse.class)
                .onErrorReturn(new ValidateUserResponse(false, userId, "", ""))
                .block();
    }
}
