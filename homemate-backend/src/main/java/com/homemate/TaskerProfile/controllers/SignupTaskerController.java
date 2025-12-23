package com.homemate.TaskerProfile.controllers;

import com.homemate.TaskerProfile.DTO.TaskerSignupDTO;
import com.homemate.TaskerProfile.services.TaskerSignupService;
import com.homemate.security.service.ValidateSignupService;
import com.homemate.security.service.JwtService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/tasker")
public class SignupTaskerController {

    private TaskerSignupService taskerSignupService;
    private ValidateSignupService validateSignup;
    private JwtService jwtService;

    private final String NO_TOKEN = "No verification token found";
    private final String INVALID_TOKEN = "Invalid or expired verification token";
    private final String EMAIL_MISMATCH = "Email does not match verified email";
    private final String USER_ALREADY_EXISTS = "user with the same username or email already exists";

    SignupTaskerController(
        TaskerSignupService taskerSignupService, 
        ValidateSignupService validateSignup,
        JwtService jwtService
    ) {
        this.taskerSignupService = taskerSignupService;
        this.validateSignup = validateSignup;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody TaskerSignupDTO dto) {

        if (dto.getVerifyToken() == null || dto.getVerifyToken().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(NO_TOKEN);
        }

        String verifiedEmail = jwtService.validateVerifyToken(dto.getVerifyToken());
        if (verifiedEmail == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(INVALID_TOKEN);
        }

        if (!verifiedEmail.equalsIgnoreCase(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(EMAIL_MISMATCH);
        }

        String errorMsg = validateSignup.validateTaskerSignup(dto);
        if (errorMsg != null) 
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);

        String jwt = taskerSignupService.registerTasker(dto);
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(USER_ALREADY_EXISTS);
        }
        return ResponseEntity.ok(jwt);
    }
}
