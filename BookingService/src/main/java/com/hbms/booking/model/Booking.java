package com.hbms.booking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Maps to Table 1 - Booking Service: Create Booking(), Cancel Booking(), Booking History()
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bookings")
public class Booking {

    @Id
    private String id;

    private String userId;
    private String hotelId;
    private String roomId;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private int adults;
    private int children;
    private String specialRequests;

    private BigDecimal totalAmount;

    // PENDING -> CONFIRMED -> CANCELLED (see Figure 3 workflow diagram)
    private BookingStatus status;

    private String paymentId;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum BookingStatus {
        PENDING,
        CONFIRMED,
        REJECTED,
        CANCELLED
    }
}
