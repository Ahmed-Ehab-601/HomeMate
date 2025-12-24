package com.homemate.UserProfile.DTO;

import lombok.Data;

import java.sql.Timestamp;
@Data
public class UserProfileDTO {
    
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Timestamp birthDate;
    private Character gender;
    private String phone;
    private Boolean admin;
    private Boolean suspended;

}
