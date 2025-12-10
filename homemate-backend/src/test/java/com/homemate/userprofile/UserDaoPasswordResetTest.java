package com.homemate.userprofile;

import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.UserProfile.Models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(scripts = "/adminData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@ActiveProfiles("test")
class UserDaoPasswordResetTest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("resettest@example.com");
        testUser.setPassword("OldPass123!");
        testUser.setBirthDate(java.sql.Timestamp.valueOf("1990-01-01 00:00:00"));
        testUser.setGender('M');
        testUser.setPhone("01012345678");
        testUser.setIsAdmin(false);
        testUser.setIsSuspended(false);

        Long userId = userDao.signup(testUser);
        testUser.setUserID(userId);
    }

    @Test
    void updatePasswordShouldChangeUserPasswordSuccessfully() {
        String newPassword = "NewPass123!";

        userDao.updatePassword(testUser.getEmail(), newPassword);

        User updatedUser = userDao.getByEmail(testUser.getEmail());
        assertEquals(newPassword, updatedUser.getPassword());
    }

    @Test
    void updatePasswordShouldNotAffectOtherUserData() {
        String newPassword = "NewPass123!";
        String originalUsername = testUser.getUsername();
        String originalEmail = testUser.getEmail();
        String originalFirstName = testUser.getFirstName();
        String originalLastName = testUser.getLastName();

        userDao.updatePassword(testUser.getEmail(), newPassword);

        User updatedUser = userDao.getByEmail(testUser.getEmail());
        assertEquals(newPassword, updatedUser.getPassword());
        assertEquals(originalUsername, updatedUser.getUsername());
        assertEquals(originalEmail, updatedUser.getEmail());
        assertEquals(originalFirstName, updatedUser.getFirstName());
        assertEquals(originalLastName, updatedUser.getLastName());
    }

    @Test
    void updatePasswordShouldWorkForMultipleUsers() {
        // Create second user
        User secondUser = new User();
        secondUser.setUsername("testuser2");
        secondUser.setFirstName("Test2");
        secondUser.setLastName("User2");
        secondUser.setEmail("resettest2@example.com");
        secondUser.setPassword("OldPass456!");
        secondUser.setBirthDate(java.sql.Timestamp.valueOf("1991-01-01 00:00:00"));
        secondUser.setGender('F');
        secondUser.setPhone("01112345678");
        secondUser.setIsAdmin(false);
        secondUser.setIsSuspended(false);
        Long userId2 = userDao.signup(secondUser);
        secondUser.setUserID(userId2);

        String newPassword1 = "NewPass123!";
        String newPassword2 = "NewPass456!";

        // Update both passwords
        userDao.updatePassword(testUser.getEmail(), newPassword1);
        userDao.updatePassword(secondUser.getEmail(), newPassword2);

        // Verify both passwords were updated correctly
        User updatedUser1 = userDao.getByEmail(testUser.getEmail());
        User updatedUser2 = userDao.getByEmail(secondUser.getEmail());

        assertEquals(newPassword1, updatedUser1.getPassword());
        assertEquals(newPassword2, updatedUser2.getPassword());
    }

    @Test
    void updatePasswordShouldHandleSpecialCharactersInPassword() {
        String complexPassword = "P@ssw0rd!#$%^&*()";

        userDao.updatePassword(testUser.getEmail(), complexPassword);

        User updatedUser = userDao.getByEmail(testUser.getEmail());
        assertEquals(complexPassword, updatedUser.getPassword());
    }

    @Test
    void updatePasswordShouldNotUpdateNonExistentUser() {
        String newPassword = "NewPass123!";
        String nonExistentEmail = "nonexistent@example.com";

        // Should not throw exception but also should not update anything
        assertDoesNotThrow(() -> userDao.updatePassword(nonExistentEmail, newPassword));

        // Verify original user password is unchanged
        User originalUser = userDao.getByEmail(testUser.getEmail());
        assertEquals("OldPass123!", originalUser.getPassword());
    }

    @Test
    void updatePasswordShouldUpdateOnlyTheSpecifiedUser() {
        // Create second user
        User secondUser = new User();
        secondUser.setUsername("testuser2");
        secondUser.setFirstName("Test2");
        secondUser.setLastName("User2");
        secondUser.setEmail("resettest2@example.com");
        secondUser.setPassword("OldPass456!");
        secondUser.setBirthDate(java.sql.Timestamp.valueOf("1991-01-01 00:00:00"));
        secondUser.setGender('F');
        secondUser.setPhone("01112345678");
        secondUser.setIsAdmin(false);
        secondUser.setIsSuspended(false);
        Long userId2 = userDao.signup(secondUser);
        secondUser.setUserID(userId2);

        String newPassword = "NewPass123!";

        // Update only first user's password
        userDao.updatePassword(testUser.getEmail(), newPassword);

        // Verify first user's password was updated
        User updatedUser1 = userDao.getByEmail(testUser.getEmail());
        assertEquals(newPassword, updatedUser1.getPassword());

        // Verify second user's password was NOT updated
        User unchangedUser2 = userDao.getByEmail(secondUser.getEmail());
        assertEquals("OldPass456!", unchangedUser2.getPassword());
    }
}
