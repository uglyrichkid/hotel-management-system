package com.project.app.service.pricing;

import com.project.app.dto.pricing.RoomPricePeriodCreateRequest;
import com.project.app.dto.pricing.RoomPricePeriodListItemResponse;
import com.project.app.dto.pricing.RoomPricePeriodResponse;
import com.project.app.dto.pricing.RoomPricePeriodUpdateRequest;
import com.project.app.entity.room.Room;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PricingService {

    BigDecimal calculateTotalPrice(
            List<Room> rooms,
            LocalDate checkIn,
            LocalDate checkOut
    );

    RoomPricePeriodResponse createPricePeriod(
            Long hotelId,
            Long roomId,
            RoomPricePeriodCreateRequest request
    );

    RoomPricePeriodResponse getPricePeriodById(
            Long hotelId,
            Long roomId,
            Long pricePeriodId
    );

    List<RoomPricePeriodListItemResponse> getPricePeriodsByRoom(
            Long hotelId,
            Long roomId
    );

    RoomPricePeriodResponse updatePricePeriod(
            Long hotelId,
            Long roomId,
            Long pricePeriodId,
            RoomPricePeriodUpdateRequest request
    );

    void deletePricePeriod(
            Long hotelId,
            Long roomId,
            Long pricePeriodId
    );
}