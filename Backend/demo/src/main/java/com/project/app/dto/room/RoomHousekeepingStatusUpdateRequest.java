package com.project.app.dto.room;

import com.project.app.entity.room.HousekeepingStatus;
import jakarta.validation.constraints.NotNull;

public class RoomHousekeepingStatusUpdateRequest {

    @NotNull
    private HousekeepingStatus housekeepingStatus;

    public RoomHousekeepingStatusUpdateRequest() {
    }

    public HousekeepingStatus getHousekeepingStatus() {
        return housekeepingStatus;
    }

    public void setHousekeepingStatus(HousekeepingStatus housekeepingStatus) {
        this.housekeepingStatus = housekeepingStatus;
    }
}