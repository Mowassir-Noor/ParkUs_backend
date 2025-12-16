package com.gazi.ParkUs.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //disabling csrf
        http.csrf(csrf -> csrf.disable())

//                checking authorizaion header for role based authorization
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register", "/auth/login","/").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")

                        .requestMatchers("/users/**").hasAuthority("ROLE_USER")
                        .anyRequest().authenticated())
                .httpBasic(withDefaults())

//                session creation policy is stateless base 64 basic auth header is enough
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth/login?logout")
                        .invalidateHttpSession(true)
                        .permitAll())
        ;

        return http.build();
    }
}
