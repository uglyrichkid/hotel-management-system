package com.project.app.service.hotel;

import com.project.app.dto.hotel.*;
import com.project.app.entity.access.User;
import com.project.app.entity.access.UserHotel;
import com.project.app.entity.access.UserRole;
import com.project.app.entity.common.CurrencyCode;
import com.project.app.entity.geo.City;
import com.project.app.entity.hotel.*;
import com.project.app.exception.ConflictException;
import com.project.app.exception.NotFoundException;
import com.project.app.repository.access.UserHotelRepository;
import com.project.app.repository.access.UserRoleRepository;
import com.project.app.repository.hotel.HotelContactRepository;
import com.project.app.repository.hotel.HotelImageRepository;
import com.project.app.repository.hotel.HotelPolicyRepository;
import com.project.app.repository.hotel.HotelRepository;
import com.project.app.repository.geo.CityRepository;
import com.project.app.repository.room.RoomRepository;
import com.project.app.dto.hotel.HotelImageItemResponse;
import com.project.app.dto.hotel.HotelManageResponse;
import com.project.app.dto.hotel.HotelPolicyItemResponse;
import com.project.app.entity.hotel.HotelContact;
import com.project.app.entity.hotel.HotelImage;
import com.project.app.entity.hotel.HotelPolicy;
import com.project.app.entity.hotel.Hotel;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import com.project.app.dto.hotel.HotelDirectorCreateRequest;
import com.project.app.dto.hotel.HotelMemberCreateRequest;
import com.project.app.dto.hotel.HotelMemberResponse;
import com.project.app.entity.access.Role;
import com.project.app.repository.access.RoleRepository;
import com.project.app.repository.access.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Objects;

@Service
public class HotelService {
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final UserHotelRepository userHotelRepository;
    private final UserRoleRepository userRoleRepository;
    private final HotelContactRepository hotelContactRepository;
    private final HotelPolicyRepository hotelPolicyRepository;
    private final HotelImageRepository hotelImageRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public HotelService(
            HotelRepository hotelRepository,
            RoomRepository roomRepository,
            UserHotelRepository userHotelRepository,
            UserRoleRepository userRoleRepository,
            HotelContactRepository hotelContactRepository,
            HotelPolicyRepository hotelPolicyRepository,
            HotelImageRepository hotelImageRepository,
            CityRepository cityRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.userHotelRepository = userHotelRepository;
        this.userRoleRepository = userRoleRepository;
        this.hotelContactRepository = hotelContactRepository;
        this.hotelPolicyRepository = hotelPolicyRepository;
        this.hotelImageRepository = hotelImageRepository;
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public HotelResponse create(@Valid HotelCreationRequest request) {

        if (hotelRepository.existsByName(request.getName())) {
            throw new ConflictException("Hotel with name already exists: " + request.getName());
        }

        if (request.getDirector() == null) {
            throw new ConflictException("Director information is required");
        }

        if (userRepository.existsByEmail(request.getDirector().getEmail())) {
            throw new ConflictException("User already exists with email: " + request.getDirector().getEmail());
        }

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new NotFoundException("City not found: id=" + request.getCityId()));

        Hotel hotel = new Hotel();
        hotel.setName(request.getName().trim());
        hotel.setAddress(request.getAddress().trim());
        hotel.setStars(request.getStars());
        hotel.setCity(city);
        hotel.setDescription(request.getDescription());
        hotel.setCurrencyCode(parseCurrencyCode(request.getCurrencyCode()));

        if (request.getCheckInTime() != null && !request.getCheckInTime().isBlank()) {
            hotel.setCheckInTime(java.time.LocalTime.parse(request.getCheckInTime()));
        }

        if (request.getCheckOutTime() != null && !request.getCheckOutTime().isBlank()) {
            hotel.setCheckOutTime(java.time.LocalTime.parse(request.getCheckOutTime()));
        }

        Hotel savedHotel = hotelRepository.save(hotel);

        HotelContact contact = new HotelContact();
        contact.setHotel(savedHotel);
        contact.setPhone(request.getPhone());
        contact.setEmail(request.getEmail());
        contact.setWebsite(request.getWebsite());
        hotelContactRepository.save(contact);

        createDirectorForHotel(savedHotel, request.getDirector());

        return toResponse(savedHotel);
    }

