package com.project.app.controller.hotel;

import com.project.app.dto.hotel.*;
import com.project.app.entity.hotel.HotelStatus;
import com.project.app.service.hotel.HotelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {
    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HotelResponse create(@Valid @RequestBody HotelCreationRequest request) {
        return hotelService.create(request);
    }

    @PostMapping("/{id}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public HotelMemberResponse addMember(
            @PathVariable Long id,
            @Valid @RequestBody HotelMemberCreateRequest request
    ) {
        return hotelService.addMember(id, request);
    }

    @GetMapping("/{id}")
    public HotelResponse getById(@PathVariable Long id) {
        return hotelService.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        hotelService.deactivate(id);
    }

    @PutMapping("/{id}")
    public HotelResponse update(@PathVariable Long id, @Valid @RequestBody HotelUpdateRequest request) {
        return hotelService.update(id, request);
    }

    @PutMapping("/{id}/activate")
    public HotelResponse activate(@PathVariable Long id) {
        return hotelService.activate(id);
    }

    @GetMapping
    public List<HotelListItemResponse> getHotels(
            @RequestParam(required = false) HotelStatus status,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Short stars,
            @RequestParam(required = false) String name
    ) {
        return hotelService.getHotels(status, cityId, stars, name);
    }

    @GetMapping("/{id}/manage")
    public HotelManageResponse getManageDetails(@PathVariable Long id) {
        return hotelService.getManageDetails(id);
    }
}