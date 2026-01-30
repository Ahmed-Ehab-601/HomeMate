package com.homemate.taskerprofile;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.homemate.TaskerProfile.DTO.AddressCityDTO;
import com.homemate.TaskerProfile.DTO.ChangeAvailabilityDTO;
import com.homemate.TaskerProfile.DTO.ChangeBioDTO;
import com.homemate.TaskerProfile.DTO.ChangeImageDTO;
import com.homemate.TaskerProfile.DTO.DateOfBirthDTO;
import com.homemate.TaskerProfile.DTO.EmailDTO;
import com.homemate.TaskerProfile.DTO.HourRateDTO;
import com.homemate.TaskerProfile.DTO.NameDTO;
import com.homemate.TaskerProfile.DTO.PaginatedReviewRequest;
import com.homemate.TaskerProfile.DTO.PasswordDTO;
import com.homemate.TaskerProfile.DTO.PhoneNumberDTO;
import com.homemate.TaskerProfile.DTO.TaskerProfileDTO;
import com.homemate.TaskerProfile.DTO.UsernameDTO;
import com.homemate.TaskerProfile.services.TaskerProfileService;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Transactional
@ActiveProfiles("taskerprofile")
class TaskerProfileServiceTest {

    @Autowired
    private TaskerProfileService taskerProfileService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Data is initialized via SQL scripts (taskerProfileData.sql)
    }

    @Test
    void testChangePassword_Success() {
        PasswordDTO passwordDTO = new PasswordDTO();
        passwordDTO.setTaskerID(1L);
        passwordDTO.setOldPassword("password123");
        passwordDTO.setNewPassword("NewStrongPass1!");

        Boolean result = taskerProfileService.changePassword(passwordDTO);

        assertTrue(result);
        String newPassword = jdbcTemplate.queryForObject(
            "SELECT password FROM Tasker WHERE taskerID = ?", String.class, 1L);
    }

    @Test
    void testChangePassword_ValidationErrors() {
        // Test old password required
        PasswordDTO passwordDTO1 = new PasswordDTO();
        passwordDTO1.setTaskerID(1L);
        passwordDTO1.setOldPassword(null);
        passwordDTO1.setNewPassword("newPassword456");
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
            taskerProfileService.changePassword(passwordDTO1);
        });
        assertEquals("Old password is required.", exception1.getMessage());

        // Test new password required
        PasswordDTO passwordDTO2 = new PasswordDTO();
        passwordDTO2.setTaskerID(1L);
        passwordDTO2.setOldPassword("password123");
        passwordDTO2.setNewPassword("");
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            taskerProfileService.changePassword(passwordDTO2);
        });
        assertEquals("New password is required.", exception2.getMessage());
    }

    @Test
    void testChangePassword_MaxLengthExceeded() {
        PasswordDTO passwordDTO = new PasswordDTO();
        passwordDTO.setTaskerID(1L);
        passwordDTO.setOldPassword("password123");
        passwordDTO.setNewPassword("a".repeat(256)); // 256 characters, exceeds 255

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskerProfileService.changePassword(passwordDTO);
        });
        assertEquals("Password must not exceed 255 characters.", exception.getMessage());
    }

    @Test
    void testChangePassword_MaxLengthBoundary() {
        PasswordDTO passwordDTO = new PasswordDTO();
        passwordDTO.setTaskerID(1L);
        passwordDTO.setOldPassword("password123");
        // Ensure complex password of length 255
        String prefix = "Strong1!";
        String padding = "a".repeat(70 - prefix.length());
        passwordDTO.setNewPassword(prefix + padding);

        Boolean result = taskerProfileService.changePassword(passwordDTO);
        assertTrue(result);
    }

    @Test
    void testChangePassword_IncorrectOldPassword() {
        PasswordDTO passwordDTO = new PasswordDTO();
        passwordDTO.setTaskerID(1L);
        passwordDTO.setOldPassword("wrongPassword");
        passwordDTO.setNewPassword("NewStrongPass1!");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskerProfileService.changePassword(passwordDTO);
        });
        assertEquals("Old password is incorrect.", exception.getMessage());
    }

    @Test
    void testChangeUsername_LengthValidation() {
        // Test success
        UsernameDTO usernameDTO1 = new UsernameDTO();
        usernameDTO1.setTaskerID(1L);
        usernameDTO1.setNewUsername("newusername");
        Boolean result1 = taskerProfileService.changeUsername(usernameDTO1);
        assertTrue(result1);

        // Test max length exceeded
        UsernameDTO usernameDTO2 = new UsernameDTO();
        usernameDTO2.setTaskerID(1L);
        usernameDTO2.setNewUsername("a".repeat(51)); // 51 characters, exceeds 50
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskerProfileService.changeUsername(usernameDTO2);
        });
        assertEquals("Username must not exceed 50 characters.", exception.getMessage());

        // Test max length boundary
        UsernameDTO usernameDTO3 = new UsernameDTO();
        usernameDTO3.setTaskerID(1L);
        usernameDTO3.setNewUsername("b".repeat(50)); // Exactly 50 characters, use 'b' to avoid duplicate key with seeded 'a'*50
        Boolean result3 = taskerProfileService.changeUsername(usernameDTO3);
        assertTrue(result3);
    }

    @Test
    void testChangePhoneNumber_MaxLengthExceeded() {
        PhoneNumberDTO phoneDTO = new PhoneNumberDTO();
        phoneDTO.setTaskerID(1L);
        phoneDTO.setNewPhoneNumber("1".repeat(51)); // 51 digits, exceeds 50

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskerProfileService.changePhoneNumber(phoneDTO);
        });
        assertEquals("Phone number must contain between 10 and 15 digits.", exception.getMessage());
    }

    @Test
    void testChangeBio_LengthValidation() {
        // Test max length exceeded
        ChangeBioDTO bioDTO1 = new ChangeBioDTO();
        bioDTO1.setTaskerID(1L);
        bioDTO1.setNewBio("a".repeat(501)); // 501 characters, exceeds 500
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskerProfileService.changeBio(bioDTO1);
        });
        assertEquals("Bio must not exceed 500 characters.", exception.getMessage());

        // Test max length boundary
        ChangeBioDTO bioDTO2 = new ChangeBioDTO();
        bioDTO2.setTaskerID(1L);
        bioDTO2.setNewBio("a".repeat(500)); // Exactly 500 characters
        Boolean result = taskerProfileService.changeBio(bioDTO2);
        assertTrue(result);
    }



    @Test
    void testChangeName_FirstNameMaxLengthExceeded() {
        NameDTO nameDTO = new NameDTO();
        nameDTO.setTaskerID(1L);
        nameDTO.setNewFirstName("a".repeat(51)); // 51 characters, exceeds 50
        nameDTO.setNewLastName("Doe");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskerProfileService.changeName(nameDTO);
        });
        assertEquals("First name must not exceed 50 characters.", exception.getMessage());
    }

    @Test
    void testChangeName_LastNameMaxLengthExceeded() {
        NameDTO nameDTO = new NameDTO();
        nameDTO.setTaskerID(1L);
        nameDTO.setNewFirstName("John");
        nameDTO.setNewLastName("a".repeat(51)); // 51 characters, exceeds 50

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskerProfileService.changeName(nameDTO);
        });
        assertEquals("Last name must not exceed 50 characters.", exception.getMessage());
    }

    @Test
    void testChangeEmail_MaxLengthExceeded() {
        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setTaskerID(1L);
        emailDTO.setNewEmail("a".repeat(51)); // 51 characters, exceeds 50

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskerProfileService.ChangeEmail(emailDTO);
        });
        assertEquals("Email must not exceed 50 characters nor empty.", exception.getMessage());
    }
    @Test
    void testChangeDOB_Success() {
        DateOfBirthDTO dto = new DateOfBirthDTO();
        dto.setTaskerID(1L);
        dto.setNewDateOfBirth(Timestamp.valueOf("2000-01-01 00:00:00"));

        Boolean result = taskerProfileService.changeDOB(dto);
        assertTrue(result);

        Timestamp dob = jdbcTemplate.queryForObject(
            "SELECT birthDate FROM Tasker WHERE taskerID = ?", Timestamp.class, 1L);
        
        assertEquals(Timestamp.valueOf("2000-01-01 00:00:00"), dob);
    }
    @Test
    void testGetData_ReturnsCorrectProfile() {
        TaskerProfileDTO profile = taskerProfileService.getData(1L);

        assertNotNull(profile);
        assertEquals("John", profile.getFirstName());
        assertEquals("Doe", profile.getLastName());
    }
    @Test
    void testGetReviews_Empty() {
        PaginatedReviewRequest req = new PaginatedReviewRequest();
        req.setTaskerID(1L);
        req.setPage(1);
        req.setPageSize(10);

        var response = taskerProfileService.getReviews(req);

        assertEquals(0, response.getTotalReviews());
        assertTrue(response.getReviews().isEmpty());
        assertEquals(1, response.getCurrentPage());
    }

       @Test
    void testChangeAddressCity_Success() {
        AddressCityDTO cityDTO = new AddressCityDTO();
        cityDTO.setTaskerID(1L);
        cityDTO.setNewAddressCity("San Francisco");

        Boolean result = taskerProfileService.changeAddressCity(cityDTO);
        assertTrue(result);

        String city = jdbcTemplate.queryForObject(
            "SELECT addressCity FROM Tasker WHERE taskerID = ?",
            String.class, 1L);
        assertEquals("San Francisco", city);
    }

    @Test
    void testChangeAddressCity_Validation() {
        AddressCityDTO emptyCityDTO = new AddressCityDTO();
        emptyCityDTO.setTaskerID(1L);
        emptyCityDTO.setNewAddressCity("   ");

        IllegalArgumentException missingException = assertThrows(IllegalArgumentException.class, () -> {
            taskerProfileService.changeAddressCity(emptyCityDTO);
        });
        assertEquals("City is required.", missingException.getMessage());

        AddressCityDTO longCityDTO = new AddressCityDTO();
        longCityDTO.setTaskerID(1L);
        longCityDTO.setNewAddressCity("a".repeat(201));

        IllegalArgumentException longException = assertThrows(IllegalArgumentException.class, () -> {
            taskerProfileService.changeAddressCity(longCityDTO);
        });
        assertEquals("City must not exceed 50 characters.", longException.getMessage());
    }

    @Test
    void testChangeHourRate_Success() {
        HourRateDTO dto = new HourRateDTO();
        dto.setTaskerID(1L);
        dto.setNewHourRate(75.5);

        Boolean result = taskerProfileService.changeHourRate(dto);
        assertTrue(result);

        Double rate = jdbcTemplate.queryForObject(
            "SELECT hourrate FROM Tasker WHERE taskerID = ?", Double.class, 1L);

        assertEquals(75.5, rate);
    }
    @Test
    void testChangeService_Success() {
        Boolean result = taskerProfileService.changeService(1L, 1L);
        assertTrue(result);

        Long serviceID = jdbcTemplate.queryForObject(
            "SELECT serviceID FROM Tasker WHERE taskerID = ?", Long.class, 1L);

        assertEquals(1L, serviceID);
    }
    @Test
    void testGetAvailableServices() {
        List<?> services = taskerProfileService.getAvailableServices();
        assertEquals(2, services.size());
    }
    @Test
    void testChangeAvailability_Success() {
        ChangeAvailabilityDTO dto = new ChangeAvailabilityDTO();
        dto.setTaskerID(1L);
        dto.setNewAvailability("unavailable");

        Boolean result = taskerProfileService.changeAvailablity(dto);
        assertTrue(result);

        String availability = jdbcTemplate.queryForObject(
            "SELECT availability FROM Tasker WHERE taskerID = ?", String.class, 1L);

        assertEquals("unavailable", availability);
    }
    @Test
    void testDeleteAccount_Success() {
        Boolean result = taskerProfileService.deleteAccount(1L);
        assertTrue(result);

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM Tasker WHERE taskerID = ?", Integer.class, 1L);

        assertEquals(0, count);
    }
    @Test
    void testChangeImage_EmptyImage() {
        ChangeImageDTO dto = new ChangeImageDTO();
        dto.setTaskerID(1L);
        dto.setNewImage("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            taskerProfileService.changeImage(dto);
        });

        assertEquals("Image cannot be empty.", ex.getMessage());
    }
    @Test
    void testChangeBio_Success() {
        ChangeBioDTO dto = new ChangeBioDTO();
        dto.setTaskerID(1L);
        dto.setNewBio("Updated bio");

        Boolean result = taskerProfileService.changeBio(dto);
        assertTrue(result);

        String bio = jdbcTemplate.queryForObject(
            "SELECT bio FROM Tasker WHERE taskerID = ?", String.class, 1L);

        assertEquals("Updated bio", bio);
    }
    @Test
    void testChangePhoneNumber_Success() {
        PhoneNumberDTO dto = new PhoneNumberDTO();
        dto.setTaskerID(1L);
        dto.setNewPhoneNumber("01234567890");

        Boolean result = taskerProfileService.changePhoneNumber(dto);
        assertTrue(result);
    }
    @Test
    void testChangeEmail_Success() {
        EmailDTO dto = new EmailDTO();
        dto.setTaskerID(1L);
        dto.setNewEmail("new.email@test.com");

        Boolean result = taskerProfileService.ChangeEmail(dto);
        assertTrue(result);
    }

    @Test
    void testChangeUsername_EmptyShouldFail() {
        UsernameDTO dto = new UsernameDTO();
        dto.setTaskerID(1L);
        dto.setNewUsername("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            taskerProfileService.changeUsername(dto);
        });

        assertEquals("Username must not exceed 50 characters.", ex.getMessage());
    }

   

}

