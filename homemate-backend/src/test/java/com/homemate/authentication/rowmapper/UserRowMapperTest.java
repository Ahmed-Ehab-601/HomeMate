package com.homemate.authentication.rowmapper;

import com.homemate.Authentication.Entity.User;
import com.homemate.Authentication.rowmapper.UserRowMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRowMapperTest {

    @Mock
    private ResultSet resultSet;

    private UserRowMapper userRowMapper;

    @BeforeEach
    void setUp() {
        userRowMapper = new UserRowMapper();
    }

    @Test
    void mapRowShouldMapAllFieldsCorrectly() throws SQLException {
        Timestamp birthDate = Timestamp.valueOf(LocalDateTime.of(1990, 1, 15, 10, 30));
        
        when(resultSet.getInt("userID")).thenReturn(1);
        when(resultSet.getString("firstName")).thenReturn("John");
        when(resultSet.getString("lastName")).thenReturn("Doe");
        when(resultSet.getString("username")).thenReturn("johndoe");
        when(resultSet.getString("password")).thenReturn("hashedPassword123");
        when(resultSet.getString("email")).thenReturn("john.doe@example.com");
        when(resultSet.getTimestamp("birthDate")).thenReturn(birthDate);
        when(resultSet.getString("gender")).thenReturn("M");
        when(resultSet.getString("phone")).thenReturn("01012345678");
        when(resultSet.getBoolean("admin")).thenReturn(false);
        when(resultSet.getBoolean("suspended")).thenReturn(false);

        User user = userRowMapper.mapRow(resultSet, 1);

        assertNotNull(user);
        assertEquals(1, user.getUserID());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("johndoe", user.getUsername());
        assertEquals("hashedPassword123", user.getPassword());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals(LocalDateTime.of(1990, 1, 15, 10, 30), user.getBirthDate());
        assertEquals("M", user.getGender());
        assertEquals("01012345678", user.getPhone());
        assertFalse(user.isAdmin());
        assertFalse(user.isSuspended());
    }

}

