package com.project.app.repository.access;

import com.project.app.entity.access.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndStatus(String email, String status);

    boolean existsByEmail(String email);

    @Query("""
            select distinct uh.user
            from UserHotel uh
            where uh.hotel.id in :hotelIds
            """)
    List<User> findDistinctUsersByHotelIds(Collection<Long> hotelIds);

    @Query("""
            select count(uh) > 0
            from UserHotel uh
            where uh.user.id = :userId
              and uh.hotel.id in :hotelIds
            """)
    boolean existsUserInHotels(Long userId, Collection<Long> hotelIds);



    boolean existsByEmailIgnoreCaseAndIdNot(String normalizedEmail, Long userId);
}