package com.homemate.Authentication.dto;

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
public class PasswordResetDto {
    private String verifyToken;
    @Size(max = 72, message = "password max size is 72")
    private String newPassword;
}
