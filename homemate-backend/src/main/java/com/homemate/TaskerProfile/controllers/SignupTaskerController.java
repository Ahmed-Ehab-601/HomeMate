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
        // Validate verification token if provided
        if (dto.getVerifyToken() != null && !dto.getVerifyToken().isEmpty()) {
            String verifiedEmail = jwtService.validateVerifyToken(dto.getVerifyToken());
            if (verifiedEmail == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired verification token");
            }
            // Ensure the email in the DTO matches the verified email
            if (!verifiedEmail.equalsIgnoreCase(dto.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email does not match verified email");
            }
        }

        String error = validateSignup.validateTaskerSignup(dto);
        if (error != null) 
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        String jwt = taskerSignupService.registerTasker(dto);
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user with the same username or email already exists");
        }
        return ResponseEntity.ok(jwt);
    }
}
