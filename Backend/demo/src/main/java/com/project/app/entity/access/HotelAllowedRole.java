package com.project.app.entity.access;


import com.project.app.entity.hotel.Hotel;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "hotel_allowed_roles")
@IdClass(HotelAllowedRole.HotelAllowedRoleId.class)
public class HotelAllowedRole {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public HotelAllowedRole() {}

    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // --- composite key ---
    public static class HotelAllowedRoleId implements Serializable {
        private Long hotel;
        private Long role;

        public HotelAllowedRoleId() {}

        public HotelAllowedRoleId(Long hotel, Long role) {
            this.hotel = hotel;
            this.role = role;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof HotelAllowedRoleId)) return false;
            HotelAllowedRoleId that = (HotelAllowedRoleId) o;
            return Objects.equals(hotel, that.hotel)
                    && Objects.equals(role, that.role);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hotel, role);
        }
    }
}