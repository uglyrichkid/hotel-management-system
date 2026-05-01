package com.project.app.service.room;

import com.project.app.dto.room.*;

import java.time.LocalDate;
import java.util.List;

public interface RoomService {

    List<RoomListItemResponse> search(RoomSearchRequest request);

    RoomResponse getById(Long id);

    RoomResponse create(RoomCreateRequest request);

    RoomResponse update(Long id, RoomUpdateRequest request);

    void delete(Long id);

    RoomResponse changeOccupancyStatus(Long id, RoomOccupancyStatusUpdateRequest request);

    RoomResponse changeHousekeepingStatus(Long id, RoomHousekeepingStatusUpdateRequest request);

    RoomResponse changeTechnicalStatus(Long id, RoomTechnicalStatusUpdateRequest request);

    RoomResponse markClean(Long id);

    RoomResponse markDirty(Long id);

    List<RoomListItemResponse> getAvailableRooms(Long hotelId, LocalDate checkIn, LocalDate checkOut, Integer capacity);
}