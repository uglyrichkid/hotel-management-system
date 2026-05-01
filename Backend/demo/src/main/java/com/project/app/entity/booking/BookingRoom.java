package com.project.app.entity.booking;


import com.project.app.entity.room.Room;
import jakarta.persistence.*;


@Entity
@Table(
        name = "booking_rooms",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_booking_room_unique",
                        columnNames = {"booking_id", "room_id"}
                )
        }
)
public class BookingRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // Какая комната забронирована
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}


