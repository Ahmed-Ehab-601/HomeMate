package com.homemate.Authentication.controller;

import com.homemate.Authentication.dto.GoogleTokenDto;
import com.homemate.Authentication.dto.GoogleUserDto;
import com.homemate.Authentication.dto.LoginRequestDto;
import com.homemate.Authentication.dto.LoginResponseDto;
import com.homemate.Authentication.dto.PasswordResetDto;
import com.homemate.Authentication.service.GoogleTokenVerifierService;
import com.homemate.Authentication.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    LoginService loginService;
    GoogleTokenVerifierService googleTokenVerifierService;


    public LoginController(LoginService loginService, GoogleTokenVerifierService googleTokenVerifierService) {
        this.loginService = loginService;
        this.googleTokenVerifierService = googleTokenVerifierService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        LoginResponseDto response = loginService.loginWithEmailPassword(loginRequestDto);

        if (response == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponseDto> googleSignIn(@RequestBody GoogleTokenDto request) {
        if (request == null || request.getIdToken() == null) {
            return ResponseEntity.badRequest().build();
        }

        GoogleUserDto googleUser = googleTokenVerifierService.verify(request.getIdToken());
        if (googleUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        LoginResponseDto dto = loginService.login(googleUser.getEmail());

        if (dto == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid PasswordResetDto passwordResetDto) {
        if (passwordResetDto == null || passwordResetDto.getVerifyToken() == null || 
            passwordResetDto.getNewPassword() == null) {
            return ResponseEntity.badRequest().body("Invalid request: token and password are required");
        }

        String result = loginService.resetPassword(passwordResetDto);

        if (result == null) {
            return ResponseEntity.ok("Password reset successfully");
        }

        return ResponseEntity.badRequest().body(result);
    }

}
