package com.project.app.dto.pricing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RoomPricePeriodCreateRequest {

    @NotNull(message = "dateFrom is required")
    private LocalDate dateFrom;

    @NotNull(message = "dateTo is required")
    private LocalDate dateTo;

    @NotNull(message = "pricePerNight is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal pricePerNight;

    public RoomPricePeriodCreateRequest() {
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }
}