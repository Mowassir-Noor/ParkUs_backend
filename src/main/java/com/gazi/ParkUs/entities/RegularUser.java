package com.gazi.ParkUs.entities;


import com.gazi.ParkUs.User.UserRole;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("RegularUser")
public class RegularUser extends UserEntity{


    public RegularUser(String firstName,
                       String lastName,
                       String email,
                       String password) {

        super(email, firstName, lastName, password, UserRole.ROLE_USER);
    }

    public RegularUser() {}

    @Override
    public void userAction(){
        System.out.println("RegularUser");
    }
}
