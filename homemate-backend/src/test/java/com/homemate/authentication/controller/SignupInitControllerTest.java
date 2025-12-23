package com.homemate.authentication.controller;

import com.homemate.Authentication.controller.SignupInitController;
import com.homemate.Authentication.dto.GoogleTokenDto;
import com.homemate.Authentication.dto.GoogleUserDto;
import com.homemate.Authentication.service.GoogleTokenVerifierService;
import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.TaskerProfile.models.Tasker;
import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.UserProfile.DTO.GoogleSignupResponseDTO;
import com.homemate.UserProfile.Models.User;
import com.homemate.security.service.JwtService;
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
class SignupInitControllerTest {

    @Mock
    private GoogleTokenVerifierService googleTokenVerifierService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDao userDao;

    @Mock
    private TaskerDao taskerDao;

    @InjectMocks
    private SignupInitController signupInitController;

    private GoogleTokenDto googleTokenDto;
    private GoogleUserDto googleUserDto;

    @BeforeEach
    void setUp() {
        googleTokenDto = new GoogleTokenDto();
        googleTokenDto.setIdToken("valid-google-token-123");

        googleUserDto = new GoogleUserDto(
            "google-sub-123",
            "newuser@example.com",
            true,
            "John Doe",
            "John",
            "Doe",
            "picture-url",
            "en",
            System.currentTimeMillis() / 1000,
            System.currentTimeMillis() / 1000 + 3600
        );
    }

    @Test
    void initGoogleSignupShouldReturnOkWithValidToken() {
        when(googleTokenVerifierService.verify(googleTokenDto.getIdToken()))
            .thenReturn(googleUserDto);
        when(userDao.getByEmail(googleUserDto.getEmail())).thenReturn(null);
        when(taskerDao.getByEmail(googleUserDto.getEmail())).thenReturn(null);
        when(jwtService.generateVerifyToken(googleUserDto.getEmail()))
            .thenReturn("verify-token-123");

        ResponseEntity<?> response = signupInitController.initGoogleSignup(googleTokenDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof GoogleSignupResponseDTO);

        GoogleSignupResponseDTO responseBody = (GoogleSignupResponseDTO) response.getBody();
        assertEquals("newuser@example.com", responseBody.getEmail());
        assertEquals("John", responseBody.getFirstName());
        assertEquals("Doe", responseBody.getLastName());
        assertEquals("newuser", responseBody.getUsername());
        assertEquals("verify-token-123", responseBody.getVerifyToken());

        verify(googleTokenVerifierService, times(1))
            .verify(googleTokenDto.getIdToken());
        verify(userDao, times(1)).getByEmail(googleUserDto.getEmail());
        verify(taskerDao, times(1)).getByEmail(googleUserDto.getEmail());
        verify(jwtService, times(1)).generateVerifyToken(googleUserDto.getEmail());
    }

    @Test
    void initGoogleSignupShouldReturnBadRequestWithInvalidToken() {
        when(googleTokenVerifierService.verify(googleTokenDto.getIdToken()))
            .thenReturn(null);

        ResponseEntity<?> response = signupInitController.initGoogleSignup(googleTokenDto);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid Google token", response.getBody());

        verify(googleTokenVerifierService, times(1))
            .verify(googleTokenDto.getIdToken());
        verify(userDao, never()).getByEmail(any());
        verify(taskerDao, never()).getByEmail(any());
        verify(jwtService, never()).generateVerifyToken(any());
    }

    @Test
    void initGoogleSignupShouldReturnBadRequestWithUnverifiedEmail() {
        GoogleUserDto unverifiedUser = new GoogleUserDto(
            "google-sub-456",
            "unverified@example.com",
            false, // Email not verified
            "Jane Doe",
            "Jane",
            "Doe",
            "picture-url",
            "en",
            System.currentTimeMillis() / 1000,
            System.currentTimeMillis() / 1000 + 3600
        );
        when(googleTokenVerifierService.verify(googleTokenDto.getIdToken()))
            .thenReturn(unverifiedUser);

        ResponseEntity<?> response = signupInitController.initGoogleSignup(googleTokenDto);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email not verified by Google", response.getBody());

        verify(googleTokenVerifierService, times(1))
            .verify(googleTokenDto.getIdToken());
        verify(userDao, never()).getByEmail(any());
        verify(taskerDao, never()).getByEmail(any());
        verify(jwtService, never()).generateVerifyToken(any());
    }

