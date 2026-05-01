package com.project.app.dto.hotel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HotelCreationRequest {

    @NotBlank(message = "Hotel name must not be empty")
    private String name;

    @NotBlank(message = "Hotel address must not be empty")
    private String address;

    @NotNull(message = "Stars must be provided")
    @Min(value = 1, message = "Stars must be between 1 and 5")
    @Max(value = 5, message = "Stars must be between 1 and 5")
    private Short stars;
    @NotBlank(message = "Currency code is required")
    private String currencyCode;
    @NotNull(message = "cityId must be provided")
    private Long cityId;

    private String description;
    private String checkInTime;
    private String checkOutTime;

    private String phone;
    private String email;
    private String website;

    @Valid
    @NotNull(message = "Director information must be provided")
    private HotelDirectorCreateRequest director;

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
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
    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public HotelDirectorCreateRequest getDirector() {
        return director;
    }

    public void setDirector(HotelDirectorCreateRequest director) {
        this.director = director;
    }
}