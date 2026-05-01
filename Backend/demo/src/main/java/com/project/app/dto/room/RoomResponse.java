package com.project.app.dto.room;

import com.project.app.entity.room.HousekeepingStatus;
import com.project.app.entity.room.OccupancyStatus;
import com.project.app.entity.room.TechnicalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RoomResponse {

    private Long id;
    private Long hotelId;
    private String hotelName;

    private Long roomTypeId;
    private String roomTypeName;

    private String roomNumber;
    private Short floor;
    private Short capacity;
    private BigDecimal basePrice;

    private OccupancyStatus occupancyStatus;
    private HousekeepingStatus housekeepingStatus;
    private TechnicalStatus technicalStatus;

    private String description;
    private String notes;
    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RoomResponse() {
    }

    public RoomResponse(
            Long id,
            Long hotelId,
            String hotelName,
            Long roomTypeId,
            String roomTypeName,
            String roomNumber,
            Short floor,
            Short capacity,
            BigDecimal basePrice,
            OccupancyStatus occupancyStatus,
            HousekeepingStatus housekeepingStatus,
            TechnicalStatus technicalStatus,
            String description,
            String notes,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.roomTypeId = roomTypeId;
        this.roomTypeName = roomTypeName;
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.capacity = capacity;
        this.basePrice = basePrice;
        this.occupancyStatus = occupancyStatus;
        this.housekeepingStatus = housekeepingStatus;
        this.technicalStatus = technicalStatus;
        this.description = description;
        this.notes = notes;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public Short getFloor() {
        return floor;
    }

    public Short getCapacity() {
        return capacity;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public OccupancyStatus getOccupancyStatus() {
        return occupancyStatus;
    }

    public HousekeepingStatus getHousekeepingStatus() {
        return housekeepingStatus;
    }

    public TechnicalStatus getTechnicalStatus() {
        return technicalStatus;
    }

    public String getDescription() {
        return description;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public void setRoomTypeId(Long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setFloor(Short floor) {
        this.floor = floor;
    }

    public void setCapacity(Short capacity) {
        this.capacity = capacity;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public void setOccupancyStatus(OccupancyStatus occupancyStatus) {
        this.occupancyStatus = occupancyStatus;
    }

    public void setHousekeepingStatus(HousekeepingStatus housekeepingStatus) {
        this.housekeepingStatus = housekeepingStatus;
    }

    public void setTechnicalStatus(TechnicalStatus technicalStatus) {
        this.technicalStatus = technicalStatus;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}