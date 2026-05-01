package com.project.app.entity.amenity;

import com.project.app.entity.hotel.Hotel;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "hotel_amenities")
@IdClass(HotelAmenity.HotelAmenityId.class)
public class HotelAmenity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "amenity_id", nullable = false)
    private Amenity amenity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public HotelAmenity() {}

    public HotelAmenity(Hotel hotel, Amenity amenity) {
        this.hotel = hotel;
        this.amenity = amenity;
    }

    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public Amenity getAmenity() { return amenity; }
    public void setAmenity(Amenity amenity) { this.amenity = amenity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // --- composite key class ---
    public static class HotelAmenityId implements Serializable {
        private Long hotel;
        private Long amenity;

        public HotelAmenityId() {}

        public HotelAmenityId(Long hotel, Long amenity) {
            this.hotel = hotel;
            this.amenity = amenity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof HotelAmenityId)) return false;
            HotelAmenityId that = (HotelAmenityId) o;
            return Objects.equals(hotel, that.hotel) && Objects.equals(amenity, that.amenity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hotel, amenity);
        }
    }
}
