package com.project.app.repository.hotel;

import com.project.app.entity.hotel.HotelContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HotelContactRepository extends JpaRepository<HotelContact, Long> {

    Optional<HotelContact> findByHotel_Id(Long hotelId);

}