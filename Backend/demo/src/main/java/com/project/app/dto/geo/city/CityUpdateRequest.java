package com.project.app.dto.geo.city;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CityUpdateRequest {

    @NotBlank(message = "City name is required")
    @Size(max = 100, message = "City name must be at most 100 characters")
    private String name;

    public CityUpdateRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}