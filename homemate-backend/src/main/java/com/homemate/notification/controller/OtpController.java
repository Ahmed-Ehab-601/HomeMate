package com.homemate.notification.controller;
import com.homemate.notification.domains.dto.OtpSendRequest;
import com.homemate.notification.domains.dto.OtpVerificationResult;
import com.homemate.notification.domains.dto.OtpVerifyRequest;
import com.homemate.notification.service.imp.OTPServiceImp;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/otp")
public class OtpController {
    private final OTPServiceImp otpService;

    public OtpController(OTPServiceImp otpService) {

        this.otpService = otpService;
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendOtp(
            @Valid @RequestBody
            OtpSendRequest otpSendRequest) {
        otpService.sendOtp(otpSendRequest);
        return ResponseEntity.accepted().body(Map.of("message", "OTP sent"));
    }

    @PostMapping("/verify")
    public ResponseEntity<OtpVerificationResult> verifyOtp(
            @Valid @RequestBody
            OtpVerifyRequest otpVerifyRequest) {
        OtpVerificationResult result = otpService.validateCode(otpVerifyRequest);
        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(result);
    }
}
