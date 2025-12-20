package com.gazi.ParkUs.services;

import com.gazi.ParkUs.exceptions.UserAlreadyExists;
import com.gazi.ParkUs.exceptions.ResourceNotFoundException;
import com.gazi.ParkUs.dto.AuthResponseDto;
import com.gazi.ParkUs.dto.LoginUserDto;
import com.gazi.ParkUs.dto.RegisterUserDto;
import com.gazi.ParkUs.entities.RegularUser;
import com.gazi.ParkUs.entities.UserEntity;
import com.gazi.ParkUs.repositories.UserRepository;
import com.gazi.ParkUs.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                      JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponseDto register(RegisterUserDto dto) {
        Optional<UserEntity> existingUser = userRepository.findByEmail(dto.getEmail());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExists("User with this email already exists");
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        UserEntity user = new RegularUser(dto.getFirstName(), dto.getLastName(), dto.getEmail(), encodedPassword);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().toString());

        return new AuthResponseDto(
                token,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getRegistrationDate()
        );
    }

    public AuthResponseDto login(LoginUserDto dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        UserEntity user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().toString());

        return new AuthResponseDto(
                token,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getRegistrationDate()
        );
    }
}
