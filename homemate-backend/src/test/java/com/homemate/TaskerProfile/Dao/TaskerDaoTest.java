package com.homemate.TaskerProfile.Dao;

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

import com.homemate.TaskerProfile.DTO.TaskerProfileDTO;
import com.homemate.TaskerProfile.mappers.TaskerProfileDTORowMapper;
import com.homemate.TaskerProfile.mappers.TaskerRowMapper;
import com.homemate.TaskerProfile.models.Tasker;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TaskerRowMapper.class, TaskerProfileDTORowMapper.class})
@TestPropertySource(locations = "classpath:application.properties")
class TaskerDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TaskerRowMapper taskerRowMapper;

    @Autowired
    private TaskerProfileDTORowMapper taskerProfileDTORowMapper;

    private TaskerDao taskerDao;

    @BeforeEach
    void setUp() {
        taskerDao = new TaskerDao(jdbcTemplate, taskerProfileDTORowMapper, taskerRowMapper);

        // Clean up before each test
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
        jdbcTemplate.update("DELETE FROM Users");
        jdbcTemplate.update("SET FOREIGN_KEY_CHECKS = 1");

        // Reset auto-increment counters
        jdbcTemplate.update("ALTER TABLE Service AUTO_INCREMENT = 1");
        jdbcTemplate.update("ALTER TABLE Tasker AUTO_INCREMENT = 1");

        // Insert test service
        jdbcTemplate.update(
            "INSERT INTO Service (name, description) VALUES (?, ?)",
            "Plumbing", "Professional plumbing services"
        );
        jdbcTemplate.update(
            "INSERT INTO Service (name, description) VALUES (?, ?)",
            "Electrical", "Professional electrical services"
        );

        // Insert test taskers
        insertTestTaskers();
    }

    private void insertTestTaskers() {
        String sql = "INSERT INTO Tasker (firstName, lastName, username, password, email, birthDate, phone, gender, availability, rating, hourrate, bio, serviceID, totalEarning, WorkedHours, addressCity) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Insert 12 test taskers with various edge cases
        jdbcTemplate.update(sql,
            "John", "Doe", "johndoe", "password123",
            "john.doe@email.com",
            Timestamp.valueOf(LocalDateTime.of(1990, 5, 15, 0, 0)),
            "+1-555-0101", "M", "available", 4.5, 50.0,
            "Experienced tasker", 1L, 1000.0, 20.0, "New York"
        );

        jdbcTemplate.update(sql,
            "Jane", "Smith", "janesmith", "password456",
            "jane.smith@email.com",
            Timestamp.valueOf(LocalDateTime.of(1988, 8, 22, 0, 0)),
            "+1-555-0102", "F", "unavailable", 4.8, 60.0,
            "Professional service", 1L, 2000.0, 35.0, "Los Angeles"
        );

        // Tasker with max length username (50 chars)
        jdbcTemplate.update(sql,
            "Max", "User", "a".repeat(50), "password789",
            "max.user@email.com",
            Timestamp.valueOf(LocalDateTime.of(1992, 3, 10, 0, 0)),
            "+1-555-0103", "M", "available", 4.0, 45.0,
            "Test bio", 1L, 500.0, 10.0, "Chicago"
        );

        // Tasker with max length email (50 chars total, so 40 chars + "@email.com" = 50)
        jdbcTemplate.update(sql,
            "Email", "Test", "emailtest", "password123",
            "a".repeat(40) + "@email.com",
            Timestamp.valueOf(LocalDateTime.of(1995, 11, 30, 0, 0)),
            "+1-555-0104", "F", "available", 4.2, 55.0,
            "Email test", 2L, 750.0, 15.0, "Houston"
        );

        // Tasker with max length phone (50 chars)
        jdbcTemplate.update(sql,
            "Phone", "Test", "phonetest", "password999",
            "phone.test@email.com",
            Timestamp.valueOf(LocalDateTime.of(1987, 7, 18, 0, 0)),
            "a".repeat(50), "M", "available", 4.7, 65.0,
            "Phone test", 1L, 1500.0, 25.0, "Phoenix"
        );

        // Tasker with max length bio (500 chars)
        jdbcTemplate.update(sql,
            "Bio", "Test", "biotest", "password111",
            "bio.test@email.com",
            Timestamp.valueOf(LocalDateTime.of(1993, 1, 25, 0, 0)),
            "+1-555-0105", "F", "unavailable", 4.9, 70.0,
            "a".repeat(500), 2L, 3000.0, 50.0, "Philadelphia"
        );

        // Tasker with null bio
        jdbcTemplate.update(sql,
            "Null", "Bio", "nullbio", "password222",
            "null.bio@email.com",
            Timestamp.valueOf(LocalDateTime.of(1991, 9, 5, 0, 0)),
            "+1-555-0106", "M", "available", 3.5, 40.0,
            null, 1L, 200.0, 5.0, "San Antonio"
        );

        // Tasker with null phone
        jdbcTemplate.update(sql,
            "Null", "Phone", "nullphone", "password333",
            "null.phone@email.com",
            Timestamp.valueOf(LocalDateTime.of(1989, 12, 12, 0, 0)),
            null, "F", "available", 4.3, 55.0,
            "No phone", 2L, 800.0, 18.0, "San Diego"
        );

        // Tasker with null gender
        jdbcTemplate.update(sql,
            "Null", "Gender", "nullgender", "password444",
            "null.gender@email.com",
            Timestamp.valueOf(LocalDateTime.of(1994, 4, 8, 0, 0)),
            "+1-555-0107", null, "available", 4.6, 58.0,
            "No gender", 1L, 1200.0, 22.0, "Dallas"
        );

        // Tasker with minimum values
        jdbcTemplate.update(sql,
            "A", "B", "ab", "p",
            "a@b.c",
            Timestamp.valueOf(LocalDateTime.of(2000, 1, 1, 0, 0)),
            "1", "M", "available", 0.0, 0.0,
            "Min", 1L, 0.0, 0.0, "A"
        );

        // Tasker with high rating
        jdbcTemplate.update(sql,
            "High", "Rating", "highrating", "password555",
            "high.rating@email.com",
            Timestamp.valueOf(LocalDateTime.of(1985, 6, 20, 0, 0)),
            "+1-555-0108", "M", "available", 5.0, 100.0,
            "Perfect rating", 2L, 10000.0, 100.0, "San Jose"
        );

        // Tasker with no service (will use serviceID 1)
        jdbcTemplate.update(sql,
            "No", "Service", "noservice", "password666",
            "no.service@email.com",
            Timestamp.valueOf(LocalDateTime.of(1996, 2, 14, 0, 0)),
            "+1-555-0109", "F", "unavailable", 3.0, 30.0,
            "No service", 1L, 100.0, 3.0, "Austin"
        );
    }

    @Test
    void testGetByID_Success() {
        Tasker tasker = taskerDao.getByID(1L);

        assertNotNull(tasker);
        assertEquals(1L, tasker.getTaskerID());
        assertEquals("johndoe", tasker.getUsername());
        assertEquals("John", tasker.getFirstName());
        assertEquals("Doe", tasker.getLastName());
        assertEquals("john.doe@email.com", tasker.getEmail());
        assertEquals("+1-555-0101", tasker.getPhone());
        assertEquals('M', tasker.getGender().charValue());
        assertEquals(4.5, tasker.getRating());
        assertEquals(50.0, tasker.getHourrate());
    }

    @Test
    void testGetByEmail_WithServiceName() {
        Tasker tasker = taskerDao.getByEmail("jane.smith@email.com");

        assertNotNull(tasker);
        assertEquals(2L, tasker.getTaskerID());
        assertEquals("janesmith", tasker.getUsername());
        assertEquals("Jane", tasker.getFirstName());
        assertEquals("Smith", tasker.getLastName());
        assertNotNull(tasker.getServiceName());
        assertEquals("Plumbing", tasker.getServiceName());
    }

    @Test
    void testGetProfile_Success() {
        TaskerProfileDTO profile = taskerDao.getProfile(1L);

        assertNotNull(profile);
        assertEquals(1L, profile.getTaskerID());
        assertEquals("johndoe", profile.getUsername());
        assertEquals("John", profile.getFirstName());
        assertEquals("Doe", profile.getLastName());
        assertEquals("john.doe@email.com", profile.getEmail());
    }

    @Test
    void testUpdate_AllFields() {
        Tasker tasker = taskerDao.getByID(1L);
        tasker.setUsername("updated_username");
        tasker.setEmail("updated@email.com");
        tasker.setPhone("+1-555-9999");
        tasker.setFirstName("Updated");
        tasker.setLastName("Name");
        tasker.setHourrate(75.0);
        tasker.setBio("Updated bio");

        taskerDao.update(tasker);

        Tasker updatedTasker = taskerDao.getByID(1L);
        assertEquals("updated_username", updatedTasker.getUsername());
        assertEquals("updated@email.com", updatedTasker.getEmail());
        assertEquals("+1-555-9999", updatedTasker.getPhone());
        assertEquals("Updated", updatedTasker.getFirstName());
        assertEquals("Name", updatedTasker.getLastName());
        assertEquals(75.0, updatedTasker.getHourrate());
        assertEquals("Updated bio", updatedTasker.getBio());
    }

    @Test
    void testUpdate_WithNullValues() {
        Tasker tasker = taskerDao.getByID(1L);
        tasker.setPhone(null);
        tasker.setGender(null);
        tasker.setBio(null);

        taskerDao.update(tasker);

        Tasker updatedTasker = taskerDao.getByID(1L);
        assertNull(updatedTasker.getPhone());
        assertNull(updatedTasker.getGender());
        assertNull(updatedTasker.getBio());
    }

    @Test
    void testDelete_Success() {
        taskerDao.delete(1L);

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM Tasker WHERE taskerID = ?", Integer.class, 1L);
        assertEquals(0, count);
    }

    @Test
    void testGetByID_MaxLengthFields() {
        // Test max length username
        Tasker tasker1 = taskerDao.getByID(3L);
        assertNotNull(tasker1);
        assertEquals(50, tasker1.getUsername().length());
        assertEquals("a".repeat(50), tasker1.getUsername());

        // Test max length email
        Tasker tasker2 = taskerDao.getByEmail("a".repeat(40) + "@email.com");
        assertNotNull(tasker2);
        assertEquals(4L, tasker2.getTaskerID());
        assertEquals("a".repeat(40) + "@email.com", tasker2.getEmail());

        // Test max length phone
        Tasker tasker3 = taskerDao.getByID(5L);
        assertNotNull(tasker3);
        assertEquals(50, tasker3.getPhone().length());
        assertEquals("a".repeat(50), tasker3.getPhone());

        // Test max length bio
        Tasker tasker4 = taskerDao.getByID(6L);
        assertNotNull(tasker4);
        assertEquals(500, tasker4.getBio().length());
        assertEquals("a".repeat(500), tasker4.getBio());
    }

    @Test
    void testGetByID_NullValues() {
        Tasker taskerNullBio = taskerDao.getByID(7L);
        assertNotNull(taskerNullBio);
        assertNull(taskerNullBio.getBio());

        Tasker taskerNullPhone = taskerDao.getByID(8L);
        assertNotNull(taskerNullPhone);
        assertNull(taskerNullPhone.getPhone());

        Tasker taskerNullGender = taskerDao.getByID(9L);
        assertNotNull(taskerNullGender);
        assertNull(taskerNullGender.getGender());
    }

    @Test
    void testGetByID_MinimumValues() {
        Tasker tasker = taskerDao.getByID(10L);

        assertNotNull(tasker);
        assertEquals("A", tasker.getFirstName());
        assertEquals("B", tasker.getLastName());
        assertEquals("ab", tasker.getUsername());
        assertEquals("a@b.c", tasker.getEmail());
        assertEquals("1", tasker.getPhone());
        assertEquals(0.0, tasker.getRating());
        assertEquals(0.0, tasker.getHourrate());
    }

    @Test
    void testGetByID_HighRating() {
        Tasker tasker = taskerDao.getByID(11L);

        assertNotNull(tasker);
        assertEquals(5.0, tasker.getRating());
        assertEquals(100.0, tasker.getHourrate());
        assertEquals(10000.0, tasker.getTotalEarning());
        assertEquals(100.0, tasker.getWorkedHours());
    }

    @Test
    void testUpdate_WithMinValues() {
        Tasker tasker = taskerDao.getByID(1L);
        tasker.setFirstName("A");
        tasker.setLastName("B");
        tasker.setUsername("ab");
        tasker.setEmail("a@b.c");
        tasker.setPhone("1");
        tasker.setHourrate(0.0);
        tasker.setRating(0.0);

        taskerDao.update(tasker);

        Tasker updatedTasker = taskerDao.getByID(1L);
        assertEquals("A", updatedTasker.getFirstName());
        assertEquals("B", updatedTasker.getLastName());
        assertEquals("ab", updatedTasker.getUsername());
        assertEquals("a@b.c", updatedTasker.getEmail());
        assertEquals("1", updatedTasker.getPhone());
        assertEquals(0.0, updatedTasker.getHourrate());
        assertEquals(0.0, updatedTasker.getRating());
    }

    @Test
    void testGetProfile_EdgeCases() {
        // Test profile with max length bio
        TaskerProfileDTO profile1 = taskerDao.getProfile(6L);
        assertNotNull(profile1);
        assertEquals(500, profile1.getBio().length());

        // Test profile with null bio
        TaskerProfileDTO profile2 = taskerDao.getProfile(7L);
        assertNotNull(profile2);
        assertNull(profile2.getBio());

        // Test profile with high values
        TaskerProfileDTO profile3 = taskerDao.getProfile(11L);
        assertNotNull(profile3);
        assertEquals(5.0, profile3.getRating());
        assertEquals(100.0, profile3.getHourrate());
    }
}

