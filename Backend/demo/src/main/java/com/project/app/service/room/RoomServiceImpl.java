package com.project.app.service.room;

import com.project.app.dto.room.*;
import com.project.app.entity.hotel.Hotel;
import com.project.app.entity.room.*;
import com.project.app.repository.booking.BookingRoomRepository;
import com.project.app.repository.hotel.HotelRepository;
import com.project.app.repository.room.RoomRepository;
import com.project.app.repository.room.RoomSpecifications;
import com.project.app.repository.room.RoomTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.project.app.entity.booking.BookingStatus;
import com.project.app.service.booking.BookingAvailabilityRules;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;
    private final BookingRoomRepository bookingRoomRepository;

    public RoomServiceImpl(
            RoomRepository roomRepository,
            RoomTypeRepository roomTypeRepository,
            HotelRepository hotelRepository,
            BookingRoomRepository bookingRoomRepository
    ) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.hotelRepository = hotelRepository;
        this.bookingRoomRepository = bookingRoomRepository;
    }

    @Override
    public List<RoomListItemResponse> search(RoomSearchRequest request) {
        Specification<Room> spec = RoomSpecifications.build(request);
        List<Room> rooms = roomRepository.findAll(spec);

        if (Boolean.TRUE.equals(request.getOnlyAvailable())
                && request.getHotelId() != null
                && request.getCheckIn() != null
                && request.getCheckOut() != null) {

            validateDateRange(request.getCheckIn(), request.getCheckOut());

            List<BookingStatus> blockingStatuses = BookingAvailabilityRules.blockingStatuses();

            List<Long> busyRoomIds = bookingRoomRepository.findBusyRoomIds(
                    request.getHotelId(),
                    blockingStatuses,
                    request.getCheckIn(),
                    request.getCheckOut()
            );

            Set<Long> busyIds = new HashSet<>(busyRoomIds);

            rooms = rooms.stream()
                    .filter(Room::isActive)
                    .filter(room -> room.getTechnicalStatus() == TechnicalStatus.ACTIVE)
                    .filter(room -> room.getOccupancyStatus() != OccupancyStatus.BLOCKED)
                    .filter(room -> !busyIds.contains(room.getId()))
                    .toList();
        }

        return rooms.stream()
                .map(this::toListItemResponse)
                .toList();
    }

    @Override
    public RoomResponse getById(Long id) {
        Room room = getActiveRoomOrThrow(id);
        return toResponse(room);
    }

    @Override
    public RoomResponse create(RoomCreateRequest request) {
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + request.getHotelId()));

        RoomType roomType = roomTypeRepository.findByIdAndActiveTrue(request.getRoomTypeId())
                .orElseThrow(() -> new RuntimeException("Room type not found with id: " + request.getRoomTypeId()));

        if (roomRepository.existsByHotel_IdAndRoomNumberAndActiveTrue(request.getHotelId(), request.getRoomNumber())) {
            throw new RuntimeException("Room number already exists in this hotel");
        }

        Room room = new Room();
        room.setHotel(hotel);
        room.setRoomType(roomType);
        room.setRoomNumber(request.getRoomNumber());
        room.setFloor(request.getFloor());
        room.setCapacity(request.getCapacity());
        room.setBasePrice(request.getBasePrice());
        room.setDescription(request.getDescription());
        room.setNotes(request.getNotes());
        room.setOccupancyStatus(OccupancyStatus.AVAILABLE);
        room.setHousekeepingStatus(HousekeepingStatus.CLEAN);
        room.setTechnicalStatus(TechnicalStatus.ACTIVE);
        room.setActive(true);

        Room saved = roomRepository.save(room);
        return toResponse(saved);
    }

    @Override
    public RoomResponse update(Long id, RoomUpdateRequest request) {
        Room room = getActiveRoomOrThrow(id);

        if (request.getRoomTypeId() != null) {
            RoomType roomType = roomTypeRepository.findByIdAndActiveTrue(request.getRoomTypeId())
                    .orElseThrow(() -> new RuntimeException("Room type not found with id: " + request.getRoomTypeId()));
            room.setRoomType(roomType);
        }

        if (request.getRoomNumber() != null && !request.getRoomNumber().isBlank()) {
            if (roomRepository.existsByHotel_IdAndRoomNumberAndActiveTrueAndIdNot(
                    room.getHotel().getId(),
                    request.getRoomNumber(),
                    room.getId()
            )) {
                throw new RuntimeException("Room number already exists in this hotel");
            }
            room.setRoomNumber(request.getRoomNumber());
        }

        if (request.getFloor() != null) {
            room.setFloor(request.getFloor());
        }

        if (request.getCapacity() != null) {
            room.setCapacity(request.getCapacity());
        }

        if (request.getBasePrice() != null) {
            room.setBasePrice(request.getBasePrice());
        }

        if (request.getDescription() != null) {
            room.setDescription(request.getDescription());
        }

        if (request.getNotes() != null) {
            room.setNotes(request.getNotes());
        }

        if (request.getActive() != null) {
            room.setActive(request.getActive());
        }

        Room saved = roomRepository.save(room);
        return toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Room room = getActiveRoomOrThrow(id);
        room.setActive(false);
        roomRepository.save(room);
    }

    @Override
    public RoomResponse changeOccupancyStatus(Long id, RoomOccupancyStatusUpdateRequest request) {
        Room room = getActiveRoomOrThrow(id);
        room.setOccupancyStatus(request.getOccupancyStatus());
        return toResponse(roomRepository.save(room));
    }

    @Override
    public RoomResponse changeHousekeepingStatus(Long id, RoomHousekeepingStatusUpdateRequest request) {
        Room room = getActiveRoomOrThrow(id);
        room.setHousekeepingStatus(request.getHousekeepingStatus());
        return toResponse(roomRepository.save(room));
    }

    @Override
    public RoomResponse changeTechnicalStatus(Long id, RoomTechnicalStatusUpdateRequest request) {
        Room room = getActiveRoomOrThrow(id);
        room.setTechnicalStatus(request.getTechnicalStatus());

        if (request.getTechnicalStatus() != TechnicalStatus.ACTIVE) {
            room.setOccupancyStatus(OccupancyStatus.BLOCKED);
        }

        return toResponse(roomRepository.save(room));
    }

    @Override
    public RoomResponse markClean(Long id) {
        Room room = getActiveRoomOrThrow(id);
        room.setHousekeepingStatus(HousekeepingStatus.CLEAN);
        return toResponse(roomRepository.save(room));
    }

    @Override
    public RoomResponse markDirty(Long id) {
        Room room = getActiveRoomOrThrow(id);
        room.setHousekeepingStatus(HousekeepingStatus.DIRTY);
        return toResponse(roomRepository.save(room));
    }

    @Override
    public List<RoomListItemResponse> getAvailableRooms(Long hotelId, LocalDate checkIn, LocalDate checkOut, Integer capacity) {
        validateDateRange(checkIn, checkOut);

        RoomSearchRequest request = new RoomSearchRequest();
        request.setHotelId(hotelId);
        request.setActive(true);
        request.setTechnicalStatus(TechnicalStatus.ACTIVE);
        request.setCapacity(capacity);
        request.setCheckIn(checkIn);
        request.setCheckOut(checkOut);
        request.setOnlyAvailable(true);

        return search(request);
    }

    private Room getActiveRoomOrThrow(Long id) {
        return roomRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
    }

    private void validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new RuntimeException("Check-in and check-out are required");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }
    }

    private RoomListItemResponse toListItemResponse(Room room) {
        return new RoomListItemResponse(
                room.getId(),
                room.getRoomNumber(),
                room.getRoomType() != null ? room.getRoomType().getName() : null,
                room.getFloor(),
                room.getCapacity(),
                room.getBasePrice(),
                room.getOccupancyStatus(),
                room.getHousekeepingStatus(),
                room.getTechnicalStatus(),
                room.isActive()
        );
    }

    private RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getHotel() != null ? room.getHotel().getId() : null,
                room.getHotel() != null ? room.getHotel().getName() : null,
                room.getRoomType() != null ? room.getRoomType().getId() : null,
                room.getRoomType() != null ? room.getRoomType().getName() : null,
                room.getRoomNumber(),
                room.getFloor(),
                room.getCapacity(),
                room.getBasePrice(),
                room.getOccupancyStatus(),
                room.getHousekeepingStatus(),
                room.getTechnicalStatus(),
                room.getDescription(),
                room.getNotes(),
                room.isActive(),
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }
}