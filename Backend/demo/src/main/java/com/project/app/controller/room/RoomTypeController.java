package com.project.app.controller.room;

import com.project.app.dto.room.RoomTypeResponse;
import com.project.app.entity.room.RoomType;
import com.project.app.service.room.RoomTypeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-types")
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    public RoomTypeController(RoomTypeService roomTypeService) {
        this.roomTypeService = roomTypeService;
    }

    @GetMapping
    public List<RoomTypeResponse> getAll() {
        return roomTypeService.getAll();
    }

    @PostMapping
    public RoomTypeResponse create(@RequestBody RoomType roomType) {
        return roomTypeService.create(roomType);
    }
}