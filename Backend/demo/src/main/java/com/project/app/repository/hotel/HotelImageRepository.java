package com.project.app.repository.hotel;

import com.project.app.entity.hotel.HotelImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelImageRepository extends JpaRepository<HotelImage, Long> {

    List<HotelImage> findByHotelIdOrderBySortOrderAscIdAsc(Long hotelId);
    void deleteByHotelId(Long hotelId);
}