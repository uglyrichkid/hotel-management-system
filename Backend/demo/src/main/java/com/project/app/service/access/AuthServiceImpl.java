package com.project.app.service.access;

import com.project.app.dto.access.LoginRequest;
import com.project.app.dto.access.LoginResponse;
import com.project.app.entity.access.User;
import com.project.app.exception.ConflictException;
import com.project.app.exception.ForbiddenException;
import com.project.app.exception.NotFoundException;
import com.project.app.exception.UnauthorizedException;
import com.project.app.repository.access.UserHotelRepository;
import com.project.app.repository.access.UserRepository;
import com.project.app.repository.access.UserRoleRepository;
import com.project.app.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.project.app.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserHotelRepository userHotelRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           UserRoleRepository userRoleRepository,
                           UserHotelRepository userHotelRepository,
                           PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.userHotelRepository = userHotelRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new ForbiddenException("User is not active");
        }

        return buildAuthResponse(user);
    }

    @Override
    public LoginResponse me() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof CustomUserDetails customUserDetails)) {
            throw new UnauthorizedException("Authenticated user not found");
        }

        Long userId = customUserDetails.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new ForbiddenException("User is not active");
        }

        return buildAuthResponse(user);
    }

    private LoginResponse buildAuthResponse(User user) {
        List<String> roles = userRoleRepository.findByUserId(user.getId())
                .stream()
                .map(userRole -> userRole.getRole().getName())
                .toList();

        List<Long> hotelIds = userHotelRepository.findByUserId(user.getId())
                .stream()
                .map(userHotel -> userHotel.getHotel().getId())
                .toList();

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFirstName() + " " + user.getLastName());
        response.setStatus(user.getStatus());
        response.setRoles(roles);
        response.setHotelIds(hotelIds);
        response.setToken(token);

        return response;
    }
}