package com.project.app.dto.hotel;

import com.project.app.entity.hotel.HotelStatus;

import java.time.LocalTime;
import java.util.List;

public class HotelManageResponse {

    private Long id;
    private String name;
    private String address;
    private Long cityId;
    private String cityName;
    private Short stars;
    private String description;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private HotelStatus status;

    private Long roomsCount;
    private String directorName;

    private String phone;
    private String email;
    private String website;
    private String currencyCode;

    private List<HotelPolicyItemResponse> policies;
    private List<HotelImageItemResponse> images;
    private List<HotelMemberResponse> members;

    public HotelManageResponse() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Long getCityId() {
        return cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public Short getStars() {
        return stars;
    }

    public String getDescription() {
        return description;
    }

    public LocalTime getCheckInTime() {
        return checkInTime;
    }

    public LocalTime getCheckOutTime() {
        return checkOutTime;
    }

    public HotelStatus getStatus() {
        return status;
    }

    public Long getRoomsCount() {
        return roomsCount;
    }

    public String getDirectorName() {
        return directorName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getWebsite() {
        return website;
    }

    public List<HotelPolicyItemResponse> getPolicies() {
        return policies;
    }

    public List<HotelImageItemResponse> getImages() {
        return images;
    }

    public List<HotelMemberResponse> getMembers() {
        return members;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setStars(Short stars) {
        this.stars = stars;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCheckInTime(LocalTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public void setCheckOutTime(LocalTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public void setStatus(HotelStatus status) {
        this.status = status;
    }

    public void setRoomsCount(Long roomsCount) {
        this.roomsCount = roomsCount;
    }

    public void setDirectorName(String directorName) {
        this.directorName = directorName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setPolicies(List<HotelPolicyItemResponse> policies) {
        this.policies = policies;
    }

    public void setImages(List<HotelImageItemResponse> images) {
        this.images = images;
    }

    public void setMembers(List<HotelMemberResponse> members) {
        this.members = members;
    }
}