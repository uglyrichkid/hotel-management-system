package com.project.app.dto.room;

import com.project.app.entity.room.OccupancyStatus;
import jakarta.validation.constraints.NotNull;

public class RoomOccupancyStatusUpdateRequest {

    @NotNull
    private OccupancyStatus occupancyStatus;

    public RoomOccupancyStatusUpdateRequest() {
    }

    public OccupancyStatus getOccupancyStatus() {
        return occupancyStatus;
    }

    public void setOccupancyStatus(OccupancyStatus occupancyStatus) {
        this.occupancyStatus = occupancyStatus;
    }
}