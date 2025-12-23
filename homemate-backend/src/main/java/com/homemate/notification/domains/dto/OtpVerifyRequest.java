package com.homemate.notification.domains.dto;

import com.homemate.notification.domains.model.EmailType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpVerifyRequest {
    @Email
    @NotBlank
    private String recipientEmail;
    @NotBlank
    private String code;
    private EmailType emailType;
}
