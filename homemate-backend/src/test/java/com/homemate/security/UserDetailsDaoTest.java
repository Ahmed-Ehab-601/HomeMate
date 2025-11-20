package com.homemate.security;

import com.homemate.security.model.AppUserDetails;
import com.homemate.security.rowmapper.AppUserDetailsRowMapper;
import com.homemate.security.dao.UserDetailsDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserDetailsDaoTest {

    private JdbcTemplate jdbcTemplate;
    private UserDetailsDao userDetailsDao;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        userDetailsDao = new UserDetailsDao(jdbcTemplate);
    }

    @Test
    void testFindUserDetailsById() {
        // Arrange
        AppUserDetails expectedUser = new AppUserDetails(1L, "john", "john@email.com", "pass", "ROLE_USER");

        when(jdbcTemplate.queryForObject(
                anyString(),
                any(AppUserDetailsRowMapper.class),
                anyLong()
        )).thenReturn(expectedUser);

        // Act
        AppUserDetails result = userDetailsDao.findUserDetailsById(1L);

        // Assert
        assertEquals(expectedUser, result);

        // Verify SQL and params
        verify(jdbcTemplate, times(1))
                .queryForObject(eq("SELECT id, username, email, password, admin FROM users WHERE id = ?"),
                        any(AppUserDetailsRowMapper.class),
                        eq(1L));
    }

    @Test
    void testFindTaskerDetailsById() {
        AppUserDetails expectedUser = new AppUserDetails(2L, "mike", "mike@mail.com", "pass", "ROLE_TASKER");

        when(jdbcTemplate.queryForObject(
                anyString(),
                any(AppUserDetailsRowMapper.class),
                anyLong()
        )).thenReturn(expectedUser);

        AppUserDetails result = userDetailsDao.findTakserDetailsById(2L);

        assertEquals(expectedUser, result);

        verify(jdbcTemplate).queryForObject(
                eq("SELECT id, username, email, password FROM tasker WHERE id = ?"),
                any(AppUserDetailsRowMapper.class),
                eq(2L)
        );
    }
}
