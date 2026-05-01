package com.project.app.entity.hotel;
import com.project.app.entity.geo.City;
import jakarta.persistence.*;
import com.project.app.entity.common.CurrencyCode;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Entity
@Table(
        name = "hotels",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_hotels_city_name_address",
                        columnNames = {"city_id", "name", "address"}
                )
        }
)
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ----- Relations -----
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id",nullable = false)
    private City city;


    // ----- Main Fields -----
    @Column(name = "name", nullable = false, length = 120)
    private String name;
    @Column(name = "address", nullable = false, length = 255)
    private String address;
    @Column(name = "stars")
    private Short stars;
    @Column(name = "description", columnDefinition = "text")
    private String description;
    @Column(name = "check_in_time")
    private LocalTime checkInTime;
    @Column(name = "check_out_time")
    private LocalTime checkOutTime;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private HotelStatus status = HotelStatus.ACTIVE;
    @Enumerated(EnumType.STRING)
    @Column(name = "currency_code", length = 10)
    private CurrencyCode currencyCode = CurrencyCode.USD;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ----- Constructors -----
    public Hotel(){}

    // ----- Getter & Setter -----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Short getStars() {
        return stars;
    }

    public void setStars(Short stars) {
        this.stars = stars;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalTime getCheckOutTime() {
        return checkOutTime;
    }
    public CurrencyCode getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(CurrencyCode currencyCode) {
        this.currencyCode = currencyCode;
    }
    public void setCheckOutTime(LocalTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public HotelStatus getStatus() {
        return status;
    }

    public void setStatus(HotelStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreateAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public City getCity() {return city;}
    public void setCity(City city) {this.city = city;}


}
