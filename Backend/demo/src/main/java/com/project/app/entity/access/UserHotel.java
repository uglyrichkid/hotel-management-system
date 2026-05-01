package com.project.app.entity.access;


import com.project.app.entity.hotel.Hotel;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_hotels")
@IdClass(UserHotel.UserHotelId.class)
public class UserHotel {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id")
    private User assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    public UserHotel() {}

    // --- getters/setters ---

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public User getAssignedBy() { return assignedBy; }
    public void setAssignedBy(User assignedBy) { this.assignedBy = assignedBy; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    // --- composite key ---
    public static class UserHotelId implements Serializable {
        private Long user;
        private Long hotel;

        public UserHotelId() {}

        public UserHotelId(Long user, Long hotel) {
            this.user = user;
            this.hotel = hotel;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserHotelId)) return false;
            UserHotelId that = (UserHotelId) o;
            return Objects.equals(user, that.user)
                    && Objects.equals(hotel, that.hotel);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, hotel);
        }
    }
}
