package com.project.app.repository.room;

import com.project.app.entity.room.OccupancyStatus;
import com.project.app.entity.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    boolean existsByHotel_IdAndRoomNumberAndActiveTrue(Long hotelId, String roomNumber);

    boolean existsByHotel_IdAndRoomNumberAndActiveTrueAndIdNot(Long hotelId, String roomNumber, Long id);

    Optional<Room> findByIdAndActiveTrue(Long id);

    List<Room> findAllByHotel_IdAndActiveTrueOrderByRoomNumberAsc(Long hotelId);

    long countByHotel_Id(Long hotelId);

    long countByHotel_IdAndActiveTrue(Long hotelId);

    long countByHotel_IdAndActiveTrueAndOccupancyStatus(Long hotelId, OccupancyStatus occupancyStatus);
}