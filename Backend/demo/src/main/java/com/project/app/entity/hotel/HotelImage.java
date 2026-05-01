package com.project.app.entity.hotel;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "hotel_images",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_hotel_images_hotel_url", columnNames = {"hotel_id", "url"})
        }
)
public class HotelImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Many images belong to one hotel


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;


    // ----- Main Fields -----
    @Column(name = "url", nullable = false, length = 500)
    private String url;
    @Column(name = "is_main", nullable = false)
    private Boolean isMain = false;
    @Column(name = "sort_order")
    private Integer sortOrder;
    //------- Audit Fields ----------
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();


    public HotelImage(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getMain() {
        return isMain;
    }

    public void setMain(Boolean main) {
        isMain = main;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }
}
