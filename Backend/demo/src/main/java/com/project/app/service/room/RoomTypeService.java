package com.project.app.service.room;

import com.project.app.dto.room.RoomTypeResponse;
import com.project.app.entity.room.RoomType;
import com.project.app.repository.room.RoomTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    public RoomTypeService(RoomTypeRepository roomTypeRepository) {
        this.roomTypeRepository = roomTypeRepository;
    }

    public List<RoomTypeResponse> getAll() {
        return roomTypeRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RoomTypeResponse create(RoomType roomType) {
        RoomType saved = roomTypeRepository.save(roomType);
        return toResponse(saved);
    }

    private RoomTypeResponse toResponse(RoomType roomType) {
        return new RoomTypeResponse(
                roomType.getId(),
                roomType.getName(),
                roomType.getDescription(),
                roomType.getDefaultCapacity(),
                roomType.getDefaultPrice(),
                roomType.isActive()
        );
    }
}