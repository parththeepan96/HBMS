package com.hbms.booking.controller;

import com.hbms.booking.dto.BookingDtos.*;
import com.hbms.booking.model.Booking;
import com.hbms.booking.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // POST api/bookings -> Create Booking() -> Output: Booking ID (BR3)
    @PostMapping
    public ResponseEntity<CreateBookingResponse> createBooking(@RequestBody CreateBookingRequest request) {
        CreateBookingResponse response = bookingService.createBooking(request);
        if (response.getBookingId() == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // DELETE api/bookings/{bookingId} -> Cancel Booking() -> Output: Success (BR3)
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable String bookingId) {
        try {
            CancelBookingResponse response = bookingService.cancelBooking(bookingId);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }

    // GET api/bookings/user/{userId} -> Booking History() -> Output: Booking List
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingHistory(@PathVariable String userId) {
        return ResponseEntity.ok(bookingService.getBookingHistory(userId));
    }

    // GET api/bookings/{bookingId}
    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBooking(@PathVariable String bookingId) {
        try {
            return ResponseEntity.ok(bookingService.getBookingById(bookingId));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }
}
