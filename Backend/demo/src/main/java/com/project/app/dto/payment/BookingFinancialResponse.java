package com.project.app.dto.payment;

import com.project.app.entity.booking.BookingPaymentStatus;

import java.math.BigDecimal;

public class BookingFinancialResponse {

    private Long bookingId;
    private BigDecimal total;
    private BigDecimal paid;
    private BigDecimal remaining;
    private String currencyCode;
    private BookingPaymentStatus status;

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getPaid() {
        return paid;
    }

    public void setPaid(BigDecimal paid) {
        this.paid = paid;
    }

    public BigDecimal getRemaining() {
        return remaining;
    }

    public void setRemaining(BigDecimal remaining) {
        this.remaining = remaining;
    }

    public BookingPaymentStatus getStatus() {
        return status;
    }

    public void setStatus(BookingPaymentStatus status) {
        this.status = status;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}