package com.project.app.controller;

import com.project.app.dto.booking.BookingCreateRequest;
import com.project.app.dto.booking.BookingQuickGuestCreateRequest;
import com.project.app.dto.booking.BookingResponse;
import com.project.app.dto.guest.GuestResponse;
import com.project.app.dto.hotel.HotelListItemResponse;
import com.project.app.dto.room.RoomListItemResponse;
import com.project.app.entity.hotel.HotelStatus;
import com.project.app.service.booking.BookingService;
import com.project.app.service.hotel.HotelService;
import com.project.app.service.room.RoomService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final HotelService hotelService;
    private final RoomService roomService;
    private final BookingService bookingService;

    public PublicController(HotelService hotelService, RoomService roomService, BookingService bookingService) {
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.bookingService = bookingService;
    }

    @GetMapping("/hotels")
    public List<HotelListItemResponse> getActiveHotels() {
        return hotelService.getHotels(HotelStatus.ACTIVE, null, null, null);
    }

    @GetMapping("/hotels/{hotelId}/rooms/available")
    public List<RoomListItemResponse> getAvailableRooms(
            @PathVariable Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) Integer capacity
    ) {
        return roomService.getAvailableRooms(hotelId, checkIn, checkOut, capacity);
    }

    @PostMapping("/hotels/{hotelId}/bookings/guest")
    @ResponseStatus(HttpStatus.CREATED)
    public GuestResponse quickCreateGuest(
            @PathVariable Long hotelId,
            @Valid @RequestBody BookingQuickGuestCreateRequest request
    ) {
        return bookingService.quickCreateGuest(request);
    }

    @PostMapping("/hotels/{hotelId}/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(
            @PathVariable Long hotelId,
            @Valid @RequestBody BookingCreateRequest request
    ) {
        return bookingService.create(hotelId, request);
    }
}