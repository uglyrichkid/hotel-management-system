package com.project.app.repository.guest;

import com.project.app.entity.guest.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    Optional<Guest> findByIdAndActiveTrue(Long id);

    List<Guest> findByActiveTrueOrderByIdDesc();

    boolean existsByEmailAndActiveTrue(String email);

    boolean existsByPhoneAndActiveTrue(String phone);

    List<Guest> findByActiveTrueAndFirstNameContainingIgnoreCaseOrActiveTrueAndLastNameContainingIgnoreCaseOrActiveTrueAndEmailContainingIgnoreCaseOrActiveTrueAndPhoneContainingIgnoreCaseOrderByIdDesc(
            String firstName,
            String lastName,
            String email,
            String phone
    );
}