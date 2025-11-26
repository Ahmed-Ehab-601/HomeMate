package com.homemate.authentication.rowmapper;

import com.homemate.Authentication.Entity.Tasker;
import com.homemate.Authentication.rowmapper.TaskerRowMapper;

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
class TaskerRowMapperTest {

    @Mock
    private ResultSet resultSet;

    private TaskerRowMapper taskerRowMapper;

    @BeforeEach
    void setUp() {
        taskerRowMapper = new TaskerRowMapper();
    }

    @Test
    void mapRowShouldMapAllFieldsCorrectly() throws SQLException {
        Timestamp birthDate = Timestamp.valueOf(LocalDateTime.of(1988, 6, 20, 14, 45));
        byte[] imageData = new byte[]{1, 2, 3, 4, 5};
        
        when(resultSet.getInt("taskerID")).thenReturn(1);
        when(resultSet.getString("firstName")).thenReturn("Mike");
        when(resultSet.getString("lastName")).thenReturn("Plumber");
        when(resultSet.getString("username")).thenReturn("mikeplumber");
        when(resultSet.getString("password")).thenReturn("hashedPass123");
        when(resultSet.getString("email")).thenReturn("mike@example.com");
        when(resultSet.getTimestamp("birthDate")).thenReturn(birthDate);
        when(resultSet.getString("phone")).thenReturn("01011111111");
        when(resultSet.getString("gender")).thenReturn("M");
        when(resultSet.getBytes("image")).thenReturn(imageData);
        when(resultSet.getString("availability")).thenReturn("available");
        when(resultSet.getDouble("rating")).thenReturn(4.75);
        when(resultSet.getDouble("hourrate")).thenReturn(50.0);
        when(resultSet.getString("bio")).thenReturn("Experienced plumber with 10 years of experience");
        when(resultSet.getInt("serviceID")).thenReturn(1);
        when(resultSet.getDouble("totalEarning")).thenReturn(5000.0);
        when(resultSet.getDouble("WorkedHours")).thenReturn(100.0);
        when(resultSet.getString("addressCity")).thenReturn("Cairo");

        Tasker tasker = taskerRowMapper.mapRow(resultSet, 1);

        assertNotNull(tasker);
        assertEquals(1, tasker.getTaskerID());
        assertEquals("Mike", tasker.getFirstName());
        assertEquals("Plumber", tasker.getLastName());
        assertEquals("mikeplumber", tasker.getUsername());
        assertEquals("hashedPass123", tasker.getPassword());
        assertEquals("mike@example.com", tasker.getEmail());
        assertEquals(LocalDateTime.of(1988, 6, 20, 14, 45), tasker.getBirthDate());
        assertEquals("01011111111", tasker.getPhone());
        assertEquals("M", tasker.getGender());
        assertArrayEquals(imageData, tasker.getImage());
        assertEquals("available", tasker.getAvailability());
        assertEquals(4.75, tasker.getRating());
        assertEquals(50.0, tasker.getHourRate());
        assertEquals("Experienced plumber with 10 years of experience", tasker.getBio());
        assertEquals(1, tasker.getServiceID());
        assertEquals(5000.0, tasker.getTotalEarning());
        assertEquals(100.0, tasker.getWorkedHours());
        assertEquals("Cairo", tasker.getAddressCity());
    }

    @Test
    void mapRowShouldHandleNullBirthDate() throws SQLException {
        byte[] imageData = new byte[]{10, 20, 30};
        
        when(resultSet.getInt("taskerID")).thenReturn(2);
        when(resultSet.getString("firstName")).thenReturn("Sarah");
        when(resultSet.getString("lastName")).thenReturn("Electrician");
        when(resultSet.getString("username")).thenReturn("sarahelec");
        when(resultSet.getString("password")).thenReturn("password456");
        when(resultSet.getString("email")).thenReturn("sarah@example.com");
        when(resultSet.getTimestamp("birthDate")).thenReturn(null);
        when(resultSet.getString("phone")).thenReturn("01122222222");
        when(resultSet.getString("gender")).thenReturn("F");
        when(resultSet.getBytes("image")).thenReturn(imageData);
        when(resultSet.getString("availability")).thenReturn("unavailable");
        when(resultSet.getDouble("rating")).thenReturn(4.5);
        when(resultSet.getDouble("hourrate")).thenReturn(45.0);
        when(resultSet.getString("bio")).thenReturn("Professional electrician");
        when(resultSet.getInt("serviceID")).thenReturn(2);
        when(resultSet.getDouble("totalEarning")).thenReturn(3000.0);
        when(resultSet.getDouble("WorkedHours")).thenReturn(75.0);
        when(resultSet.getString("addressCity")).thenReturn("Alexandria");

        Tasker tasker = taskerRowMapper.mapRow(resultSet, 1);

        assertNotNull(tasker);
        assertNull(tasker.getBirthDate());
        assertEquals("Sarah", tasker.getFirstName());
        assertEquals("unavailable", tasker.getAvailability());
    }

}

