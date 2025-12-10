package com.homemate.UserProfile.DAO;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import com.homemate.UserProfile.DTO.UserProfileDTO;
import com.homemate.UserProfile.Mappers.UserProfileDTORowMapper;
import com.homemate.UserProfile.Mappers.UserRequestTaskerDTORowMapper;
import com.homemate.UserProfile.Mappers.UserRowMapper;
import com.homemate.UserProfile.Models.User;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserRowMapper.class, UserProfileDTORowMapper.class, UserRequestTaskerDTORowMapper.class})
@ActiveProfiles("userprofile")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRowMapper userRowMapper;

    @Autowired
    private UserProfileDTORowMapper userProfileRowMapper;

    @Autowired
    private UserRequestTaskerDTORowMapper userRequestTaskerRowMapper;

    private UserDao userDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDao(jdbcTemplate, userRowMapper, userProfileRowMapper, userRequestTaskerRowMapper);
    }

    @Test
    void testGetByID() {
        User user = userDao.getByID(1L);
        
        assertNotNull(user);
        assertEquals(1L, user.getUserID());
        assertEquals("jsmith", user.getUsername());
        assertEquals("John", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("john.smith@email.com", user.getEmail());
        assertEquals('M', user.getGender().charValue());
        assertEquals("+1-555-0101", user.getPhone());
        assertFalse(user.getIsAdmin());
        assertFalse(user.getIsSuspended());
    }

    @Test
    void testGetByEmail() {
        User user = userDao.getByEmail("sarah.j@email.com");
        
        assertNotNull(user);
        assertEquals(2L, user.getUserID());
        assertEquals("sjohnson", user.getUsername());
        assertEquals("Sarah", user.getFirstName());
        assertEquals("Johnson", user.getLastName());
        assertEquals('F', user.getGender().charValue());
    }

    @Test
    void testGetProfile() {
        UserProfileDTO profile = userDao.getProfile(1L);
        
        assertNotNull(profile);
        assertEquals(1L, profile.getUserId());
        assertEquals("jsmith", profile.getUsername());
        assertEquals("John", profile.getFirstName());
        assertEquals("Smith", profile.getLastName());
        assertEquals("john.smith@email.com", profile.getEmail());
    }

    @Test
    void testGetUserProfile() {
        User user = userDao.getUserProfile(1L);
        
        assertNotNull(user);
        assertEquals(1L, user.getUserID());
        assertEquals("jsmith", user.getUsername());
        assertEquals("John", user.getFirstName());
    }

    @Test
    void testUpdate() {
        User user = userDao.getByID(1L);
        user.setUsername("updated_username");
        user.setEmail("updated@email.com");
        user.setPhone("+1-555-9999");
        
        userDao.update(user);
        
        User updatedUser = userDao.getByID(1L);
        assertEquals("updated_username", updatedUser.getUsername());
        assertEquals("updated@email.com", updatedUser.getEmail());
        assertEquals("+1-555-9999", updatedUser.getPhone());
    }

    @Test
    void testDelete() {
        userDao.delete(1L);
        
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM Users WHERE userID = ?", 
            Integer.class, 1L);
        assertEquals(0, count);
    }

    @Test
    void testSignup() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setEmail("newuser@email.com");
        newUser.setPassword("$2a$10$newPasswordHash");
        newUser.setBirthDate(Timestamp.valueOf(LocalDateTime.of(1995, 1, 1, 0, 0)));
        newUser.setGender('M');
        newUser.setPhone("+1-555-9999");
        newUser.setIsAdmin(false);
        newUser.setIsSuspended(false);
        
        userDao.signup(newUser);
        
        User savedUser = userDao.getByEmail("newuser@email.com");
        assertNotNull(savedUser);
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("New", savedUser.getFirstName());
        assertEquals("User", savedUser.getLastName());
    }

    @Test
    void testGetByIDForAdmin() {
        User admin = userDao.getByID(9L);
        
        assertNotNull(admin);
        assertEquals(9L, admin.getUserID());
        assertEquals("admin", admin.getUsername());
        assertTrue(admin.getIsAdmin());
    }

    @Test
    void testGetProfileForAllUsers() {
        // Test getting profiles for multiple users
        for (long i = 1; i <= 12; i++) {
            UserProfileDTO profile = userDao.getProfile(i);
            assertNotNull(profile);
            assertNotNull(profile.getUsername());
            assertNotNull(profile.getEmail());
        }
    }
}

