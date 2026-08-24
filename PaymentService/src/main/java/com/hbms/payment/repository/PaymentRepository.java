package com.hbms.payment.repository;

import com.hbms.payment.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByBookingId(String bookingId);
    List<Payment> findAllByBookingId(String bookingId);
}
