package com.project.app.service.guest;

import com.project.app.dto.guest.GuestCreateRequest;
import com.project.app.dto.guest.GuestListItemResponse;
import com.project.app.dto.guest.GuestResponse;
import com.project.app.dto.guest.GuestSearchRequest;
import com.project.app.dto.guest.GuestUpdateRequest;
import com.project.app.entity.guest.Guest;
import com.project.app.exception.ConflictException;
import com.project.app.exception.NotFoundException;
import com.project.app.repository.guest.GuestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;

    public GuestServiceImpl(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    @Override
    public GuestResponse create(GuestCreateRequest request) {
        validateGuestUniqueness(request.getEmail(), request.getPhone());

        Guest guest = new Guest();
        guest.setFirstName(request.getFirstName());
        guest.setLastName(request.getLastName());
        guest.setPhone(request.getPhone());
        guest.setEmail(request.getEmail());
        guest.setDocumentNumber(request.getDocumentNumber());
        guest.setActive(true);

        Guest saved = guestRepository.save(guest);

        return toResponse(saved);
    }

    @Override
    public GuestResponse getById(Long guestId) {
        Guest guest = getActiveGuestOrThrow(guestId);
        return toResponse(guest);
    }

    @Override
    public List<GuestListItemResponse> getAll() {
        return guestRepository.findByActiveTrueOrderByIdDesc()
                .stream()
                .map(this::toListItemResponse)
                .toList();
    }

    @Override
    public List<GuestListItemResponse> search(GuestSearchRequest request) {
        String query = request != null ? request.getQuery() : null;

        if (query == null || query.isBlank()) {
            return getAll();
        }

        return guestRepository
                .findByActiveTrueAndFirstNameContainingIgnoreCaseOrActiveTrueAndLastNameContainingIgnoreCaseOrActiveTrueAndEmailContainingIgnoreCaseOrActiveTrueAndPhoneContainingIgnoreCaseOrderByIdDesc(
                        query, query, query, query
                )
                .stream()
                .map(this::toListItemResponse)
                .toList();
    }

    @Override
    public GuestResponse update(Long guestId, GuestUpdateRequest request) {
        Guest guest = getActiveGuestOrThrow(guestId);

        validateGuestUniquenessForUpdate(guestId, request.getEmail(), request.getPhone());

        guest.setFirstName(request.getFirstName());
        guest.setLastName(request.getLastName());
        guest.setPhone(request.getPhone());
        guest.setEmail(request.getEmail());
        guest.setDocumentNumber(request.getDocumentNumber());

        Guest saved = guestRepository.save(guest);

        return toResponse(saved);
    }

    @Override
    public void delete(Long guestId) {
        Guest guest = getActiveGuestOrThrow(guestId);
        guest.setActive(false);
        guestRepository.save(guest);
    }

    private Guest getActiveGuestOrThrow(Long guestId) {
        return guestRepository.findByIdAndActiveTrue(guestId)
                .orElseThrow(() -> new NotFoundException("Guest not found"));
    }

    private void validateGuestUniqueness(String email, String phone) {
        if (guestRepository.existsByEmailAndActiveTrue(email)) {
            throw new ConflictException("Guest with this email already exists");
        }

        if (guestRepository.existsByPhoneAndActiveTrue(phone)) {
            throw new ConflictException("Guest with this phone already exists");
        }
    }

    private void validateGuestUniquenessForUpdate(Long guestId, String email, String phone) {
        List<Guest> activeGuests = guestRepository.findByActiveTrueOrderByIdDesc();

        for (Guest guest : activeGuests) {
            if (!guest.getId().equals(guestId)) {
                if (guest.getEmail().equalsIgnoreCase(email)) {
                    throw new ConflictException("Guest with this email already exists");
                }
                if (guest.getPhone().equals(phone)) {
                    throw new ConflictException("Guest with this phone already exists");
                }
            }
        }
    }

    private GuestResponse toResponse(Guest guest) {
        GuestResponse response = new GuestResponse();
        response.setId(guest.getId());
        response.setFirstName(guest.getFirstName());
        response.setLastName(guest.getLastName());
        response.setFullName(guest.getFullName());
        response.setPhone(guest.getPhone());
        response.setEmail(guest.getEmail());
        response.setDocumentNumber(guest.getDocumentNumber());
        response.setActive(guest.isActive());
        response.setCreatedAt(guest.getCreatedAt());
        response.setUpdatedAt(guest.getUpdatedAt());
        return response;
    }

    private GuestListItemResponse toListItemResponse(Guest guest) {
        GuestListItemResponse response = new GuestListItemResponse();
        response.setId(guest.getId());
        response.setFullName(guest.getFullName());
        response.setPhone(guest.getPhone());
        response.setEmail(guest.getEmail());
        response.setDocumentNumber(guest.getDocumentNumber());
        response.setActive(guest.isActive());
        return response;
    }
}