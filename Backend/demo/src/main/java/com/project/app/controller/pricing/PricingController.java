package com.project.app.controller.pricing;

import com.project.app.dto.pricing.RoomPricePeriodCreateRequest;
import com.project.app.dto.pricing.RoomPricePeriodListItemResponse;
import com.project.app.dto.pricing.RoomPricePeriodResponse;
import com.project.app.dto.pricing.RoomPricePeriodUpdateRequest;
import com.project.app.service.pricing.PricingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels/{hotelId}/rooms/{roomId}/price-periods")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomPricePeriodResponse createPricePeriod(
            @PathVariable Long hotelId,
            @PathVariable Long roomId,
            @Valid @RequestBody RoomPricePeriodCreateRequest request
    ) {
        return pricingService.createPricePeriod(hotelId, roomId, request);
    }

    @GetMapping("/{pricePeriodId}")
    public RoomPricePeriodResponse getPricePeriodById(
            @PathVariable Long hotelId,
            @PathVariable Long roomId,
            @PathVariable Long pricePeriodId
    ) {
        return pricingService.getPricePeriodById(hotelId, roomId, pricePeriodId);
    }

    @GetMapping
    public List<RoomPricePeriodListItemResponse> getPricePeriodsByRoom(
            @PathVariable Long hotelId,
            @PathVariable Long roomId
    ) {
        return pricingService.getPricePeriodsByRoom(hotelId, roomId);
    }

    @PutMapping("/{pricePeriodId}")
    public RoomPricePeriodResponse updatePricePeriod(
            @PathVariable Long hotelId,
            @PathVariable Long roomId,
            @PathVariable Long pricePeriodId,
            @Valid @RequestBody RoomPricePeriodUpdateRequest request
    ) {
        return pricingService.updatePricePeriod(hotelId, roomId, pricePeriodId, request);
    }

    @DeleteMapping("/{pricePeriodId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePricePeriod(
            @PathVariable Long hotelId,
            @PathVariable Long roomId,
            @PathVariable Long pricePeriodId
    ) {
        pricingService.deletePricePeriod(hotelId, roomId, pricePeriodId);
    }
}