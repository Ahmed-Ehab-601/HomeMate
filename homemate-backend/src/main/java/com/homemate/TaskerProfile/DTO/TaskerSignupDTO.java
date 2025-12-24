package com.homemate.TaskerProfile.DTO;

import java.sql.Timestamp;

import jakarta.validation.constraints.Size;
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
    @Size(max = 72, message = "password max size is 72")
    private String password;
    private String phoneNumber;
    private Timestamp dateOfBirth;
    private String bio;
    private byte[] profileImage;
    private Long serviceID;
    private Double hourRate;
    private String firstName;
    private String lastName;
    private String city;
    private String verifyToken;
}
