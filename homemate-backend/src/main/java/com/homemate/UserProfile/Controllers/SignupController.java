package com.homemate.UserProfile.Controllers;

import com.homemate.UserProfile.DTO.SignupUserDTO;
import com.homemate.UserProfile.Services.UserSignupService;
import com.homemate.security.service.ValidateSignupService;
import com.homemate.security.service.JwtService;

import jakarta.validation.Valid;
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
    public ResponseEntity<String> signupUser(@RequestBody @Valid SignupUserDTO signupUserDTO) {
        if (signupUserDTO.getVerifyToken() == null || signupUserDTO.getVerifyToken().trim().isEmpty()) {
            String NO_TOKEN = "No verification token found";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(NO_TOKEN);
        }

        String verifiedEmail = jwtService.validateVerifyToken(signupUserDTO.getVerifyToken());
        if (verifiedEmail == null) {
            String INVALID_TOKEN = "Invalid or expired verification token";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(INVALID_TOKEN);
        }

        if (!verifiedEmail.equalsIgnoreCase(signupUserDTO.getEmail())) {
            String EMAIL_MISMATCH = "Email does not match verified email";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(EMAIL_MISMATCH);
        }
        
        String errorMsg = validateSignup.validateUserSignup(signupUserDTO);
        if (errorMsg != null) 
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg);

        String result = userSignupService.signup(signupUserDTO);
        if (result == null) {
            String USER_ALREADY_EXISTS = "user with the same username or email already exists";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(USER_ALREADY_EXISTS);
        }

        return ResponseEntity.ok(result);
    }

}
