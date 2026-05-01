package com.project.app.dto.hotel;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class HotelDirectorCreateRequest {

    @NotBlank(message = "Director first name is required")
    @Size(max = 100, message = "Director first name must be at most 100 characters")
    private String firstName;

    @NotBlank(message = "Director last name is required")
    @Size(max = 100, message = "Director last name must be at most 100 characters")
    private String lastName;

    @NotBlank(message = "Director email is required")
    @Email(message = "Director email must be valid")
    @Size(max = 150, message = "Director email must be at most 150 characters")
    private String email;

    @NotBlank(message = "Director password is required")
    @Size(min = 6, max = 100, message = "Director password must be between 6 and 100 characters")
    private String password;

    private String phone;

    public HotelDirectorCreateRequest() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}