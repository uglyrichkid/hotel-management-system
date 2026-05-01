package com.project.app.controller.room;

import com.project.app.dto.room.*;
import com.project.app.entity.room.HousekeepingStatus;
import com.project.app.entity.room.OccupancyStatus;
import com.project.app.entity.room.TechnicalStatus;
import com.project.app.service.room.RoomService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<RoomListItemResponse> searchRooms(
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) OccupancyStatus occupancyStatus,
            @RequestParam(required = false) HousekeepingStatus housekeepingStatus,
            @RequestParam(required = false) TechnicalStatus technicalStatus,
            @RequestParam(required = false) Short floorFrom,
            @RequestParam(required = false) Short floorTo,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) Boolean onlyAvailable,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut
    ) {
        RoomSearchRequest request = new RoomSearchRequest();
        request.setHotelId(hotelId);
        request.setRoomNumber(roomNumber);
        request.setRoomTypeId(roomTypeId);
        request.setOccupancyStatus(occupancyStatus);
        request.setHousekeepingStatus(housekeepingStatus);
        request.setTechnicalStatus(technicalStatus);
        request.setFloorFrom(floorFrom);
        request.setFloorTo(floorTo);
        request.setActive(active);
        request.setCapacity(capacity);
        request.setOnlyAvailable(onlyAvailable);
        request.setCheckIn(checkIn);
        request.setCheckOut(checkOut);

        return roomService.search(request);
    }

    @GetMapping("/{id}")
    public RoomResponse getRoomById(@PathVariable Long id) {
        return roomService.getById(id);
    }

    @PostMapping
    public RoomResponse createRoom(@Valid @RequestBody RoomCreateRequest request) {
        return roomService.create(request);
    }

    @PutMapping("/{id}")
    public RoomResponse updateRoom(@PathVariable Long id, @RequestBody RoomUpdateRequest request) {
        return roomService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteRoom(@PathVariable Long id) {
        roomService.delete(id);
    }

    @PatchMapping("/{id}/occupancy")
    public RoomResponse changeOccupancyStatus(
            @PathVariable Long id,
            @Valid @RequestBody RoomOccupancyStatusUpdateRequest request
    ) {
        return roomService.changeOccupancyStatus(id, request);
    }

    @PatchMapping("/{id}/housekeeping")
    public RoomResponse changeHousekeepingStatus(
            @PathVariable Long id,
            @Valid @RequestBody RoomHousekeepingStatusUpdateRequest request
    ) {
        return roomService.changeHousekeepingStatus(id, request);
    }

    @PatchMapping("/{id}/technical")
    public RoomResponse changeTechnicalStatus(
            @PathVariable Long id,
            @Valid @RequestBody RoomTechnicalStatusUpdateRequest request
    ) {
        return roomService.changeTechnicalStatus(id, request);
    }

    @PatchMapping("/{id}/mark-clean")
    public RoomResponse markClean(@PathVariable Long id) {
        return roomService.markClean(id);
    }

    @PatchMapping("/{id}/mark-dirty")
    public RoomResponse markDirty(@PathVariable Long id) {
        return roomService.markDirty(id);
    }

    @GetMapping("/available")
    public List<RoomListItemResponse> getAvailableRooms(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) Integer capacity
    ) {
        return roomService.getAvailableRooms(hotelId, checkIn, checkOut, capacity);
    }
}