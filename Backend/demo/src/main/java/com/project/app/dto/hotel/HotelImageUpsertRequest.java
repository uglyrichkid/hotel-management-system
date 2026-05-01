package com.project.app.dto.hotel;

public class HotelImageUpsertRequest {

    private Long id;
    private String url;
    private Boolean isMain;
    private Integer sortOrder;

    public HotelImageUpsertRequest() {
    }

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public Boolean getIsMain() {
        return isMain;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setIsMain(Boolean isMain) {
        this.isMain = isMain;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}