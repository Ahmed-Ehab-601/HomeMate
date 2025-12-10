package com.homemate.authentication.controller;

import com.homemate.Authentication.controller.LoginController;
import com.homemate.Authentication.dto.GoogleTokenDto;
import com.homemate.Authentication.dto.GoogleUserDto;
import com.homemate.Authentication.dto.LoginRequestDto;
import com.homemate.Authentication.dto.LoginResponseDto;
import com.homemate.Authentication.dto.PasswordResetDto;
import com.homemate.Authentication.service.GoogleTokenVerifierService;
import com.homemate.Authentication.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private LoginService loginService;

    @Mock
    private GoogleTokenVerifierService googleTokenVerifierService;

    @InjectMocks
    private LoginController loginController;

    private LoginRequestDto loginRequestDto;
    private LoginResponseDto loginResponseDto;
    private GoogleTokenDto googleTokenDto;
    private GoogleUserDto googleUserDto;

    @BeforeEach
    void setUp() {
        loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("test@example.com");
        loginRequestDto.setPassword("password123");

        loginResponseDto = new LoginResponseDto(
            "jwt-token-123",
            "ROLE_USER",
            "testuser",
            "Test",
            "User"
        );

        googleTokenDto = new GoogleTokenDto();
        googleTokenDto.setIdToken("google-id-token-123");

        googleUserDto = new GoogleUserDto(
            "google-sub-123",
            "test@example.com",
            true,
            "Test User",
            "Test",
            "User",
            "picture-url",
            "en",
            System.currentTimeMillis() / 1000,
            System.currentTimeMillis() / 1000 + 3600
        );
    }

    @Test
    void loginShouldReturnOkWithValidCredentials() {
        when(loginService.loginWithEmailPassword(loginRequestDto)).thenReturn(loginResponseDto);

        ResponseEntity<LoginResponseDto> response = loginController.login(loginRequestDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ROLE_USER", response.getBody().getRole());
        assertEquals("testuser", response.getBody().getUsername());
        assertEquals("Test", response.getBody().getFirstname());
        assertEquals("User", response.getBody().getLastname());
        assertEquals("jwt-token-123", response.getBody().getToken());
        verify(loginService, times(1)).loginWithEmailPassword(loginRequestDto);
    }

    @Test
    void loginShouldReturnUnauthorizedWithInvalidCredentials() {
        when(loginService.loginWithEmailPassword(loginRequestDto)).thenReturn(null);

        ResponseEntity<LoginResponseDto> response = loginController.login(loginRequestDto);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(loginService, times(1)).loginWithEmailPassword(loginRequestDto);
    }

    @Test
    void loginShouldReturnOkWithCorrectRole() {
        LoginResponseDto adminResponse = new LoginResponseDto(
            "admin-token-123",
            "ROLE_ADMIN",
            "admin",
            "Admin",
            "User"
        );
        when(loginService.loginWithEmailPassword(loginRequestDto)).thenReturn(adminResponse);

        ResponseEntity<LoginResponseDto> response = loginController.login(loginRequestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ROLE_ADMIN", response.getBody().getRole());
    }

    // @Test
    // void loginShouldReturnOkWithTaskerRole() {
    //     LoginResponseDto taskerResponse = new LoginResponseDto(
    //         "ROLE_TASKER",
    //         "tasker",
    //         "Tasker",
    //         "User",
    //         "tasker-token-123"
    //     );
    //     when(loginService.loginWithEmailPassword(loginRequestDto)).thenReturn(taskerResponse);

    //     ResponseEntity<LoginResponseDto> response = loginController.login(loginRequestDto);

    //     assertEquals(HttpStatus.OK, response.getStatusCode());
    //     assertEquals("ROLE_TASKER", response.getBody().getRole());
    // }

    @Test
    void googleSignInShouldReturnOkWithValidToken() {
        when(googleTokenVerifierService.verify(googleTokenDto.getIdToken())).thenReturn(googleUserDto);
        when(loginService.login(googleUserDto.getEmail())).thenReturn(loginResponseDto);

        ResponseEntity<LoginResponseDto> response = loginController.googleSignIn(googleTokenDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-token-123", response.getBody().getToken());
        verify(googleTokenVerifierService, times(1)).verify(googleTokenDto.getIdToken());
        verify(loginService, times(1)).login(googleUserDto.getEmail());
    }

    @Test
    void googleSignInShouldReturnBadRequestWithNullRequest() {
        ResponseEntity<LoginResponseDto> response = loginController.googleSignIn(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(googleTokenVerifierService, never()).verify(anyString());
        verify(loginService, never()).login(anyString());
    }

    @Test
    void googleSignInShouldReturnBadRequestWithNullIdToken() {
        GoogleTokenDto nullTokenDto = new GoogleTokenDto();
        nullTokenDto.setIdToken(null);

        ResponseEntity<LoginResponseDto> response = loginController.googleSignIn(nullTokenDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(googleTokenVerifierService, never()).verify(anyString());
        verify(loginService, never()).login(anyString());
    }

    @Test
    void googleSignInShouldReturnUnauthorizedWithInvalidToken() {
        when(googleTokenVerifierService.verify(googleTokenDto.getIdToken())).thenReturn(null);

        ResponseEntity<LoginResponseDto> response = loginController.googleSignIn(googleTokenDto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(googleTokenVerifierService, times(1)).verify(googleTokenDto.getIdToken());
        verify(loginService, never()).login(anyString());
    }

    @Test
    void googleSignInShouldReturnUnauthorizedWhenUserNotFound() {
        when(googleTokenVerifierService.verify(googleTokenDto.getIdToken())).thenReturn(googleUserDto);
        when(loginService.login(googleUserDto.getEmail())).thenReturn(null);

        ResponseEntity<LoginResponseDto> response = loginController.googleSignIn(googleTokenDto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(googleTokenVerifierService, times(1)).verify(googleTokenDto.getIdToken());
        verify(loginService, times(1)).login(googleUserDto.getEmail());
    }

    @Test
    void resetPasswordShouldReturnOkWithValidRequest() {
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setVerifyToken("valid-token");
        passwordResetDto.setNewPassword("NewPass123!");

        when(loginService.resetPassword(passwordResetDto)).thenReturn(null);

        ResponseEntity<String> response = loginController.resetPassword(passwordResetDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset successfully", response.getBody());
        verify(loginService, times(1)).resetPassword(passwordResetDto);
    }

    @Test
    void resetPasswordShouldReturnBadRequestWithNullDto() {
        ResponseEntity<String> response = loginController.resetPassword(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request: token and password are required", response.getBody());
        verify(loginService, never()).resetPassword(any());
    }

    @Test
    void resetPasswordShouldReturnBadRequestWithNullToken() {
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setVerifyToken(null);
        passwordResetDto.setNewPassword("NewPass123!");

        ResponseEntity<String> response = loginController.resetPassword(passwordResetDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request: token and password are required", response.getBody());
        verify(loginService, never()).resetPassword(any());
    }

    @Test
    void resetPasswordShouldReturnBadRequestWithNullPassword() {
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setVerifyToken("valid-token");
        passwordResetDto.setNewPassword(null);

        ResponseEntity<String> response = loginController.resetPassword(passwordResetDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request: token and password are required", response.getBody());
        verify(loginService, never()).resetPassword(any());
    }

    @Test
    void resetPasswordShouldReturnBadRequestWithInvalidToken() {
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setVerifyToken("invalid-token");
        passwordResetDto.setNewPassword("NewPass123!");

        when(loginService.resetPassword(passwordResetDto))
            .thenReturn("Invalid or expired verification token");

        ResponseEntity<String> response = loginController.resetPassword(passwordResetDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid or expired verification token", response.getBody());
        verify(loginService, times(1)).resetPassword(passwordResetDto);
    }

    @Test
    void resetPasswordShouldReturnBadRequestWithWeakPassword() {
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setVerifyToken("valid-token");
        passwordResetDto.setNewPassword("weak");

        when(loginService.resetPassword(passwordResetDto))
            .thenReturn("password must be between 8 and 25 characters");

        ResponseEntity<String> response = loginController.resetPassword(passwordResetDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("password must be between 8 and 25 characters", response.getBody());
        verify(loginService, times(1)).resetPassword(passwordResetDto);
    }

    @Test
    void resetPasswordShouldReturnBadRequestWhenUserNotFound() {
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setVerifyToken("valid-token");
        passwordResetDto.setNewPassword("NewPass123!");

        when(loginService.resetPassword(passwordResetDto))
            .thenReturn("No user or tasker found with this email");

        ResponseEntity<String> response = loginController.resetPassword(passwordResetDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No user or tasker found with this email", response.getBody());
        verify(loginService, times(1)).resetPassword(passwordResetDto);
    }
}

