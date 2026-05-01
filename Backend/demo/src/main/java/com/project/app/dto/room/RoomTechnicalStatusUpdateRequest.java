package com.project.app.dto.room;

import com.project.app.entity.room.TechnicalStatus;
import jakarta.validation.constraints.NotNull;

public class RoomTechnicalStatusUpdateRequest {

    @NotNull
    private TechnicalStatus technicalStatus;

    public RoomTechnicalStatusUpdateRequest() {
    }

    public TechnicalStatus getTechnicalStatus() {
        return technicalStatus;
    }

    public void setTechnicalStatus(TechnicalStatus technicalStatus) {
        this.technicalStatus = technicalStatus;
    }
}