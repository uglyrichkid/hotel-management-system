package com.project.app.dto.payment;

import java.math.BigDecimal;

public class PaymentSummaryResponse {

    private Long bookingId;
    private BigDecimal bookingTotalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;

    public PaymentSummaryResponse() {
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public BigDecimal getBookingTotalAmount() {
        return bookingTotalAmount;
    }

    public void setBookingTotalAmount(BigDecimal bookingTotalAmount) {
        this.bookingTotalAmount = bookingTotalAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }
}