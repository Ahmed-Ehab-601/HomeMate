package com.homemate.notification.controller;
import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.OtpVerificationResult;
import com.homemate.notification.domains.dto.OtpVerifyRequest;
import com.homemate.notification.service.OTPService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/auth/otp")
public class OtpController {
    private final OTPService otpService;
    public OtpController(OTPService otpService) {
        this.otpService = otpService;
    }
    @PostMapping("/send")
    public ResponseEntity<OtpVerificationResult> sendOtp(
            @Valid @RequestBody EmailRequest emailRequest) throws ExecutionException, InterruptedException {
        OtpVerificationResult result = otpService.sendOtp(emailRequest).get();
        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(result);
    }

    @PostMapping("/verify")
    public ResponseEntity<OtpVerificationResult> verifyOtp(
            @Valid @RequestBody
            OtpVerifyRequest otpVerifyRequest) throws ExecutionException, InterruptedException {
        OtpVerificationResult result = otpService.validateCode(otpVerifyRequest).get();
        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(result);
    }
}
