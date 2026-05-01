package com.project.app.dto.hotel;

import com.project.app.entity.hotel.HotelStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


import java.util.List;


public class HotelUpdateRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "CityId is required")
    private Long cityId;

    @NotNull(message = "Stars mus be provided")
    @Min(value = 1, message = "Stars must be between 1 and 5")
    @Max(value = 5, message = "Stars must be between 1 and 5")
    private Short stars;

    @NotNull(message = "Status is required")
    private HotelStatus status;
    @NotBlank(message = "Currency code is required")
    private String currencyCode;
    private String description;
    private String checkInTime;
    private String checkOutTime;

    private String phone;
    private String email;
    private String website;

    private java.util.List<HotelPolicyUpsertRequest> policies;
    private java.util.List<HotelImageUpsertRequest> images;

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

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public Short getStars() {
        return stars;
    }

    public void setStars(Short stars) {
        this.stars = stars;
    }

    public HotelStatus getStatus() {
        return status;
    }

    public void setStatus(HotelStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public List<HotelPolicyUpsertRequest> getPolicies() {
        return policies;
    }

    public void setPolicies(List<HotelPolicyUpsertRequest> policies) {
        this.policies = policies;
    }

    public List<HotelImageUpsertRequest> getImages() {
        return images;
    }

    public void setImages(List<HotelImageUpsertRequest> images) {
        this.images = images;
    }
}
