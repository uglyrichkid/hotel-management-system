package com.project.app.repository.payment;

import com.project.app.entity.payment.Payment;
import com.project.app.entity.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBookingIdAndActiveTrueOrderByCreatedAtDesc(Long bookingId);

    List<Payment> findByBookingIdAndPaymentStatusAndActiveTrue(Long bookingId, PaymentStatus paymentStatus);

    List<Payment> findByBooking_Hotel_IdAndActiveTrueOrderByCreatedAtDesc(Long hotelId);

    @Query("""
            select coalesce(sum(p.amount), 0)
            from Payment p
            where p.booking.id = :bookingId
              and p.paymentStatus = com.project.app.entity.payment.PaymentStatus.COMPLETED
              and p.active = true
            """)
    BigDecimal getTotalCompletedAmountByBookingId(Long bookingId);
}