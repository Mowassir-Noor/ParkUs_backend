package com.gazi.ParkUs.dto;

import com.gazi.ParkUs.User.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private String token;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private LocalDateTime registrationDate;
}
