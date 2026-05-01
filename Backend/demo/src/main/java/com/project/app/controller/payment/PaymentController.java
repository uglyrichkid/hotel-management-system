package com.project.app.controller.payment;

import com.project.app.dto.payment.BookingFinancialResponse;
import com.project.app.dto.payment.CardPaymentRequest;
import com.project.app.dto.payment.CashPaymentRequest;
import com.project.app.dto.payment.PaymentResponse;
import com.project.app.service.payment.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments/cash")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createCashPayment(@Valid @RequestBody CashPaymentRequest request) {
        return paymentService.createCashPayment(request);
    }

    @PostMapping("/payments/card")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createCardPayment(@Valid @RequestBody CardPaymentRequest request) {
        return paymentService.createCardPayment(request);
    }

    @GetMapping("/bookings/{bookingId}/payments")
    public List<PaymentResponse> getPaymentsByBookingId(@PathVariable Long bookingId) {
        return paymentService.getPaymentsByBookingId(bookingId);
    }

    @GetMapping("/bookings/{bookingId}/financial")
    public BookingFinancialResponse getBookingFinancialByBookingId(@PathVariable Long bookingId) {
        return paymentService.getBookingFinancialByBookingId(bookingId);
    }

    @DeleteMapping("/payments/{paymentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePayment(@PathVariable Long paymentId) {
        paymentService.deletePayment(paymentId);
    }
}