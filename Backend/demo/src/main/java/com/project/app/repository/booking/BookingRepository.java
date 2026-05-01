package com.project.app.repository.booking;

import com.project.app.entity.booking.Booking;
import com.project.app.entity.booking.BookingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {
            "hotel",
            "guest",
            "rooms",
            "rooms.room"
    })
    List<Booking> findAllByHotel_IdAndActiveTrueOrderByCreatedAtDesc(Long hotelId);

    @EntityGraph(attributePaths = {
            "hotel",
            "guest",
            "rooms",
            "rooms.room"
    })
    Optional<Booking> findByIdAndHotel_IdAndActiveTrue(Long bookingId, Long hotelId);

    @EntityGraph(attributePaths = {
            "hotel",
            "guest",
            "rooms",
            "rooms.room"
    })
    List<Booking> findAllByHotel_IdAndActiveTrueAndStatusOrderByCreatedAtDesc(
            Long hotelId,
            BookingStatus status
    );

    @EntityGraph(attributePaths = {
            "hotel",
            "guest",
            "rooms",
            "rooms.room"
    })
    List<Booking> findAllByHotel_IdAndActiveTrueAndGuest_FirstNameContainingIgnoreCaseOrderByCreatedAtDesc(
            Long hotelId,
            String guestFirstName
    );

    @EntityGraph(attributePaths = {
            "hotel",
            "guest",
            "rooms",
            "rooms.room"
    })
    List<Booking> findAllByHotel_IdAndActiveTrueAndGuest_LastNameContainingIgnoreCaseOrderByCreatedAtDesc(
            Long hotelId,
            String guestLastName
    );

    @EntityGraph(attributePaths = {
            "hotel",
            "guest",
            "rooms",
            "rooms.room"
    })
    List<Booking> findAllByHotel_IdAndActiveTrueAndGuest_EmailContainingIgnoreCaseOrderByCreatedAtDesc(
            Long hotelId,
            String guestEmail
    );

    @EntityGraph(attributePaths = {
            "hotel",
            "guest",
            "rooms",
            "rooms.room"
    })
    List<Booking> findAllByHotel_IdAndActiveTrueAndGuest_PhoneContainingIgnoreCaseOrderByCreatedAtDesc(
            Long hotelId,
            String guestPhone
    );

    @EntityGraph(attributePaths = {
            "hotel",
            "guest",
            "rooms",
            "rooms.room"
    })
    List<Booking> findAllByHotel_IdAndActiveTrueAndStatusAndGuest_FirstNameContainingIgnoreCaseOrderByCreatedAtDesc(
            Long hotelId,
            BookingStatus status,
            String guestFirstName
    );

    @EntityGraph(attributePaths = {
            "hotel",
            "guest",
            "rooms",
            "rooms.room"
    })
    List<Booking> findAllByHotel_IdAndActiveTrueAndStatusAndGuest_LastNameContainingIgnoreCaseOrderByCreatedAtDesc(
            Long hotelId,
            BookingStatus status,
            String guestLastName
    );

    @EntityGraph(attributePaths = {
            "hotel",
            "guest",
            "rooms",
            "rooms.room"
    })
    List<Booking> findAllByHotel_IdAndActiveTrueAndStatusAndGuest_EmailContainingIgnoreCaseOrderByCreatedAtDesc(
            Long hotelId,
            BookingStatus status,
            String guestEmail
    );

    @EntityGraph(attributePaths = {
            "hotel",
            "guest",
            "rooms",
            "rooms.room"
    })
    List<Booking> findAllByHotel_IdAndActiveTrueAndStatusAndGuest_PhoneContainingIgnoreCaseOrderByCreatedAtDesc(
            Long hotelId,
            BookingStatus status,
            String guestPhone
    );
}