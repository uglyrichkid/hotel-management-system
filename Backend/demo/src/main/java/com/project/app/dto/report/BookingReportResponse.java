package com.project.app.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BookingReportResponse {

    private Long hotelId;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String currencyCode;

    private long totalBookings;
    private long createdBookings;
    private long confirmedBookings;
    private long checkedInBookings;
    private long checkedOutBookings;
    private long cancelledBookings;

    private BigDecimal averageBookingValue;
    private BigDecimal averageStayLength;

    private List<ReportCountItemResponse> statusBreakdown;
    private List<ReportTrendPointResponse> dailyTrend;
    private List<ReportCountItemResponse> roomTypeBreakdown;
    private List<BookingReportRowResponse> rows;

    public BookingReportResponse() {
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

    public long getTotalBookings() {
        return totalBookings;
    }

    public long getCreatedBookings() {
        return createdBookings;
    }

    public long getConfirmedBookings() {
        return confirmedBookings;
    }

    public long getCheckedInBookings() {
        return checkedInBookings;
    }

    public long getCheckedOutBookings() {
        return checkedOutBookings;
    }

    public long getCancelledBookings() {
        return cancelledBookings;
    }

    public BigDecimal getAverageBookingValue() {
        return averageBookingValue;
    }

    public BigDecimal getAverageStayLength() {
        return averageStayLength;
    }

    public List<ReportCountItemResponse> getStatusBreakdown() {
        return statusBreakdown;
    }

    public List<ReportTrendPointResponse> getDailyTrend() {
        return dailyTrend;
    }

    public List<ReportCountItemResponse> getRoomTypeBreakdown() {
        return roomTypeBreakdown;
    }

    public List<BookingReportRowResponse> getRows() {
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

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public void setCreatedBookings(long createdBookings) {
        this.createdBookings = createdBookings;
    }

    public void setConfirmedBookings(long confirmedBookings) {
        this.confirmedBookings = confirmedBookings;
    }

    public void setCheckedInBookings(long checkedInBookings) {
        this.checkedInBookings = checkedInBookings;
    }

    public void setCheckedOutBookings(long checkedOutBookings) {
        this.checkedOutBookings = checkedOutBookings;
    }

    public void setCancelledBookings(long cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }

    public void setAverageBookingValue(BigDecimal averageBookingValue) {
        this.averageBookingValue = averageBookingValue;
    }

    public void setAverageStayLength(BigDecimal averageStayLength) {
        this.averageStayLength = averageStayLength;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public void setStatusBreakdown(List<ReportCountItemResponse> statusBreakdown) {
        this.statusBreakdown = statusBreakdown;
    }

    public void setDailyTrend(List<ReportTrendPointResponse> dailyTrend) {
        this.dailyTrend = dailyTrend;
    }

    public void setRoomTypeBreakdown(List<ReportCountItemResponse> roomTypeBreakdown) {
        this.roomTypeBreakdown = roomTypeBreakdown;
    }

    public void setRows(List<BookingReportRowResponse> rows) {
        this.rows = rows;
    }
}