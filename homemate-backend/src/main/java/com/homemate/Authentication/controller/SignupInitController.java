package com.homemate.Authentication.controller;

import com.homemate.Authentication.dto.GoogleTokenDto;
import com.homemate.Authentication.dto.GoogleUserDto;
import com.homemate.Authentication.service.GoogleTokenVerifierService;
import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.TaskerProfile.models.Tasker;
import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.UserProfile.DTO.GoogleSignupResponseDTO;
import com.homemate.UserProfile.Models.User;
import com.homemate.security.service.JwtService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/signup")
@RequiredArgsConstructor
public class SignupInitController {

    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final JwtService jwtService;
    private final UserDao userDao;
    private final TaskerDao taskerDao;

    private String INVALID_TOKEN = "Invalid Google token";
    private String EMAIL_NOT_VERIFIED = "Email not verified by Google";
    private String EMAIL_ALREADY_EXISTS = "Email already Exists"; 
    
    @PostMapping("/google/init")
    public ResponseEntity<?> initGoogleSignup(@RequestBody GoogleTokenDto googleTokenDto) {
        GoogleUserDto googleUser = googleTokenVerifierService.verify(googleTokenDto.getIdToken());
        
        if (googleUser == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(INVALID_TOKEN);
        }

        if (!googleUser.isEmailVerified()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(EMAIL_NOT_VERIFIED);
        }

        User user = userDao.getByEmail(googleUser.getEmail());
        Tasker tasker = null;
        if (user == null)
            tasker = taskerDao.getByEmail(googleUser.getEmail());

        if (user != null || tasker != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(EMAIL_ALREADY_EXISTS);
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
