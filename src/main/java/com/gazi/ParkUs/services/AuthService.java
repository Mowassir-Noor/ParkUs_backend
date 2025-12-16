package com.gazi.ParkUs.services;

import com.gazi.ParkUs.Exceptions.UserAlreadyExists;
import com.gazi.ParkUs.dto.LoginUserDto;
import com.gazi.ParkUs.dto.RegisterUserDto;
import com.gazi.ParkUs.dto.UserResponseDto;
import com.gazi.ParkUs.entities.RegularUser;
import com.gazi.ParkUs.entities.UserEntity;
import com.gazi.ParkUs.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


public UserResponseDto register(RegisterUserDto dto){
    Optional<UserEntity> existingUser=userRepository.findByEmail(dto.getEmail());
    if(existingUser.isPresent()){
        throw new UserAlreadyExists("User with this email already exists");
    }
    else {
//encode the password before mapping and saving it to the db
      String encodedPassword = passwordEncoder.encode(dto.getPassword());

      UserEntity user=new RegularUser(dto.getFirstName(),dto.getLastName(),dto.getEmail(),encodedPassword);

      userRepository.save(user);

      return new UserResponseDto(user.getFirstName(),user.getLastName(),user.getEmail(),user.getRole(),user.getRegistrationDate());

    }
}

public UserResponseDto login(LoginUserDto dto){
     Optional<UserEntity> existingUser=userRepository.findByEmail(dto.getEmail());

     if(existingUser.isEmpty() || !passwordEncoder.matches(dto.getPassword(),existingUser.get().getPassword())){
         throw new IllegalArgumentException("Invalid username or password");


     }
     UserEntity user=existingUser.get();
     return new UserResponseDto(user.getFirstName(),user.getLastName(),user.getEmail(),user.getRole(),user.getRegistrationDate());
}


}
