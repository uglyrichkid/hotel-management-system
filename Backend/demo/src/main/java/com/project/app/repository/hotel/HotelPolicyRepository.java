package com.project.app.repository.hotel;

import com.project.app.entity.hotel.HotelPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelPolicyRepository extends JpaRepository<HotelPolicy, Long> {

    List<HotelPolicy> findByHotelIdOrderByIdAsc(Long hotelId);
    void deleteByHotelId(Long hotelId);
}