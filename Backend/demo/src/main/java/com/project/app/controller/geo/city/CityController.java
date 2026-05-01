package com.project.app.controller.geo.city;

import com.project.app.dto.geo.city.CityCreateRequest;
import com.project.app.dto.geo.city.CityResponse;
import com.project.app.dto.geo.city.CityUpdateRequest;
import com.project.app.service.geo.city.CityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @PostMapping
    public CityResponse create(@Valid @RequestBody CityCreateRequest request) {
        return cityService.create(request);
    }

    @GetMapping
    public List<CityResponse> getAll(
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly
    ) {
        return cityService.getAll(activeOnly);
    }

    @GetMapping("/{id}")
    public CityResponse getById(@PathVariable Long id) {
        return cityService.getById(id);
    }

    @PutMapping("/{id}")
    public CityResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CityUpdateRequest request
    ) {
        return cityService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        cityService.delete(id);
    }
}