    @Transactional
    public HotelResponse update(Long id, @Valid HotelUpdateRequest request) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Hotel not found: " + id));

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new NotFoundException("City not found: " + request.getCityId()));

        boolean duplicateExist = hotelRepository.existsByCity_IdAndNameAndAddressAndIdNot(
                request.getCityId(),
                request.getName(),
                request.getAddress(),
                id
        );

        if (duplicateExist) {
            throw new ConflictException("Hotel already exist in this city with same name and address");
        }

        hotel.setName(request.getName().trim());
        hotel.setAddress(request.getAddress().trim());
        hotel.setStars(request.getStars());
        hotel.setCity(city);
        hotel.setStatus(request.getStatus());
        hotel.setDescription(request.getDescription());
        hotel.setCurrencyCode(parseCurrencyCode(request.getCurrencyCode()));

        if (request.getCheckInTime() != null && !request.getCheckInTime().isBlank()) {
            hotel.setCheckInTime(java.time.LocalTime.parse(request.getCheckInTime()));
        } else {
            hotel.setCheckInTime(null);
        }

        if (request.getCheckOutTime() != null && !request.getCheckOutTime().isBlank()) {
            hotel.setCheckOutTime(java.time.LocalTime.parse(request.getCheckOutTime()));
        } else {
            hotel.setCheckOutTime(null);
        }

        hotel.setUpdatedAt(java.time.LocalDateTime.now());

        Hotel savedHotel = hotelRepository.save(hotel);

        HotelContact contact = hotelContactRepository.findByHotel_Id(id).orElse(null);

        if (contact == null) {
            contact = new HotelContact();
            contact.setHotel(savedHotel);
        }

        contact.setPhone(request.getPhone());
        contact.setEmail(request.getEmail());
        contact.setWebsite(request.getWebsite());
        contact.setUpdatedAt(java.time.LocalDateTime.now());

        hotelContactRepository.save(contact);

        hotelPolicyRepository.deleteByHotelId(id);

        if (request.getPolicies() != null) {
            for (HotelPolicyUpsertRequest item : request.getPolicies()) {
                if (item == null) continue;
                if (item.getPolicyType() == null || item.getPolicyType().isBlank()) continue;
                if (item.getText() == null || item.getText().isBlank()) continue;

                HotelPolicy policy = new HotelPolicy();
                policy.setHotel(savedHotel);
                policy.setPolicyType(item.getPolicyType().trim());
                policy.setText(item.getText().trim());
                policy.setUpdatedAt(java.time.LocalDateTime.now());

                hotelPolicyRepository.save(policy);
            }
        }

        hotelImageRepository.deleteByHotelId(id);

        if (request.getImages() != null) {
            for (HotelImageUpsertRequest item : request.getImages()) {
                if (item == null) continue;
                if (item.getUrl() == null || item.getUrl().isBlank()) continue;

                HotelImage image = new HotelImage();
                image.setHotel(savedHotel);
                image.setUrl(item.getUrl().trim());
                image.setMain(Boolean.TRUE.equals(item.getIsMain()));
                image.setSortOrder(item.getSortOrder());
                image.setCreatedAt(java.time.LocalDateTime.now());

                hotelImageRepository.save(image);
            }
        }

        return toResponse(savedHotel);
    }


    private HotelResponse toResponse(Hotel h){
        HotelResponse r = new HotelResponse();
        r.setId(h.getId());
        r.setName(h.getName());
        r.setAddress(h.getAddress());
        r.setStars(h.getStars());
        r.setStatus(h.getStatus());

        if (h.getCity() != null){
            r.setCityId(h.getCity().getId());
            r.setCityName(h.getCity().getName());
        }

        r.setCurrencyCode(
                h.getCurrencyCode() != null ? h.getCurrencyCode().name() : null
        );

        r.setCreatedAt(h.getCreatedAt());
        r.setUpdatedAt(h.getUpdatedAt());
        return r;
    }

    @Transactional(readOnly = true)
    public HotelResponse getById(Long id){

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Hotel not Found: " + id));
        return toResponse(hotel);
    }

    public List<Hotel> getAll(){
        return hotelRepository.findAll();
    }
    public List<HotelListItemResponse> getHotels(HotelStatus status, Long cityId, Short minStars, String name) {
        List<Hotel> hotels;

        boolean hasName = name != null && !name.trim().isEmpty();
        String normalizedName = hasName ? name.trim() : null;

        if (status != null && cityId != null && minStars != null && hasName) {
            hotels = hotelRepository.findByStatusAndCity_IdAndStarsGreaterThanEqualAndNameContainingIgnoreCase(
                    status, cityId, minStars, normalizedName
            );
        } else if (status != null && cityId != null && hasName) {
            hotels = hotelRepository.findByStatusAndCity_IdAndNameContainingIgnoreCase(
                    status, cityId, normalizedName
            );
        } else if (status != null && minStars != null && hasName) {
            hotels = hotelRepository.findByStatusAndStarsGreaterThanEqualAndNameContainingIgnoreCase(
                    status, minStars, normalizedName
            );
        } else if (cityId != null && minStars != null && hasName) {
            hotels = hotelRepository.findByCity_IdAndStarsGreaterThanEqualAndNameContainingIgnoreCase(
                    cityId, minStars, normalizedName
            );
        } else if (status != null && hasName) {
            hotels = hotelRepository.findByStatusAndNameContainingIgnoreCase(
                    status, normalizedName
            );
        } else if (cityId != null && hasName) {
            hotels = hotelRepository.findByCity_IdAndNameContainingIgnoreCase(
                    cityId, normalizedName
            );
        } else if (minStars != null && hasName) {
            hotels = hotelRepository.findByStarsGreaterThanEqualAndNameContainingIgnoreCase(
                    minStars, normalizedName
            );
        } else if (hasName) {
            hotels = hotelRepository.findByNameContainingIgnoreCase(normalizedName);
        } else if (status != null && cityId != null && minStars != null) {
            hotels = hotelRepository.findByStatusAndCity_IdAndStarsGreaterThanEqual(status, cityId, minStars);
        } else if (status != null && cityId != null) {
            hotels = hotelRepository.findByStatusAndCity_Id(status, cityId);
        } else if (status != null && minStars != null) {
            hotels = hotelRepository.findByStatusAndStarsGreaterThanEqual(status, minStars);
        } else if (cityId != null && minStars != null) {
            hotels = hotelRepository.findByCity_IdAndStarsGreaterThanEqual(cityId, minStars);
        } else if (status != null) {
            hotels = hotelRepository.findByStatus(status);
        } else if (cityId != null) {
            hotels = hotelRepository.findByCity_Id(cityId);
        } else if (minStars != null) {
            hotels = hotelRepository.findByStarsGreaterThanEqual(minStars);
        } else {
            hotels = hotelRepository.findAll(
                    org.springframework.data.domain.Sort.by(
                            org.springframework.data.domain.Sort.Direction.DESC,
                            "id"
                    )
            );
        }

        return hotels.stream().map(this::toListItem).toList();
    }
    private HotelListItemResponse toListItem(Hotel h) {
        HotelListItemResponse r = new HotelListItemResponse();
        r.setId(h.getId());
        r.setName(h.getName());
        r.setAddress(h.getAddress());
        r.setStars(h.getStars());
        r.setStatus(h.getStatus());

        if (h.getCity() != null) {
            r.setCityId(h.getCity().getId());
            r.setCityName(h.getCity().getName());
        }

        r.setRoomsCount(roomRepository.countByHotel_IdAndActiveTrue(h.getId()));
        r.setDirectorName(resolveDirectorName(h.getId()));

        return r;
    }

    public Hotel getByName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be empty");
        }
        return hotelRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Hotel not fount: name: " + name));
    }

    public List<Hotel> getByMinStars(Short minStars){
        return hotelRepository.findByStarsGreaterThanEqual((short) minStars);
    }
    public void deactivate(Long id){

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Hotel not found: id = " + id));

        if (hotel.getStatus() == HotelStatus.INACTIVE){
            return;
        }
        hotel.setStatus(HotelStatus.INACTIVE);
        hotel.setUpdatedAt(java.time.LocalDateTime.now());

        hotelRepository.save(hotel);
    }
    @Transactional
    public HotelResponse activate(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Hotel not found with id: " + id));

        hotel.setStatus(HotelStatus.ACTIVE);

        Hotel savedHotel = hotelRepository.save(hotel);

        return toResponse(savedHotel);
    }

    @Transactional(readOnly = true)
    public HotelManageResponse getManageDetails(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Hotel not found with id: " + id));

        HotelContact contact = hotelContactRepository.findByHotel_Id(id).orElse(null);
        List<HotelPolicy> policies = hotelPolicyRepository.findByHotelIdOrderByIdAsc(id);
        List<HotelImage> images = hotelImageRepository.findByHotelIdOrderBySortOrderAscIdAsc(id);

        long roomsCount = roomRepository.countByHotel_IdAndActiveTrue(id);
        String directorName = resolveDirectorName(id);

        HotelManageResponse response = new HotelManageResponse();
        response.setId(hotel.getId());
        response.setName(hotel.getName());
        response.setAddress(hotel.getAddress());
        response.setCityId(hotel.getCity().getId());
        response.setCityName(hotel.getCity().getName());
        response.setStars(hotel.getStars());
        response.setDescription(hotel.getDescription());
        response.setCheckInTime(hotel.getCheckInTime());
        response.setCheckOutTime(hotel.getCheckOutTime());
        response.setStatus(hotel.getStatus());
        response.setCurrencyCode(
                hotel.getCurrencyCode() != null ? hotel.getCurrencyCode().name() : null
        );

        response.setRoomsCount(roomsCount);
        response.setDirectorName(directorName);

        if (contact != null) {
            response.setPhone(contact.getPhone());
            response.setEmail(contact.getEmail());
            response.setWebsite(contact.getWebsite());
        }

        response.setPolicies(
                policies.stream()
                        .map(policy -> new HotelPolicyItemResponse(
                                policy.getId(),
                                policy.getPolicyType(),
                                policy.getText()
                        ))
                        .toList()
        );

        response.setImages(
                images.stream()
                        .map(image -> new HotelImageItemResponse(
                                image.getId(),
                                image.getUrl(),
                                image.getMain(),
                                image.getSortOrder()
                        ))
                        .toList()
        );
        response.setMembers(getHotelMembers(id));

        return response;
    }
    private String buildFullName(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();

        return fullName.isBlank() ? user.getEmail() : fullName;
    }

    private String resolveDirectorName(Long hotelId) {
        List<UserHotel> userHotels = userHotelRepository.findByHotelId(hotelId);

        for (UserHotel userHotel : userHotels) {
            User user = userHotel.getUser();

            List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());

            boolean isDirector = userRoles.stream()
                    .map(userRole -> userRole.getRole().getName())
                    .filter(Objects::nonNull)
                    .anyMatch(roleName ->
                            roleName.equalsIgnoreCase("HOTEL_ADMIN")
                                    || roleName.equalsIgnoreCase("HOTEL_ADMIN")
                    );

            if (isDirector) {
                return buildFullName(user);
            }
        }

        return "N/A";
    }

    @Transactional
    public HotelMemberResponse addMember(Long hotelId, @Valid HotelMemberCreateRequest request) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel not found with id: " + hotelId));

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("User already exists with email: " + request.getEmail());
        }

        String normalizedRoleName = request.getRoleName().trim().toUpperCase();

        Role role = roleRepository.findByName(normalizedRoleName)
                .orElseThrow(() -> new NotFoundException("Role not found: " + normalizedRoleName));

        if ("ADMIN".equalsIgnoreCase(normalizedRoleName)) {
            throw new ConflictException("ADMIN cannot be assigned as hotel member");
        }

        if ("HOTEL_ADMIN".equalsIgnoreCase(normalizedRoleName) && hasDirector(hotelId)) {
            throw new ConflictException("This hotel already has a director");
        }

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(request.getEmail().trim());
        user.setPhone(request.getPhone());
        user.setStatus(
                request.getStatus() == null || request.getStatus().isBlank()
                        ? "ACTIVE"
                        : request.getStatus().trim().toUpperCase()
        );
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setUser(savedUser);
        userRole.setRole(role);
        userRoleRepository.save(userRole);

        UserHotel userHotel = new UserHotel();
        userHotel.setUser(savedUser);
        userHotel.setHotel(hotel);
        userHotelRepository.save(userHotel);

        return toMemberResponse(savedUser, normalizedRoleName);
    }

    private void createDirectorForHotel(Hotel hotel, HotelDirectorCreateRequest directorRequest) {
        Role directorRole = roleRepository.findByName("HOTEL_ADMIN")
                .orElseThrow(() -> new NotFoundException("Role not found: HOTEL_ADMIN"));

        User director = new User();
        director.setFirstName(directorRequest.getFirstName().trim());
        director.setLastName(directorRequest.getLastName().trim());
        director.setEmail(directorRequest.getEmail().trim());
        director.setPhone(directorRequest.getPhone());
        director.setStatus("ACTIVE");
        director.setPasswordHash(passwordEncoder.encode(directorRequest.getPassword()));

        User savedDirector = userRepository.save(director);

        UserRole userRole = new UserRole();
        userRole.setUser(savedDirector);
        userRole.setRole(directorRole);
        userRoleRepository.save(userRole);

        UserHotel userHotel = new UserHotel();
        userHotel.setUser(savedDirector);
        userHotel.setHotel(hotel);
        userHotelRepository.save(userHotel);
    }

    private boolean hasDirector(Long hotelId) {
        List<UserHotel> userHotels = userHotelRepository.findByHotelId(hotelId);

        for (UserHotel userHotel : userHotels) {
            User user = userHotel.getUser();

            boolean isDirector = userRoleRepository.findByUserId(user.getId())
                    .stream()
                    .map(userRole -> userRole.getRole().getName())
                    .anyMatch(roleName -> "HOTEL_ADMIN".equalsIgnoreCase(roleName));

            if (isDirector) {
                return true;
            }
        }

        return false;
    }
    private CurrencyCode parseCurrencyCode(String value) {
        try {
            return CurrencyCode.valueOf(value.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid currency code: " + value);
        }
    }
    private List<HotelMemberResponse> getHotelMembers(Long hotelId) {
        List<UserHotel> userHotels = userHotelRepository.findByHotelId(hotelId);

        return userHotels.stream()
                .map(UserHotel::getUser)
                .map(user -> {
                    List<String> roleNames = userRoleRepository.findByUserId(user.getId())
                            .stream()
                            .map(userRole -> userRole.getRole().getName())
                            .toList();

                    String mainRole = roleNames.isEmpty() ? "NO_ROLE" : roleNames.get(0);

                    return toMemberResponse(user, mainRole);
                })
                .toList();
    }

    private HotelMemberResponse toMemberResponse(User user, String roleName) {
        HotelMemberResponse response = new HotelMemberResponse();
        response.setUserId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(buildFullName(user));
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setStatus(user.getStatus());
        response.setRoleName(roleName);
        return response;
    }
}
