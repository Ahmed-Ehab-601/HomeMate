package com.homemate.UserProfile.DTO;

import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class PasswordDTO {
    
    private Long userId;
    @Size(max = 72, message = "password max size is 72")
    private String oldPassword;
    @Size(max = 72, message = "password max size is 72")
    private String newPassword;

}
