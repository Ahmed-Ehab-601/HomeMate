package com.homemate.taskerprofile;

import com.homemate.TaskerProfile.DTO.TaskerSignupDTO;
import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.TaskerProfile.models.Tasker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(scripts = "/adminData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@ActiveProfiles("test")
class TaskerDaoPasswordResetTest {

    @Autowired
    private TaskerDao taskerDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private TaskerSignupDTO testTaskerDto;
    private Long testTaskerId;

    @BeforeEach
    void setUp() {
        testTaskerDto = new TaskerSignupDTO();
        testTaskerDto.setUsername("testtasker");
        testTaskerDto.setFirstName("Test");
        testTaskerDto.setLastName("Tasker");
        testTaskerDto.setEmail("taskeremail@example.com");
        testTaskerDto.setPassword("OldPass123!");
        testTaskerDto.setPhoneNumber("01012345678");
        testTaskerDto.setDateOfBirth(Timestamp.valueOf("1990-01-01 00:00:00"));
        testTaskerDto.setBio("Test bio for password reset testing");
        testTaskerDto.setServiceID(1L); // Assuming service with ID 1 exists
        testTaskerDto.setHourRate(50.0);
        testTaskerDto.setCity("Cairo");
        testTaskerDto.setProfileImage(new byte[]{1, 2, 3, 4, 5});

        testTaskerId = taskerDao.saveTasker(testTaskerDto);
    }

    @Test
    void updatePasswordShouldChangeTaskerPasswordSuccessfully() {
        String newPassword = "NewPass123!";

        taskerDao.updatePassword(testTaskerDto.getEmail(), newPassword);

        Tasker updatedTasker = taskerDao.getByEmail(testTaskerDto.getEmail());
        assertEquals(newPassword, updatedTasker.getPassword());
    }

    @Test
    void updatePasswordShouldNotAffectOtherTaskerData() {
        String newPassword = "NewPass123!";
        String originalUsername = testTaskerDto.getUsername();
        String originalEmail = testTaskerDto.getEmail();
        String originalFirstName = testTaskerDto.getFirstName();
        String originalLastName = testTaskerDto.getLastName();
        String originalBio = testTaskerDto.getBio();

        taskerDao.updatePassword(testTaskerDto.getEmail(), newPassword);

        Tasker updatedTasker = taskerDao.getByEmail(testTaskerDto.getEmail());
        assertEquals(newPassword, updatedTasker.getPassword());
        assertEquals(originalUsername, updatedTasker.getUsername());
        assertEquals(originalEmail, updatedTasker.getEmail());
        assertEquals(originalFirstName, updatedTasker.getFirstName());
        assertEquals(originalLastName, updatedTasker.getLastName());
        assertEquals(originalBio, updatedTasker.getBio());
    }

    @Test
    void updatePasswordShouldWorkForMultipleTaskers() {
        // Create second tasker
        TaskerSignupDTO secondTaskerDto = new TaskerSignupDTO();
        secondTaskerDto.setUsername("testtasker2");
        secondTaskerDto.setFirstName("Test2");
        secondTaskerDto.setLastName("Tasker2");
        secondTaskerDto.setEmail("taskeremail2@example.com");
        secondTaskerDto.setPassword("OldPass456!");
        secondTaskerDto.setPhoneNumber("01112345678");
        secondTaskerDto.setDateOfBirth(Timestamp.valueOf("1991-01-01 00:00:00"));
        secondTaskerDto.setBio("Test bio for second tasker");
        secondTaskerDto.setServiceID(1L);
        secondTaskerDto.setHourRate(60.0);
        secondTaskerDto.setCity("Alexandria");
        secondTaskerDto.setProfileImage(new byte[]{5, 4, 3, 2, 1});
        Long taskerId2 = taskerDao.saveTasker(secondTaskerDto);

        String newPassword1 = "NewPass123!";
        String newPassword2 = "NewPass456!";

        // Update both passwords
        taskerDao.updatePassword(testTaskerDto.getEmail(), newPassword1);
        taskerDao.updatePassword(secondTaskerDto.getEmail(), newPassword2);

        // Verify both passwords were updated correctly
        Tasker updatedTasker1 = taskerDao.getByEmail(testTaskerDto.getEmail());
        Tasker updatedTasker2 = taskerDao.getByEmail(secondTaskerDto.getEmail());

        assertEquals(newPassword1, updatedTasker1.getPassword());
        assertEquals(newPassword2, updatedTasker2.getPassword());
    }

    @Test
    void updatePasswordShouldHandleSpecialCharactersInPassword() {
        String complexPassword = "P@ssw0rd!#$%^&*()";

        taskerDao.updatePassword(testTaskerDto.getEmail(), complexPassword);

        Tasker updatedTasker = taskerDao.getByEmail(testTaskerDto.getEmail());
        assertEquals(complexPassword, updatedTasker.getPassword());
    }

    @Test
    void updatePasswordShouldNotUpdateNonExistentTasker() {
        String newPassword = "NewPass123!";
        String nonExistentEmail = "nonexistent@example.com";

        // Should not throw exception but also should not update anything
        assertDoesNotThrow(() -> taskerDao.updatePassword(nonExistentEmail, newPassword));

        // Verify original tasker password is unchanged
        Tasker originalTasker = taskerDao.getByEmail(testTaskerDto.getEmail());
        assertEquals("OldPass123!", originalTasker.getPassword());
    }

    @Test
    void updatePasswordShouldUpdateOnlyTheSpecifiedTasker() {
        // Create second tasker
        TaskerSignupDTO secondTaskerDto = new TaskerSignupDTO();
        secondTaskerDto.setUsername("testtasker2");
        secondTaskerDto.setFirstName("Test2");
        secondTaskerDto.setLastName("Tasker2");
        secondTaskerDto.setEmail("taskeremail2@example.com");
        secondTaskerDto.setPassword("OldPass456!");
        secondTaskerDto.setPhoneNumber("01112345678");
        secondTaskerDto.setDateOfBirth(Timestamp.valueOf("1991-01-01 00:00:00"));
        secondTaskerDto.setBio("Test bio for second tasker");
        secondTaskerDto.setServiceID(1L);
        secondTaskerDto.setHourRate(60.0);
        secondTaskerDto.setCity("Alexandria");
        secondTaskerDto.setProfileImage(new byte[]{5, 4, 3, 2, 1});
        Long taskerId2 = taskerDao.saveTasker(secondTaskerDto);

        String newPassword = "NewPass123!";

        // Update only first tasker's password
        taskerDao.updatePassword(testTaskerDto.getEmail(), newPassword);

        // Verify first tasker's password was updated
        Tasker updatedTasker1 = taskerDao.getByEmail(testTaskerDto.getEmail());
        assertEquals(newPassword, updatedTasker1.getPassword());

        // Verify second tasker's password was NOT updated
        Tasker unchangedTasker2 = taskerDao.getByEmail(secondTaskerDto.getEmail());
        assertEquals("OldPass456!", unchangedTasker2.getPassword());
    }

    @Test
    void updatePasswordShouldWorkWithLongPasswords() {
        String longPassword = "VeryLongPassword123!@#$%";

        taskerDao.updatePassword(testTaskerDto.getEmail(), longPassword);

        Tasker updatedTasker = taskerDao.getByEmail(testTaskerDto.getEmail());
        assertEquals(longPassword, updatedTasker.getPassword());
    }
}
