package com.project.app.service.access;

import com.project.app.dto.access.CreateUserRequest;
import com.project.app.dto.access.HotelOptionResponse;
import com.project.app.dto.access.RoleOptionResponse;
import com.project.app.dto.access.UpdateUserRequest;
import com.project.app.dto.access.UserResponse;
import com.project.app.entity.access.Role;
import com.project.app.entity.access.User;
import com.project.app.entity.access.UserHotel;
import com.project.app.entity.access.UserRole;
import com.project.app.entity.hotel.Hotel;
import com.project.app.exception.ConflictException;
import com.project.app.exception.ForbiddenException;
import com.project.app.exception.NotFoundException;
import com.project.app.repository.access.RoleRepository;
import com.project.app.repository.access.UserHotelRepository;
import com.project.app.repository.access.UserRepository;
import com.project.app.repository.access.UserRoleRepository;
import com.project.app.repository.hotel.HotelRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Transactional
public class AccessUserServiceImpl implements AccessUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserHotelRepository userHotelRepository;
    private final HotelRepository hotelRepository;
    private final PasswordEncoder passwordEncoder;

    public AccessUserServiceImpl(UserRepository userRepository,
                                 RoleRepository roleRepository,
                                 UserRoleRepository userRoleRepository,
                                 UserHotelRepository userHotelRepository,
                                 HotelRepository hotelRepository,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.userHotelRepository = userHotelRepository;
        this.hotelRepository = hotelRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        Long currentUserId = getCurrentUserId();
        List<String> currentRoleNames = getCurrentUserRoleNames();

        String normalizedEmail = normalizeEmail(request.getEmail());
        String normalizedStatus = normalizeStatus(request.getStatus());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("User with this email already exists");
        }

        validateRequestedRolesForCreate(request.getRoleIds(), currentRoleNames);
        validateRequestedHotelsForAssignment(request.getHotelIds(), currentUserId, currentRoleNames);

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(normalizedStatus);

        User savedUser = userRepository.save(user);

        assignRoles(savedUser, request.getRoleIds());
        assignHotels(savedUser, request.getHotelIds());

        return mapToResponse(savedUser.getId());
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        Long currentUserId = getCurrentUserId();
        List<String> currentRoleNames = getCurrentUserRoleNames();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        validateManageUserAccess(userId, currentUserId, currentRoleNames);

        String normalizedEmail = normalizeEmail(request.getEmail());
        String normalizedStatus = normalizeStatus(request.getStatus());

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, userId)) {
            throw new ConflictException("Another user with this email already exists");
        }

        validateRequestedRolesForCreate(request.getRoleIds(), currentRoleNames);
        validateRequestedHotelsForAssignment(request.getHotelIds(), currentUserId, currentRoleNames);

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(normalizedEmail);
        user.setStatus(normalizedStatus);

        userRepository.save(user);

        userRoleRepository.deleteAll(userRoleRepository.findByUserId(userId));
        userHotelRepository.deleteAll(userHotelRepository.findByUserId(userId));

        assignRoles(user, request.getRoleIds());
        assignHotels(user, request.getHotelIds());

        return mapToResponse(userId);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long userId, String status) {
        Long currentUserId = getCurrentUserId();
        List<String> currentRoleNames = getCurrentUserRoleNames();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        validateManageUserAccess(userId, currentUserId, currentRoleNames);

        user.setStatus(normalizeStatus(status));
        userRepository.save(user);

        return mapToResponse(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        Long currentUserId = getCurrentUserId();
        List<String> currentRoleNames = getCurrentUserRoleNames();

        if (hasRole(currentRoleNames, "SYSTEM_ADMIN")) {
            userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
            return mapToResponse(userId);
        }

        if (hasRole(currentRoleNames, "HOTEL_ADMIN")) {
            List<Long> allowedHotelIds = getAccessibleHotelIds(currentUserId);

            boolean allowed = userRepository.existsUserInHotels(userId, allowedHotelIds);
            if (!allowed) {
                throw new ForbiddenException(
                        "You do not have permission to view this user"
                );
            }

            userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

            return mapToResponse(userId);
        }

        throw new ForbiddenException(
                "You do not have permission to view this user"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        Long currentUserId = getCurrentUserId();
        List<String> currentRoleNames = getCurrentUserRoleNames();

        if (hasRole(currentRoleNames, "SYSTEM_ADMIN")) {
            return userRepository.findAll()
                    .stream()
                    .map(user -> mapToResponse(user.getId()))
                    .toList();
        }

        if (hasRole(currentRoleNames, "HOTEL_ADMIN")) {
            List<Long> allowedHotelIds = getAccessibleHotelIds(currentUserId);

            if (allowedHotelIds.isEmpty()) {
                return List.of();
            }

            return userRepository.findDistinctUsersByHotelIds(allowedHotelIds)
                    .stream()
                    .map(user -> mapToResponse(user.getId()))
                    .toList();
        }

        throw new ForbiddenException(
                "You do not have permission to view users"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleOptionResponse> getAllRoles() {
        List<String> currentRoleNames = getCurrentUserRoleNames();

        if (hasRole(currentRoleNames, "SYSTEM_ADMIN")) {
            return roleRepository.findAll().stream()
                    .sorted(Comparator.comparing(Role::getName))
                    .map(role -> new RoleOptionResponse(role.getId(), role.getName()))
                    .toList();
        }

        if (hasRole(currentRoleNames, "HOTEL_ADMIN")) {
            return roleRepository.findAll().stream()
                    .filter(role -> role.getName() != null)
                    .filter(role ->
                            "FRONT_DESK".equalsIgnoreCase(role.getName())
                                    || "OPERATIONS_MANAGER".equalsIgnoreCase(role.getName())
                                    || "ACCOUNTANT".equalsIgnoreCase(role.getName())
                    )
                    .sorted(Comparator.comparing(Role::getName))
                    .map(role -> new RoleOptionResponse(role.getId(), role.getName()))
                    .toList();
        }

        throw new ForbiddenException("You do not have permission to view roles");
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelOptionResponse> getAllHotelsForAssignment() {
        Long currentUserId = getCurrentUserId();
        List<String> currentRoleNames = getCurrentUserRoleNames();

        if (hasRole(currentRoleNames, "SYSTEM_ADMIN")) {
            return hotelRepository.findAll().stream()
                    .sorted(Comparator.comparing(Hotel::getName))
                    .map(hotel -> new HotelOptionResponse(hotel.getId(), hotel.getName()))
                    .toList();
        }

        if (hasRole(currentRoleNames, "HOTEL_ADMIN")) {
            return getAccessibleHotelIds(currentUserId).stream()
                    .map(hotelId -> hotelRepository.findById(hotelId)
                            .orElseThrow(() -> new NotFoundException("Hotel not found with id: " + hotelId)))
                    .sorted(Comparator.comparing(Hotel::getName))
                    .map(hotel -> new HotelOptionResponse(hotel.getId(), hotel.getName()))
                    .toList();
        }

        throw new ForbiddenException("You do not have permission to view hotels");
    }

    private void assignRoles(User user, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new ConflictException("At least one role must be assigned");
        }

        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new NotFoundException("Role not found with id: " + roleId));

            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRoleRepository.save(userRole);
        }
    }

    private void assignHotels(User user, List<Long> hotelIds) {
        if (hotelIds == null || hotelIds.isEmpty()) {
            return;
        }

        for (Long hotelId : hotelIds) {
            Hotel hotel = hotelRepository.findById(hotelId)
                    .orElseThrow(() -> new NotFoundException("Hotel not found with id: " + hotelId));

            UserHotel userHotel = new UserHotel();
            userHotel.setUser(user);
            userHotel.setHotel(hotel);
            userHotelRepository.save(userHotel);
        }
    }

    private UserResponse mapToResponse(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        List<UserHotel> userHotels = userHotelRepository.findByUserId(userId);

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName((safe(user.getFirstName()) + " " + safe(user.getLastName())).trim());
        response.setEmail(user.getEmail());
        response.setStatus(user.getStatus());

        response.setRoles(
                userRoles.stream()
                        .map(UserRole::getRole)
                        .filter(role -> role != null && role.getName() != null)
                        .map(Role::getName)
                        .sorted()
                        .toList()
        );

        response.setHotelIds(
                userHotels.stream()
                        .map(UserHotel::getHotel)
                        .filter(hotel -> hotel != null && hotel.getId() != null)
                        .map(Hotel::getId)
                        .sorted()
                        .toList()
        );

        response.setHotelNames(
                userHotels.stream()
                        .map(UserHotel::getHotel)
                        .filter(hotel -> hotel != null && hotel.getName() != null)
                        .map(Hotel::getName)
                        .sorted()
                        .toList()
        );

        return response;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ACTIVE";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private Long getCurrentUserId() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof com.project.app.security.CustomUserDetails customUserDetails) {
            return customUserDetails.getUserId();
        }

        throw new ForbiddenException("Authenticated user not found");
    }

    private List<String> getCurrentUserRoleNames() {
        Long currentUserId = getCurrentUserId();
        return userRoleRepository.findByUserId(currentUserId).stream()
                .map(UserRole::getRole)
                .filter(role -> role != null && role.getName() != null)
                .map(Role::getName)
                .toList();
    }

    private boolean hasRole(List<String> roles, String roleName) {
        return roles.stream().anyMatch(role -> roleName.equalsIgnoreCase(role));
    }

    private List<Long> getAccessibleHotelIds(Long userId) {
        return userHotelRepository.findByUserId(userId).stream()
                .map(UserHotel::getHotel)
                .filter(hotel -> hotel != null && hotel.getId() != null)
                .map(Hotel::getId)
                .distinct()
                .toList();
    }

    private void validateViewUserAccess(Long targetUserId, Long currentUserId, List<String> currentRoleNames) {
        if (hasRole(currentRoleNames, "SYSTEM_ADMIN")) {
            return;
        }

        if (hasRole(currentRoleNames, "HOTEL_ADMIN")) {
            if (!isUserWithinHotelScope(targetUserId, currentUserId)) {
                throw new ForbiddenException("You cannot view this user");
            }
            return;
        }

        throw new ForbiddenException("You do not have permission to view users");
    }

    private void validateManageUserAccess(Long targetUserId, Long currentUserId, List<String> currentRoleNames) {
        if (hasRole(currentRoleNames, "SYSTEM_ADMIN")) {
            return;
        }

        if (hasRole(currentRoleNames, "HOTEL_ADMIN")) {
            boolean sameScope = userRepository.existsUserInHotels(targetUserId, getAccessibleHotelIds(currentUserId));

            if (!sameScope) {
                throw new ForbiddenException(
                        "You do not have permission to manage this user"
                );
            }

            List<String> targetRoleNames = userRoleRepository.findByUserId(targetUserId).stream()
                    .map(UserRole::getRole)
                    .filter(Objects::nonNull)
                    .map(Role::getName)
                    .filter(Objects::nonNull)
                    .toList();

            boolean forbiddenTarget = targetRoleNames.stream()
                    .anyMatch(role ->
                            role.equalsIgnoreCase("SYSTEM_ADMIN")
                                    || role.equalsIgnoreCase("HOTEL_ADMIN")
                    );

            if (forbiddenTarget) {
                throw new ForbiddenException(
                        "You do not have permission to manage this user"
                );
            }

            return;
        }

        throw new ForbiddenException(
                "You do not have permission to manage users"
        );
    }

    private boolean isUserWithinHotelScope(Long targetUserId, Long currentUserId) {
        List<Long> currentUserHotelIds = getAccessibleHotelIds(currentUserId);
        List<Long> targetUserHotelIds = userHotelRepository.findByUserId(targetUserId).stream()
                .map(UserHotel::getHotel)
                .filter(hotel -> hotel != null && hotel.getId() != null)
                .map(Hotel::getId)
                .toList();

        return targetUserHotelIds.stream().anyMatch(currentUserHotelIds::contains);
    }

    private void validateRequestedRolesForCurrentUser(List<Long> requestedRoleIds, List<String> currentRoleNames) {
        if (hasRole(currentRoleNames, "SYSTEM_ADMIN")) {
            return;
        }

        if (hasRole(currentRoleNames, "HOTEL_ADMIN")) {
            List<Role> requestedRoles = requestedRoleIds.stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new NotFoundException("Role not found with id: " + roleId)))
                    .toList();

            boolean containsForbiddenRole = requestedRoles.stream()
                    .map(Role::getName)
                    .anyMatch(roleName ->
                            "SYSTEM_ADMIN".equalsIgnoreCase(roleName)
                                    || "HOTEL_ADMIN".equalsIgnoreCase(roleName)
                    );

            if (containsForbiddenRole) {
                throw new ForbiddenException("You cannot assign this role");
            }

            return;
        }

        throw new ForbiddenException("You do not have permission to assign roles");
    }

    private void validateRequestedHotelsForCurrentUser(List<Long> requestedHotelIds,
                                                       Long currentUserId,
                                                       List<String> currentRoleNames) {
        if (requestedHotelIds == null || requestedHotelIds.isEmpty()) {
            if (hasRole(currentRoleNames, "SYSTEM_ADMIN")) {
                return;
            }
            throw new ConflictException("At least one hotel must be assigned");
        }

        if (hasRole(currentRoleNames, "SYSTEM_ADMIN")) {
            return;
        }

        if (hasRole(currentRoleNames, "HOTEL_ADMIN")) {
            List<Long> allowedHotelIds = getAccessibleHotelIds(currentUserId);

            boolean containsForbiddenHotel = requestedHotelIds.stream()
                    .anyMatch(hotelId -> !allowedHotelIds.contains(hotelId));

            if (containsForbiddenHotel) {
                throw new ForbiddenException("You cannot assign users to this hotel");
            }

            return;
        }

        throw new ForbiddenException("You do not have permission to assign hotels");
    }

    private void validateRequestedRolesForCreate(List<Long> requestedRoleIds, List<String> currentRoleNames) {
        if (requestedRoleIds == null || requestedRoleIds.isEmpty()) {
            throw new ConflictException("At least one role must be assigned");
        }

        List<Role> requestedRoles = requestedRoleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new NotFoundException("Role not found with id: " + roleId)))
                .toList();

        if (hasRole(currentRoleNames, "SYSTEM_ADMIN")) {
            return;
        }

        if (hasRole(currentRoleNames, "HOTEL_ADMIN")) {
            boolean containsForbiddenRole = requestedRoles.stream()
                    .map(Role::getName)
                    .filter(Objects::nonNull)
                    .anyMatch(roleName ->
                            roleName.equalsIgnoreCase("SYSTEM_ADMIN")
                                    || roleName.equalsIgnoreCase("HOTEL_ADMIN")
                    );

            if (containsForbiddenRole) {
                throw new ForbiddenException(
                        "You cannot assign this role"
                );
            }

            return;
        }

        throw new ForbiddenException(
                "You do not have permission to assign roles"
        );
    }
    private void validateRequestedHotelsForAssignment(
            List<Long> requestedHotelIds,
            Long currentUserId,
            List<String> currentRoleNames
    ) {
        if (requestedHotelIds == null || requestedHotelIds.isEmpty()) {
            throw new ConflictException("At least one hotel must be assigned");
        }

        for (Long hotelId : requestedHotelIds) {
            if (!hotelRepository.existsById(hotelId)) {
                throw new NotFoundException("Hotel not found with id: " + hotelId);
            }
        }

        if (hasRole(currentRoleNames, "SYSTEM_ADMIN")) {
            return;
        }

        if (hasRole(currentRoleNames, "HOTEL_ADMIN")) {
            List<Long> allowedHotelIds = getAccessibleHotelIds(currentUserId);

            boolean containsForbiddenHotel = requestedHotelIds.stream()
                    .anyMatch(hotelId -> !allowedHotelIds.contains(hotelId));

            if (containsForbiddenHotel) {
                throw new ForbiddenException(
                        "You cannot assign user to this hotel"
                );
            }

            return;
        }

        throw new ForbiddenException(
                "You do not have permission to assign hotels"
        );
    }
}