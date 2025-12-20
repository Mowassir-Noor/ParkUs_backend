package com.gazi.ParkUs.services;


import com.gazi.ParkUs.entities.UserEntity;
import com.gazi.ParkUs.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {


        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));


//        String role = "ROLE_" + user.getRole().toString();
        String role = user.getRole().toString();
        System.out.println(role);


        return User.withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(role)
                .build();

    }

}