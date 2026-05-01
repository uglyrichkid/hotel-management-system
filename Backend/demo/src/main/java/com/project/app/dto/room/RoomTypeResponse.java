package com.project.app.dto.room;

import java.math.BigDecimal;

public class RoomTypeResponse {

    private Long id;
    private String name;
    private String description;
    private Short defaultCapacity;
    private BigDecimal defaultPrice;
    private boolean active;

    public RoomTypeResponse(
            Long id,
            String name,
            String description,
            Short defaultCapacity,
            BigDecimal defaultPrice,
            boolean active
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.defaultCapacity = defaultCapacity;
        this.defaultPrice = defaultPrice;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Short getDefaultCapacity() {
        return defaultCapacity;
    }

    public BigDecimal getDefaultPrice() {
        return defaultPrice;
    }

    public boolean isActive() {
        return active;
    }
}