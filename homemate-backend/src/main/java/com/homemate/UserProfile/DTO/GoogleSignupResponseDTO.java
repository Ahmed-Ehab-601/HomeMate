package com.homemate.UserProfile.DTO;

public class GoogleSignupResponseDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String username;
    private String verifyToken;

    public GoogleSignupResponseDTO() {}

    public GoogleSignupResponseDTO(String email, String firstName, String lastName, String username, String verifyToken) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.verifyToken = verifyToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getVerifyToken() {
        return verifyToken;
    }

    public void setVerifyToken(String verifyToken) {
        this.verifyToken = verifyToken;
    }
}
