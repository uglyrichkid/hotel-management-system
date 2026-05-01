package com.project.app.dto.pricing;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RoomPricePeriodListItemResponse {

    private Long id;

    private LocalDate dateFrom;
    private LocalDate dateTo;

    private BigDecimal pricePerNight;

    private boolean active;

    public RoomPricePeriodListItemResponse() {
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public boolean isActive() {
        return active;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}