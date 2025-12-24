package com.homemate.signup;

import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.UserProfile.DTO.SignupUserDTO;
import com.homemate.UserProfile.Services.UserSignupService;
import com.homemate.hashing.HashingService;
import com.homemate.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSignupServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private JwtService jwtService;

    @Mock
    private TaskerDao taskerDao;

    @Mock
    private HashingService hashingService;

    @InjectMocks
    private UserSignupService userSignupService;

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
    void signupShouldReturnJwtTokenWithValidData() {
        when(taskerDao.getByEmail(signupUserDTO.getEmail()))
            .thenThrow(new EmptyResultDataAccessException(1));
        when(userDao.signup(any())).thenReturn(1L);
        when(jwtService.generateToken(1L, "testuser", "test@example.com", "ROLE_USER"))
            .thenReturn("jwt-token-123");

        String result = userSignupService.signup(signupUserDTO);

        assertNotNull(result);
        assertEquals("jwt-token-123", result);
        verify(taskerDao, times(1)).getByEmail(signupUserDTO.getEmail());
        verify(userDao, times(1)).signup(any());
        verify(jwtService, times(1)).generateToken(1L, "testuser", "test@example.com", "ROLE_USER");
    }

    @Test
    void signupShouldReturnNullWhenUserDataIsNull() {
        String result = userSignupService.signup(null);

        assertNull(result);
        verify(taskerDao, never()).getByEmail(anyString());
        verify(userDao, never()).signup(any());
        verify(jwtService, never()).generateToken(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void signupShouldReturnNullWhenTaskerWithSameEmailExists() {
        when(taskerDao.getByEmail(signupUserDTO.getEmail())).thenReturn(null);

        String result = userSignupService.signup(signupUserDTO);

        assertNull(result);
        verify(taskerDao, times(1)).getByEmail(signupUserDTO.getEmail());
        verify(userDao, never()).signup(any());
        verify(jwtService, never()).generateToken(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void signupShouldReturnNullWhenUserDaoReturnsNegativeOne() {
        when(taskerDao.getByEmail(signupUserDTO.getEmail()))
            .thenThrow(new EmptyResultDataAccessException(1));
        when(userDao.signup(any())).thenReturn(-1L);

        String result = userSignupService.signup(signupUserDTO);

        assertNull(result);
        verify(taskerDao, times(1)).getByEmail(signupUserDTO.getEmail());
        verify(userDao, times(1)).signup(any());
        verify(jwtService, never()).generateToken(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void signupShouldCreateUserWithCorrectFields() {
        when(taskerDao.getByEmail(signupUserDTO.getEmail()))
            .thenThrow(new EmptyResultDataAccessException(1));
        when(userDao.signup(any())).thenReturn(1L);
        when(jwtService.generateToken(anyLong(), anyString(), anyString(), anyString()))
            .thenReturn("jwt-token-123");

        userSignupService.signup(signupUserDTO);

        verify(userDao, times(1)).signup(argThat(user -> {
            return user.getUsername().equals("testuser") &&
                   user.getFirstName().equals("Test") &&
                   user.getLastName().equals("User") &&
                   user.getEmail().equals("test@example.com") &&
                   user.getGender().equals('M') &&
                   user.getPhone().equals("01012345678") &&
                   !user.getIsAdmin() &&
                   !user.getIsSuspended();
        }));
    }

    // @Test
    // void signupShouldGenerateTokenWithCorrectRole() {
    //     when(taskerDao.getByEmail(signupUserDTO.getEmail()))
    //         .thenThrow(new EmptyResultDataAccessException(1));
    //     when(userDao.signup(any())).thenReturn(1L);
    //     when(jwtService.generateToken(anyLong(), anyString(), anyString(), anyString()))
    //         .thenReturn("jwt-token-123");

    //     userSignupService.signup(signupUserDTO);

    //     verify(jwtService, times(1)).generateToken(
    //         eq(1L),
    //         eq("testuser"),
    //         eq("test@example.com"),
    //         eq("ROLE_USER")
    //     );
    // }
}

