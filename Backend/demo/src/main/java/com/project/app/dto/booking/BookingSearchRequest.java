package com.project.app.dto.booking;

import com.project.app.entity.booking.BookingStatus;

public class BookingSearchRequest {

    private BookingStatus status;
    private String guestQuery;

    public BookingSearchRequest() {
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getGuestQuery() {
        return guestQuery;
    }

    public void setGuestQuery(String guestQuery) {
        this.guestQuery = guestQuery;
    }
}