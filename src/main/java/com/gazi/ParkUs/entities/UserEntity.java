package com.gazi.ParkUs.entities;

import com.gazi.ParkUs.User.UserRole;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type")
public abstract  class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true,name="user_id",nullable = false)
    private Long id;

    @Column(name="email",nullable = false, unique =true)
    private String email;

    @Column(name="first_name", nullable=false)
    private String firstName;

    @Column(name="last_name",nullable=false)
    private String lastName;

    @Column(name="password_hash",nullable=false)
    private String password;


    @Enumerated(EnumType.STRING)
    @Column(name="role",nullable=false)
    private UserRole role;

    @Column(name="created_at",nullable=false)
    private LocalDateTime registrationDate;


//    parameterized construction

    public UserEntity(String email, String firstName,String lastName ,String password, UserRole role) {

    setEmail(email);
    setFirstName(firstName);
    setLastName(lastName);
    setPassword(password);
    setRole(role);
    registrationDate=LocalDateTime.now();
    }

    public UserEntity(){}

    public void userAction(){
        System.out.println("user specific action");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if(email==null ){
            throw new IllegalArgumentException("Email cannot be null");
        }
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if(password==null || password.length()<8){
            throw new IllegalArgumentException("Password must be atleast 8 characters");
        }
        this.password = password;
    }

    public UserRole getRole() {

        return role;
    }

    public void setRole(UserRole role) {
        if(role==null){
            throw new IllegalArgumentException("Role cannot be null");
        }
        this.role = role;
    }

    public Long getUserId() {
        return id;
    }
    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }



}
