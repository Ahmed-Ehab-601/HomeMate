package com.homemate.taskerprofile.controllers;

import com.homemate.TaskerProfile.controllers.SignupTaskerController;
import com.homemate.TaskerProfile.DTO.TaskerSignupDTO;
import com.homemate.TaskerProfile.services.TaskerSignupService;
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
class SignupTaskerControllerTest {

    @Mock
    private TaskerSignupService taskerSignupService;

    @Mock
    private ValidateSignupService validateSignupService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private SignupTaskerController signupTaskerController;

    private TaskerSignupDTO taskerSignupDTO;
    private static final String VALID_JWT = "valid_token";
    private static final String VALID_TOKEN = "valid-verify-token";
    private static final String VALID_EMAIL = "tasker@example.com";

    @BeforeEach
    void setUp() {
        taskerSignupDTO = new TaskerSignupDTO();
        taskerSignupDTO.setUsername("testtasker");
        taskerSignupDTO.setEmail(VALID_EMAIL);
        taskerSignupDTO.setPassword("Password123!");
        taskerSignupDTO.setPhoneNumber("+1234567890");
        taskerSignupDTO.setDateOfBirth(Timestamp.valueOf("1990-01-01 00:00:00"));
        taskerSignupDTO.setBio("Experienced tasker");
        taskerSignupDTO.setProfileImage(null);
        taskerSignupDTO.setServiceID(1L);
        taskerSignupDTO.setHourRate(25.0);
        taskerSignupDTO.setFirstName("Test");
        taskerSignupDTO.setLastName("Tasker");
        taskerSignupDTO.setCity("New York");
        taskerSignupDTO.setVerifyToken(VALID_TOKEN);
    }

    @Test
    void signup_ShouldReturnJwt_WhenAllValidationsPass() {
        // Arrange
        when(jwtService.validateVerifyToken(VALID_TOKEN)).thenReturn(VALID_EMAIL);
        when(validateSignupService.validateTaskerSignup(taskerSignupDTO)).thenReturn(null);
        when(taskerSignupService.registerTasker(taskerSignupDTO)).thenReturn(VALID_JWT);

        // Act
        ResponseEntity<String> response = signupTaskerController.signup(taskerSignupDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(VALID_JWT, response.getBody());
        verify(jwtService, times(1)).validateVerifyToken(VALID_TOKEN);
        verify(validateSignupService, times(1)).validateTaskerSignup(taskerSignupDTO);
        verify(taskerSignupService, times(1)).registerTasker(taskerSignupDTO);
    }

    @Test
    void signup_ShouldReturnBadRequest_WhenVerifyTokenIsNull() {
        // Arrange
        taskerSignupDTO.setVerifyToken(null);

        // Act
        ResponseEntity<String> response = signupTaskerController.signup(taskerSignupDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No verification token found", response.getBody());
        verify(jwtService, never()).validateVerifyToken(any());
        verify(validateSignupService, never()).validateTaskerSignup(any());
        verify(taskerSignupService, never()).registerTasker(any());
    }

    @Test
    void signup_ShouldReturnBadRequest_WhenVerifyTokenIsEmpty() {
        // Arrange
        taskerSignupDTO.setVerifyToken("");

        // Act
        ResponseEntity<String> response = signupTaskerController.signup(taskerSignupDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No verification token found", response.getBody());
        verify(jwtService, never()).validateVerifyToken(any());
        verify(validateSignupService, never()).validateTaskerSignup(any());
        verify(taskerSignupService, never()).registerTasker(any());
    }

    @Test
    void signup_ShouldReturnBadRequest_WhenVerifyTokenIsInvalid() {
        // Arrange
        when(jwtService.validateVerifyToken(VALID_TOKEN)).thenReturn(null);

        // Act
        ResponseEntity<String> response = signupTaskerController.signup(taskerSignupDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid or expired verification token", response.getBody());
        verify(jwtService, times(1)).validateVerifyToken(VALID_TOKEN);
        verify(validateSignupService, never()).validateTaskerSignup(any());
        verify(taskerSignupService, never()).registerTasker(any());
    }

    @Test
    void signup_ShouldReturnBadRequest_WhenEmailDoesNotMatchVerifiedEmail() {
        // Arrange
        when(jwtService.validateVerifyToken(VALID_TOKEN)).thenReturn("different@example.com");

        // Act
        ResponseEntity<String> response = signupTaskerController.signup(taskerSignupDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email does not match verified email", response.getBody());
        verify(jwtService, times(1)).validateVerifyToken(VALID_TOKEN);
        verify(validateSignupService, never()).validateTaskerSignup(any());
        verify(taskerSignupService, never()).registerTasker(any());
    }

    @Test
    void signup_ShouldReturnBadRequest_WhenValidationFails() {
        // Arrange
        String validationError = "Hour rate must be a positive number";
        when(jwtService.validateVerifyToken(VALID_TOKEN)).thenReturn(VALID_EMAIL);
        when(validateSignupService.validateTaskerSignup(taskerSignupDTO)).thenReturn(validationError);

        // Act
        ResponseEntity<String> response = signupTaskerController.signup(taskerSignupDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(validationError, response.getBody());
        verify(jwtService, times(1)).validateVerifyToken(VALID_TOKEN);
        verify(validateSignupService, times(1)).validateTaskerSignup(taskerSignupDTO);
        verify(taskerSignupService, never()).registerTasker(any());
    }

    @Test
    void signup_ShouldReturnBadRequest_WhenTaskerAlreadyExists() {
        // Arrange
        when(jwtService.validateVerifyToken(VALID_TOKEN)).thenReturn(VALID_EMAIL);
        when(validateSignupService.validateTaskerSignup(taskerSignupDTO)).thenReturn(null);
        when(taskerSignupService.registerTasker(taskerSignupDTO)).thenReturn(null);

        // Act
        ResponseEntity<String> response = signupTaskerController.signup(taskerSignupDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("user with the same username or email already exists", response.getBody());
        verify(jwtService, times(1)).validateVerifyToken(VALID_TOKEN);
        verify(validateSignupService, times(1)).validateTaskerSignup(taskerSignupDTO);
        verify(taskerSignupService, times(1)).registerTasker(taskerSignupDTO);
    }

    @Test
    void signup_ShouldBeCaseInsensitive_WhenComparingEmails() {
        // Arrange
        taskerSignupDTO.setEmail("TASKER@EXAMPLE.COM");
        when(jwtService.validateVerifyToken(VALID_TOKEN)).thenReturn("tasker@example.com");
        when(validateSignupService.validateTaskerSignup(taskerSignupDTO)).thenReturn(null);
        when(taskerSignupService.registerTasker(taskerSignupDTO)).thenReturn(VALID_JWT);

        // Act
        ResponseEntity<String> response = signupTaskerController.signup(taskerSignupDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(VALID_JWT, response.getBody());
        verify(jwtService, times(1)).validateVerifyToken(VALID_TOKEN);
        verify(validateSignupService, times(1)).validateTaskerSignup(taskerSignupDTO);
        verify(taskerSignupService, times(1)).registerTasker(taskerSignupDTO);
    }

    @Test
    void signup_ShouldHandleWhitespaceInToken() {
        // Arrange
        taskerSignupDTO.setVerifyToken("   ");

        // Act
        ResponseEntity<String> response = signupTaskerController.signup(taskerSignupDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
