package com.project.app.dto.hotel;

public class HotelPolicyItemResponse {

    private Long id;
    private String policyType;
    private String text;

    public HotelPolicyItemResponse() {
    }

    public HotelPolicyItemResponse(Long id, String policyType, String text) {
        this.id = id;
        this.policyType = policyType;
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public String getPolicyType() {
        return policyType;
    }

    public String getText() {
        return text;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    public void setText(String text) {
        this.text = text;
    }
}