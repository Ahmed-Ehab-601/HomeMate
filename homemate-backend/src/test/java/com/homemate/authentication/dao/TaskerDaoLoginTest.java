package com.homemate.authentication.dao;

import com.homemate.Authentication.Entity.Tasker;
import com.homemate.Authentication.dao.TaskerDaoLogin;

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
class TaskerDaoLoginTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private TaskerDaoLogin taskerDaoLogin;

    @BeforeEach
    void setUp() {
        taskerDaoLogin = new TaskerDaoLogin(jdbcTemplate);
        
        jdbcTemplate.update("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.update("DELETE FROM tasker");
        jdbcTemplate.update("DELETE FROM service");
        
        insertTestService();
        insertTestTaskers();
    }

    private void insertTestService() {
        jdbcTemplate.update(
            "INSERT INTO service (name, description) VALUES (?, ?)",
            "Plumbing", "Professional plumbing services"
        );
    }

    private void insertTestTaskers() {
        String insertSQL = "INSERT INTO tasker (firstName, lastName, username, password, email, birthDate, phone, gender, image, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        byte[] image1 = new byte[]{1, 2, 3, 4, 5};
        jdbcTemplate.update(insertSQL,
            "Mike", "Plumber", "mikeplumber", "password123", "mike@example.com",
            Timestamp.valueOf(LocalDateTime.of(1988, 6, 20, 0, 0)), "01011111111", "M",
            image1, "available", 4.75, 50.0, "Experienced plumber with 10 years", 1, 5000.0, 100.0, "Cairo");
        
        byte[] image2 = new byte[]{10, 20, 30};
        jdbcTemplate.update(insertSQL,
            "Sarah", "Electrician", "sarahelec", "password456", "sarah@example.com",
            Timestamp.valueOf(LocalDateTime.of(1990, 3, 15, 0, 0)), "01122222222", "F",
            image2, "unavailable", 4.5, 45.0, "Professional electrician", 1, 3000.0, 75.0, "Alexandria");
        
        byte[] image3 = new byte[]{100, 101, 102};
        jdbcTemplate.update(insertSQL,
            "Tom", "Carpenter", "tomcarp", "password789", "tom@example.com",
            Timestamp.valueOf(LocalDateTime.of(1985, 9, 10, 0, 0)), "01233333333", "M",
            image3, "available", 4.8, 60.0, "Expert carpenter", 1, 8000.0, 150.0, "Giza");
        
        jdbcTemplate.update(insertSQL,
            "Lisa", "Cleaner", "lisaclean", "password000", "lisa@example.com",
            Timestamp.valueOf(LocalDateTime.of(1992, 11, 12, 0, 0)), "01544444444", "F",
            null, "available", 4.9, 55.0, "Professional cleaning services", 1, 6000.0, 120.0, null);
    }

    @Test
    void getTaskerByEmailShouldReturnTaskerWithValidEmail() {
        Tasker tasker = taskerDaoLogin.getTaskerByEmail("mike@example.com");

        assertNotNull(tasker);
        assertEquals("Mike", tasker.getFirstName());
        assertEquals("Plumber", tasker.getLastName());
        assertEquals("mikeplumber", tasker.getUsername());
        assertEquals("password123", tasker.getPassword());
        assertEquals("mike@example.com", tasker.getEmail());
        assertEquals("M", tasker.getGender());
        assertEquals("01011111111", tasker.getPhone());
        assertEquals("available", tasker.getAvailability());
        assertEquals(4.75, tasker.getRating());
        assertEquals(50.0, tasker.getHourRate());
        assertEquals("Experienced plumber with 10 years", tasker.getBio());
        assertEquals(1, tasker.getServiceID());
        assertEquals(5000.0, tasker.getTotalEarning());
        assertEquals(100.0, tasker.getWorkedHours());
        assertEquals("Cairo", tasker.getAddressCity());
        assertNotNull(tasker.getImage());
        assertEquals(5, tasker.getImage().length);
    }

    @Test
    void getTaskerByEmailShouldReturnTaskerWithDifferentEmail() {
        Tasker tasker = taskerDaoLogin.getTaskerByEmail("sarah@example.com");

        assertNotNull(tasker);
        assertEquals("Sarah", tasker.getFirstName());
        assertEquals("Electrician", tasker.getLastName());
        assertEquals("sarahelec", tasker.getUsername());
        assertEquals("unavailable", tasker.getAvailability());
        assertEquals(4.5, tasker.getRating());
        assertEquals("Alexandria", tasker.getAddressCity());
    }

    @Test
    void getTaskerByEmailShouldReturnTaskerWithNullImage() {
        Tasker tasker = taskerDaoLogin.getTaskerByEmail("lisa@example.com");

        assertNotNull(tasker);
        assertEquals("Lisa", tasker.getFirstName());
        assertNull(tasker.getImage());
        assertEquals("available", tasker.getAvailability());
    }

    @Test
    void getTaskerByEmailShouldReturnTaskerWithNullAddressCity() {
        Tasker tasker = taskerDaoLogin.getTaskerByEmail("lisa@example.com");

        assertNotNull(tasker);
        assertEquals("Lisa", tasker.getFirstName());
        assertNull(tasker.getAddressCity());
    }

    @Test
    void getTaskerByEmailShouldReturnCorrectBirthDate() {
        Tasker tasker = taskerDaoLogin.getTaskerByEmail("mike@example.com");

        assertNotNull(tasker);
        assertNotNull(tasker.getBirthDate());
        assertEquals(LocalDateTime.of(1988, 6, 20, 0, 0), tasker.getBirthDate());
    }

    @Test
    void getTaskerByEmailShouldReturnTaskerWithHighRating() {
        Tasker tasker = taskerDaoLogin.getTaskerByEmail("tom@example.com");

        assertNotNull(tasker);
        assertEquals("Tom", tasker.getFirstName());
        assertEquals(4.8, tasker.getRating());
        assertEquals(60.0, tasker.getHourRate());
        assertEquals(8000.0, tasker.getTotalEarning());
        assertEquals(150.0, tasker.getWorkedHours());
    }

    @Test
    void getTaskerByEmailShouldReturnAllTaskerFields() {
        Tasker tasker = taskerDaoLogin.getTaskerByEmail("mike@example.com");

        assertNotNull(tasker);
        assertNotNull(tasker.getTaskerID());
        assertNotNull(tasker.getFirstName());
        assertNotNull(tasker.getLastName());
        assertNotNull(tasker.getUsername());
        assertNotNull(tasker.getPassword());
        assertNotNull(tasker.getEmail());
        assertNotNull(tasker.getGender());
        assertNotNull(tasker.getPhone());
        assertNotNull(tasker.getBirthDate());
        assertNotNull(tasker.getAvailability());
        assertNotNull(tasker.getRating());
        assertNotNull(tasker.getHourRate());
        assertNotNull(tasker.getBio());
        assertNotNull(tasker.getServiceID());
        assertNotNull(tasker.getTotalEarning());
        assertNotNull(tasker.getWorkedHours());
    }

    @Test
    void getTaskerByEmailShouldThrowExceptionForNonExistentEmail() {
        assertThrows(org.springframework.dao.EmptyResultDataAccessException.class, () -> {
            taskerDaoLogin.getTaskerByEmail("nonexistent@example.com");
        });
    }

    @Test
    void getTaskerByEmailShouldReturnTaskerWithZeroRating() {
        // Insert a tasker with zero rating
        jdbcTemplate.update(
            "INSERT INTO tasker (firstName, lastName, username, password, email, birthDate, phone, gender, image, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            "New", "Tasker", "newtasker", "newpass", "new@example.com",
            Timestamp.valueOf(LocalDateTime.of(1995, 1, 1, 0, 0)), "01099999999", "M",
            new byte[]{1}, "available", 0.0, 30.0, "New tasker", 1, 0.0, 0.0, "Cairo"
        );

        Tasker tasker = taskerDaoLogin.getTaskerByEmail("new@example.com");

        assertNotNull(tasker);
        assertEquals("New", tasker.getFirstName());
        assertEquals(0.0, tasker.getRating());
        assertEquals(0.0, tasker.getTotalEarning());
        assertEquals(0.0, tasker.getWorkedHours());
    }

    @Test
    void getTaskerByEmailShouldReturnTaskerWithNullBirthDate() {
        // Insert a tasker with null birthDate
        jdbcTemplate.update(
            "INSERT INTO tasker (firstName, lastName, username, password, email, birthDate, phone, gender, image, availability, rating, hourRate, bio, serviceID, totalEarning, WorkedHours, addressCity) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            "No", "BirthDate", "nobirth", "pass", "nobirth@example.com",
            null, "01088888888", "F",
            new byte[]{2}, "available", 4.0, 40.0, "No birth date", 1, 1000.0, 25.0, "Cairo"
        );

        Tasker tasker = taskerDaoLogin.getTaskerByEmail("nobirth@example.com");

        assertNotNull(tasker);
        assertEquals("No", tasker.getFirstName());
        assertNull(tasker.getBirthDate());
    }
}

