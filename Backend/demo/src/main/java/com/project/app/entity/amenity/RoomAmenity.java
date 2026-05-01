package com.project.app.entity.amenity;

import com.project.app.entity.room.Room;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "room_amenities")
@IdClass(RoomAmenity.RoomAmenityId.class)
public class RoomAmenity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "amenity_id", nullable = false)
    private Amenity amenity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public RoomAmenity() {}

    public RoomAmenity(Room room, Amenity amenity) {
        this.room = room;
        this.amenity = amenity;
    }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public Amenity getAmenity() { return amenity; }
    public void setAmenity(Amenity amenity) { this.amenity = amenity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // --- composite key class ---
    public static class RoomAmenityId implements Serializable {
        private Long room;
        private Long amenity;

        public RoomAmenityId() {}

        public RoomAmenityId(Long room, Long amenity) {
            this.room = room;
            this.amenity = amenity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RoomAmenityId)) return false;
            RoomAmenityId that = (RoomAmenityId) o;
            return Objects.equals(room, that.room) && Objects.equals(amenity, that.amenity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(room, amenity);
        }
    }
}