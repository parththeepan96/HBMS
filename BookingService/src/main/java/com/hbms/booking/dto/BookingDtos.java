package com.hbms.booking.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.hbms.booking.model.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingDtos {

    // Input: User ID, Room ID -> Create Booking() -> Output: Booking ID
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBookingRequest {
        private String userId;
        private String hotelId;
        private String roomId;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private int adults;
        private int children;
        private String specialRequests;
        private String cardNumber; // passed through to Payment Service, never stored here
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBookingResponse {
        private String bookingId;
        private Booking.BookingStatus status;
        private BigDecimal totalAmount;
        private String message;
    }

    // Input: Booking ID -> Cancel Booking() -> Output: Success
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelBookingResponse {
        private String message; // "Success"
        private BigDecimal refundAmount;
        private String refundStatus; // "Refunded" | "Not eligible for refund" | "FAILED" | "No payment on record"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidateUserResponse {
        // UserService (C#) serializes its "IsValid" property as "isValid" (ASP.NET Core's
        // default camelCase JSON policy), while Lombok maps this Java field to the JSON key
        // "valid". Without the alias, Jackson silently ignores the incoming "isValid" key and
        // leaves this at its default (false) for every user, causing every booking to be
        // rejected with "User validation failed." regardless of the actual user.
        @JsonAlias("isValid")
        private boolean valid;
        private String userId;
        private String name;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailabilityResponse {
        private boolean available;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomPriceResponse {
        private BigDecimal pricePerNight;
    }

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
        private String status; // "SUCCESS" | "FAILED"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundResponse {
        private String refundStatus;
        private BigDecimal refundAmount;
    }
}