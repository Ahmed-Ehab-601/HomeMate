package com.homemate.TaskerProfile.DTO;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordDTO {
    private Long taskerID;
    @Size(max = 72, message = "password max size is 72")
    private String oldPassword;
    @Size(max = 72, message = "password max size is 72")
    private String newPassword;

}

