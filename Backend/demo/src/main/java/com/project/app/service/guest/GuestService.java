package com.project.app.service.guest;

import com.project.app.dto.guest.GuestCreateRequest;
import com.project.app.dto.guest.GuestListItemResponse;
import com.project.app.dto.guest.GuestResponse;
import com.project.app.dto.guest.GuestSearchRequest;
import com.project.app.dto.guest.GuestUpdateRequest;

import java.util.List;

public interface GuestService {

    GuestResponse create(GuestCreateRequest request);

    GuestResponse getById(Long guestId);

    List<GuestListItemResponse> getAll();

    List<GuestListItemResponse> search(GuestSearchRequest request);

    GuestResponse update(Long guestId, GuestUpdateRequest request);

    void delete(Long guestId);
}