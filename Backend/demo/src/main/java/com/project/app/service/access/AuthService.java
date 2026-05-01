package com.project.app.service.access;

import com.project.app.dto.access.LoginRequest;
import com.project.app.dto.access.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
    LoginResponse me();
}