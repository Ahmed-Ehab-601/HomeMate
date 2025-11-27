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

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application.properties")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Clean up before each test - must delete in correct order due to foreign key constraints
        // ON DELETE RESTRICT prevents deleting addresses referenced by tasks
        jdbcTemplate.update("SET FOREIGN_KEY_CHECKS = 0");

        jdbcTemplate.update("DELETE FROM Reviews");
        jdbcTemplate.update("DELETE FROM Report");

        jdbcTemplate.update("DELETE FROM Message");
        jdbcTemplate.update("DELETE FROM Chat");
        jdbcTemplate.update("DELETE FROM Task");
        jdbcTemplate.update("DELETE FROM Address");
        jdbcTemplate.update("DELETE FROM Tasker");
        jdbcTemplate.update("DELETE FROM Service");
        jdbcTemplate.update("DELETE FROM Users");
        // Reset auto-increment counters
        jdbcTemplate.update("ALTER TABLE Users AUTO_INCREMENT = 1");
        jdbcTemplate.update("ALTER TABLE Address AUTO_INCREMENT = 1");
        jdbcTemplate.update("SET FOREIGN_KEY_CHECKS = 1");
        
        // Insert test users similar to data.sql
        insertTestUsers();
    }

    private void insertTestUsers() {
        // Insert 12 test users with fake data similar to data.sql
        String insertUserSQL = "INSERT INTO Users (firstName, lastName, username, password, email, birthDate, gender, phone, admin, suspended) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
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
        phoneDTO.setPhoneNumber("+1-555-9999");
        
        Boolean result = userService.changePhoneNumber(phoneDTO);
        
        assertTrue(result);
        
        // Verify phone was changed
        String newPhone = jdbcTemplate.queryForObject(
            "SELECT phone FROM Users WHERE userID = ?", 
            String.class, 1L);
        assertEquals("+1-555-9999", newPhone);
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

