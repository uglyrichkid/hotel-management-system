package com.project.app.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RevenueReportResponse {

    private Long hotelId;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String currencyCode;

    private BigDecimal expectedRevenue;
    private BigDecimal collectedRevenue;
    private BigDecimal outstandingBalance;

    private long partialBookingsCount;
    private long unpaidBookingsCount;
    private long completedPaymentsCount;
    private long failedPaymentsCount;

    private List<ReportTrendPointResponse> revenueByDay;
    private List<ReportAmountItemResponse> paymentMethodBreakdown;
    private List<RevenueReportRowResponse> revenueRows;

    public RevenueReportResponse() {
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

    public BigDecimal getExpectedRevenue() {
        return expectedRevenue;
    }

    public BigDecimal getCollectedRevenue() {
        return collectedRevenue;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public long getPartialBookingsCount() {
        return partialBookingsCount;
    }

    public long getUnpaidBookingsCount() {
        return unpaidBookingsCount;
    }

    public long getCompletedPaymentsCount() {
        return completedPaymentsCount;
    }

    public long getFailedPaymentsCount() {
        return failedPaymentsCount;
    }

    public List<ReportTrendPointResponse> getRevenueByDay() {
        return revenueByDay;
    }

    public List<ReportAmountItemResponse> getPaymentMethodBreakdown() {
        return paymentMethodBreakdown;
    }

    public List<RevenueReportRowResponse> getRevenueRows() {
        return revenueRows;
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

    public void setExpectedRevenue(BigDecimal expectedRevenue) {
        this.expectedRevenue = expectedRevenue;
    }

    public void setCollectedRevenue(BigDecimal collectedRevenue) {
        this.collectedRevenue = collectedRevenue;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public void setPartialBookingsCount(long partialBookingsCount) {
        this.partialBookingsCount = partialBookingsCount;
    }

    public void setUnpaidBookingsCount(long unpaidBookingsCount) {
        this.unpaidBookingsCount = unpaidBookingsCount;
    }

    public void setCompletedPaymentsCount(long completedPaymentsCount) {
        this.completedPaymentsCount = completedPaymentsCount;
    }

    public void setFailedPaymentsCount(long failedPaymentsCount) {
        this.failedPaymentsCount = failedPaymentsCount;
    }

    public void setRevenueByDay(List<ReportTrendPointResponse> revenueByDay) {
        this.revenueByDay = revenueByDay;
    }

    public void setPaymentMethodBreakdown(List<ReportAmountItemResponse> paymentMethodBreakdown) {
        this.paymentMethodBreakdown = paymentMethodBreakdown;
    }

    public void setRevenueRows(List<RevenueReportRowResponse> revenueRows) {
        this.revenueRows = revenueRows;
    }
    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}