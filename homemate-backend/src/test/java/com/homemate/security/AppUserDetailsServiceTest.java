package com.homemate.security;

import com.homemate.security.model.AppUserDetails;
import com.homemate.security.service.AppUserDetailsService;
import com.homemate.security.dao.UserDetailsDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AppUserDetailsServiceTest {

    private UserDetailsDao userDetailsDao;
    private AppUserDetailsService service;

    @BeforeEach
    void setUp() {
        userDetailsDao = mock(UserDetailsDao.class);
        service = new AppUserDetailsService(userDetailsDao);
    }

    @Test
    void testLoadUserById_UserFoundInUsersTable() throws Exception {
        // Arrange
        AppUserDetails expected = new AppUserDetails(1L, "john", "john@mail.com", "pass", "ROLE_USER");

        when(userDetailsDao.findUserDetailsById(1L))
                .thenReturn(expected);

        // Act
        UserDetails result = service.loadUserById(1L);

        // Assert
        assertEquals(expected, result);
        verify(userDetailsDao, times(1)).findUserDetailsById(1L);
        verify(userDetailsDao, never()).findTakserDetailsById(anyLong());
    }

    @Test
    void testLoadUserById_UserNotInUsersButFoundInTaskerTable() throws Exception {
        // Arrange
        AppUserDetails expected = new AppUserDetails(2L, "mike", "mike@mail.com", "pass", "ROLE_TASKER");

        when(userDetailsDao.findUserDetailsById(2L))
                .thenThrow(new EmptyResultDataAccessException(1));

        when(userDetailsDao.findTakserDetailsById(2L))
                .thenReturn(expected);

        // Act
        UserDetails result = service.loadUserById(2L);

        // Assert
        assertEquals(expected, result);
        verify(userDetailsDao, times(1)).findUserDetailsById(2L);
        verify(userDetailsDao, times(1)).findTakserDetailsById(2L);
    }

    @Test
    void testLoadUserById_UserNotFoundAnywhere() {
        // Arrange
        when(userDetailsDao.findUserDetailsById(999L))
                .thenThrow(new EmptyResultDataAccessException(1));

        when(userDetailsDao.findTakserDetailsById(999L))
                .thenThrow(new EmptyResultDataAccessException(1));

        // Act
        Exception e = assertThrows(
                Exception.class,
                () -> service.loadUserById(999L)
        );

        // Assert
        assertEquals("User not found with ID999", e.getMessage());
        verify(userDetailsDao, times(1)).findUserDetailsById(999L);
        verify(userDetailsDao, times(1)).findTakserDetailsById(999L);
    }

    @Test
    void testLoadUserByUsername_AlwaysThrowsException() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> service.loadUserByUsername("test")
        );
    }
}

