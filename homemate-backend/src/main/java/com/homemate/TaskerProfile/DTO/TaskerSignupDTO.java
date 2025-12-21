package com.homemate.TaskerProfile.DTO;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TaskerSignupDTO {
    private String username;
    private String email;
    private String password;
    private String phoneNumber;
    private Timestamp dateOfBirth;
    private String bio;
    private String profileImage;
    private Long serviceID;
    private Double hourRate;
    private String firstName;
    private String lastName;
    private String city;
    private String verifyToken;
}
