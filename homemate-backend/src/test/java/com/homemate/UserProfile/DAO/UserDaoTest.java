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

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserRowMapper.class, UserProfileDTORowMapper.class, UserRequestTaskerDTORowMapper.class})
@TestPropertySource(locations = "classpath:application.properties")
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
        
        // Clean up - must delete in correct order due to foreign key constraints
        // ON DELETE RESTRICT prevents deleting addresses referenced by tasks
        jdbcTemplate.update("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.update("DELETE FROM Review_Image");
        jdbcTemplate.update("DELETE FROM Reviews");
        jdbcTemplate.update("DELETE FROM Report");
        jdbcTemplate.update("DELETE FROM message_img");
        jdbcTemplate.update("DELETE FROM Message");
        jdbcTemplate.update("DELETE FROM Chat");
        jdbcTemplate.update("DELETE FROM Task");
        jdbcTemplate.update("DELETE FROM Address");
        jdbcTemplate.update("DELETE FROM Tasker");
        jdbcTemplate.update("DELETE FROM Service");
        jdbcTemplate.update("DELETE FROM User");
        // Reset auto-increment counters
        jdbcTemplate.update("ALTER TABLE User AUTO_INCREMENT = 1");
        jdbcTemplate.update("ALTER TABLE Address AUTO_INCREMENT = 1");
        jdbcTemplate.update("SET FOREIGN_KEY_CHECKS = 1");
        
        // Insert test users similar to data.sql
        insertTestUsers();
    }

    private void insertTestUsers() {
        String insertUserSQL = "INSERT INTO User (firstName, lastName, username, password, email, birthDate, gender, phone, admin, suspended) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        // Insert 12 test users with fake data
        jdbcTemplate.update(insertUserSQL, "John", "Smith", "jsmith", "$2a$10$test123456789", "john.smith@email.com", 
            Timestamp.valueOf(LocalDateTime.of(1990, 5, 15, 0, 0)), "M", "+1-555-0101", false, false);
        
        jdbcTemplate.update(insertUserSQL, "Sarah", "Johnson", "sjohnson", "$2a$10$test123456790", "sarah.j@email.com", 
            Timestamp.valueOf(LocalDateTime.of(1988, 8, 22, 0, 0)), "F", "+1-555-0102", false, false);
        
        jdbcTemplate.update(insertUserSQL, "Michael", "Brown", "mbrown", "$2a$10$test123456791", "michael.b@email.com", 
            Timestamp.valueOf(LocalDateTime.of(1992, 3, 10, 0, 0)), "M", "+1-555-0103", false, false);
        
        jdbcTemplate.update(insertUserSQL, "Emily", "Davis", "edavis", "$2a$10$test123456792", "emily.davis@email.com", 
            Timestamp.valueOf(LocalDateTime.of(1995, 11, 30, 0, 0)), "F", "+1-555-0104", false, false);
        
        jdbcTemplate.update(insertUserSQL, "David", "Wilson", "dwilson", "$2a$10$test123456793", "david.w@email.com", 
            Timestamp.valueOf(LocalDateTime.of(1987, 7, 18, 0, 0)), "M", "+1-555-0105", false, false);
        
        jdbcTemplate.update(insertUserSQL, "Jessica", "Martinez", "jmartinez", "$2a$10$test123456794", "jessica.m@email.com", 
            Timestamp.valueOf(LocalDateTime.of(1993, 1, 25, 0, 0)), "F", "+1-555-0106", false, false);
        
        jdbcTemplate.update(insertUserSQL, "Robert", "Garcia", "rgarcia", "$2a$10$test123456795", "robert.g@email.com", 
            Timestamp.valueOf(LocalDateTime.of(1991, 9, 5, 0, 0)), "M", "+1-555-0107", false, false);
        
        jdbcTemplate.update(insertUserSQL, "Amanda", "Rodriguez", "arodriguez", "$2a$10$test123456796", "amanda.r@email.com", 
            Timestamp.valueOf(LocalDateTime.of(1989, 12, 12, 0, 0)), "F", "+1-555-0108", false, false);
        
        jdbcTemplate.update(insertUserSQL, "Admin", "User", "admin", "$2a$10$test123456797", "admin@homemate.com", 
            Timestamp.valueOf(LocalDateTime.of(1985, 6, 20, 0, 0)), "M", "+1-555-0001", true, false);
        
        jdbcTemplate.update(insertUserSQL, "Chris", "Taylor", "ctaylor", "$2a$10$test123456798", "chris.t@email.com", 
            Timestamp.valueOf(LocalDateTime.of(1994, 4, 8, 0, 0)), "M", "+1-555-0109", false, false);
        
        jdbcTemplate.update(insertUserSQL, "Alex", "Moore", "amoore", "$2a$10$test123456799", "alex.moore@email.com", 
            Timestamp.valueOf(LocalDateTime.of(1996, 2, 14, 0, 0)), "M", "+1-555-0110", false, false);
        
        jdbcTemplate.update(insertUserSQL, "Sophia", "Lee", "slee", "$2a$10$test123456800", "sophia.lee@email.com", 
            Timestamp.valueOf(LocalDateTime.of(1997, 6, 3, 0, 0)), "F", "+1-555-0111", false, false);
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
            "SELECT COUNT(*) FROM User WHERE userID = ?", 
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

