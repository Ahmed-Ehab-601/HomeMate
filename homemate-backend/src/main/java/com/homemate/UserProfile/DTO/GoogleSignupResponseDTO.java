package com.homemate.UserProfile.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleSignupResponseDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String username;
    private String verifyToken;
}
