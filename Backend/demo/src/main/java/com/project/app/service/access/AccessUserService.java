package com.project.app.service.access;

import com.project.app.dto.access.CreateUserRequest;
import com.project.app.dto.access.HotelOptionResponse;
import com.project.app.dto.access.RoleOptionResponse;
import com.project.app.dto.access.UpdateUserRequest;
import com.project.app.dto.access.UserResponse;

import java.util.List;

public interface AccessUserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(Long userId, UpdateUserRequest request);

    UserResponse updateUserStatus(Long userId, String status);

    UserResponse getUserById(Long userId);

    List<UserResponse> getAllUsers();

    List<RoleOptionResponse> getAllRoles();

    List<HotelOptionResponse> getAllHotelsForAssignment();

}