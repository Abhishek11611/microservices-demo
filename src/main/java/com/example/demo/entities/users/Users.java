package com.example.demo.entities.users;

import com.example.demo.entities.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "users")
public class Users extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_code",unique = true,nullable = false)
    private String userCode;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email",unique = true,nullable = false)
    private String email;

    @Column(name = "password")
    private String passwordHash;

    @Column(name = "mobile_number",unique = true,nullable = false)
    private String mobileNumber;

    @Column(name = "date_of_birth",nullable = false)
    private LocalDate dateOfBirth;

    public Users() {
    }

    public Users(Long id, String userCode, String firstName, String lastName, String email, String passwordHash, String mobileNumber, LocalDate dateOfBirth) {
        this.id = id;
        this.userCode = userCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.mobileNumber = mobileNumber;
        this.dateOfBirth = dateOfBirth;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
