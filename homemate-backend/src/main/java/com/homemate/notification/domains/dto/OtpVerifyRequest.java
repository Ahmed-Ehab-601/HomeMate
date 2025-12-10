package com.homemate.notification.domains.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpVerifyRequest {
    @Email
    @NotBlank
    private String recipientEmail;
    @NotBlank
    private String code;
    EmailRequest.EmailType emailType;
}
