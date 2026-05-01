package com.project.app.service.payment;

import com.project.app.dto.payment.BookingFinancialResponse;
import com.project.app.dto.payment.CardPaymentRequest;
import com.project.app.dto.payment.CashPaymentRequest;
import com.project.app.dto.payment.PaymentResponse;
import com.project.app.entity.booking.Booking;
import com.project.app.entity.booking.BookingPaymentStatus;
import com.project.app.entity.booking.BookingStatus;
import com.project.app.entity.payment.Payment;
import com.project.app.entity.payment.PaymentMethod;
import com.project.app.entity.payment.PaymentStatus;
import com.project.app.exception.ConflictException;
import com.project.app.exception.NotFoundException;
import com.project.app.repository.booking.BookingRepository;
import com.project.app.repository.payment.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public PaymentResponse createCashPayment(CashPaymentRequest request) {
        Booking booking = getBookingOrThrow(request.getBookingId());

        validateBookingPaymentAllowed(booking);
        validateAmountAgainstRemaining(booking, request.getAmount());

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(PaymentMethod.CASH);
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        payment.setNotes(request.getNotes());
        payment.setActive(true);

        Payment savedPayment = paymentRepository.save(payment);

        applyCompletedPaymentToBooking(booking, savedPayment.getAmount());

        return mapToResponse(savedPayment);
    }

    @Override
    public PaymentResponse createCardPayment(CardPaymentRequest request) {
        Booking booking = getBookingOrThrow(request.getBookingId());

        validateBookingPaymentAllowed(booking);
        validateAmountAgainstRemaining(booking, request.getAmount());

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(PaymentMethod.CARD);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTransactionReference(
                request.getTransactionReference() != null && !request.getTransactionReference().isBlank()
                        ? request.getTransactionReference().trim()
                        : "MOCK-" + UUID.randomUUID()
        );
        payment.setNotes(request.getNotes());
        payment.setActive(true);

        Payment savedPayment = paymentRepository.save(payment);

        boolean terminalSuccess = processMockTerminalPayment(savedPayment);

        if (terminalSuccess) {
            savedPayment.setPaymentStatus(PaymentStatus.COMPLETED);
            savedPayment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(savedPayment);

            applyCompletedPaymentToBooking(booking, savedPayment.getAmount());
        } else {
            savedPayment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(savedPayment);
        }

        return mapToResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByBookingId(Long bookingId) {
        getBookingOrThrow(bookingId);

        return paymentRepository.findByBookingIdAndActiveTrueOrderByCreatedAtDesc(bookingId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingFinancialResponse getBookingFinancialByBookingId(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        BigDecimal total = safeAmount(booking.getTotalPrice());
        BigDecimal paid = safeAmount(booking.getPaidAmount());
        BigDecimal remaining = calculateRemaining(total, paid);

        BookingFinancialResponse response = new BookingFinancialResponse();
        response.setBookingId(booking.getId());
        response.setTotal(total);
        response.setPaid(paid);
        response.setRemaining(remaining);
        response.setStatus(booking.getPaymentStatus());
        response.setCurrencyCode(
                booking.getCurrencyCode() != null ? booking.getCurrencyCode().name() : null
        );

        return response;
    }

    @Override
    public void deletePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found with id: " + paymentId));

        if (!Boolean.TRUE.equals(payment.getActive())) {
            throw new ConflictException("Payment already deleted");
        }

        payment.setActive(false);
        paymentRepository.save(payment);
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));
    }

    private void validateBookingPaymentAllowed(Booking booking) {
        if (!booking.isActive()) {
            throw new ConflictException("Cannot create payment for inactive booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Cannot create payment for cancelled booking");
        }

        if (booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new ConflictException("Cannot create payment for checked-out booking");
        }
    }

    private void validateAmountAgainstRemaining(Booking booking, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ConflictException("Payment amount must be greater than 0");
        }

        BigDecimal total = safeAmount(booking.getTotalPrice());
        BigDecimal paid = safeAmount(booking.getPaidAmount());
        BigDecimal remaining = calculateRemaining(total, paid);

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ConflictException("Booking is already fully paid");
        }

        if (amount.compareTo(remaining) > 0) {
            throw new ConflictException("Payment amount exceeds remaining booking amount");
        }
    }

    private void applyCompletedPaymentToBooking(Booking booking, BigDecimal paymentAmount) {
        BigDecimal currentPaid = safeAmount(booking.getPaidAmount());
        BigDecimal total = safeAmount(booking.getTotalPrice());

        BigDecimal nextPaid = currentPaid.add(paymentAmount);

        booking.setPaidAmount(nextPaid);
        booking.setPaymentStatus(resolveBookingPaymentStatus(total, nextPaid));

        bookingRepository.save(booking);
    }

    private BookingPaymentStatus resolveBookingPaymentStatus(BigDecimal total, BigDecimal paid) {
        if (paid.compareTo(BigDecimal.ZERO) <= 0) {
            return BookingPaymentStatus.NEW;
        }

        if (paid.compareTo(total) < 0) {
            return BookingPaymentStatus.PARTIALLY_PAID;
        }

        return BookingPaymentStatus.PAID;
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal calculateRemaining(BigDecimal total, BigDecimal paid) {
        BigDecimal remaining = total.subtract(paid);
        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
    }

    private boolean processMockTerminalPayment(Payment payment) {
        return true;
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setBookingId(payment.getBooking().getId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setPaymentStatus(payment.getPaymentStatus());
        response.setPaidAt(payment.getPaidAt());
        response.setCreatedAt(payment.getCreatedAt());
        response.setTransactionReference(payment.getTransactionReference());
        response.setNotes(payment.getNotes());
        response.setCurrencyCode(
                payment.getBooking() != null && payment.getBooking().getCurrencyCode() != null
                        ? payment.getBooking().getCurrencyCode().name()
                        : null
        );
        return response;
    }
}