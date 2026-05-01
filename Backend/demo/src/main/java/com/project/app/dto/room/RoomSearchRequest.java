package com.project.app.dto.room;

import com.project.app.entity.room.HousekeepingStatus;
import com.project.app.entity.room.OccupancyStatus;
import com.project.app.entity.room.TechnicalStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class RoomSearchRequest {

    private Long hotelId;
    private String roomNumber;
    private Long roomTypeId;

    private OccupancyStatus occupancyStatus;
    private HousekeepingStatus housekeepingStatus;
    private TechnicalStatus technicalStatus;

    private Short floorFrom;
    private Short floorTo;
    private Boolean active;

    private Integer capacity;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkIn;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOut;

    private Boolean onlyAvailable;

    public RoomSearchRequest() {
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public OccupancyStatus getOccupancyStatus() {
        return occupancyStatus;
    }

    public void setOccupancyStatus(OccupancyStatus occupancyStatus) {
        this.occupancyStatus = occupancyStatus;
    }

    public HousekeepingStatus getHousekeepingStatus() {
        return housekeepingStatus;
    }

    public void setHousekeepingStatus(HousekeepingStatus housekeepingStatus) {
        this.housekeepingStatus = housekeepingStatus;
    }

    public TechnicalStatus getTechnicalStatus() {
        return technicalStatus;
    }

    public void setTechnicalStatus(TechnicalStatus technicalStatus) {
        this.technicalStatus = technicalStatus;
    }

    public Short getFloorFrom() {
        return floorFrom;
    }

    public void setFloorFrom(Short floorFrom) {
        this.floorFrom = floorFrom;
    }

    public Short getFloorTo() {
        return floorTo;
    }

    public void setFloorTo(Short floorTo) {
        this.floorTo = floorTo;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDate checkOut) {
        this.checkOut = checkOut;
    }

    public Boolean getOnlyAvailable() {
        return onlyAvailable;
    }

    public void setOnlyAvailable(Boolean onlyAvailable) {
        this.onlyAvailable = onlyAvailable;
    }
}