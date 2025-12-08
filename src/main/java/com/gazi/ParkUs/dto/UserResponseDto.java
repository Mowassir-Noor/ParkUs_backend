package com.gazi.ParkUs.dto;

import com.gazi.ParkUs.User.UserRole;

import java.time.LocalDateTime;

public class UserResponseDto {

    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
    private LocalDateTime registrationDate;


    public UserResponseDto(String firstName, String lastName, String email,  UserRole role,LocalDateTime registrationDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role=UserRole.valueOf(role.name());
        this.registrationDate = registrationDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }
}
