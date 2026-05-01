package com.project.app.service.payment;

import com.project.app.dto.payment.BookingFinancialResponse;
import com.project.app.dto.payment.CardPaymentRequest;
import com.project.app.dto.payment.CashPaymentRequest;
import com.project.app.dto.payment.PaymentResponse;

import java.util.List;

public interface PaymentService {

    PaymentResponse createCashPayment(CashPaymentRequest request);

    PaymentResponse createCardPayment(CardPaymentRequest request);

    List<PaymentResponse> getPaymentsByBookingId(Long bookingId);

    BookingFinancialResponse getBookingFinancialByBookingId(Long bookingId);

    void deletePayment(Long paymentId);
}