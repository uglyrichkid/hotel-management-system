package com.project.app.service.report;

import com.project.app.dto.report.BookingReportResponse;
import com.project.app.dto.report.BookingReportRowResponse;
import com.project.app.dto.report.OccupancyReportResponse;
import com.project.app.dto.report.OccupancyRoomRowResponse;
import com.project.app.dto.report.ReportAmountItemResponse;
import com.project.app.dto.report.ReportCountItemResponse;
import com.project.app.dto.report.ReportTrendPointResponse;
import com.project.app.dto.report.RevenueReportResponse;
import com.project.app.dto.report.RevenueReportRowResponse;
import com.project.app.entity.booking.Booking;
import com.project.app.entity.booking.BookingRoom;
import com.project.app.entity.booking.BookingStatus;
import com.project.app.entity.payment.Payment;
import com.project.app.entity.payment.PaymentMethod;
import com.project.app.entity.payment.PaymentStatus;
import com.project.app.entity.room.Room;
import com.project.app.entity.room.TechnicalStatus;
import com.project.app.exception.NotFoundException;
import com.project.app.repository.booking.BookingRepository;
import com.project.app.repository.hotel.HotelRepository;
import com.project.app.repository.payment.PaymentRepository;
import com.project.app.repository.room.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final PaymentRepository paymentRepository;
    private final RoomRepository roomRepository;

    public ReportServiceImpl(BookingRepository bookingRepository,
                             HotelRepository hotelRepository,
                             PaymentRepository paymentRepository,
                             RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
        this.paymentRepository = paymentRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public BookingReportResponse getBookingReport(Long hotelId, LocalDate dateFrom, LocalDate dateTo) {
        var hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel not found with id: " + hotelId));

        LocalDate from = resolveDateFrom(dateFrom, dateTo);
        LocalDate to = resolveDateTo(dateFrom, dateTo);
        validateDateRange(from, to);

        List<Booking> allHotelBookings = bookingRepository.findAllByHotel_IdAndActiveTrueOrderByCreatedAtDesc(hotelId);
        List<Booking> filteredBookings = allHotelBookings.stream()
                .filter(booking -> overlaps(booking.getCheckInDate(), booking.getCheckOutDate(), from, to))
                .toList();

        BookingReportResponse response = new BookingReportResponse();
        response.setHotelId(hotelId);
        response.setDateFrom(from);
        response.setDateTo(to);
        response.setCurrencyCode(
                hotel.getCurrencyCode() != null ? hotel.getCurrencyCode().name() : null
        );

        response.setTotalBookings((long) filteredBookings.size());
        response.setCreatedBookings(countByStatus(filteredBookings, BookingStatus.CREATED));
        response.setConfirmedBookings(countByStatus(filteredBookings, BookingStatus.CONFIRMED));
        response.setCheckedInBookings(countByStatus(filteredBookings, BookingStatus.CHECKED_IN));
        response.setCheckedOutBookings(countByStatus(filteredBookings, BookingStatus.CHECKED_OUT));
        response.setCancelledBookings(countByStatus(filteredBookings, BookingStatus.CANCELLED));
        response.setAverageBookingValue(calculateAverageBookingValue(filteredBookings));
        response.setAverageStayLength(calculateAverageStayLength(filteredBookings));

        response.setStatusBreakdown(buildBookingStatusBreakdown(filteredBookings));
        response.setDailyTrend(buildBookingDailyTrend(filteredBookings, from, to));
        response.setRoomTypeBreakdown(buildBookingRoomTypeBreakdown(filteredBookings));
        response.setRows(buildBookingRows(filteredBookings));

        return response;
    }

    @Override
    public RevenueReportResponse getRevenueReport(Long hotelId, LocalDate dateFrom, LocalDate dateTo) {
        var hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel not found with id: " + hotelId));

        LocalDate from = resolveDateFrom(dateFrom, dateTo);
        LocalDate to = resolveDateTo(dateFrom, dateTo);
        validateDateRange(from, to);

        List<Booking> allHotelBookings = bookingRepository.findAllByHotel_IdAndActiveTrueOrderByCreatedAtDesc(hotelId);
        List<Booking> filteredBookings = allHotelBookings.stream()
                .filter(booking -> overlaps(booking.getCheckInDate(), booking.getCheckOutDate(), from, to))
                .toList();

        List<Payment> hotelPayments = paymentRepository.findByBooking_Hotel_IdAndActiveTrueOrderByCreatedAtDesc(hotelId);

        Map<Long, Booking> bookingMap = filteredBookings.stream()
                .collect(Collectors.toMap(Booking::getId, b -> b));

        List<Payment> filteredPayments = hotelPayments.stream()
                .filter(payment -> bookingMap.containsKey(payment.getBooking().getId()))
                .toList();

        BigDecimal expectedRevenue = filteredBookings.stream()
                .map(booking -> safeAmount(booking.getTotalPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal collectedRevenue = filteredPayments.stream()
                .filter(payment -> payment.getPaymentStatus() == PaymentStatus.COMPLETED)
                .map(payment -> safeAmount(payment.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long partialBookings = filteredBookings.stream()
                .filter(booking -> booking.getPaidAmount() != null)
                .filter(booking -> booking.getPaidAmount().compareTo(BigDecimal.ZERO) > 0)
                .filter(booking -> booking.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0)
                .count();

        long unpaidBookings = filteredBookings.stream()
                .filter(booking -> safeAmount(booking.getPaidAmount()).compareTo(BigDecimal.ZERO) == 0)
                .count();

        long failedPayments = filteredPayments.stream()
                .filter(payment -> payment.getPaymentStatus() == PaymentStatus.FAILED)
                .count();

        RevenueReportResponse response = new RevenueReportResponse();
        response.setHotelId(hotelId);
        response.setDateFrom(from);
        response.setDateTo(to);
        response.setCurrencyCode(
                hotel.getCurrencyCode() != null ? hotel.getCurrencyCode().name() : null
        );
        response.setExpectedRevenue(expectedRevenue);
        response.setCollectedRevenue(collectedRevenue);
        response.setOutstandingBalance(expectedRevenue.subtract(collectedRevenue).max(BigDecimal.ZERO));
        response.setPartialBookingsCount(partialBookings);
        response.setUnpaidBookingsCount(unpaidBookings);
        response.setCompletedPaymentsCount(
                filteredPayments.stream().filter(p -> p.getPaymentStatus() == PaymentStatus.COMPLETED).count()
        );
        response.setFailedPaymentsCount(failedPayments);

        response.setRevenueByDay(buildRevenueTrend(filteredBookings, from, to));
        response.setPaymentMethodBreakdown(buildPaymentMethodBreakdown(filteredPayments));
        response.setRevenueRows(buildRevenueRows(filteredBookings, filteredPayments));

        return response;
    }
    @Override
    public OccupancyReportResponse getOccupancyReport(Long hotelId, LocalDate dateFrom, LocalDate dateTo) {
        validateHotelExists(hotelId);

        LocalDate from = resolveDateFrom(dateFrom, dateTo);
        LocalDate to = resolveDateTo(dateFrom, dateTo);
        validateDateRange(from, to);

        List<Room> rooms = roomRepository.findAllByHotel_IdAndActiveTrueOrderByRoomNumberAsc(hotelId);
        List<Booking> bookings = bookingRepository.findAllByHotel_IdAndActiveTrueOrderByCreatedAtDesc(hotelId);

        long totalRooms = rooms.size();
        long occupiedRooms = rooms.stream()
                .filter(room -> room.getOccupancyStatus() != null && room.getOccupancyStatus().name().equals("OCCUPIED"))
                .count();

        long availableRooms = rooms.stream()
                .filter(room -> room.getOccupancyStatus() != null && room.getOccupancyStatus().name().equals("AVAILABLE"))
                .count();

        long outOfServiceRooms = rooms.stream()
                .filter(room -> room.getTechnicalStatus() != null && room.getTechnicalStatus() != TechnicalStatus.ACTIVE)
                .count();

        BigDecimal occupancyRate = calculateCurrentOccupancyRate(occupiedRooms, totalRooms);

        OccupancyReportResponse response = new OccupancyReportResponse();
        response.setHotelId(hotelId);
        response.setDateFrom(from);
        response.setDateTo(to);
        response.setTotalRooms(totalRooms);
        response.setOccupiedRooms(occupiedRooms);
        response.setAvailableRooms(availableRooms);
        response.setOutOfServiceRooms(outOfServiceRooms);
        response.setOccupancyRate(occupancyRate);

        response.setOccupancyByDay(buildOccupancyTrend(bookings, rooms, from, to));
        response.setRoomStatusBreakdown(buildRoomStatusBreakdown(rooms));
        response.setRows(buildOccupancyRows(rooms));

        return response;
    }

    private void validateHotelExists(Long hotelId) {
        hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel not found with id: " + hotelId));
    }

    private LocalDate resolveDateFrom(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom != null) {
            return dateFrom;
        }
        if (dateTo != null) {
            return dateTo.minusDays(29);
        }
        return LocalDate.now().minusDays(29);
    }

    private LocalDate resolveDateTo(LocalDate dateFrom, LocalDate dateTo) {
        if (dateTo != null) {
            return dateTo;
        }
        if (dateFrom != null) {
            return dateFrom.plusDays(29);
        }
        return LocalDate.now();
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("dateFrom cannot be after dateTo");
        }
    }

    private boolean overlaps(LocalDate bookingCheckIn, LocalDate bookingCheckOut, LocalDate from, LocalDate to) {
        return !bookingCheckOut.isBefore(from) && !bookingCheckIn.isAfter(to);
    }

    private long countByStatus(List<Booking> bookings, BookingStatus status) {
        return bookings.stream()
                .filter(booking -> booking.getStatus() == status)
                .count();
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private BigDecimal calculateAverageBookingValue(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = bookings.stream()
                .map(booking -> safeAmount(booking.getTotalPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(BigDecimal.valueOf(bookings.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverageStayLength(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long totalNights = bookings.stream()
                .mapToLong(booking -> booking.getCheckInDate().until(booking.getCheckOutDate()).getDays())
                .sum();

        return BigDecimal.valueOf(totalNights)
                .divide(BigDecimal.valueOf(bookings.size()), 2, RoundingMode.HALF_UP);
    }

    private List<ReportCountItemResponse> buildBookingStatusBreakdown(List<Booking> bookings) {
        List<ReportCountItemResponse> items = new ArrayList<>();
        for (BookingStatus status : BookingStatus.values()) {
            long count = bookings.stream()
                    .filter(booking -> booking.getStatus() == status)
                    .count();
            items.add(new ReportCountItemResponse(status.name(), count));
        }
        return items;
    }

    private List<ReportTrendPointResponse> buildBookingDailyTrend(List<Booking> bookings, LocalDate from, LocalDate to) {
        Map<LocalDate, Long> byDate = new LinkedHashMap<>();
        LocalDate cursor = from;

        while (!cursor.isAfter(to)) {
            LocalDate current = cursor;
            long count = bookings.stream()
                    .filter(booking -> booking.getCheckInDate() != null && booking.getCheckInDate().isEqual(current))
                    .count();
            byDate.put(current, count);
            cursor = cursor.plusDays(1);
        }

        return byDate.entrySet().stream()
                .map(entry -> new ReportTrendPointResponse(entry.getKey(), BigDecimal.valueOf(entry.getValue())))
                .toList();
    }

    private List<ReportCountItemResponse> buildBookingRoomTypeBreakdown(List<Booking> bookings) {
        Map<String, Long> counts = new LinkedHashMap<>();

        for (Booking booking : bookings) {
            if (booking.getRooms() == null) {
                continue;
            }

            for (BookingRoom bookingRoom : booking.getRooms()) {
                if (bookingRoom.getRoom() == null || bookingRoom.getRoom().getRoomType() == null) {
                    continue;
                }

                String roomTypeName = bookingRoom.getRoom().getRoomType().getName();
                counts.put(roomTypeName, counts.getOrDefault(roomTypeName, 0L) + 1);
            }
        }

        return counts.entrySet().stream()
                .map(entry -> new ReportCountItemResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<BookingReportRowResponse> buildBookingRows(List<Booking> bookings) {
        return bookings.stream()
                .sorted(Comparator.comparing(Booking::getCreatedAt).reversed())
                .map(booking -> {
                    BookingReportRowResponse row = new BookingReportRowResponse();
                    row.setBookingId(booking.getId());
                    row.setGuestName(buildGuestName(booking));
                    row.setRooms(buildRoomNumbers(booking));
                    row.setCheckInDate(booking.getCheckInDate());
                    row.setCheckOutDate(booking.getCheckOutDate());
                    row.setStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
                    row.setTotalAmount(safeAmount(booking.getTotalPrice()));
                    row.setPaidAmount(safeAmount(booking.getPaidAmount()));
                    row.setRemainingAmount(booking.getRemainingAmount());
                    row.setCurrencyCode(
                            booking.getCurrencyCode() != null ? booking.getCurrencyCode().name() : null
                    );
                    return row;
                })
                .toList();
    }

    private List<ReportTrendPointResponse> buildRevenueTrend(List<Booking> bookings, LocalDate from, LocalDate to) {
        Map<LocalDate, BigDecimal> byDate = new LinkedHashMap<>();
        LocalDate cursor = from;

        while (!cursor.isAfter(to)) {
            LocalDate current = cursor;
            BigDecimal total = bookings.stream()
                    .filter(booking -> booking.getCheckInDate() != null && booking.getCheckInDate().isEqual(current))
                    .map(booking -> safeAmount(booking.getTotalPrice()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            byDate.put(current, total);
            cursor = cursor.plusDays(1);
        }

        return byDate.entrySet().stream()
                .map(entry -> new ReportTrendPointResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<ReportAmountItemResponse> buildPaymentMethodBreakdown(List<Payment> payments) {
        Map<PaymentMethod, BigDecimal> totals = new EnumMap<>(PaymentMethod.class);

        for (PaymentMethod method : PaymentMethod.values()) {
            totals.put(method, BigDecimal.ZERO);
        }

        for (Payment payment : payments) {
            if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
                continue;
            }

            PaymentMethod method = payment.getPaymentMethod();
            totals.put(method, totals.get(method).add(safeAmount(payment.getAmount())));
        }

        return totals.entrySet().stream()
                .map(entry -> new ReportAmountItemResponse(entry.getKey().name(), entry.getValue()))
                .toList();
    }

    private List<RevenueReportRowResponse> buildRevenueRows(List<Booking> bookings, List<Payment> payments) {
        Map<Long, List<Payment>> paymentsByBookingId = payments.stream()
                .collect(Collectors.groupingBy(payment -> payment.getBooking().getId()));

        return bookings.stream()
                .sorted(Comparator.comparing(Booking::getCreatedAt).reversed())
                .map(booking -> {
                    List<Payment> bookingPayments = paymentsByBookingId.getOrDefault(booking.getId(), List.of());

                    String paymentMethods = bookingPayments.stream()
                            .filter(payment -> payment.getPaymentStatus() == PaymentStatus.COMPLETED)
                            .map(payment -> payment.getPaymentMethod().name())
                            .distinct()
                            .sorted()
                            .collect(Collectors.joining(", "));

                    RevenueReportRowResponse row = new RevenueReportRowResponse();
                    row.setBookingId(booking.getId());
                    row.setGuestName(buildGuestName(booking));
                    row.setTotalAmount(safeAmount(booking.getTotalPrice()));
                    row.setPaidAmount(safeAmount(booking.getPaidAmount()));
                    row.setRemainingAmount(booking.getRemainingAmount());
                    row.setPaymentStatus(booking.getPaymentStatus() != null ? booking.getPaymentStatus().name() : null);
                    row.setPaymentMethods(paymentMethods);
                    row.setCurrencyCode(
                            booking.getCurrencyCode() != null ? booking.getCurrencyCode().name() : null
                    );
                    return row;
                })
                .toList();
    }

    private BigDecimal calculateCurrentOccupancyRate(long occupiedRooms, long totalRooms) {
        if (totalRooms == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(occupiedRooms)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalRooms), 2, RoundingMode.HALF_UP);
    }

    private List<ReportTrendPointResponse> buildOccupancyTrend(List<Booking> bookings,
                                                               List<Room> rooms,
                                                               LocalDate from,
                                                               LocalDate to) {
        long totalRooms = rooms.size();
        Map<LocalDate, BigDecimal> trend = new LinkedHashMap<>();
        LocalDate cursor = from;

        while (!cursor.isAfter(to)) {
            LocalDate current = cursor;

            long occupiedCount = bookings.stream()
                    .filter(booking -> overlaps(booking.getCheckInDate(), booking.getCheckOutDate(), current, current))
                    .flatMap(booking -> booking.getRooms().stream())
                    .map(BookingRoom::getRoom)
                    .filter(room -> room != null && room.getId() != null)
                    .map(Room::getId)
                    .distinct()
                    .count();

            BigDecimal value = totalRooms == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(occupiedCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalRooms), 2, RoundingMode.HALF_UP);

            trend.put(current, value);
            cursor = cursor.plusDays(1);
        }

        return trend.entrySet().stream()
                .map(entry -> new ReportTrendPointResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<ReportCountItemResponse> buildRoomStatusBreakdown(List<Room> rooms) {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("AVAILABLE", 0L);
        counts.put("OCCUPIED", 0L);
        counts.put("OUT_OF_SERVICE", 0L);

        for (Room room : rooms) {
            if (room.getTechnicalStatus() != null && room.getTechnicalStatus() != TechnicalStatus.ACTIVE) {
                counts.put("OUT_OF_SERVICE", counts.get("OUT_OF_SERVICE") + 1);
                continue;
            }

            String occupancy = room.getOccupancyStatus() != null ? room.getOccupancyStatus().name() : "AVAILABLE";
            if (!counts.containsKey(occupancy)) {
                counts.put(occupancy, 0L);
            }
            counts.put(occupancy, counts.get(occupancy) + 1);
        }

        return counts.entrySet().stream()
                .map(entry -> new ReportCountItemResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<OccupancyRoomRowResponse> buildOccupancyRows(List<Room> rooms) {
        return rooms.stream()
                .map(room -> {
                    OccupancyRoomRowResponse row = new OccupancyRoomRowResponse();
                    row.setRoomId(room.getId());
                    row.setRoomNumber(room.getRoomNumber());
                    row.setRoomType(room.getRoomType() != null ? room.getRoomType().getName() : null);
                    row.setFloor(room.getFloor());
                    row.setOccupancyStatus(room.getOccupancyStatus() != null ? room.getOccupancyStatus().name() : null);
                    row.setHousekeepingStatus(room.getHousekeepingStatus() != null ? room.getHousekeepingStatus().name() : null);
                    row.setTechnicalStatus(room.getTechnicalStatus() != null ? room.getTechnicalStatus().name() : null);
                    return row;
                })
                .toList();
    }

    private String buildGuestName(Booking booking) {
        if (booking.getGuest() == null) {
            return "";
        }

        String firstName = booking.getGuest().getFirstName() != null ? booking.getGuest().getFirstName() : "";
        String lastName = booking.getGuest().getLastName() != null ? booking.getGuest().getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    private String buildRoomNumbers(Booking booking) {
        if (booking.getRooms() == null || booking.getRooms().isEmpty()) {
            return "";
        }

        return booking.getRooms().stream()
                .map(BookingRoom::getRoom)
                .filter(room -> room != null && room.getRoomNumber() != null)
                .map(Room::getRoomNumber)
                .sorted()
                .collect(Collectors.joining(", "));
    }
}