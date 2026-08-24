package com.hbms.booking.repository;

import com.hbms.booking.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {

    // Booking History() - Input: User ID -> Output: Booking List
    List<Booking> findByUserId(String userId);
}
