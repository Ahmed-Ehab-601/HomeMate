package com.homemate.Authentication.dto;

public class GoogleTokenDto {
    private String idToken;

    public GoogleTokenDto() {}
    public GoogleTokenDto(String idToken) { this.idToken = idToken; }

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
}
