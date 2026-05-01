package com.project.app.service.booking;

import com.project.app.dto.booking.BookingCreateRequest;
import com.project.app.dto.booking.BookingListItemResponse;
import com.project.app.dto.booking.BookingQuickGuestCreateRequest;
import com.project.app.dto.booking.BookingResponse;
import com.project.app.dto.booking.BookingSearchRequest;
import com.project.app.dto.booking.BookingUpdateRequest;
import com.project.app.dto.guest.GuestResponse;
import com.project.app.entity.booking.Booking;
import com.project.app.entity.booking.BookingRoom;
import com.project.app.entity.booking.BookingStatus;
import com.project.app.entity.guest.Guest;
import com.project.app.entity.hotel.Hotel;
import com.project.app.entity.room.OccupancyStatus;
import com.project.app.entity.room.Room;
import com.project.app.entity.room.RoomType;
import com.project.app.entity.room.TechnicalStatus;
import com.project.app.exception.ConflictException;
import com.project.app.exception.NotFoundException;
import com.project.app.repository.booking.BookingRepository;
import com.project.app.repository.booking.BookingRoomRepository;
import com.project.app.repository.guest.GuestRepository;
import com.project.app.repository.hotel.HotelContactRepository;
import com.project.app.repository.hotel.HotelRepository;
import com.project.app.repository.room.RoomRepository;
import com.project.app.service.email.EmailService;
import com.project.app.service.pricing.PricingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingRoomRepository bookingRoomRepository;
    private final HotelRepository hotelRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final PricingService pricingService;
    private final EmailService emailService;
    private final HotelContactRepository hotelContactRepository;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            BookingRoomRepository bookingRoomRepository,
            HotelRepository hotelRepository,
            GuestRepository guestRepository,
            RoomRepository roomRepository,
            PricingService pricingService,
            EmailService emailService, HotelContactRepository hotelContactRepository) {
        this.bookingRepository = bookingRepository;
        this.bookingRoomRepository = bookingRoomRepository;
        this.hotelRepository = hotelRepository;
        this.guestRepository = guestRepository;
        this.roomRepository = roomRepository;
        this.pricingService = pricingService;
        this.emailService = emailService;
        this.hotelContactRepository = hotelContactRepository;
    }

    @Override
    public BookingResponse create(Long hotelId, BookingCreateRequest request) {
        validateCreateRequest(hotelId, request);

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel not found"));

        Guest guest = guestRepository.findByIdAndActiveTrue(request.getGuestId())
                .orElseThrow(() -> new NotFoundException("Guest not found"));

        List<Long> uniqueRoomIds = request.getRoomIds().stream().distinct().toList();
        List<Room> rooms = roomRepository.findAllById(uniqueRoomIds);

        if (rooms.size() != uniqueRoomIds.size()) {
            throw new NotFoundException("One or more rooms were not found");
        }

        validateRoomsBelongToHotel(rooms, hotelId);
        validateRoomsOperationallyAvailable(rooms);
        validateRoomAvailabilityForDates(hotelId, rooms, request.getCheckInDate(), request.getCheckOutDate(), null);
        validateCapacity(rooms, request.getAdults(), request.getChildren());

        Booking booking = new Booking();
        booking.setHotel(hotel);
        booking.setGuest(guest);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setStatus(BookingStatus.CREATED);
        booking.setCurrencyCode(hotel.getCurrencyCode());
        booking.setActive(true);

        Integer adults = request.getAdults() != null ? request.getAdults() : 1;
        Integer children = request.getChildren() != null ? request.getChildren() : 0;

        booking.setAdults(adults);
        booking.setChildren(children);
        booking.setNotes(request.getNotes());

        booking.setTotalPrice(
                pricingService.calculateTotalPrice(
                        rooms,
                        request.getCheckInDate(),
                        request.getCheckOutDate()
                )
        );

        Booking savedBooking = bookingRepository.save(booking);

        List<BookingRoom> bookingRooms = rooms.stream()
                .map(room -> {
                    BookingRoom bookingRoom = new BookingRoom();
                    bookingRoom.setBooking(savedBooking);
                    bookingRoom.setRoom(room);
                    return bookingRoom;
                })
                .toList();

        bookingRoomRepository.saveAll(bookingRooms);
        savedBooking.setRooms(bookingRooms);

        return toResponse(savedBooking, uniqueRoomIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingListItemResponse> getAllByHotel(Long hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new NotFoundException("Hotel not found");
        }

        return bookingRepository.findAllByHotel_IdAndActiveTrueOrderByCreatedAtDesc(hotelId)
                .stream()
                .map(this::toListItemResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingListItemResponse> search(Long hotelId, BookingSearchRequest request) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new NotFoundException("Hotel not found");
        }

        BookingStatus status = request != null ? request.getStatus() : null;
        String guestQuery = request != null ? request.getGuestQuery() : null;

        if ((guestQuery == null || guestQuery.isBlank()) && status == null) {
            return getAllByHotel(hotelId);
        }

        List<Booking> bookings;

        if (guestQuery == null || guestQuery.isBlank()) {
            bookings = bookingRepository.findAllByHotel_IdAndActiveTrueAndStatusOrderByCreatedAtDesc(
                    hotelId,
                    status
            );
            return bookings.stream()
                    .map(this::toListItemResponse)
                    .toList();
        }

        String q = guestQuery.trim();

        List<Booking> byFirstName = status == null
                ? bookingRepository.findAllByHotel_IdAndActiveTrueAndGuest_FirstNameContainingIgnoreCaseOrderByCreatedAtDesc(hotelId, q)
                : bookingRepository.findAllByHotel_IdAndActiveTrueAndStatusAndGuest_FirstNameContainingIgnoreCaseOrderByCreatedAtDesc(hotelId, status, q);

        List<Booking> byLastName = status == null
                ? bookingRepository.findAllByHotel_IdAndActiveTrueAndGuest_LastNameContainingIgnoreCaseOrderByCreatedAtDesc(hotelId, q)
                : bookingRepository.findAllByHotel_IdAndActiveTrueAndStatusAndGuest_LastNameContainingIgnoreCaseOrderByCreatedAtDesc(hotelId, status, q);

        List<Booking> byEmail = status == null
                ? bookingRepository.findAllByHotel_IdAndActiveTrueAndGuest_EmailContainingIgnoreCaseOrderByCreatedAtDesc(hotelId, q)
                : bookingRepository.findAllByHotel_IdAndActiveTrueAndStatusAndGuest_EmailContainingIgnoreCaseOrderByCreatedAtDesc(hotelId, status, q);

        List<Booking> byPhone = status == null
                ? bookingRepository.findAllByHotel_IdAndActiveTrueAndGuest_PhoneContainingIgnoreCaseOrderByCreatedAtDesc(hotelId, q)
                : bookingRepository.findAllByHotel_IdAndActiveTrueAndStatusAndGuest_PhoneContainingIgnoreCaseOrderByCreatedAtDesc(hotelId, status, q);

        Map<Long, Booking> uniqueBookings = new LinkedHashMap<>();

        addBookings(uniqueBookings, byFirstName);
        addBookings(uniqueBookings, byLastName);
        addBookings(uniqueBookings, byEmail);
        addBookings(uniqueBookings, byPhone);


        return uniqueBookings.values().stream()
                .map(this::toListItemResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getById(Long hotelId, Long bookingId) {
        Booking booking = getBookingOrThrow(hotelId, bookingId);

        List<Long> roomIds = booking.getRooms().stream()
                .map(br -> br.getRoom().getId())
                .toList();

        return toResponse(booking, roomIds);
    }

    @Override
    public BookingResponse update(Long hotelId, Long bookingId, BookingUpdateRequest request) {
        validateUpdateRequest(hotelId, request);

        Booking booking = getBookingOrThrow(hotelId, bookingId);

        if (booking.getStatus() != BookingStatus.CREATED && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ConflictException("Only CREATED or CONFIRMED booking can be updated");
        }

        Guest guest = guestRepository.findByIdAndActiveTrue(request.getGuestId())
                .orElseThrow(() -> new NotFoundException("Guest not found"));

        List<Long> uniqueRoomIds = request.getRoomIds().stream().distinct().toList();
        List<Room> rooms = roomRepository.findAllById(uniqueRoomIds);

        if (rooms.size() != uniqueRoomIds.size()) {
            throw new NotFoundException("One or more rooms were not found");
        }

        validateRoomsBelongToHotel(rooms, hotelId);
        validateRoomsOperationallyAvailable(rooms);
        validateRoomAvailabilityForDates(hotelId, rooms, request.getCheckInDate(), request.getCheckOutDate(), booking.getId());
        validateCapacity(rooms, request.getAdults(), request.getChildren());

        booking.setGuest(guest);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());

        Integer adults = request.getAdults() != null ? request.getAdults() : 1;
        Integer children = request.getChildren() != null ? request.getChildren() : 0;

        booking.setAdults(adults);
        booking.setChildren(children);
        booking.setNotes(request.getNotes());

        booking.setTotalPrice(
                pricingService.calculateTotalPrice(
                        rooms,
                        request.getCheckInDate(),
                        request.getCheckOutDate()
                )
        );

        bookingRoomRepository.deleteAll(booking.getRooms());

        List<BookingRoom> newBookingRooms = rooms.stream()
                .map(room -> {
                    BookingRoom bookingRoom = new BookingRoom();
                    bookingRoom.setBooking(booking);
                    bookingRoom.setRoom(room);
                    return bookingRoom;
                })
                .toList();

        bookingRoomRepository.saveAll(newBookingRooms);
        booking.setRooms(newBookingRooms);

        Booking saved = bookingRepository.save(booking);

        return toResponse(saved, uniqueRoomIds);
    }

    @Override
    public BookingResponse confirm(Long hotelId, Long bookingId) {
        Booking booking = getBookingOrThrow(hotelId, bookingId);

        if (booking.getStatus() != BookingStatus.CREATED) {
            throw new ConflictException("Only CREATED booking can be confirmed");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);

        // ── отправка email ──────────────────────────────────────
        String guestEmail = saved.getGuest().getEmail();
        if (guestEmail != null && !guestEmail.isBlank()) {

            // берём email отеля из hotel_contacts
            String hotelEmail = hotelContactRepository
                    .findByHotel_Id(hotelId)
                    .stream()
                    .filter(c -> c.getEmail() != null && !c.getEmail().isBlank())
                    .findFirst()
                    .map(c -> c.getEmail())
                    .orElse(null);

            if (hotelEmail != null) {
                String roomTypeName = saved.getRooms().isEmpty() ? "—" :
                        saved.getRooms().get(0).getRoom().getRoomType() != null ?
                                saved.getRooms().get(0).getRoom().getRoomType().getName() : "—";

                String roomNumber = saved.getRooms().isEmpty() ? "—" :
                        saved.getRooms().get(0).getRoom().getRoomNumber();

                String totalPrice = saved.getTotalPrice() != null ?
                        saved.getTotalPrice().toString() : "—";

                String currency = saved.getCurrencyCode() != null ?
                        saved.getCurrencyCode().name() : "USD";

                String guestName = saved.getGuest().getFirstName() + " "
                        + saved.getGuest().getLastName();

                emailService.sendBookingConfirmation(
                        hotelEmail,                      // from — email отеля
                        saved.getHotel().getName(),      // from name — название отеля
                        guestEmail,                      // to — email гостя
                        guestName,
                        saved.getId(),
                        saved.getHotel().getName(),
                        roomTypeName,
                        roomNumber,
                        saved.getCheckInDate(),
                        saved.getCheckOutDate(),
                        totalPrice,
                        currency
                );
            }
        }
        // ────────────────────────────────────────────────────────

        List<Long> roomIds = saved.getRooms().stream()
                .map(br -> br.getRoom().getId())
                .toList();

        return toResponse(saved, roomIds);
    }

    @Override
    public BookingResponse cancel(Long hotelId, Long bookingId) {
        Booking booking = getBookingOrThrow(hotelId, bookingId);

        if (booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new ConflictException("Checked-out booking cannot be cancelled");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        Booking saved = bookingRepository.save(booking);

        List<Long> roomIds = saved.getRooms().stream()
                .map(br -> br.getRoom().getId())
                .toList();

        return toResponse(saved, roomIds);
    }

    @Override
    public BookingResponse checkIn(Long hotelId, Long bookingId) {
        Booking booking = getBookingOrThrow(hotelId, bookingId);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ConflictException("Only CONFIRMED booking can be checked in");
        }

        booking.setStatus(BookingStatus.CHECKED_IN);

        for (BookingRoom bookingRoom : booking.getRooms()) {
            Room room = bookingRoom.getRoom();
            room.setOccupancyStatus(OccupancyStatus.OCCUPIED);
            roomRepository.save(room);
        }

        Booking saved = bookingRepository.save(booking);

        List<Long> roomIds = saved.getRooms().stream()
                .map(br -> br.getRoom().getId())
                .toList();

        return toResponse(saved, roomIds);
    }

    @Override
    public BookingResponse checkOut(Long hotelId, Long bookingId) {
        Booking booking = getBookingOrThrow(hotelId, bookingId);

        validateBookingCanBeCheckedOut(booking);

        booking.setStatus(BookingStatus.CHECKED_OUT);

        for (BookingRoom bookingRoom : booking.getRooms()) {
            Room room = bookingRoom.getRoom();
            if (room.getOccupancyStatus() == OccupancyStatus.OCCUPIED) {
                room.setOccupancyStatus(OccupancyStatus.AVAILABLE);
            }
            roomRepository.save(room);
        }

        Booking saved = bookingRepository.save(booking);

        return mapToResponse(saved);
    }

    @Override
    public GuestResponse quickCreateGuest(BookingQuickGuestCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Guest request is required");
        }

        if (guestRepository.existsByEmailAndActiveTrue(request.getEmail())) {
            throw new ConflictException("Guest with this email already exists");
        }

        if (guestRepository.existsByPhoneAndActiveTrue(request.getPhone())) {
            throw new ConflictException("Guest with this phone already exists");
        }

        Guest guest = new Guest();
        guest.setFirstName(request.getFirstName());
        guest.setLastName(request.getLastName());
        guest.setPhone(request.getPhone());
        guest.setEmail(request.getEmail());
        guest.setDocumentNumber(request.getDocumentNumber());
        guest.setActive(true);

        Guest saved = guestRepository.save(guest);

        GuestResponse response = new GuestResponse();
        response.setId(saved.getId());
        response.setFirstName(saved.getFirstName());
        response.setLastName(saved.getLastName());
        response.setFullName(saved.getFullName());
        response.setPhone(saved.getPhone());
        response.setEmail(saved.getEmail());
        response.setDocumentNumber(saved.getDocumentNumber());
        response.setActive(saved.isActive());
        response.setCreatedAt(saved.getCreatedAt());
        response.setUpdatedAt(saved.getUpdatedAt());

        return response;
    }

    private void addBookings(Map<Long, Booking> uniqueBookings, List<Booking> bookings) {
        for (Booking booking : bookings) {
            uniqueBookings.putIfAbsent(booking.getId(), booking);
        }
    }

    private Booking getBookingOrThrow(Long hotelId, Long bookingId) {
        return bookingRepository.findByIdAndHotel_IdAndActiveTrue(bookingId, hotelId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
    }

    private void validateCreateRequest(Long hotelId, BookingCreateRequest request) {
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel id is required");
        }

        if (request == null) {
            throw new IllegalArgumentException("Booking request is required");
        }

        if (request.getGuestId() == null) {
            throw new IllegalArgumentException("Guest id is required");
        }

        if (request.getRoomIds() == null || request.getRoomIds().isEmpty()) {
            throw new IllegalArgumentException("At least one room must be selected");
        }

        Set<Long> uniqueRoomIds = new HashSet<>(request.getRoomIds());
        if (uniqueRoomIds.size() != request.getRoomIds().size()) {
            throw new IllegalArgumentException("Duplicate room ids are not allowed");
        }

        if (request.getCheckInDate() == null || request.getCheckOutDate() == null) {
            throw new IllegalArgumentException("Check-in and check-out dates are required");
        }

        if (!request.getCheckInDate().isBefore(request.getCheckOutDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }

        Integer adults = request.getAdults() != null ? request.getAdults() : 1;
        Integer children = request.getChildren() != null ? request.getChildren() : 0;

        if (adults <= 0) {
            throw new IllegalArgumentException("Adults must be at least 1");
        }

        if (children < 0) {
            throw new IllegalArgumentException("Children cannot be negative");
        }
    }

    private void validateUpdateRequest(Long hotelId, BookingUpdateRequest request) {
        if (hotelId == null) {
            throw new IllegalArgumentException("Hotel id is required");
        }

        if (request == null) {
            throw new IllegalArgumentException("Booking update request is required");
        }

        if (request.getGuestId() == null) {
            throw new IllegalArgumentException("Guest id is required");
        }

        if (request.getRoomIds() == null || request.getRoomIds().isEmpty()) {
            throw new IllegalArgumentException("At least one room must be selected");
        }

        Set<Long> uniqueRoomIds = new HashSet<>(request.getRoomIds());
        if (uniqueRoomIds.size() != request.getRoomIds().size()) {
            throw new IllegalArgumentException("Duplicate room ids are not allowed");
        }

        if (request.getCheckInDate() == null || request.getCheckOutDate() == null) {
            throw new IllegalArgumentException("Check-in and check-out dates are required");
        }

        if (!request.getCheckInDate().isBefore(request.getCheckOutDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }

        Integer adults = request.getAdults() != null ? request.getAdults() : 1;
        Integer children = request.getChildren() != null ? request.getChildren() : 0;

        if (adults <= 0) {
            throw new IllegalArgumentException("Adults must be at least 1");
        }

        if (children < 0) {
            throw new IllegalArgumentException("Children cannot be negative");
        }
    }

    private void validateRoomsBelongToHotel(List<Room> rooms, Long hotelId) {
        boolean invalidRoomFound = rooms.stream()
                .anyMatch(room -> room.getHotel() == null || !room.getHotel().getId().equals(hotelId));

        if (invalidRoomFound) {
            throw new IllegalArgumentException("One or more selected rooms do not belong to the selected hotel");
        }
    }

    private void validateRoomsOperationallyAvailable(List<Room> rooms) {
        for (Room room : rooms) {
            if (!room.isActive()) {
                throw new ConflictException("Room " + room.getRoomNumber() + " is inactive");
            }

            if (room.getTechnicalStatus() != TechnicalStatus.ACTIVE) {
                throw new ConflictException("Room " + room.getRoomNumber() + " is not technically available");
            }

            if (room.getOccupancyStatus() == OccupancyStatus.BLOCKED) {
                throw new ConflictException("Room " + room.getRoomNumber() + " is blocked");
            }
        }
    }

    private void validateRoomAvailabilityForDates(
            Long hotelId,
            List<Room> rooms,
            LocalDate checkIn,
            LocalDate checkOut,
            Long excludeBookingId
    ) {
        List<BookingStatus> blockingStatuses = BookingAvailabilityRules.blockingStatuses();

        List<Long> busyRoomIds = bookingRoomRepository.findBusyRoomIds(
                hotelId,
                blockingStatuses,
                checkIn,
                checkOut
        );

        Set<Long> busyRoomIdSet = new HashSet<>(busyRoomIds);

        if (excludeBookingId != null) {
            Booking currentBooking = bookingRepository.findByIdAndHotel_IdAndActiveTrue(excludeBookingId, hotelId)
                    .orElseThrow(() -> new NotFoundException("Booking not found"));

            Set<Long> currentRoomIds = currentBooking.getRooms().stream()
                    .map(br -> br.getRoom().getId())
                    .collect(HashSet::new, HashSet::add, HashSet::addAll);

            busyRoomIdSet.removeAll(currentRoomIds);
        }

        for (Room room : rooms) {
            if (busyRoomIdSet.contains(room.getId())) {
                throw new ConflictException(
                        "Room " + room.getRoomNumber() + " is not available for the selected dates"
                );
            }
        }
    }

    private void validateCapacity(List<Room> rooms, Integer adultsValue, Integer childrenValue) {
        int adults = adultsValue != null ? adultsValue : 1;
        int children = childrenValue != null ? childrenValue : 0;
        int totalGuests = adults + children;

        int totalCapacity = 0;

        for (Room room : rooms) {
            if (room.getCapacity() != null) {
                totalCapacity += room.getCapacity().intValue();
            }
        }

        if (totalCapacity < totalGuests) {
            throw new IllegalArgumentException(
                    "Selected rooms capacity is not enough for " + totalGuests + " guests"
            );
        }
    }
    private BookingResponse mapToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();

        response.setId(booking.getId());
        response.setHotelId(booking.getHotel().getId());

        response.setGuestId(booking.getGuest().getId());
        response.setGuestFullName(buildGuestFullName(booking));
        response.setGuestEmail(booking.getGuest().getEmail());
        response.setGuestPhone(booking.getGuest().getPhone());

        response.setAdults(booking.getAdults());
        response.setChildren(booking.getChildren());

        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());

        response.setStatus(booking.getStatus());
        response.setPaymentStatus(booking.getPaymentStatus());

        response.setTotalPrice(booking.getTotalPrice());
        response.setPaidAmount(booking.getPaidAmount());
        response.setCurrencyCode(
                booking.getCurrencyCode() != null ? booking.getCurrencyCode().name() : null
        );
        response.setNotes(booking.getNotes());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());

        response.setRooms(
                booking.getRooms().stream()
                        .map(this::mapBookingRoomItem)
                        .toList()
        );

        return response;
    }

    private BookingListItemResponse mapToListItemResponse(Booking booking) {
        BookingListItemResponse response = new BookingListItemResponse();

        response.setId(booking.getId());
        response.setGuestFullName(buildGuestFullName(booking));
        response.setGuestEmail(booking.getGuest().getEmail());
        response.setGuestPhone(booking.getGuest().getPhone());

        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());

        response.setStatus(booking.getStatus());
        response.setPaymentStatus(booking.getPaymentStatus());

        response.setTotalPrice(booking.getTotalPrice());
        response.setPaidAmount(booking.getPaidAmount());

        response.setRoomNumbers(
                booking.getRooms().stream()
                        .map(bookingRoom -> bookingRoom.getRoom().getRoomNumber())
                        .toList()
        );

        response.setCreatedAt(booking.getCreatedAt());

        return response;
    }

    private BookingResponse.BookingRoomItemResponse mapBookingRoomItem(BookingRoom bookingRoom) {
        BookingResponse.BookingRoomItemResponse item = new BookingResponse.BookingRoomItemResponse();

        item.setRoomId(bookingRoom.getRoom().getId());
        item.setRoomNumber(bookingRoom.getRoom().getRoomNumber());

        if (bookingRoom.getRoom().getRoomType() != null) {
            item.setRoomTypeName(bookingRoom.getRoom().getRoomType().getName());
        }

        item.setFloor(
                bookingRoom.getRoom().getFloor() != null
                        ? bookingRoom.getRoom().getFloor().intValue()
                        : null
        );

        item.setCapacity(
                bookingRoom.getRoom().getCapacity() != null
                        ? bookingRoom.getRoom().getCapacity().intValue()
                        : null
        );

        return item;
    }

    private String buildGuestFullName(Booking booking) {
        if (booking.getGuest() == null) {
            return "—";
        }

        String firstName = booking.getGuest().getFirstName() != null
                ? booking.getGuest().getFirstName().trim()
                : "";

        String lastName = booking.getGuest().getLastName() != null
                ? booking.getGuest().getLastName().trim()
                : "";

        String fullName = (firstName + " " + lastName).trim();

        if (!fullName.isBlank()) {
            return fullName;
        }

        if (booking.getGuest().getFullName() != null && !booking.getGuest().getFullName().isBlank()) {
            return booking.getGuest().getFullName();
        }

        if (booking.getGuest().getEmail() != null && !booking.getGuest().getEmail().isBlank()) {
            return booking.getGuest().getEmail();
        }

        return "Guest #" + booking.getGuest().getId();
    }

    private void validateBookingCanBeCheckedOut(Booking booking) {
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new ConflictException("Only CHECKED_IN booking can be checked out");
        }

        if (booking.getTotalPrice() == null) {
            return;
        }

        if (booking.getPaidAmount() == null) {
            throw new ConflictException("Booking cannot be checked out because payment is incomplete");
        }

        if (booking.getPaidAmount().compareTo(booking.getTotalPrice()) < 0) {
            throw new ConflictException("Booking cannot be checked out until full payment is completed");
        }
    }
    private BookingResponse toResponse(Booking booking, List<Long> roomIds) {
        BookingResponse response = new BookingResponse();

        response.setId(booking.getId());
        response.setHotelId(booking.getHotel().getId());

        response.setGuestId(booking.getGuest().getId());
        response.setGuestFullName(buildGuestFullName(booking));
        response.setGuestEmail(booking.getGuest().getEmail());
        response.setGuestPhone(booking.getGuest().getPhone());

        response.setAdults(booking.getAdults());
        response.setChildren(booking.getChildren());

        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());

        response.setStatus(booking.getStatus());
        response.setPaymentStatus(booking.getPaymentStatus());

        response.setTotalPrice(booking.getTotalPrice());
        response.setPaidAmount(booking.getPaidAmount());
        response.setCurrencyCode(
                booking.getCurrencyCode() != null ? booking.getCurrencyCode().name() : null
        );
        response.setNotes(booking.getNotes());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());

        response.setRooms(
                booking.getRooms().stream()
                        .map(this::mapBookingRoomItem)
                        .toList()
        );

        return response;
    }

    private BookingListItemResponse toListItemResponse(Booking booking) {
        BookingListItemResponse response = new BookingListItemResponse();

        response.setId(booking.getId());
        response.setGuestFullName(buildGuestFullName(booking));
        response.setGuestEmail(booking.getGuest().getEmail());
        response.setGuestPhone(booking.getGuest().getPhone());

        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());

        response.setStatus(booking.getStatus());
        response.setPaymentStatus(booking.getPaymentStatus());

        response.setTotalPrice(booking.getTotalPrice());
        response.setPaidAmount(booking.getPaidAmount());

        response.setCurrencyCode(
                booking.getCurrencyCode() != null ? booking.getCurrencyCode().name() : null
        );

        response.setAdults(booking.getAdults());
        response.setChildren(booking.getChildren());

        response.setRoomNumbers(
                booking.getRooms().stream()
                        .map(br -> br.getRoom().getRoomNumber())
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList()
        );

        String roomTypesSummary = booking.getRooms().stream()
                .map(br -> br.getRoom())
                .filter(Objects::nonNull)
                .map(Room::getRoomType)
                .filter(Objects::nonNull)
                .map(RoomType::getName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(", "));

        response.setRoomType(roomTypesSummary.isBlank() ? "—" : roomTypesSummary);

        response.setCreatedAt(booking.getCreatedAt());

        return response;
    }


}