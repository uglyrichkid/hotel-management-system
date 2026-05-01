package com.project.app.dto.room;

import com.project.app.entity.room.HousekeepingStatus;
import com.project.app.entity.room.OccupancyStatus;
import com.project.app.entity.room.TechnicalStatus;

import java.math.BigDecimal;

public class RoomListItemResponse {

    private Long id;
    private String roomNumber;
    private String roomTypeName;
    private Short floor;
    private Short capacity;
    private BigDecimal basePrice;
    private OccupancyStatus occupancyStatus;
    private HousekeepingStatus housekeepingStatus;
    private TechnicalStatus technicalStatus;
    private boolean active;

    public RoomListItemResponse() {
    }

    public RoomListItemResponse(
            Long id,
            String roomNumber,
            String roomTypeName,
            Short floor,
            Short capacity,
            BigDecimal basePrice,
            OccupancyStatus occupancyStatus,
            HousekeepingStatus housekeepingStatus,
            TechnicalStatus technicalStatus,
            boolean active
    ) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomTypeName = roomTypeName;
        this.floor = floor;
        this.capacity = capacity;
        this.basePrice = basePrice;
        this.occupancyStatus = occupancyStatus;
        this.housekeepingStatus = housekeepingStatus;
        this.technicalStatus = technicalStatus;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getRoomTypeName() {
        return roomTypeName;
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

    public boolean isActive() {
        return active;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
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

    public void setActive(boolean active) {
        this.active = active;
    }
}