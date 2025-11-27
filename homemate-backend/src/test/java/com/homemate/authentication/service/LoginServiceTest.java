package com.homemate.authentication.service;

import com.homemate.TaskerProfile.models.Tasker;
import com.homemate.UserProfile.Models.User;
import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.Authentication.dto.LoginRequestDto;
import com.homemate.Authentication.dto.LoginResponseDto;
import com.homemate.Authentication.service.LoginService;
import com.homemate.security.service.JwtService;
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
    }

    @Test
    void loginWithEmailPasswordShouldReturnResponseWithValidUserCredentials() {
        when(userDao.getByEmail(loginRequestDto.getEmail())).thenReturn(user);
        when(jwtService.generateToken(1L, "testuser", "test@example.com", "ROLE_USER"))
            .thenReturn("jwt-token-123");

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
}

