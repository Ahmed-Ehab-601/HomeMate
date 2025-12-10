package com.homemate.UserProfile.DTO;

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
public class GoogleSignupResponseDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String username;
    private String verifyToken;
}
