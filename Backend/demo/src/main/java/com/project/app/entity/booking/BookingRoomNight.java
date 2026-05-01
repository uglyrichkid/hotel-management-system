package com.project.app.entity.booking;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "booking_room_nights",
        uniqueConstraints = @UniqueConstraint(columnNames = {"booking_room_id", "night_date"}),
        indexes = {
                @Index(name = "idx_booking_room_nights_br", columnList = "booking_room_id"),
                @Index(name = "idx_booking_room_nights_date", columnList = "night_date")
        }
)
public class BookingRoomNight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_room_id", nullable = false)
    private BookingRoom bookingRoom;

    /**
     * Дата ночи (например 2026-06-10 означает ночь 10->11)
     */
    @Column(name = "night_date", nullable = false)
    private LocalDate nightDate;

    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public BookingRoomNight() {}

    public Long getId() {
        return id;
    }

    public BookingRoom getBookingRoom() {
        return bookingRoom;
    }

    public void setBookingRoom(BookingRoom bookingRoom) {
        this.bookingRoom = bookingRoom;
    }

    public LocalDate getNightDate() {
        return nightDate;
    }

    public void setNightDate(LocalDate nightDate) {
        this.nightDate = nightDate;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
