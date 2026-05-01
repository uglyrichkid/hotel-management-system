package com.project.app.service.pricing;

import com.project.app.dto.pricing.RoomPricePeriodCreateRequest;
import com.project.app.dto.pricing.RoomPricePeriodListItemResponse;
import com.project.app.dto.pricing.RoomPricePeriodResponse;
import com.project.app.dto.pricing.RoomPricePeriodUpdateRequest;
import com.project.app.entity.pricing.RoomPricePeriod;
import com.project.app.entity.room.Room;
import com.project.app.exception.ConflictException;
import com.project.app.exception.NotFoundException;
import com.project.app.repository.pricing.RoomPricePeriodRepository;
import com.project.app.repository.room.RoomRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PricingServiceImpl implements PricingService {

    private final RoomPricePeriodRepository pricePeriodRepository;
    private final RoomRepository roomRepository;

    public PricingServiceImpl(
            RoomPricePeriodRepository pricePeriodRepository,
            RoomRepository roomRepository
    ) {
        this.pricePeriodRepository = pricePeriodRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public BigDecimal calculateTotalPrice(
            List<Room> rooms,
            LocalDate checkIn,
            LocalDate checkOut
    ) {

        BigDecimal total = BigDecimal.ZERO;

        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            for (Room room : rooms) {
                BigDecimal priceForNight = resolvePriceForNight(room, date);
                total = total.add(priceForNight);
            }
        }

        return total;
    }

    @Override
    public RoomPricePeriodResponse createPricePeriod(
            Long hotelId,
            Long roomId,
            RoomPricePeriodCreateRequest request
    ) {
        Room room = getActiveRoomOrThrow(hotelId, roomId);

        validateDateRange(request.getDateFrom(), request.getDateTo());

        boolean hasOverlap =
                pricePeriodRepository.existsByRoomIdAndActiveTrueAndDateFromLessThanAndDateToGreaterThan(
                        roomId,
                        request.getDateTo(),
                        request.getDateFrom()
                );

        if (hasOverlap) {
            throw new ConflictException("Price period overlaps with existing active period");
        }

        RoomPricePeriod pricePeriod = new RoomPricePeriod();
        pricePeriod.setRoom(room);
        pricePeriod.setDateFrom(request.getDateFrom());
        pricePeriod.setDateTo(request.getDateTo());
        pricePeriod.setPricePerNight(request.getPricePerNight());
        pricePeriod.setActive(true);

        RoomPricePeriod saved = pricePeriodRepository.save(pricePeriod);

        return toResponse(saved);
    }

    @Override
    public RoomPricePeriodResponse getPricePeriodById(
            Long hotelId,
            Long roomId,
            Long pricePeriodId
    ) {
        getActiveRoomOrThrow(hotelId, roomId);

        RoomPricePeriod pricePeriod = getActivePricePeriodOrThrow(roomId, pricePeriodId);

        return toResponse(pricePeriod);
    }

    private RoomPricePeriod getActivePricePeriodOrThrow(Long roomId, Long pricePeriodId) {
        RoomPricePeriod pricePeriod = pricePeriodRepository.findById(pricePeriodId)
                .orElseThrow(() -> new NotFoundException("Price period not found"));

        if (!pricePeriod.isActive()) {
            throw new NotFoundException("Price period not found");
        }

        if (!pricePeriod.getRoom().getId().equals(roomId)) {
            throw new ConflictException("Price period does not belong to this room");
        }

        return pricePeriod;
    }

    @Override
    public List<RoomPricePeriodListItemResponse> getPricePeriodsByRoom(
            Long hotelId,
            Long roomId
    ) {
        getActiveRoomOrThrow(hotelId, roomId);

        return pricePeriodRepository.findByRoomIdAndActiveTrueOrderByDateFromAsc(roomId)
                .stream()
                .map(this::toListItemResponse)
                .toList();
    }

    @Override
    public RoomPricePeriodResponse updatePricePeriod(
            Long hotelId,
            Long roomId,
            Long pricePeriodId,
            RoomPricePeriodUpdateRequest request
    ) {
        getActiveRoomOrThrow(hotelId, roomId);

        RoomPricePeriod pricePeriod = getActivePricePeriodOrThrow(roomId, pricePeriodId);

        validateDateRange(request.getDateFrom(), request.getDateTo());

        boolean hasOverlap =
                pricePeriodRepository.existsByRoomIdAndActiveTrueAndIdNotAndDateFromLessThanAndDateToGreaterThan(
                        roomId,
                        pricePeriodId,
                        request.getDateTo(),
                        request.getDateFrom()
                );

        if (hasOverlap) {
            throw new ConflictException("Price period overlaps with existing active period");
        }

        pricePeriod.setDateFrom(request.getDateFrom());
        pricePeriod.setDateTo(request.getDateTo());
        pricePeriod.setPricePerNight(request.getPricePerNight());

        RoomPricePeriod saved = pricePeriodRepository.save(pricePeriod);

        return toResponse(saved);
    }

    @Override
    public void deletePricePeriod(
            Long hotelId,
            Long roomId,
            Long pricePeriodId
    ) {
        getActiveRoomOrThrow(hotelId, roomId);

        RoomPricePeriod pricePeriod = getActivePricePeriodOrThrow(roomId, pricePeriodId);

        pricePeriod.setActive(false);

        pricePeriodRepository.save(pricePeriod);
    }

    private BigDecimal resolvePriceForNight(Room room, LocalDate date) {

        List<RoomPricePeriod> periods =
                pricePeriodRepository.findByRoomIdAndActiveTrueAndDateFromLessThanAndDateToGreaterThan(
                        room.getId(),
                        date.plusDays(1),
                        date
                );

        if (!periods.isEmpty()) {
            return periods.get(0).getPricePerNight();
        }

        if (room.getBasePrice() != null) {
            return room.getBasePrice();
        }

        if (room.getRoomType().getDefaultPrice() == null) {
            throw new ConflictException("Base price is not defined for room type");
        }

        return room.getRoomType().getDefaultPrice();
    }

    private Room getActiveRoomOrThrow(Long hotelId, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        if (!room.isActive()) {
            throw new NotFoundException("Room not found");
        }

        if (!room.getHotel().getId().equals(hotelId)) {
            throw new ConflictException("Room does not belong to this hotel");
        }

        return room;
    }

    private void validateDateRange(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null || dateTo == null) {
            throw new ConflictException("Date range is required");
        }

        if (!dateFrom.isBefore(dateTo)) {
            throw new ConflictException("dateFrom must be before dateTo");
        }
    }


    private RoomPricePeriodListItemResponse toListItemResponse(RoomPricePeriod entity) {
        RoomPricePeriodListItemResponse response = new RoomPricePeriodListItemResponse();
        response.setId(entity.getId());
        response.setDateFrom(entity.getDateFrom());
        response.setDateTo(entity.getDateTo());
        response.setPricePerNight(entity.getPricePerNight());
        response.setActive(entity.isActive());
        return response;
    }
    private RoomPricePeriodResponse toResponse(RoomPricePeriod entity) {
        RoomPricePeriodResponse response = new RoomPricePeriodResponse();
        response.setId(entity.getId());
        response.setRoomId(entity.getRoom().getId());
        response.setDateFrom(entity.getDateFrom());
        response.setDateTo(entity.getDateTo());
        response.setPricePerNight(entity.getPricePerNight());
        response.setActive(entity.isActive());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}