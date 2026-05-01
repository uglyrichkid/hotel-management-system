package com.project.app.repository.booking;

import com.project.app.entity.booking.BookingRoom;
import com.project.app.entity.booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BookingRoomRepository extends JpaRepository<BookingRoom, Long> {

    @Query("""
            select br
            from BookingRoom br
            join br.booking b
            where br.room.id = :roomId
              and b.active = true
              and b.status in :statuses
              and b.checkInDate < :checkOut
              and b.checkOutDate > :checkIn
            """)
    List<BookingRoom> findOverlappingBookings(
            Long roomId,
            Collection<BookingStatus> statuses,
            LocalDate checkIn,
            LocalDate checkOut
    );

    @Query("""
            select distinct br.room.id
            from BookingRoom br
            join br.booking b
            where br.room.hotel.id = :hotelId
              and b.active = true
              and b.status in :statuses
              and b.checkInDate < :checkOut
              and b.checkOutDate > :checkIn
            """)
    List<Long> findBusyRoomIds(
            Long hotelId,
            Collection<BookingStatus> statuses,
            LocalDate checkIn,
            LocalDate checkOut
    );

    boolean existsByRoom_IdAndBooking_StatusInAndBooking_CheckInDateLessThanAndBooking_CheckOutDateGreaterThanAndBooking_ActiveTrue(
            Long roomId,
            Collection<BookingStatus> statuses,
            LocalDate checkOut,
            LocalDate checkIn
    );
}