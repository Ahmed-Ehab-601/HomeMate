package com.homemate.taskerprofile;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.homemate.TaskerProfile.DTO.TaskerProfileDTO;
import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.TaskerProfile.mappers.TaskerProfileDTORowMapper;
import com.homemate.TaskerProfile.mappers.TaskerRowMapper;
import com.homemate.TaskerProfile.models.Tasker;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TaskerRowMapper.class, TaskerProfileDTORowMapper.class})
@ActiveProfiles("taskerprofile")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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

