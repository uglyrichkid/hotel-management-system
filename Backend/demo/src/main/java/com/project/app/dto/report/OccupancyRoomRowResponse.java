package com.project.app.dto.report;

public class OccupancyRoomRowResponse {

    private Long roomId;
    private String roomNumber;
    private String roomType;
    private Short floor;
    private String occupancyStatus;
    private String housekeepingStatus;
    private String technicalStatus;

    public OccupancyRoomRowResponse() {
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public Short getFloor() {
        return floor;
    }

    public String getOccupancyStatus() {
        return occupancyStatus;
    }

    public String getHousekeepingStatus() {
        return housekeepingStatus;
    }

    public String getTechnicalStatus() {
        return technicalStatus;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public void setFloor(Short floor) {
        this.floor = floor;
    }

    public void setOccupancyStatus(String occupancyStatus) {
        this.occupancyStatus = occupancyStatus;
    }

    public void setHousekeepingStatus(String housekeepingStatus) {
        this.housekeepingStatus = housekeepingStatus;
    }

    public void setTechnicalStatus(String technicalStatus) {
        this.technicalStatus = technicalStatus;
    }
}