package com.homemate.signup;

import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.UserProfile.Models.User;
import com.homemate.TaskerProfile.DTO.TaskerSignupDTO;
import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.TaskerProfile.services.TaskerSignupService;
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
class TaskerSignupServiceTest {

    @Mock
    private TaskerDao taskerDao;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private TaskerSignupService taskerSignupService;

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
    void registerTaskerShouldReturnJwtTokenWithValidData() {
        when(userDao.getByEmail(taskerSignupDTO.getEmail()))
            .thenThrow(new EmptyResultDataAccessException(1));
        when(taskerDao.saveTasker(taskerSignupDTO)).thenReturn(1L);
        when(jwtService.generateToken(1L, "taskeruser", "tasker@example.com", "ROLE_TASKER"))
            .thenReturn("jwt-token-123");

        String result = taskerSignupService.registerTasker(taskerSignupDTO);

        assertNotNull(result);
        assertEquals("jwt-token-123", result);
        verify(userDao, times(1)).getByEmail(taskerSignupDTO.getEmail());
        verify(taskerDao, times(1)).saveTasker(taskerSignupDTO);
        verify(jwtService, times(1)).generateToken(1L, "taskeruser", "tasker@example.com", "ROLE_TASKER");
    }

    @Test
    void registerTaskerShouldReturnNullWhenUserWithSameEmailExists() {
        User existingUser = new User();
        existingUser.setEmail(taskerSignupDTO.getEmail());
        when(userDao.getByEmail(taskerSignupDTO.getEmail())).thenReturn(existingUser);

        String result = taskerSignupService.registerTasker(taskerSignupDTO);

        assertNull(result);
        verify(userDao, times(1)).getByEmail(taskerSignupDTO.getEmail());
        verify(taskerDao, never()).saveTasker(any());
        verify(jwtService, never()).generateToken(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void registerTaskerShouldReturnNullWhenTaskerDaoReturnsNegativeOne() {
        when(userDao.getByEmail(taskerSignupDTO.getEmail()))
            .thenThrow(new EmptyResultDataAccessException(1));
        when(taskerDao.saveTasker(taskerSignupDTO)).thenReturn(-1L);

        String result = taskerSignupService.registerTasker(taskerSignupDTO);

        assertNull(result);
        verify(userDao, times(1)).getByEmail(taskerSignupDTO.getEmail());
        verify(taskerDao, times(1)).saveTasker(taskerSignupDTO);
        verify(jwtService, never()).generateToken(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void registerTaskerShouldGenerateTokenWithCorrectRole() {
        when(userDao.getByEmail(taskerSignupDTO.getEmail()))
            .thenThrow(new EmptyResultDataAccessException(1));
        when(taskerDao.saveTasker(taskerSignupDTO)).thenReturn(1L);
        when(jwtService.generateToken(anyLong(), anyString(), anyString(), anyString()))
            .thenReturn("jwt-token-123");

        taskerSignupService.registerTasker(taskerSignupDTO);

        verify(jwtService, times(1)).generateToken(
            eq(1L),
            eq("taskeruser"),
            eq("tasker@example.com"),
            eq("ROLE_TASKER")
        );
    }

    // @Test
    // void registerTaskerShouldCheckUserEmailBeforeCreatingTasker() {
    //     when(userDaoLogin.getUserByEmail(taskerSignupDTO.getEmail()))
    //         .thenThrow(new EmptyResultDataAccessException(1));
    //     when(taskerDao.saveTasker(taskerSignupDTO)).thenReturn(1L);
    //     when(jwtService.generateToken(anyLong(), anyString(), anyString(), anyString()))
    //         .thenReturn("jwt-token-123");

    //     taskerSignupService.registerTasker(taskerSignupDTO);

    //     verify(userDaoLogin, times(1)).getUserByEmail(taskerSignupDTO.getEmail());
    //     verify(taskerDao, times(1)).saveTasker(taskerSignupDTO);
    // }

    // @Test
    // void registerTaskerShouldHandleMultipleTaskerRegistrations() {
    //     TaskerSignupDTO secondTasker = new TaskerSignupDTO();
    //     secondTasker.setUsername("taskeruser2");
    //     secondTasker.setEmail("tasker2@example.com");
    //     secondTasker.setFirstName("Tasker");
    //     secondTasker.setLastName("Two");
    //     secondTasker.setPassword("Password123!");
    //     secondTasker.setPhoneNumber("01012345679");
    //     secondTasker.setDateOfBirth(Timestamp.valueOf(LocalDateTime.of(1990, 1, 1, 0, 0)));
    //     secondTasker.setBio("This is a valid bio that is long enough");
    //     secondTasker.setHourRate(60.0);
    //     secondTasker.setCity("Alexandria");
    //     secondTasker.setProfileImage(new byte[1024]);

    //     when(userDaoLogin.getUserByEmail(anyString()))
    //         .thenThrow(new EmptyResultDataAccessException(1));
    //     when(taskerDao.saveTasker(any())).thenReturn(1L, 2L);
    //     when(jwtService.generateToken(anyLong(), anyString(), anyString(), anyString()))
    //         .thenReturn("jwt-token-123", "jwt-token-456");

    //     String result1 = taskerSignupService.registerTasker(taskerSignupDTO);
    //     String result2 = taskerSignupService.registerTasker(secondTasker);

    //     assertNotNull(result1);
    //     assertNotNull(result2);
    //     verify(userDaoLogin, times(2)).getUserByEmail(anyString());
    //     verify(taskerDao, times(2)).saveTasker(any());
    // }
}

