package com.homemate.signup;

import com.homemate.TaskerProfile.DTO.TaskerSignupDTO;
import com.homemate.TaskerProfile.controllers.SignupTaskerController;
import com.homemate.TaskerProfile.services.TaskerSignupService;
import com.homemate.security.service.ValidateSignupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignupTaskerControllerTest {

    @Mock
    private TaskerSignupService taskerSignupService;

    @Mock
    private ValidateSignupService validateSignup;

    @InjectMocks
    private SignupTaskerController signupTaskerController;

    private TaskerSignupDTO taskerSignupDTO;

    @BeforeEach
    void setUp() {
        taskerSignupDTO = new TaskerSignupDTO();
        taskerSignupDTO.setUsername("taskeruser");
        taskerSignupDTO.setFirstName("Tasker");
        taskerSignupDTO.setLastName("User");
        taskerSignupDTO.setEmail("tasker@example.com");
        taskerSignupDTO.setPassword("Password123!");
        taskerSignupDTO.setPhoneNumber("01012345678");
        taskerSignupDTO.setDateOfBirth(Timestamp.valueOf(LocalDateTime.of(1990, 1, 1, 0, 0)));
        taskerSignupDTO.setBio("This is a valid bio that is long enough");
        taskerSignupDTO.setHourRate(50.0);
        taskerSignupDTO.setCity("Cairo");
        taskerSignupDTO.setProfileImage(new byte[1024]);
    }

    @Test
    void signupShouldReturnOkWithValidData() {
        when(validateSignup.validateTaskerSignup(taskerSignupDTO)).thenReturn(null);
        when(taskerSignupService.registerTasker(taskerSignupDTO)).thenReturn("jwt-token-123");

        ResponseEntity<String> response = signupTaskerController.signup(taskerSignupDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token-123", response.getBody());
        verify(validateSignup, times(1)).validateTaskerSignup(taskerSignupDTO);
        verify(taskerSignupService, times(1)).registerTasker(taskerSignupDTO);
    }

    @Test
    void signupShouldReturnBadRequestWhenTaskerAlreadyExists() {
        when(validateSignup.validateTaskerSignup(taskerSignupDTO)).thenReturn(null);
        when(taskerSignupService.registerTasker(taskerSignupDTO)).thenReturn(null);

        ResponseEntity<String> response = signupTaskerController.signup(taskerSignupDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("user with the same username or email already exists", response.getBody());
        verify(validateSignup, times(1)).validateTaskerSignup(taskerSignupDTO);
        verify(taskerSignupService, times(1)).registerTasker(taskerSignupDTO);
    }

    @Test
    void signupShouldReturnBadRequestWithEmailValidationError() {
        String emailError = "Email is required";
        when(validateSignup.validateTaskerSignup(taskerSignupDTO)).thenReturn(emailError);

        ResponseEntity<String> response = signupTaskerController.signup(taskerSignupDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(emailError, response.getBody());
        verify(taskerSignupService, never()).registerTasker(any());
    }

}

