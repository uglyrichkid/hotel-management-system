package com.project.app.repository.room;

import com.project.app.entity.room.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    List<RoomType> findByActiveTrue();

    Optional<RoomType> findByIdAndActiveTrue(Long id);
}