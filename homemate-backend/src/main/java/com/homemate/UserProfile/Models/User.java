package com.homemate.UserProfile.Models;

import java.sql.Timestamp;

public class User {
    private Long userID;
    private String username;
    private String password;
    private String email;
    private Timestamp birthDate;
    private Character gender;
    private String phone;
    private Boolean admin;
    private Boolean suspended;
    private String firstName;
    private String lastName;

    public Long getUserID() {
        return this.userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(Timestamp birthDate) {
        this.birthDate = birthDate;
    }

    public Character getGender() {
        return this.gender;
    }

    public void setGender(Character gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getIsAdmin() {
        return this.admin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.admin = isAdmin;
    }

    public Boolean getIsSuspended() {
        return this.suspended;
    }

    public void setIsSuspended(Boolean isSuspended) {
        this.suspended = isSuspended;
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
}