    @Test
    void initGoogleSignupShouldReturnBadRequestIfEmailAlreadyExistsAsUser() {
        User existingUser = new User();
        existingUser.setEmail(googleUserDto.getEmail());

        when(googleTokenVerifierService.verify(googleTokenDto.getIdToken()))
            .thenReturn(googleUserDto);
        when(userDao.getByEmail(googleUserDto.getEmail())).thenReturn(existingUser);

        ResponseEntity<?> response = signupInitController.initGoogleSignup(googleTokenDto);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already Exists", response.getBody());

        verify(googleTokenVerifierService, times(1))
            .verify(googleTokenDto.getIdToken());
        verify(userDao, times(1)).getByEmail(googleUserDto.getEmail());
        verify(taskerDao, never()).getByEmail(any());
        verify(jwtService, never()).generateVerifyToken(any());
    }

    @Test
    void initGoogleSignupShouldReturnBadRequestIfEmailAlreadyExistsAsTasker() {
        Tasker existingTasker = new Tasker();
        existingTasker.setEmail(googleUserDto.getEmail());

        when(googleTokenVerifierService.verify(googleTokenDto.getIdToken()))
            .thenReturn(googleUserDto);
        when(userDao.getByEmail(googleUserDto.getEmail())).thenReturn(null);
        when(taskerDao.getByEmail(googleUserDto.getEmail())).thenReturn(existingTasker);

        ResponseEntity<?> response = signupInitController.initGoogleSignup(googleTokenDto);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already Exists", response.getBody());

        verify(googleTokenVerifierService, times(1))
            .verify(googleTokenDto.getIdToken());
        verify(userDao, times(1)).getByEmail(googleUserDto.getEmail());
        verify(taskerDao, times(1)).getByEmail(googleUserDto.getEmail());
        verify(jwtService, never()).generateVerifyToken(any());
    }

    @Test
    void initGoogleSignupShouldExtractUsernameFromEmailCorrectly() {
        GoogleUserDto userWithComplexEmail = new GoogleUserDto(
            "google-sub-789",
            "john.doe123@example.com",
            true,
            "John Doe",
            "John",
            "Doe",
            "picture-url",
            "en",
            System.currentTimeMillis() / 1000,
            System.currentTimeMillis() / 1000 + 3600
        );

        when(googleTokenVerifierService.verify(googleTokenDto.getIdToken()))
            .thenReturn(userWithComplexEmail);
        when(userDao.getByEmail(userWithComplexEmail.getEmail())).thenReturn(null);
        when(taskerDao.getByEmail(userWithComplexEmail.getEmail())).thenReturn(null);
        when(jwtService.generateVerifyToken(userWithComplexEmail.getEmail()))
            .thenReturn("verify-token-456");

        ResponseEntity<?> response = signupInitController.initGoogleSignup(googleTokenDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        GoogleSignupResponseDTO responseBody = (GoogleSignupResponseDTO) response.getBody();
        assertEquals("john.doe123", responseBody.getUsername());
    }

    @Test
    void initGoogleSignupShouldGenerateVerifyTokenWithCorrectEmail() {
        when(googleTokenVerifierService.verify(googleTokenDto.getIdToken()))
            .thenReturn(googleUserDto);
        when(userDao.getByEmail(googleUserDto.getEmail())).thenReturn(null);
        when(taskerDao.getByEmail(googleUserDto.getEmail())).thenReturn(null);
        when(jwtService.generateVerifyToken(googleUserDto.getEmail()))
            .thenReturn("verify-token-789");

        ResponseEntity<?> response = signupInitController.initGoogleSignup(googleTokenDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jwtService, times(1))
            .generateVerifyToken("newuser@example.com");
    }
}
