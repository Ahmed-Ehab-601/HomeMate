package com.homemate.Authentication.dto;

public class LoginResponseDto {

    private String token;
    private String role;
    private String username;
    private String firstname;
    private String lastname;

    public LoginResponseDto(String role, String username, String firstname, String lastname, String token) {
        this.role = role;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
}
