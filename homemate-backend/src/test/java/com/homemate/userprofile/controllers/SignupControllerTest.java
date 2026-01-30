package com.homemate.userprofile.controllers;

import com.homemate.UserProfile.Controllers.SignupController;
import com.homemate.UserProfile.DTO.SignupUserDTO;
import com.homemate.UserProfile.Services.UserSignupService;
import com.homemate.security.service.ValidateSignupService;
import com.homemate.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignupControllerTest {

    @Mock
    private UserSignupService userSignupService;

    @Mock
    private ValidateSignupService validateSignupService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private SignupController signupController;

    private SignupUserDTO signupUserDTO;
    private static final String VALID_JWT = "valid_token";
    private static final String VALID_TOKEN = "valid-verify-token";
    private static final String VALID_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        signupUserDTO = new SignupUserDTO();
        signupUserDTO.setUsername("testuser");
        signupUserDTO.setFirstName("Test");
        signupUserDTO.setLastName("User");
        signupUserDTO.setEmail(VALID_EMAIL);
        signupUserDTO.setPassword("Password123!");
        signupUserDTO.setBirthDate(Timestamp.valueOf("1990-01-01 00:00:00"));
        signupUserDTO.setGender('M');
        signupUserDTO.setPhone("+1234567890");
        signupUserDTO.setVerifyToken(VALID_TOKEN);
    }

    @Test
    void signupUser_ShouldReturnJwt_WhenAllValidationsPass() {
        // Arrange
        when(jwtService.validateVerifyToken(VALID_TOKEN)).thenReturn(VALID_EMAIL);
        when(validateSignupService.validateUserSignup(signupUserDTO)).thenReturn(null);
        when(userSignupService.signup(signupUserDTO)).thenReturn(VALID_JWT);

        // Act
        ResponseEntity<String> response = signupController.signupUser(signupUserDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(VALID_JWT, response.getBody());
        verify(jwtService, times(1)).validateVerifyToken(VALID_TOKEN);
        verify(validateSignupService, times(1)).validateUserSignup(signupUserDTO);
        verify(userSignupService, times(1)).signup(signupUserDTO);
    }

    @Test
    void signupUser_ShouldReturnBadRequest_WhenVerifyTokenIsNull() {
        // Arrange
        signupUserDTO.setVerifyToken(null);

        // Act
        ResponseEntity<String> response = signupController.signupUser(signupUserDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No verification token found", response.getBody());
        verify(jwtService, never()).validateVerifyToken(any());
        verify(validateSignupService, never()).validateUserSignup(any());
        verify(userSignupService, never()).signup(any());
    }

    @Test
    void signupUser_ShouldReturnBadRequest_WhenVerifyTokenIsEmpty() {
        // Arrange
        signupUserDTO.setVerifyToken("");

        // Act
        ResponseEntity<String> response = signupController.signupUser(signupUserDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No verification token found", response.getBody());
        verify(jwtService, never()).validateVerifyToken(any());
        verify(validateSignupService, never()).validateUserSignup(any());
        verify(userSignupService, never()).signup(any());
    }

    @Test
    void signupUser_ShouldReturnBadRequest_WhenVerifyTokenIsInvalid() {
        // Arrange
        when(jwtService.validateVerifyToken(VALID_TOKEN)).thenReturn(null);

        // Act
        ResponseEntity<String> response = signupController.signupUser(signupUserDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid or expired verification token", response.getBody());
        verify(jwtService, times(1)).validateVerifyToken(VALID_TOKEN);
        verify(validateSignupService, never()).validateUserSignup(any());
        verify(userSignupService, never()).signup(any());
    }

    @Test
    void signupUser_ShouldReturnBadRequest_WhenEmailDoesNotMatchVerifiedEmail() {
        // Arrange
        when(jwtService.validateVerifyToken(VALID_TOKEN)).thenReturn("different@example.com");

        // Act
        ResponseEntity<String> response = signupController.signupUser(signupUserDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email does not match verified email", response.getBody());
        verify(jwtService, times(1)).validateVerifyToken(VALID_TOKEN);
        verify(validateSignupService, never()).validateUserSignup(any());
        verify(userSignupService, never()).signup(any());
    }

    @Test
    void signupUser_ShouldReturnBadRequest_WhenValidationFails() {
        // Arrange
        String validationError = "Password must contain at least one uppercase letter";
        when(jwtService.validateVerifyToken(VALID_TOKEN)).thenReturn(VALID_EMAIL);
        when(validateSignupService.validateUserSignup(signupUserDTO)).thenReturn(validationError);

        // Act
        ResponseEntity<String> response = signupController.signupUser(signupUserDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(validationError, response.getBody());
        verify(jwtService, times(1)).validateVerifyToken(VALID_TOKEN);
        verify(validateSignupService, times(1)).validateUserSignup(signupUserDTO);
        verify(userSignupService, never()).signup(any());
    }

    @Test
    void signupUser_ShouldReturnBadRequest_WhenUserAlreadyExists() {
        // Arrange
        when(jwtService.validateVerifyToken(VALID_TOKEN)).thenReturn(VALID_EMAIL);
        when(validateSignupService.validateUserSignup(signupUserDTO)).thenReturn(null);
        when(userSignupService.signup(signupUserDTO)).thenReturn(null);

        // Act
        ResponseEntity<String> response = signupController.signupUser(signupUserDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("user with the same username or email already exists", response.getBody());
        verify(jwtService, times(1)).validateVerifyToken(VALID_TOKEN);
        verify(validateSignupService, times(1)).validateUserSignup(signupUserDTO);
        verify(userSignupService, times(1)).signup(signupUserDTO);
    }

    @Test
    void signupUser_ShouldBeCaseInsensitive_WhenComparingEmails() {
        // Arrange
        signupUserDTO.setEmail("TEST@EXAMPLE.COM");
        when(jwtService.validateVerifyToken(VALID_TOKEN)).thenReturn("test@example.com");
        when(validateSignupService.validateUserSignup(signupUserDTO)).thenReturn(null);
        when(userSignupService.signup(signupUserDTO)).thenReturn(VALID_JWT);

        // Act
        ResponseEntity<String> response = signupController.signupUser(signupUserDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(VALID_JWT, response.getBody());
        verify(jwtService, times(1)).validateVerifyToken(VALID_TOKEN);
        verify(validateSignupService, times(1)).validateUserSignup(signupUserDTO);
        verify(userSignupService, times(1)).signup(signupUserDTO);
    }
}
