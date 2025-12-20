package com.gazi.ParkUs.controller;

import com.gazi.ParkUs.dto.AuthResponseDto;
import com.gazi.ParkUs.dto.LoginUserDto;
import com.gazi.ParkUs.dto.RegisterUserDto;
import com.gazi.ParkUs.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterUserDto dto) {
        AuthResponseDto response = authService.register(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginUserDto dto) {
        AuthResponseDto response = authService.login(dto);
        return ResponseEntity.ok(response);
    }
}
