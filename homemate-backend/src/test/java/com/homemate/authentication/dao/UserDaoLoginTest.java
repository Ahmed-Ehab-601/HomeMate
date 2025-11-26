package com.homemate.authentication.dao;

import com.homemate.Authentication.Entity.User;
import com.homemate.Authentication.dao.UserDaoLogin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-auth.properties")
class UserDaoLoginTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UserDaoLogin userDaoLogin;

    @BeforeEach
    void setUp() {
        userDaoLogin = new UserDaoLogin(jdbcTemplate);
        
        jdbcTemplate.update("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.update("DELETE FROM users");
        
        insertTestUsers();
    }

    private void insertTestUsers() {
        String insertSQL = "INSERT INTO users (firstName, lastName, username, password, email, birthDate, gender, phone, admin, suspended) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(insertSQL, 
            "John", "Doe", "johndoe", "password123", "john.doe@example.com",
            Timestamp.valueOf(LocalDateTime.of(1990, 1, 15, 0, 0)), "M", "01012345678", false, false);
        
        jdbcTemplate.update(insertSQL,
            "Jane", "Smith", "janesmith", "password456", "jane.smith@example.com",
            Timestamp.valueOf(LocalDateTime.of(1992, 5, 20, 0, 0)), "F", "01112345678", false, false);
        
        jdbcTemplate.update(insertSQL,
            "Admin", "User", "admin", "adminpass", "admin@example.com",
            Timestamp.valueOf(LocalDateTime.of(1985, 3, 10, 0, 0)), "M", "01212345678", true, false);
        
        jdbcTemplate.update(insertSQL,
            "Suspended", "User", "suspended", "suspass", "suspended@example.com",
            Timestamp.valueOf(LocalDateTime.of(1995, 7, 25, 0, 0)), "F", "01512345678", false, true);
        
        jdbcTemplate.update(insertSQL,
            "Bob", "Builder", "bobbuilder", "bobpass", "bob@example.com",
            Timestamp.valueOf(LocalDateTime.of(1988, 11, 30, 0, 0)), "M", "01098765432", false, false);
    }

    @Test
    void getUserByEmailShouldReturnUserWithValidEmail() {
        User user = userDaoLogin.getUserByEmail("john.doe@example.com");

        assertNotNull(user);
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("johndoe", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("M", user.getGender());
        assertEquals("01012345678", user.getPhone());
        assertFalse(user.isAdmin());
        assertFalse(user.isSuspended());
    }

    @Test
    void getUserByEmailShouldReturnUserWithDifferentEmail() {
        User user = userDaoLogin.getUserByEmail("jane.smith@example.com");

        assertNotNull(user);
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("janesmith", user.getUsername());
        assertEquals("jane.smith@example.com", user.getEmail());
        assertEquals("F", user.getGender());
    }

    @Test
    void getUserByEmailShouldReturnAdminUser() {
        User user = userDaoLogin.getUserByEmail("admin@example.com");

        assertNotNull(user);
        assertEquals("Admin", user.getFirstName());
        assertEquals("admin", user.getUsername());
        assertTrue(user.isAdmin());
        assertFalse(user.isSuspended());
    }

    @Test
    void getUserByEmailShouldReturnSuspendedUser() {
        User user = userDaoLogin.getUserByEmail("suspended@example.com");

        assertNotNull(user);
        assertEquals("Suspended", user.getFirstName());
        assertEquals("suspended", user.getUsername());
        assertFalse(user.isAdmin());
        assertTrue(user.isSuspended());
    }

    @Test
    void getUserByEmailShouldReturnUserWithNullBirthDate() {
        // Insert a user with null birthDate
        jdbcTemplate.update(
            "INSERT INTO users (firstName, lastName, username, password, email, birthDate, gender, phone, admin, suspended) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            "Null", "Date", "nulldate", "pass", "nulldate@example.com",
            null, "M", "01011111111", false, false
        );

        User user = userDaoLogin.getUserByEmail("nulldate@example.com");

        assertNotNull(user);
        assertEquals("Null", user.getFirstName());
        assertNull(user.getBirthDate());
    }

    @Test
    void getUserByEmailShouldThrowExceptionForNonExistentEmail() {
        assertThrows(org.springframework.dao.EmptyResultDataAccessException.class, () -> {
            userDaoLogin.getUserByEmail("nonexistent@example.com");
        });
    }

    @Test
    void getUserByEmailShouldBeCaseSensitiveForEmail() {
        assertThrows(org.springframework.dao.EmptyResultDataAccessException.class, () -> {
            userDaoLogin.getUserByEmail("BOB@EXAMPLE.COM");
        });
    }

    @Test
    void getUserByEmailShouldReturnCorrectBirthDate() {
        User user = userDaoLogin.getUserByEmail("john.doe@example.com");

        assertNotNull(user);
        assertNotNull(user.getBirthDate());
        assertEquals(LocalDateTime.of(1990, 1, 15, 0, 0), user.getBirthDate());
    }

    @Test
    void getUserByEmailShouldReturnAllUserFields() {
        User user = userDaoLogin.getUserByEmail("bob@example.com");

        assertNotNull(user);
        assertNotNull(user.getUserID());
        assertNotNull(user.getFirstName());
        assertNotNull(user.getLastName());
        assertNotNull(user.getUsername());
        assertNotNull(user.getPassword());
        assertNotNull(user.getEmail());
        assertNotNull(user.getGender());
        assertNotNull(user.getPhone());
        assertNotNull(user.getBirthDate());
    }
}

