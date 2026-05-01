package com.project.app.repository.access;

import com.project.app.entity.access.UserHotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserHotelRepository
        extends JpaRepository<UserHotel, UserHotel.UserHotelId> {

    List<UserHotel> findByHotelId(Long hotelId);

    List<UserHotel> findByUserId(Long userId);

    boolean existsByUserIdAndHotelId(Long userId, Long hotelId);
}