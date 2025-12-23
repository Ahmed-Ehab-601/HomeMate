package com.homemate.Authentication.controller;

import com.homemate.Authentication.dto.GoogleTokenDto;
import com.homemate.Authentication.dto.GoogleUserDto;
import com.homemate.Authentication.service.GoogleTokenVerifierService;
import com.homemate.UserProfile.DTO.GoogleSignupResponseDTO;
import com.homemate.security.service.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/signup")
public class SignupInitController {

    private GoogleTokenVerifierService googleTokenVerifierService;
    private JwtService jwtService;

    public SignupInitController(
        GoogleTokenVerifierService googleTokenVerifierService,
        JwtService jwtService
    ) {
        this.googleTokenVerifierService = googleTokenVerifierService;
        this.jwtService = jwtService;
    }
    
    @PostMapping("/google/init")
    public ResponseEntity<?> initGoogleSignup(@RequestBody GoogleTokenDto googleTokenDto) {
        GoogleUserDto googleUser = googleTokenVerifierService.verify(googleTokenDto.getIdToken());
        
        if (googleUser == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Google token");
        }

        if (!googleUser.isEmailVerified()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email not verified by Google");
        }

        String verifyToken = jwtService.generateVerifyToken(googleUser.getEmail());
        String username = googleUser.getEmail().split("@")[0];

        GoogleSignupResponseDTO response = new GoogleSignupResponseDTO(
            googleUser.getEmail(),
            googleUser.getGivenName(),
            googleUser.getFamilyName(),
            username,
            verifyToken
        );

        return ResponseEntity.ok(response);
    }
}
