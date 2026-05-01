package com.project.app.repository.room;

import com.project.app.dto.room.RoomSearchRequest;
import com.project.app.entity.room.Room;
import org.springframework.data.jpa.domain.Specification;

public final class RoomSpecifications {

    private RoomSpecifications() {
    }

    public static Specification<Room> build(RoomSearchRequest request) {
        return Specification.where(hasHotelId(request.getHotelId()))
                .and(roomNumberContains(request.getRoomNumber()))
                .and(hasRoomTypeId(request.getRoomTypeId()))
                .and(hasOccupancyStatus(request.getOccupancyStatus()))
                .and(hasHousekeepingStatus(request.getHousekeepingStatus()))
                .and(hasTechnicalStatus(request.getTechnicalStatus()))
                .and(hasFloorFrom(request.getFloorFrom()))
                .and(hasFloorTo(request.getFloorTo()))
                .and(hasActive(request.getActive()))
                .and(hasCapacity(request.getCapacity()));
    }

    public static Specification<Room> hasHotelId(Long hotelId) {
        return (root, query, cb) ->
                hotelId == null ? null : cb.equal(root.get("hotel").get("id"), hotelId);
    }

    public static Specification<Room> roomNumberContains(String roomNumber) {
        return (root, query, cb) ->
                roomNumber == null || roomNumber.isBlank()
                        ? null
                        : cb.like(cb.lower(root.get("roomNumber")), "%" + roomNumber.toLowerCase() + "%");
    }

    public static Specification<Room> hasRoomTypeId(Long roomTypeId) {
        return (root, query, cb) ->
                roomTypeId == null ? null : cb.equal(root.get("roomType").get("id"), roomTypeId);
    }

    public static Specification<Room> hasOccupancyStatus(Object occupancyStatus) {
        return (root, query, cb) ->
                occupancyStatus == null ? null : cb.equal(root.get("occupancyStatus"), occupancyStatus);
    }

    public static Specification<Room> hasHousekeepingStatus(Object housekeepingStatus) {
        return (root, query, cb) ->
                housekeepingStatus == null ? null : cb.equal(root.get("housekeepingStatus"), housekeepingStatus);
    }

    public static Specification<Room> hasTechnicalStatus(Object technicalStatus) {
        return (root, query, cb) ->
                technicalStatus == null ? null : cb.equal(root.get("technicalStatus"), technicalStatus);
    }

    public static Specification<Room> hasFloorFrom(Short floorFrom) {
        return (root, query, cb) ->
                floorFrom == null ? null : cb.greaterThanOrEqualTo(root.get("floor"), floorFrom);
    }

    public static Specification<Room> hasFloorTo(Short floorTo) {
        return (root, query, cb) ->
                floorTo == null ? null : cb.lessThanOrEqualTo(root.get("floor"), floorTo);
    }

    public static Specification<Room> hasActive(Boolean active) {
        return (root, query, cb) ->
                active == null ? null : cb.equal(root.get("active"), active);
    }

    public static Specification<Room> hasCapacity(Integer capacity) {
        return (root, query, cb) ->
                capacity == null ? null : cb.greaterThanOrEqualTo(root.get("capacity"), capacity.shortValue());
    }
}