/* package com.example.demo.repository.booking;

import com.example.demo.dto.booking.BookingSearchRequest;
import com.example.demo.entity.booking.Booking;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookingSpecifications {

    private BookingSpecifications() {
    }

    public static Specification<Booking> withFilters(Long hotelId, BookingSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("hotel").get("id"), hotelId));
            predicates.add(cb.equal(root.get("active"), true));

            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            if (request.getGuestId() != null) {
                predicates.add(cb.equal(root.get("guest").get("id"), request.getGuestId()));
            }

            if (request.getCheckInFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("checkInDate"), request.getCheckInFrom()));
            }

            if (request.getCheckInTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("checkInDate"), request.getCheckInTo()));
            }

            query.orderBy(cb.desc(root.get("id")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}*/