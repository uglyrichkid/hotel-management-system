package com.project.app.service.booking;

import com.project.app.entity.booking.BookingStatus;

import java.util.List;

public final class BookingAvailabilityRules {

    private BookingAvailabilityRules() {
    }

    public static List<BookingStatus> blockingStatuses() {
        return List.of(
                BookingStatus.CREATED,
                BookingStatus.CONFIRMED,
                BookingStatus.CHECKED_IN
        );
    }

    public static boolean blocksRoom(BookingStatus status) {
        return blockingStatuses().contains(status);
    }
}