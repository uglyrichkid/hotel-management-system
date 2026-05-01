package com.project.app.controller.access;

import com.project.app.dto.access.LoginRequest;
import com.project.app.dto.access.LoginResponse;
import com.project.app.service.access.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public LoginResponse me() {
        return authService.me();
    }
}