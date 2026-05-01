package com.project.app.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class OccupancyReportResponse {

    private Long hotelId;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    private long totalRooms;
    private long occupiedRooms;
    private long availableRooms;
    private long outOfServiceRooms;
    private BigDecimal occupancyRate;

    private List<ReportTrendPointResponse> occupancyByDay;
    private List<ReportCountItemResponse> roomStatusBreakdown;
    private List<OccupancyRoomRowResponse> rows;

    public OccupancyReportResponse() {
    }

    public Long getHotelId() {
        return hotelId;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public long getTotalRooms() {
        return totalRooms;
    }

    public long getOccupiedRooms() {
        return occupiedRooms;
    }

    public long getAvailableRooms() {
        return availableRooms;
    }

    public long getOutOfServiceRooms() {
        return outOfServiceRooms;
    }

    public BigDecimal getOccupancyRate() {
        return occupancyRate;
    }

    public List<ReportTrendPointResponse> getOccupancyByDay() {
        return occupancyByDay;
    }

    public List<ReportCountItemResponse> getRoomStatusBreakdown() {
        return roomStatusBreakdown;
    }

    public List<OccupancyRoomRowResponse> getRows() {
        return rows;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public void setTotalRooms(long totalRooms) {
        this.totalRooms = totalRooms;
    }

    public void setOccupiedRooms(long occupiedRooms) {
        this.occupiedRooms = occupiedRooms;
    }

    public void setAvailableRooms(long availableRooms) {
        this.availableRooms = availableRooms;
    }

    public void setOutOfServiceRooms(long outOfServiceRooms) {
        this.outOfServiceRooms = outOfServiceRooms;
    }

    public void setOccupancyRate(BigDecimal occupancyRate) {
        this.occupancyRate = occupancyRate;
    }

    public void setOccupancyByDay(List<ReportTrendPointResponse> occupancyByDay) {
        this.occupancyByDay = occupancyByDay;
    }

    public void setRoomStatusBreakdown(List<ReportCountItemResponse> roomStatusBreakdown) {
        this.roomStatusBreakdown = roomStatusBreakdown;
    }

    public void setRows(List<OccupancyRoomRowResponse> rows) {
        this.rows = rows;
    }
}