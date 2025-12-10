package com.homemate.UserProfile.Controllers;

import com.homemate.UserProfile.DTO.SignupUserDTO;
import com.homemate.UserProfile.Services.UserSignupService;
import com.homemate.security.service.ValidateSignupService;
import com.homemate.security.service.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class SignupController {

    UserSignupService userSignupService;
    ValidateSignupService validateSignup;
    JwtService jwtService;

    SignupController(
        UserSignupService userSignupService,
        ValidateSignupService validateSignup,
        JwtService jwtService
    ) {
        this.userSignupService = userSignupService;
        this.validateSignup = validateSignup;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signupUser(@RequestBody SignupUserDTO signupUserDTO) {
        // Validate verification token if provided
        if (signupUserDTO.getVerifyToken() != null && !signupUserDTO.getVerifyToken().isEmpty()) {
            String verifiedEmail = jwtService.validateVerifyToken(signupUserDTO.getVerifyToken());
            if (verifiedEmail == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired verification token");
            }
            // Ensure the email in the DTO matches the verified email
            if (!verifiedEmail.equalsIgnoreCase(signupUserDTO.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email does not match verified email");
            }
        }

        String error = validateSignup.validateUserSignup(signupUserDTO);
        if (error != null) 
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        String result = userSignupService.signup(signupUserDTO);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user with the same username or email already exists");
        }

        return ResponseEntity.ok(result);
    }

}
