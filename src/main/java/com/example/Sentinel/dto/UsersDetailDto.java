package com.example.Sentinel.dto;

import jakarta.validation.constraints.*;

public class UsersDetailDto {
    @Email(message = "email must be valid")
    @NotNull(message = "email can not be null")
    @NotEmpty(message = "email can not be empty")
    private String email;

    @NotNull(message = "phone number can not be null")
    @NotEmpty(message = "phone number can not be empty")
    @Size(min=10, max=10, message = "phone number must be exactly 10 digits")
    @Pattern(regexp = "^[0-9]{10}$", message = "phone number must contain only digits")
    private String phoneNumber;

    @NotNull(message = "name can not be null")
    @NotEmpty(message = "name can not be empty")
    @NotBlank(message = "name can not be blank")
    private String name;

    @NotNull(message = "home location can not be null")
    @NotEmpty(message = "home location can not be empty")
    @NotBlank(message = "home location can not be blank")
    private String homeLocation;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHomeLocation() {
        return homeLocation;
    }

    public void setHomeLocation(String homeLocation) {
        this.homeLocation = homeLocation;
    }
}
