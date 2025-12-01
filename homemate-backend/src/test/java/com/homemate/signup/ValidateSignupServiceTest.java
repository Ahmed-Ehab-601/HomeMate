package com.homemate.signup;

import com.homemate.TaskerProfile.DTO.TaskerSignupDTO;
import com.homemate.UserProfile.DTO.SignupUserDTO;
import com.homemate.security.service.ValidateSignupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ValidateSignupServiceTest {

    @InjectMocks
    private ValidateSignupService validateSignupService;

    private SignupUserDTO signupUserDTO;
    private TaskerSignupDTO taskerSignupDTO;

    @BeforeEach
    void setUp() {
        signupUserDTO = new SignupUserDTO();
        signupUserDTO.setUsername("testuser");
        signupUserDTO.setFirstName("Test");
        signupUserDTO.setLastName("User");
        signupUserDTO.setEmail("test@example.com");
        signupUserDTO.setPassword("Password123!");
        signupUserDTO.setBirthDate(Timestamp.valueOf(LocalDateTime.of(1990, 1, 1, 0, 0)));
        signupUserDTO.setGender('M');
        signupUserDTO.setPhone("01012345678");

        taskerSignupDTO = new TaskerSignupDTO();
        taskerSignupDTO.setUsername("taskeruser");
        taskerSignupDTO.setFirstName("Tasker");
        taskerSignupDTO.setLastName("User");
        taskerSignupDTO.setEmail("tasker@example.com");
        taskerSignupDTO.setPassword("Password123!");
        taskerSignupDTO.setPhoneNumber("01012345678");
        taskerSignupDTO.setDateOfBirth(Timestamp.valueOf(LocalDateTime.of(1990, 1, 1, 0, 0)));
        taskerSignupDTO.setBio("This is a valid bio that is long enough");
        taskerSignupDTO.setHourRate(50.0);
        taskerSignupDTO.setCity("Cairo");
        taskerSignupDTO.setProfileImage(new byte[1024]);
    }

    @Test
    void validateUserSignupShouldReturnNullWithValidData() {
        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNull(result);
    }

    @Test
    void validateUserSignupShouldReturnErrorWithNullUsername() {
        signupUserDTO.setUsername(null);

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
        assertEquals("Username is required", result);
    }

    @Test
    void validateUserSignupShouldReturnErrorWithEmptyUsername() {
        signupUserDTO.setUsername("");

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
        assertEquals("Username is required", result);
    }

    @Test
    void validateUserSignupShouldReturnErrorWithShortUsername() {
        signupUserDTO.setUsername("ab");

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
        assertTrue(result.contains("Username must be between"));
    }

    @Test
    void validateUserSignupShouldReturnErrorWithLongUsername() {
        signupUserDTO.setUsername("a".repeat(26));

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
        assertTrue(result.contains("Username must be between"));
    }

    @Test
    void validateUserSignupShouldReturnErrorWithInvalidUsernameCharacters() {
        signupUserDTO.setUsername("test-user!");

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
        assertTrue(result.contains("Username must contain only letters and numbers"));
    }

    @Test
    void validateUserSignupShouldReturnErrorWithNullEmail() {
        signupUserDTO.setEmail(null);

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
        assertEquals("Email is required", result);
    }

    @Test
    void validateUserSignupShouldReturnErrorWithShortEmail() {
        signupUserDTO.setEmail("ab");

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
        assertTrue(result.contains("Email must be between"));
    }

    @Test
    void validateUserSignupShouldReturnErrorWithNullPassword() {
        signupUserDTO.setPassword(null);

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
        assertEquals("Password is required", result);
    }

    @Test
    void validateUserSignupShouldReturnErrorWithShortPassword() {
        signupUserDTO.setPassword("Pass1!");

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
        assertTrue(result.contains("password must be between"));
    }

    @Test
    void validateUserSignupShouldReturnErrorWithPasswordMissingUpperCase() {
        signupUserDTO.setPassword("password123!");

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
        assertTrue(result.contains("Password must contain at least one uppercase letter"));
    }

    @Test
    void validateUserSignupShouldReturnErrorWithPasswordMissingLowerCase() {
        signupUserDTO.setPassword("PASSWORD123!");

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
    }

    @Test
    void validateUserSignupShouldReturnErrorWithPasswordMissingDigit() {
        signupUserDTO.setPassword("Password!");

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
    }

    @Test
    void validateUserSignupShouldReturnErrorWithPasswordMissingSpecialChar() {
        signupUserDTO.setPassword("Password123");

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
    }

    @Test
    void validateUserSignupShouldReturnErrorWithNullBirthDate() {
        signupUserDTO.setBirthDate(null);

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
        assertEquals("Birth date is required", result);
    }

    @Test
    void validateUserSignupShouldReturnErrorWithFutureBirthDate() {
        signupUserDTO.setBirthDate(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
        assertEquals("Birth date must be in the past", result);
    }

    @Test
    void validateUserSignupShouldReturnErrorWithNullGender() {
        signupUserDTO.setGender(null);

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
        assertEquals("Gender is required", result);
    }

    @Test
    void validateUserSignupShouldReturnErrorWithInvalidGender() {
        signupUserDTO.setGender('X');

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
        assertEquals("Gender must be M or F", result);
    }

    @Test
    void validateUserSignupShouldReturnErrorWithNullPhone() {
        signupUserDTO.setPhone(null);

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
        assertEquals("Phone is required", result);
    }

    @Test
    void validateUserSignupShouldReturnErrorWithInvalidPhoneLength() {
        signupUserDTO.setPhone("0101234567");

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
        assertEquals("Phone must be 11 characters", result);
    }

    @Test
    void validateUserSignupShouldReturnErrorWithInvalidPhonePrefix() {
        signupUserDTO.setPhone("02012345678");

        String result = validateSignupService.validateUserSignup(signupUserDTO);

        assertNotNull(result);
        assertTrue(result.contains("Phone must start with 010, 011, 012, or 015"));
    }

    @Test
    void validateTaskerSignupShouldReturnNullWithValidData() {
        String result = validateSignupService.validateTaskerSignup(taskerSignupDTO);

        assertNull(result);
    }

    @Test
    void validateTaskerSignupShouldReturnErrorWithNullFirstName() {
        taskerSignupDTO.setFirstName(null);

        String result = validateSignupService.validateTaskerSignup(taskerSignupDTO);

        assertNotNull(result);
        assertEquals("first name and last name are required", result);
    }

    @Test
    void validateTaskerSignupShouldReturnErrorWithShortFirstName() {
        taskerSignupDTO.setFirstName("ab");

        String result = validateSignupService.validateTaskerSignup(taskerSignupDTO);

        assertNotNull(result);
        assertTrue(result.contains("name must be between 3 and 200 characters"));
    }

    @Test
    void validateTaskerSignupShouldReturnErrorWithInvalidFirstNameCharacters() {
        taskerSignupDTO.setFirstName("Test123");

        String result = validateSignupService.validateTaskerSignup(taskerSignupDTO);

        assertNotNull(result);
        assertTrue(result.contains("name must contain only letters"));
    }

    @Test
    void validateTaskerSignupShouldReturnErrorWithNullBio() {
        taskerSignupDTO.setBio(null);

        String result = validateSignupService.validateTaskerSignup(taskerSignupDTO);

        assertNotNull(result);
        assertEquals("Bio is required", result);
    }

    @Test
    void validateTaskerSignupShouldReturnErrorWithShortBio() {
        taskerSignupDTO.setBio("Short");

        String result = validateSignupService.validateTaskerSignup(taskerSignupDTO);

        assertNotNull(result);
        assertTrue(result.contains("Bio must be at least 10 characters"));
    }

    @Test
    void validateTaskerSignupShouldReturnErrorWithLongBio() {
        taskerSignupDTO.setBio("a".repeat(501));

        String result = validateSignupService.validateTaskerSignup(taskerSignupDTO);

        assertNotNull(result);
        assertTrue(result.contains("Bio must be less than 500 characters"));
    }

    @Test
    void validateTaskerSignupShouldReturnErrorWithNullCity() {
        taskerSignupDTO.setCity(null);

        String result = validateSignupService.validateTaskerSignup(taskerSignupDTO);

        assertNotNull(result);
        assertEquals("City is required", result);
    }

    @Test
    void validateTaskerSignupShouldReturnErrorWithInvalidCityCharacters() {
        taskerSignupDTO.setCity("Cairo123");

        String result = validateSignupService.validateTaskerSignup(taskerSignupDTO);

        assertNotNull(result);
        assertTrue(result.contains("City must contain only letters"));
    }

    @Test
    void validateTaskerSignupShouldReturnErrorWithNullHourRate() {
        taskerSignupDTO.setHourRate(null);

        String result = validateSignupService.validateTaskerSignup(taskerSignupDTO);

        assertNotNull(result);
        assertEquals("Hour rate is required", result);
    }

    @Test
    void validateTaskerSignupShouldReturnErrorWithNegativeHourRate() {
        taskerSignupDTO.setHourRate(-10.0);

        String result = validateSignupService.validateTaskerSignup(taskerSignupDTO);

        assertNotNull(result);
        assertEquals("Hour rate must be greater than 0", result);
    }

    @Test
    void validateTaskerSignupShouldReturnErrorWithNullProfileImage() {
        taskerSignupDTO.setProfileImage(null);

        String result = validateSignupService.validateTaskerSignup(taskerSignupDTO);

        assertNotNull(result);
        assertEquals("Profile image is required", result);
    }

    @Test
    void validateTaskerSignupShouldReturnErrorWithLargeProfileImage() {
        taskerSignupDTO.setProfileImage(new byte[16 * 1024 * 1024 + 1]);

        String result = validateSignupService.validateTaskerSignup(taskerSignupDTO);

        assertNotNull(result);
        assertTrue(result.contains("Profile image must be less than 16MB"));
    }
}

