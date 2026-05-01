package com.project.app.service.geo.city;

import com.project.app.dto.geo.city.CityCreateRequest;
import com.project.app.dto.geo.city.CityResponse;
import com.project.app.dto.geo.city.CityUpdateRequest;
import com.project.app.entity.geo.City;
import com.project.app.exception.ConflictException;
import com.project.app.exception.NotFoundException;
import com.project.app.repository.geo.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CityService {

    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Transactional
    public CityResponse create(CityCreateRequest request) {
        String normalizedName = request.getName().trim();

        if (cityRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ConflictException("City with this name already exists");
        }

        City city = new City();
        city.setName(normalizedName);
        city.setActive(true);

        City saved = cityRepository.save(city);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CityResponse> getAll(Boolean activeOnly) {
        List<City> cities = Boolean.TRUE.equals(activeOnly)
                ? cityRepository.findByActiveTrueOrderByNameAsc()
                : cityRepository.findAllByOrderByNameAsc();

        return cities.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CityResponse getById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("City not found with id: " + id));

        return mapToResponse(city);
    }

    @Transactional
    public CityResponse update(Long id, CityUpdateRequest request) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("City not found with id: " + id));

        String normalizedName = request.getName().trim();

        if (cityRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new NotFoundException("Another city with this name already exists");
        }

        city.setName(normalizedName);

        City updated = cityRepository.save(city);
        return mapToResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("City not found with id: " + id));
        city.setActive(false);
        cityRepository.save(city);
    }

    private CityResponse mapToResponse(City city) {
        return new CityResponse(
                city.getId(),
                city.getName(),
                city.getActive()
        );
    }
}