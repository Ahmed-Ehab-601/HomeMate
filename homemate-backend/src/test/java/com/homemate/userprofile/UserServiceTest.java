package com.homemate.UserProfile.Services;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.homemate.UserProfile.DTO.AddressDTO;
import com.homemate.UserProfile.DTO.DateOfBirthDTO;
import com.homemate.UserProfile.DTO.DeleteAccountRequestDTO;
import com.homemate.UserProfile.DTO.EmailDTO;
import com.homemate.UserProfile.DTO.PasswordDTO;
import com.homemate.UserProfile.DTO.PhoneNumberDTO;
import com.homemate.UserProfile.DTO.RemoveAddressDTO;
import com.homemate.UserProfile.DTO.UserProfileDTO;
import com.homemate.UserProfile.DTO.UsernameDTO;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Transactional
@ActiveProfiles("userprofile")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Data is initialized via SQL scripts (userProfileData.sql)
    }

    @Test
    void testGetProfile() {
        // Test getting user profile
        UserProfileDTO profile = userService.getProfile(1L);
        
        assertNotNull(profile);
        assertEquals(1L, profile.getUserId());
        assertEquals("jsmith", profile.getUsername());
        assertEquals("John", profile.getFirstName());
        assertEquals("Smith", profile.getLastName());
        assertEquals("john.smith@email.com", profile.getEmail());
        assertEquals("M", profile.getGender().toString());
        assertEquals("+1-555-0101", profile.getPhone());
        assertFalse(profile.getAdmin());
        assertFalse(profile.getSuspended());
    }

    

    @Test
    void testChangePassword() {
        PasswordDTO passwordDTO = new PasswordDTO();
        passwordDTO.setUserId(1L);
        passwordDTO.setOldPassword("$2a$10$test123456789");
        passwordDTO.setNewPassword("$2a$10$newPasswordHash123");
        
        Boolean result = userService.changePassword(passwordDTO);
        
        assertTrue(result);
        
        // Verify password was changed
        String newPassword = jdbcTemplate.queryForObject(
            "SELECT password FROM Users WHERE userID = ?", 
            String.class, 1L);
        assertEquals("$2a$10$newPasswordHash123", newPassword);
    }

    @Test
    void testChangeUsername() {
        UsernameDTO usernameDTO = new UsernameDTO();
        usernameDTO.setUserId(1L);
        usernameDTO.setUsername("newusername");
        
        Boolean result = userService.changeUsername(usernameDTO);
        
        assertTrue(result);
        
        // Verify username was changed
        String newUsername = jdbcTemplate.queryForObject(
            "SELECT username FROM Users WHERE userID = ?", 
            String.class, 1L);
        assertEquals("newusername", newUsername);
    }

    @Test
    void testChangeEmail() {
        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setUserId(1L);
        emailDTO.setEmail("newemail@test.com");
        
        Boolean result = userService.changeEmail(emailDTO);
        
        assertTrue(result);
        
        // Verify email was changed
        String newEmail = jdbcTemplate.queryForObject(
            "SELECT email FROM Users WHERE userID = ?", 
            String.class, 1L);
        assertEquals("newemail@test.com", newEmail);
    }

    @Test
    void testChangePhoneNumber() {
        PhoneNumberDTO phoneDTO = new PhoneNumberDTO();
        phoneDTO.setUserId(1L);
        phoneDTO.setPhoneNumber("+15559999999");
        
        Boolean result = userService.changePhoneNumber(phoneDTO);
        
        assertTrue(result);
        
        // Verify phone was changed
        String newPhone = jdbcTemplate.queryForObject(
            "SELECT phone FROM Users WHERE userID = ?", 
            String.class, 1L);
        assertEquals("+15559999999", newPhone);
    }

    @Test
    void testChangeDOB() {
        DateOfBirthDTO dobDTO = new DateOfBirthDTO();
        dobDTO.setUserId(1L);
        dobDTO.setDateOfBirth(Timestamp.valueOf(LocalDateTime.of(1995, 1, 1, 0, 0)));
        
        Boolean result = userService.changeDOB(dobDTO);
        
        assertTrue(result);
        
        // Verify DOB was changed
        Timestamp newDOB = jdbcTemplate.queryForObject(
            "SELECT birthDate FROM Users WHERE userID = ?", 
            Timestamp.class, 1L);
        assertNotNull(newDOB);
    }

    @Test
    void testAddAddress() {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setUserId(1L);
        addressDTO.setCountry("USA");
        addressDTO.setCity("New York");
        addressDTO.setStreet("123 Test Street");
        addressDTO.setApartment("Apt 5B");
        
        Boolean result = userService.addAddress(addressDTO);
        
        assertTrue(result);
        
        // Verify address was added
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM Address WHERE userID = ?", 
            Integer.class, 1L);
        assertNotNull(count);
        assertTrue(count > 0);
    }

    @Test
    void testGetAddresses() {
        // First add an address
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setUserId(1L);
        addressDTO.setCountry("USA");
        addressDTO.setCity("New York");
        addressDTO.setStreet("123 Main Street");
        addressDTO.setApartment("Apt 4B");
        userService.addAddress(addressDTO);
        
        // Get addresses
        AddressDTO[] addresses = userService.getAddresses(1L);
        
        assertNotNull(addresses);
        assertTrue(addresses.length > 0);
        assertEquals("USA", addresses[0].getCountry());
        assertEquals("New York", addresses[0].getCity());
    }

    @Test
    void testUpdateAddress() {
        // First add an address
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setUserId(1L);
        addressDTO.setCountry("USA");
        addressDTO.setCity("New York");
        addressDTO.setStreet("123 Main Street");
        addressDTO.setApartment("Apt 4B");
        userService.addAddress(addressDTO);
        
        // Get the address ID
        Long addressId = jdbcTemplate.queryForObject(
            "SELECT addressID FROM Address WHERE userID = 1 LIMIT 1", 
            Long.class);
        
        // Update the address
        AddressDTO updateDTO = new AddressDTO();
        updateDTO.setAddressId(addressId);
        updateDTO.setUserId(1L);
        updateDTO.setCountry("USA");
        updateDTO.setCity("Los Angeles");
        updateDTO.setStreet("456 Updated Street");
        updateDTO.setApartment("Suite 10");
        
        Boolean result = userService.updateAddress(updateDTO);
        
        assertTrue(result);
        
        // Verify address was updated
        String city = jdbcTemplate.queryForObject(
            "SELECT city FROM Address WHERE addressID = ?", 
            String.class, addressId);
        assertEquals("Los Angeles", city);
    }

    @Test
    void testRemoveAddress() {
        // First add an address
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setUserId(1L);
        addressDTO.setCountry("USA");
        addressDTO.setCity("New York");
        addressDTO.setStreet("123 Main Street");
        addressDTO.setApartment("Apt 4B");
        userService.addAddress(addressDTO);
        
        // Get the address ID
        Long addressId = jdbcTemplate.queryForObject(
            "SELECT addressID FROM Address WHERE userID = 1 LIMIT 1", 
            Long.class);
        
        RemoveAddressDTO removeDTO = new RemoveAddressDTO();
        removeDTO.setAddressId(addressId);
        
        Boolean result = userService.removeAddress(removeDTO);
        
        assertTrue(result);
        
        // Verify address was removed
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM Address WHERE addressID = ?", 
            Integer.class, addressId);
        assertEquals(0, count);
    }

    @Test
    void testDeleteAccount() {
        DeleteAccountRequestDTO deleteDTO = new DeleteAccountRequestDTO();
        deleteDTO.setUserId(1L);
        
        Boolean result = userService.deleteAccount(deleteDTO);
        
        assertTrue(result);
        
        // Verify user was deleted
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM Users WHERE userID = ?", 
            Integer.class, 1L);
        assertEquals(0, count);
    }

    @Test
    void testGetProfileWithNullUserId() {
        assertThrows(NullPointerException.class, () -> {
            userService.getProfile(null);
        });
    }

    @Test
    void testChangePasswordWithNullDTO() {
        assertThrows(NullPointerException.class, () -> {
            userService.changePassword(null);
        });
    }
}

