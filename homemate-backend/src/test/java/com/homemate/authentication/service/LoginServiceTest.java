package com.homemate.authentication.service;

import com.homemate.TaskerProfile.models.Tasker;
import com.homemate.UserProfile.Models.User;
import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.Authentication.dto.LoginRequestDto;
import com.homemate.Authentication.dto.LoginResponseDto;
import com.homemate.Authentication.dto.PasswordResetDto;
import com.homemate.Authentication.service.LoginService;
import com.homemate.chat.Service.ChatService;
import com.homemate.hashing.HashingService;
import com.homemate.security.service.JwtService;
import com.homemate.security.service.ValidateSignupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private TaskerDao taskerDao;

    @Mock
    private UserDao userDao;

    @Mock
    private JwtService jwtService;
    @Mock
    private ChatService chatService;

    @Mock
    private ValidateSignupService validateSignup;

    @Mock
    private HashingService hashingService;

    @InjectMocks
    private LoginService loginService;

    private LoginRequestDto loginRequestDto;
    private User user;
    private Tasker tasker;

    @BeforeEach
    void setUp() {
        loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("test@example.com");
        loginRequestDto.setPassword("password123");

        user = new User();
        user.setUserID(1L);
        user.setUsername("testuser");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setIsAdmin(Boolean.FALSE);
        user.setIsSuspended(Boolean.FALSE);
        user.setBirthDate(java.sql.Timestamp.valueOf(LocalDateTime.of(1990, 1, 1, 0, 0)));
        user.setGender('M');
        user.setPhone("01012345678");

        tasker = new Tasker();
        tasker.setTaskerID(1L);
        tasker.setUsername("taskeruser");
        tasker.setFirstName("Tasker");
        tasker.setLastName("User");
        tasker.setEmail("tasker@example.com");
        tasker.setPassword("password123");
        tasker.setBirthDate(java.sql.Timestamp.valueOf(LocalDateTime.of(1990, 1, 1, 0, 0)));
        tasker.setGender('M');
        tasker.setPhone("01012345678");
        tasker.setIsSuspended(false);

        lenient().when(validateSignup.validateEmail(anyString())).thenReturn((String) null);
        lenient().when(validateSignup.validatePassword(anyString())).thenReturn((String) null);
    }

    @Test
    void loginWithEmailPasswordShouldReturnResponseWithValidUserCredentials() {
        when(userDao.getByEmail(loginRequestDto.getEmail())).thenReturn(user);
        when(jwtService.generateToken(1L, "testuser", "test@example.com", "ROLE_USER"))
            .thenReturn("jwt-token-123");
        when(hashingService.verifyPassword(loginRequestDto.getPassword(),user.getPassword())).thenReturn(true);

        LoginResponseDto response = loginService.loginWithEmailPassword(loginRequestDto);

        assertNotNull(response);
        assertEquals("ROLE_USER", response.getRole());
        assertEquals("testuser", response.getUsername());
        assertEquals("Test", response.getFirstname());
        assertEquals("User", response.getLastname());
        assertEquals("jwt-token-123", response.getToken());
        verify(userDao, times(1)).getByEmail(loginRequestDto.getEmail());
        verify(jwtService, times(1)).generateToken(1L, "testuser", "test@example.com", "ROLE_USER");
    }

    @Test
    void loginWithEmailPasswordShouldReturnResponseWithAdminRole() {
        user.setIsAdmin(Boolean.TRUE);
        when(userDao.getByEmail(loginRequestDto.getEmail())).thenReturn(user);
        when(jwtService.generateToken(1L, "testuser", "test@example.com", "ROLE_ADMIN"))
            .thenReturn("admin-token-123");
        when(hashingService.verifyPassword(loginRequestDto.getPassword(),user.getPassword())).thenReturn(true);

        LoginResponseDto response = loginService.loginWithEmailPassword(loginRequestDto);

        assertEquals("ROLE_ADMIN", response.getRole());
        verify(jwtService, times(1)).generateToken(1L, "testuser", "test@example.com", "ROLE_ADMIN");
    }

    @Test
    void loginWithEmailPasswordShouldReturnResponseWithValidTaskerCredentials() {
        loginRequestDto.setEmail("tasker@example.com");
        when(userDao.getByEmail(loginRequestDto.getEmail()))
            .thenThrow(new EmptyResultDataAccessException(1));
        when(taskerDao.getByEmail(loginRequestDto.getEmail())).thenReturn(tasker);
        when(jwtService.generateToken(1L, "taskeruser", "tasker@example.com", "ROLE_TASKER"))
            .thenReturn("tasker-token-123");
        when(hashingService.verifyPassword(loginRequestDto.getPassword(),tasker.getPassword())).thenReturn(true);

        LoginResponseDto response = loginService.loginWithEmailPassword(loginRequestDto);

        assertNotNull(response);
        assertEquals("ROLE_TASKER", response.getRole());
        assertEquals("taskeruser", response.getUsername());
        assertEquals("Tasker", response.getFirstname());
        assertEquals("User", response.getLastname());
        assertEquals("tasker-token-123", response.getToken());
        verify(userDao, times(1)).getByEmail(loginRequestDto.getEmail());
        verify(taskerDao, times(1)).getByEmail(loginRequestDto.getEmail());
    }

    @Test
    void loginWithEmailPasswordShouldReturnNullWithWrongPassword() {
        when(userDao.getByEmail(loginRequestDto.getEmail())).thenReturn(user);
        loginRequestDto.setPassword("wrongpassword");
        when(hashingService.verifyPassword(loginRequestDto.getPassword(),user.getPassword())).thenReturn(false);

        LoginResponseDto response = loginService.loginWithEmailPassword(loginRequestDto);

        assertNull(response);
        verify(userDao, times(1)).getByEmail(loginRequestDto.getEmail());
        verify(jwtService, never()).generateToken(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void loginWithEmailPasswordShouldReturnNullWithNonExistentEmail() {
        when(userDao.getByEmail(loginRequestDto.getEmail()))
            .thenThrow(new EmptyResultDataAccessException(1));
        when(taskerDao.getByEmail(loginRequestDto.getEmail()))
            .thenThrow(new EmptyResultDataAccessException(1));

        LoginResponseDto response = loginService.loginWithEmailPassword(loginRequestDto);

        assertNull(response);
        verify(userDao, times(1)).getByEmail(loginRequestDto.getEmail());
        verify(taskerDao, times(1)).getByEmail(loginRequestDto.getEmail());
    }

    @Test
    void loginWithEmailPasswordShouldReturnNullWithNullEmail() {
        loginRequestDto.setEmail(null);

        LoginResponseDto response = loginService.loginWithEmailPassword(loginRequestDto);

        assertNull(response);
        verify(userDao, never()).getByEmail(anyString());
    }

    @Test
    void loginWithEmailPasswordShouldReturnNullWithEmptyEmail() {
        loginRequestDto.setEmail("");

        LoginResponseDto response = loginService.loginWithEmailPassword(loginRequestDto);

        assertNull(response);
        verify(userDao, never()).getByEmail(anyString());
    }

    @Test
    void loginWithEmailPasswordShouldReturnNullWithNullPassword() {
        loginRequestDto.setPassword(null);

        LoginResponseDto response = loginService.loginWithEmailPassword(loginRequestDto);

        assertNull(response);
        verify(userDao, never()).getByEmail(anyString());
    }

    @Test
    void loginWithEmailPasswordShouldReturnNullWithEmptyPassword() {
        loginRequestDto.setPassword("");

        LoginResponseDto response = loginService.loginWithEmailPassword(loginRequestDto);

        assertNull(response);
        verify(userDao, never()).getByEmail(anyString());
    }

    @Test
    void loginWithEmailShouldReturnResponseWithValidUserEmail() {
        when(userDao.getByEmail("test@example.com")).thenReturn(user);
        when(jwtService.generateToken(1L, "testuser", "test@example.com", "ROLE_USER"))
            .thenReturn("jwt-token-123");

        LoginResponseDto response = loginService.login("test@example.com");

        assertNotNull(response);
        assertEquals("ROLE_USER", response.getRole());
        assertEquals("testuser", response.getUsername());
        assertEquals("jwt-token-123", response.getToken());
        verify(userDao, times(1)).getByEmail("test@example.com");
    }

    @Test
    void loginWithEmailShouldReturnResponseWithAdminRole() {
        user.setIsAdmin(Boolean.TRUE);
        when(userDao.getByEmail("test@example.com")).thenReturn(user);
        when(jwtService.generateToken(1L, "testuser", "test@example.com", "ROLE_ADMIN"))
            .thenReturn("admin-token-123");

        LoginResponseDto response = loginService.login("test@example.com");

        assertEquals("ROLE_ADMIN", response.getRole());
        verify(jwtService, times(1)).generateToken(1L, "testuser", "test@example.com", "ROLE_ADMIN");
    }

    @Test
    void loginWithEmailShouldReturnResponseWithValidTaskerEmail() {
        when(userDao.getByEmail("tasker@example.com"))
            .thenThrow(new EmptyResultDataAccessException(1));
        when(taskerDao.getByEmail("tasker@example.com")).thenReturn(tasker);
        when(jwtService.generateToken(1L, "taskeruser", "tasker@example.com", "ROLE_TASKER"))
            .thenReturn("tasker-token-123");

        LoginResponseDto response = loginService.login("tasker@example.com");

        assertNotNull(response);
        assertEquals("ROLE_TASKER", response.getRole());
        assertEquals("taskeruser", response.getUsername());
        verify(taskerDao, times(1)).getByEmail("tasker@example.com");
    }

    @Test
    void loginWithEmailShouldReturnNullWithNonExistentEmail() {
        when(userDao.getByEmail("nonexistent@example.com"))
            .thenThrow(new EmptyResultDataAccessException(1));
        when(taskerDao.getByEmail("nonexistent@example.com"))
            .thenThrow(new EmptyResultDataAccessException(1));

        LoginResponseDto response = loginService.login("nonexistent@example.com");

        assertNull(response);
        verify(userDao, times(1)).getByEmail("nonexistent@example.com");
        verify(taskerDao, times(1)).getByEmail("nonexistent@example.com");
    }

    @Test
    void resetPasswordShouldReturnNullForValidUserPasswordReset() {
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setVerifyToken("valid-token");
        passwordResetDto.setNewPassword("NewPass123!");

        when(jwtService.validateVerifyToken("valid-token")).thenReturn("test@example.com");
        when(validateSignup.validatePassword("NewPass123!")).thenReturn(null);
        when(userDao.getByEmail("test@example.com")).thenReturn(user);
        when(hashingService.hashPassword(passwordResetDto.getNewPassword())).
                thenReturn(passwordResetDto.getNewPassword());

        String result = loginService.resetPassword(passwordResetDto);

        assertNull(result);
        verify(jwtService, times(1)).validateVerifyToken("valid-token");
        verify(validateSignup, times(1)).validatePassword("NewPass123!");
        verify(userDao, times(1)).getByEmail("test@example.com");
        verify(userDao, times(1)).updatePassword("test@example.com", "NewPass123!");
        verify(taskerDao, never()).getByEmail(anyString());
    }

    @Test
    void resetPasswordShouldReturnNullForValidTaskerPasswordReset() {
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setVerifyToken("valid-token");
        passwordResetDto.setNewPassword("NewPass123!");

        when(jwtService.validateVerifyToken("valid-token")).thenReturn("tasker@example.com");
        when(validateSignup.validatePassword("NewPass123!")).thenReturn(null);
        when(userDao.getByEmail("tasker@example.com")).thenThrow(new EmptyResultDataAccessException(1));
        when(taskerDao.getByEmail("tasker@example.com")).thenReturn(tasker);
        when(hashingService.hashPassword(passwordResetDto.getNewPassword())).
                thenReturn(passwordResetDto.getNewPassword());

        String result = loginService.resetPassword(passwordResetDto);

        assertNull(result);
        verify(jwtService, times(1)).validateVerifyToken("valid-token");
        verify(validateSignup, times(1)).validatePassword("NewPass123!");
        verify(userDao, times(1)).getByEmail("tasker@example.com");
        verify(taskerDao, times(1)).getByEmail("tasker@example.com");
        verify(taskerDao, times(1)).updatePassword("tasker@example.com", "NewPass123!");
    }

    @Test
    void resetPasswordShouldReturnErrorForInvalidToken() {
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setVerifyToken("invalid-token");
        passwordResetDto.setNewPassword("NewPass123!");

        when(jwtService.validateVerifyToken("invalid-token")).thenReturn(null);

        String result = loginService.resetPassword(passwordResetDto);

        assertEquals("Invalid or expired verification token", result);
        verify(jwtService, times(1)).validateVerifyToken("invalid-token");
        verify(validateSignup, never()).validatePassword(anyString());
        verify(userDao, never()).getByEmail(anyString());
        verify(taskerDao, never()).getByEmail(anyString());
    }

    @Test
    void resetPasswordShouldReturnErrorForWeakPassword() {
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setVerifyToken("valid-token");
        passwordResetDto.setNewPassword("weak");

        when(jwtService.validateVerifyToken("valid-token")).thenReturn("test@example.com");
        when(validateSignup.validatePassword("weak"))
            .thenReturn("password must be between 8 and 25 characters");

        String result = loginService.resetPassword(passwordResetDto);

        assertEquals("password must be between 8 and 25 characters", result);
        verify(jwtService, times(1)).validateVerifyToken("valid-token");
        verify(validateSignup, times(1)).validatePassword("weak");
        verify(userDao, never()).getByEmail(anyString());
        verify(taskerDao, never()).getByEmail(anyString());
    }

    @Test
    void resetPasswordShouldReturnErrorWhenUserNotFound() {
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setVerifyToken("valid-token");
        passwordResetDto.setNewPassword("NewPass123!");

        when(jwtService.validateVerifyToken("valid-token")).thenReturn("nonexistent@example.com");
        when(validateSignup.validatePassword("NewPass123!")).thenReturn(null);
        when(userDao.getByEmail("nonexistent@example.com")).thenThrow(new EmptyResultDataAccessException(1));
        when(taskerDao.getByEmail("nonexistent@example.com")).thenThrow(new EmptyResultDataAccessException(1));

        String result = loginService.resetPassword(passwordResetDto);

        assertEquals("No user or tasker found with this email", result);
        verify(userDao, times(1)).getByEmail("nonexistent@example.com");
        verify(taskerDao, times(1)).getByEmail("nonexistent@example.com");
        verify(userDao, never()).updatePassword(anyString(), anyString());
        verify(taskerDao, never()).updatePassword(anyString(), anyString());
    }

    @Test
    void resetPasswordShouldReturnErrorForPasswordWithoutUppercase() {
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setVerifyToken("valid-token");
        passwordResetDto.setNewPassword("newpass123!");

        when(jwtService.validateVerifyToken("valid-token")).thenReturn("test@example.com");
        when(validateSignup.validatePassword("newpass123!"))
            .thenReturn("Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character");

        String result = loginService.resetPassword(passwordResetDto);

        assertEquals("Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character", result);
        verify(validateSignup, times(1)).validatePassword("newpass123!");
        verify(userDao, never()).updatePassword(anyString(), anyString());
    }

    @Test
    void resetPasswordShouldHandleUserDaoException() {
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setVerifyToken("valid-token");
        passwordResetDto.setNewPassword("NewPass123!");

        when(jwtService.validateVerifyToken("valid-token")).thenReturn("test@example.com");
        when(validateSignup.validatePassword("NewPass123!")).thenReturn(null);
        when(userDao.getByEmail("test@example.com")).thenReturn(user);
        when(hashingService.hashPassword(passwordResetDto.getNewPassword())).
                thenReturn(passwordResetDto.getNewPassword());
        doThrow(new RuntimeException("Database error")).when(userDao).updatePassword("test@example.com", "NewPass123!");


        String result = loginService.resetPassword(passwordResetDto);

        assertEquals("Error updating password: Database error", result);
        verify(userDao, times(1)).updatePassword("test@example.com", "NewPass123!");
    }

    @Test
    void resetPasswordShouldHandleTaskerDaoException() {
        PasswordResetDto passwordResetDto = new PasswordResetDto();
        passwordResetDto.setVerifyToken("valid-token");
        passwordResetDto.setNewPassword("NewPass123!");

        when(jwtService.validateVerifyToken("valid-token")).thenReturn("tasker@example.com");
        when(validateSignup.validatePassword("NewPass123!")).thenReturn(null);
        when(userDao.getByEmail("tasker@example.com")).thenThrow(new EmptyResultDataAccessException(1));
        when(taskerDao.getByEmail("tasker@example.com")).thenReturn(tasker);
        when(hashingService.hashPassword(passwordResetDto.getNewPassword())).
                thenReturn(passwordResetDto.getNewPassword());
        doThrow(new RuntimeException("Database error")).when(taskerDao).updatePassword("tasker@example.com", "NewPass123!");

        String result = loginService.resetPassword(passwordResetDto);

        assertEquals("Error updating password: Database error", result);
        verify(taskerDao, times(1)).updatePassword("tasker@example.com", "NewPass123!");
    }
}

