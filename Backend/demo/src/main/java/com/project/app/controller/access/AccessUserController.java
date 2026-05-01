package com.project.app.controller.access;

import com.project.app.dto.access.CreateUserRequest;
import com.project.app.dto.access.HotelOptionResponse;
import com.project.app.dto.access.RoleOptionResponse;
import com.project.app.dto.access.UpdateUserRequest;
import com.project.app.dto.access.UpdateUserStatusRequest;
import com.project.app.dto.access.UserResponse;
import com.project.app.service.access.AccessUserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/access")
public class AccessUserController {

    private final AccessUserService accessUserService;

    public AccessUserController(AccessUserService accessUserService) {
        this.accessUserService = accessUserService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'HOTEL_ADMIN')")
    public List<UserResponse> getAllUsers() {
        return accessUserService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'HOTEL_ADMIN')")
    public UserResponse getUserById(@PathVariable Long id) {
        return accessUserService.getUserById(id);
    }

    @PostMapping("/users")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'HOTEL_ADMIN')")
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return accessUserService.createUser(request);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'HOTEL_ADMIN')")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return accessUserService.updateUser(id, request);
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'HOTEL_ADMIN')")
    public UserResponse updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return accessUserService.updateUserStatus(id, request.getStatus());
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'HOTEL_ADMIN')")
    public List<RoleOptionResponse> getAllRoles() {
        return accessUserService.getAllRoles();
    }

    @GetMapping("/hotels")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'HOTEL_ADMIN')")
    public List<HotelOptionResponse> getAllHotels() {
        return accessUserService.getAllHotelsForAssignment();
    }
}