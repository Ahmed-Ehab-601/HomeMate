package com.homemate.signup;

import com.homemate.UserProfile.Controllers.SignupController;
import com.homemate.UserProfile.DTO.SignupUserDTO;
import com.homemate.UserProfile.Services.UserSignupService;
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
class SignupControllerTest {

    @Mock
    private UserSignupService userSignupService;

    @Mock
    private ValidateSignupService validateSignup;

    @InjectMocks
    private SignupController signupController;

    private SignupUserDTO signupUserDTO;

    @BeforeEach
    void setUp() {
        signupUserDTO = new SignupUserDTO();
        signupUserDTO.setUsername("testuser");
        signupUserDTO.setFirstName("Test");
        signupUserDTO.setLastName("User");
        signupUserDTO.setEmail("test@example.com");
        signupUserDTO.setPassword("Password123!");
        signupUserDTO.setBirthDate(Timestamp.valueOf(LocalDateTime.of(1990, 1, 1, 0, 0)));
        signupUserDTO.setGender('M');
        signupUserDTO.setPhone("01012345678");
    }

    @Test
    void signupUserShouldReturnOkWithValidData() {
        when(validateSignup.validateUserSignup(signupUserDTO)).thenReturn(null);
        when(userSignupService.signup(signupUserDTO)).thenReturn("jwt-token-123");

        ResponseEntity<String> response = signupController.signupUser(signupUserDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token-123", response.getBody());
        verify(validateSignup, times(1)).validateUserSignup(signupUserDTO);
        verify(userSignupService, times(1)).signup(signupUserDTO);
    }

    @Test
    void signupUserShouldReturnBadRequestWhenUserAlreadyExists() {
        when(validateSignup.validateUserSignup(signupUserDTO)).thenReturn(null);
        when(userSignupService.signup(signupUserDTO)).thenReturn(null);

        ResponseEntity<String> response = signupController.signupUser(signupUserDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("user with the same username or email already exists", response.getBody());
        verify(validateSignup, times(1)).validateUserSignup(signupUserDTO);
        verify(userSignupService, times(1)).signup(signupUserDTO);
    }

    @Test
    void signupUserShouldReturnBadRequestWithEmailValidationError() {
        String emailError = "Email is required";
        when(validateSignup.validateUserSignup(signupUserDTO)).thenReturn(emailError);

        ResponseEntity<String> response = signupController.signupUser(signupUserDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(emailError, response.getBody());
        verify(userSignupService, never()).signup(any());
    }

}

