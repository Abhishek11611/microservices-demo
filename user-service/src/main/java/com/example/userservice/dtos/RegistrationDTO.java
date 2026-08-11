package com.example.userservice.dtos;

import com.example.userservice.enums.RegistrationStatus;

import java.time.LocalDate;

public class RegistrationDTO {

    private String journeyId;

    private String userCode;

    private String firstName;

    private String lastName;

    private String email;

    private String passwordHash;

    private String mobileNumber;

    private LocalDate dateOfBirth;

    private RegistrationStatus status;

    public RegistrationDTO() {
    }

    public RegistrationDTO(String journeyId, String userCode, String firstName, String lastName, String email, String passwordHash, String mobileNumber, LocalDate dateOfBirth, RegistrationStatus status) {
        this.journeyId = journeyId;
        this.userCode = userCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.mobileNumber = mobileNumber;
        this.dateOfBirth = dateOfBirth;
        this.status = status;
    }

    public String getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(String journeyId) {
        this.journeyId = journeyId;
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


    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }
}
