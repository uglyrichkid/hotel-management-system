package com.project.app.service.booking;

import com.project.app.dto.booking.BookingCreateRequest;
import com.project.app.dto.booking.BookingListItemResponse;
import com.project.app.dto.booking.BookingQuickGuestCreateRequest;
import com.project.app.dto.booking.BookingResponse;
import com.project.app.dto.booking.BookingSearchRequest;
import com.project.app.dto.booking.BookingUpdateRequest;
import com.project.app.dto.guest.GuestResponse;

import java.util.List;

public interface BookingService {

    BookingResponse create(Long hotelId, BookingCreateRequest request);

    List<BookingListItemResponse> getAllByHotel(Long hotelId);

    List<BookingListItemResponse> search(Long hotelId, BookingSearchRequest request);

    BookingResponse getById(Long hotelId, Long bookingId);

    BookingResponse update(Long hotelId, Long bookingId, BookingUpdateRequest request);

    BookingResponse confirm(Long hotelId, Long bookingId);

    BookingResponse cancel(Long hotelId, Long bookingId);

    BookingResponse checkIn(Long hotelId, Long bookingId);

    BookingResponse checkOut(Long hotelId, Long bookingId);

    GuestResponse quickCreateGuest(BookingQuickGuestCreateRequest request);
}