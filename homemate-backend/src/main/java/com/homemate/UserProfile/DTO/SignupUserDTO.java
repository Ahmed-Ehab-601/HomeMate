package com.homemate.UserProfile.DTO;

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
public class SignupUserDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    @Size(max = 72, message = "password max size is 72")
    private String password;
    private Timestamp birthDate;
    private Character gender;
    private String phone;
    private String verifyToken;
}
