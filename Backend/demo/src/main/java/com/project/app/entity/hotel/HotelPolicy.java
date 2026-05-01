package com.project.app.entity.hotel;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "hotel_policies",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_hotel_policy_type", columnNames = {"hotel_id","policy_type"})
        }
)
public class HotelPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(name = "policy_type", nullable = false, length = 30)
    private String policyType;
    @Column(name = "text", nullable = false, columnDefinition = "text")
    private String text;
    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at",nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public HotelPolicy(){}

    // -------- Getter & Setter----------


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }
}
