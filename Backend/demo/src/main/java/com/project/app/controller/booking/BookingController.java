package com.project.app.controller.booking;

import com.project.app.dto.booking.BookingCreateRequest;
import com.project.app.dto.booking.BookingListItemResponse;
import com.project.app.dto.booking.BookingQuickGuestCreateRequest;
import com.project.app.dto.booking.BookingResponse;
import com.project.app.dto.booking.BookingSearchRequest;
import com.project.app.dto.booking.BookingUpdateRequest;
import com.project.app.dto.guest.GuestResponse;
import com.project.app.service.booking.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels/{hotelId}/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(
            @PathVariable Long hotelId,
            @Valid @RequestBody BookingCreateRequest request
    ) {
        return bookingService.create(hotelId, request);
    }

    @GetMapping
    public List<BookingListItemResponse> getAllByHotel(
            @PathVariable Long hotelId,
            @ModelAttribute BookingSearchRequest request
    ) {
        return bookingService.search(hotelId, request);
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getById(
            @PathVariable Long hotelId,
            @PathVariable Long bookingId
    ) {
        return bookingService.getById(hotelId, bookingId);
    }

    @PutMapping("/{bookingId}")
    public BookingResponse update(
            @PathVariable Long hotelId,
            @PathVariable Long bookingId,
            @Valid @RequestBody BookingUpdateRequest request
    ) {
        return bookingService.update(hotelId, bookingId, request);
    }

    @PatchMapping("/{bookingId}/confirm")
    public BookingResponse confirm(
            @PathVariable Long hotelId,
            @PathVariable Long bookingId
    ) {
        return bookingService.confirm(hotelId, bookingId);
    }

    @PatchMapping("/{bookingId}/cancel")
    public BookingResponse cancel(
            @PathVariable Long hotelId,
            @PathVariable Long bookingId
    ) {
        return bookingService.cancel(hotelId, bookingId);
    }

    @PatchMapping("/{bookingId}/check-in")
    public BookingResponse checkIn(
            @PathVariable Long hotelId,
            @PathVariable Long bookingId
    ) {
        return bookingService.checkIn(hotelId, bookingId);
    }

    @PatchMapping("/{bookingId}/check-out")
    public BookingResponse checkOut(
            @PathVariable Long hotelId,
            @PathVariable Long bookingId
    ) {
        return bookingService.checkOut(hotelId, bookingId);
    }

    @PostMapping("/quick-guest")
    @ResponseStatus(HttpStatus.CREATED)
    public GuestResponse quickCreateGuest(
            @Valid @RequestBody BookingQuickGuestCreateRequest request
    ) {
        return bookingService.quickCreateGuest(request);
    }
}