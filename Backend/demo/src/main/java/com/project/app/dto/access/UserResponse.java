package com.project.app.dto.access;

import java.util.List;

public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String status;
    private List<String> roles;
    private List<Long> hotelIds;
    private List<String> hotelNames;

    public UserResponse() {
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<Long> getHotelIds() {
        return hotelIds;
    }

    public List<String> getHotelNames() {
        return hotelNames;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        rebuildFullName();
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        rebuildFullName();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void setHotelIds(List<Long> hotelIds) {
        this.hotelIds = hotelIds;
    }

    public void setHotelNames(List<String> hotelNames) {
        this.hotelNames = hotelNames;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    private void rebuildFullName() {
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        this.fullName = (first + " " + last).trim();
    }
}