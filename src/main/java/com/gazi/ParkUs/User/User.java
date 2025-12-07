package com.gazi.ParkUs.User;

import java.time.LocalDateTime;

public abstract class User {
    private String username;
    private String password;
    private String email;
    private UserRole role;
    private LocalDateTime registrationDate;



    public User(String username, String password, String email, UserRole role, LocalDateTime registrationDate) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.registrationDate = LocalDateTime.now();

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }
}
