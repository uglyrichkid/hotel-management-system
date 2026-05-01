package com.project.app.entity.booking.guest;

import com.project.app.dto.guest.GuestCreateRequest;
import com.project.app.dto.guest.GuestListItemResponse;
import com.project.app.dto.guest.GuestResponse;
import com.project.app.dto.guest.GuestSearchRequest;
import com.project.app.dto.guest.GuestUpdateRequest;
import com.project.app.service.guest.GuestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guests")
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuestResponse create(@Valid @RequestBody GuestCreateRequest request) {
        return guestService.create(request);
    }

    @GetMapping("/{guestId}")
    public GuestResponse getById(@PathVariable Long guestId) {
        return guestService.getById(guestId);
    }

    @GetMapping
    public List<GuestListItemResponse> search(@ModelAttribute GuestSearchRequest request) {
        return guestService.search(request);
    }

    @PutMapping("/{guestId}")
    public GuestResponse update(
            @PathVariable Long guestId,
            @Valid @RequestBody GuestUpdateRequest request
    ) {
        return guestService.update(guestId, request);
    }

    @DeleteMapping("/{guestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long guestId) {
        guestService.delete(guestId);
    }
}