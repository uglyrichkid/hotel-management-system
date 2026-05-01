package com.project.app.repository.hotel;

import com.project.app.entity.hotel.Hotel;
import com.project.app.entity.hotel.HotelStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    Optional<Hotel> findByName(String name);
    Boolean existsByName(String name);


    boolean existsByCity_IdAndNameAndAddressAndIdNot(
            Long cityId,
            String name,
            String address,
            Long id
    );

    List<Hotel> findByStatus(HotelStatus status);
    List<Hotel> findByCity_Id(Long cityId);
    List<Hotel> findByStarsGreaterThanEqual(Short stars);
    List<Hotel> findByStatusAndCity_Id(HotelStatus status, Long cityId);
    List<Hotel> findByStatusAndStarsGreaterThanEqual(HotelStatus status, Short stars);
    List<Hotel> findByCity_IdAndStarsGreaterThanEqual(Long cityId, Short stars);
    List<Hotel> findByStatusAndCity_IdAndStarsGreaterThanEqual(HotelStatus status, Long cityId, Short stars);

    List<Hotel> findByNameContainingIgnoreCase(String name);

    List<Hotel> findByStatusAndNameContainingIgnoreCase(HotelStatus status, String name);

    List<Hotel> findByCity_IdAndNameContainingIgnoreCase(Long cityId, String name);

    List<Hotel> findByStarsGreaterThanEqualAndNameContainingIgnoreCase(Short stars, String name);

    List<Hotel> findByStatusAndCity_IdAndNameContainingIgnoreCase(HotelStatus status, Long cityId, String name);

    List<Hotel> findByStatusAndStarsGreaterThanEqualAndNameContainingIgnoreCase(HotelStatus status, Short stars, String name);

    List<Hotel> findByCity_IdAndStarsGreaterThanEqualAndNameContainingIgnoreCase(Long cityId, Short stars, String name);

    List<Hotel> findByStatusAndCity_IdAndStarsGreaterThanEqualAndNameContainingIgnoreCase(
            HotelStatus status,
            Long cityId,
            Short stars,
            String name
    );
}
