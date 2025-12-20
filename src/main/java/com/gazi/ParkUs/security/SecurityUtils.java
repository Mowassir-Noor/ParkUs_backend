package com.gazi.ParkUs.security;

import com.gazi.ParkUs.entities.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

//    public static UserEntity currentUser() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        return auth.getPrincipal();
//    }
public static String currentUserEmail() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth.getName(); // email
}
}
