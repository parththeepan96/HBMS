package com.hbms.booking.service;

import com.hbms.booking.client.HotelServiceClient;
import com.hbms.booking.client.NotificationServiceClient;
import com.hbms.booking.client.PaymentServiceClient;
import com.hbms.booking.client.UserServiceClient;
import com.hbms.booking.dto.BookingDtos.*;
import com.hbms.booking.model.Booking;
import com.hbms.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

// Implements the workflow described in Figure 3 (Workflow diagram) and Figure 4 (Sequence diagram):
// validate user -> check room availability -> process payment -> confirm booking + notify
// and the reverse path for cancellation: update status -> refund -> notify.
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserServiceClient userServiceClient;
    private final HotelServiceClient hotelServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public BookingService(BookingRepository bookingRepository,
                           UserServiceClient userServiceClient,
                           HotelServiceClient hotelServiceClient,
                           PaymentServiceClient paymentServiceClient,
                           NotificationServiceClient notificationServiceClient) {
        this.bookingRepository = bookingRepository;
        this.userServiceClient = userServiceClient;
        this.hotelServiceClient = hotelServiceClient;
        this.paymentServiceClient = paymentServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    // Create Booking() - Input: User ID, Room ID -> Output: Booking ID  (BR3, Figure 3 & 4)
    public CreateBookingResponse createBooking(CreateBookingRequest request) {

        // Step 1: validate user (Figure 4, step 7-8)
        ValidateUserResponse user = userServiceClient.validateUser(request.getUserId());
        if (user == null || !user.isValid()) {
            return new CreateBookingResponse(null, Booking.BookingStatus.REJECTED,
                    BigDecimal.ZERO, "User validation failed.");
        }

        // Step 2: check room availability (Figure 3: "Room Available?")
        boolean available = hotelServiceClient.checkAvailability(request.getHotelId(), request.getRoomId());
        if (!available) {
            return new CreateBookingResponse(null, Booking.BookingStatus.REJECTED,
                    BigDecimal.ZERO, "Room is not available.");
        }

        // Calculate total based on duration (per Table 1 pricing behaviour)
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        if (nights <= 0) nights = 1;
        BigDecimal pricePerNight = hotelServiceClient.getRoomPrice(request.getHotelId(), request.getRoomId());
        BigDecimal totalAmount = pricePerNight.multiply(BigDecimal.valueOf(nights));

        // Create booking in PENDING state (Figure 4, step 6: "Create Booking request (pending status)")
        Booking booking = new Booking();
        booking.setUserId(request.getUserId());
        booking.setHotelId(request.getHotelId());
        booking.setRoomId(request.getRoomId());
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setAdults(request.getAdults() > 0 ? request.getAdults() : 1);
        booking.setChildren(Math.max(request.getChildren(), 0));
        booking.setSpecialRequests(request.getSpecialRequests());
        booking.setTotalAmount(totalAmount);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking = bookingRepository.save(booking);

        // Step 3: reserve the room (Figure 4, step 9-10: "Reserve a room" / "Room Reserved")
        hotelServiceClient.updateRoomAvailability(request.getHotelId(), request.getRoomId(), false);

        // Step 4: process payment (Figure 4, step 11-12 / Figure 3: "Process payment")
        ProcessPaymentResponse payment = paymentServiceClient.processPayment(
                new ProcessPaymentRequest(booking.getId(), totalAmount, request.getCardNumber()));

        if (payment == null || !"SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            // Payment failed -> "Booking Rejected" branch in Figure 3, release the room again
            booking.setStatus(Booking.BookingStatus.REJECTED);
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);
            hotelServiceClient.updateRoomAvailability(request.getHotelId(), request.getRoomId(), true);

            return new CreateBookingResponse(booking.getId(), Booking.BookingStatus.REJECTED,
                    totalAmount, "Payment failed. Booking rejected.");
        }

        // Step 5: confirm booking (Figure 4, step 13: "Confirmed Room")
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentId(payment.getPaymentId());
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // Step 6: notify (Figure 4, step 14-15: "initiate notification event" / "Send confirmation email")
        notificationServiceClient.sendConfirmation(
                booking.getId(), user.getEmail(), user.getName(),
                request.getHotelId(), request.getRoomId(), totalAmount);

        return new CreateBookingResponse(booking.getId(), Booking.BookingStatus.CONFIRMED,
                totalAmount, "Booking confirmed successfully.");
    }

    // Cancel Booking() - Input: Booking ID -> Output: Success  (BR3, Figure 3 step 6)
    public CancelBookingResponse cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found: " + bookingId));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            return new CancelBookingResponse("Booking is already cancelled.", BigDecimal.ZERO, null);
        }

        // Step 1: update booking status (Figure 3: "first booking service updates the booking status")
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // Step 2: release the room back to availability
        hotelServiceClient.updateRoomAvailability(booking.getHotelId(), booking.getRoomId(), true);

        // Step 3: refund if a payment was taken
        BigDecimal refundAmount = BigDecimal.ZERO;
        String refundStatus = "No payment on record";
        if (booking.getPaymentId() != null) {
            RefundResponse refund = paymentServiceClient.refund(booking.getId(), booking.getPaymentId());
            if (refund != null) {
                refundAmount = refund.getRefundAmount();
                refundStatus = refund.getRefundStatus();
            } else {
                refundStatus = "FAILED";
            }
        }

        // Step 4: notify customer of cancellation
        ValidateUserResponse user = userServiceClient.validateUser(booking.getUserId());
        if (user != null && user.isValid()) {
            notificationServiceClient.sendCancellation(booking.getId(), user.getEmail(), user.getName(), refundAmount);
        }

        String message = "Refunded".equalsIgnoreCase(refundStatus)
                ? "Success"
                : "Booking cancelled, but refund did not complete: " + refundStatus;

        return new CancelBookingResponse(message, refundAmount, refundStatus);
    }

    // Booking History() - Input: User ID -> Output: Booking List
    public List<Booking> getBookingHistory(String userId) {
        return bookingRepository.findByUserId(userId);
    }

    public Booking getBookingById(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found: " + bookingId));
    }
}
