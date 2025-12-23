package com.homemate.notification.controller;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.OtpVerificationResult;
import com.homemate.notification.domains.dto.OtpVerifyRequest;
import com.homemate.notification.service.OTPService;
import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.TaskerProfile.models.Tasker;
import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.UserProfile.Models.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/auth/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OTPService otpService;
    private final UserDao userDao;
    private final TaskerDao taskerDao;

    private String SIGNUP = "signup";
    private String FORGOT_PASSWORD = "forgetpassword";
    private String INVALID_TYPE = "Invalid otp request type";
    private String USER_ALREADY_EXISTS = "User already exists with this email address";
    private String NO_USER_EXISTS = "User not found with this email address";
    
    @PostMapping("/send/{type}")
    public ResponseEntity<OtpVerificationResult> sendOtp(
            @PathVariable String type,
            @Valid @RequestBody EmailRequest emailRequest
        ) throws ExecutionException, InterruptedException {
        
        if (!isValidType(type)) {
            OtpVerificationResult errorResult = OtpVerificationResult.builder()
                    .success(false)
                    .message(INVALID_TYPE)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        }

        User user = null;
        try {
            user = userDao.getByEmail(emailRequest.getRecipientEmail());
        } catch (EmptyResultDataAccessException e) {
            user = null;
        }

        Tasker tasker = null;
        if (user == null) {
            try {
                tasker = taskerDao.getByEmail(emailRequest.getRecipientEmail());
            } catch (EmptyResultDataAccessException e) {
                tasker = null;
            }
        }
        
        if (SIGNUP.equalsIgnoreCase(type) && (user != null || tasker != null)) {
            OtpVerificationResult errorResult = OtpVerificationResult.builder()
                    .success(false)
                    .message(USER_ALREADY_EXISTS)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        } 

        if (FORGOT_PASSWORD.equalsIgnoreCase(type) && user == null && tasker == null) {
            OtpVerificationResult errorResult = OtpVerificationResult.builder()
                    .success(false)
                    .message(NO_USER_EXISTS)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        }
        
        OtpVerificationResult result = otpService.sendOtp(emailRequest).get();
        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(result);
    }
    
    private boolean isValidType(String type) {
        return SIGNUP.equalsIgnoreCase(type) || FORGOT_PASSWORD.equalsIgnoreCase(type);
    }

    @PostMapping("/verify")
    public ResponseEntity<OtpVerificationResult> verifyOtp(
            @Valid @RequestBody
            OtpVerifyRequest otpVerifyRequest
        ) throws ExecutionException, InterruptedException {
        OtpVerificationResult result = otpService.validateCode(otpVerifyRequest).get();
        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(result);
    }
}
