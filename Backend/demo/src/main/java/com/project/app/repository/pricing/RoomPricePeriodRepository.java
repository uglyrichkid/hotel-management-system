package com.project.app.repository.pricing;

import com.project.app.entity.pricing.RoomPricePeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RoomPricePeriodRepository extends JpaRepository<RoomPricePeriod, Long> {

    List<RoomPricePeriod> findByRoomIdAndActiveTrueOrderByDateFromAsc(Long roomId);
    List<RoomPricePeriod> findByRoomIdAndActiveTrueAndDateFromLessThanAndDateToGreaterThan(
            Long roomId,
            LocalDate dateTo,
            LocalDate dateFrom
    );

    boolean existsByRoomIdAndActiveTrueAndDateFromLessThanAndDateToGreaterThan(
            Long roomId,
            LocalDate dateTo,
            LocalDate dateFrom
    );

    boolean existsByRoomIdAndActiveTrueAndIdNotAndDateFromLessThanAndDateToGreaterThan(
            Long roomId,
            Long id,
            LocalDate dateTo,
            LocalDate dateFrom
    );
}