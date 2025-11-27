package com.homemate.UserProfile.DAO;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import com.homemate.UserProfile.DTO.AddressDTO;
import com.homemate.UserProfile.Mappers.AddressDTORowMapper;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AddressDTORowMapper.class)
@TestPropertySource(locations = "classpath:application.properties")
class AddressDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AddressDTORowMapper addressDTORowMapper;

    private AddressDao addressDao;

    @BeforeEach
    void setUp() {
        addressDao = new AddressDao(jdbcTemplate, addressDTORowMapper);
        
        // Clean up - must delete in correct order due to foreign key constraints
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
        
        // Insert test users
        insertTestUsers();
        
        // Insert test addresses similar to data.sql
        insertTestAddresses();
    }

    private void insertTestUsers() {
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

    private void insertTestAddresses() {
        String insertAddressSQL = "INSERT INTO Address (userID, country, city, street, apartment) VALUES (?, ?, ?, ?, ?)";
        
        // Insert addresses similar to data.sql
        jdbcTemplate.update(insertAddressSQL, 1L, "USA", "New York", "123 Main Street", "Apt 4B");
        jdbcTemplate.update(insertAddressSQL, 1L, "USA", "New York", "456 Park Avenue", null);
        jdbcTemplate.update(insertAddressSQL, 2L, "USA", "Los Angeles", "789 Sunset Blvd", "Suite 12");
        jdbcTemplate.update(insertAddressSQL, 3L, "USA", "Chicago", "321 Lake Shore Dr", "Unit 5A");
        jdbcTemplate.update(insertAddressSQL, 4L, "USA", "Houston", "654 Texas Avenue", null);
        jdbcTemplate.update(insertAddressSQL, 5L, "USA", "Phoenix", "987 Desert Road", "Apt 2C");
        jdbcTemplate.update(insertAddressSQL, 6L, "USA", "Philadelphia", "147 Liberty Street", null);
        jdbcTemplate.update(insertAddressSQL, 7L, "USA", "San Antonio", "258 River Walk", "Unit 8");
        jdbcTemplate.update(insertAddressSQL, 8L, "USA", "San Diego", "369 Beach Boulevard", "Apt 3D");
        jdbcTemplate.update(insertAddressSQL, 10L, "USA", "Dallas", "741 Commerce Street", null);
        jdbcTemplate.update(insertAddressSQL, 11L, "USA", "Seattle", "852 Pine Street", "Apt 6F");
        jdbcTemplate.update(insertAddressSQL, 12L, "USA", "Boston", "963 Beacon Hill", "Unit 9B");
    }

    @Test
    void testGetByUserID() {
        List<AddressDTO> addresses = addressDao.getByUserID(1L);
        
        assertNotNull(addresses);
        assertEquals(2, addresses.size());
        
        AddressDTO firstAddress = addresses.get(0);
        assertEquals(1L, firstAddress.getUserId());
        assertEquals("USA", firstAddress.getCountry());
        assertEquals("New York", firstAddress.getCity());
        assertEquals("123 Main Street", firstAddress.getStreet());
        assertEquals("Apt 4B", firstAddress.getApartment());
    }

    @Test
    void testGetByUserIDForUserWithNoAddresses() {
        List<AddressDTO> addresses = addressDao.getByUserID(9L);
        
        assertNotNull(addresses);
        assertEquals(0, addresses.size());
    }

    @Test
    void testAddAddress() {
        AddressDTO newAddress = new AddressDTO();
        newAddress.setUserId(1L);
        newAddress.setCountry("USA");
        newAddress.setCity("Miami");
        newAddress.setStreet("159 Ocean Drive");
        newAddress.setApartment(null);
        
        addressDao.addAddress(newAddress);
        
        List<AddressDTO> addresses = addressDao.getByUserID(1L);
        assertEquals(3, addresses.size());
        
        // Verify the new address was added
        boolean found = addresses.stream()
            .anyMatch(addr -> "Miami".equals(addr.getCity()) && "159 Ocean Drive".equals(addr.getStreet()));
        assertTrue(found);
    }

    @Test
    void testUpdateAddress() {
        // Get an existing address
        List<AddressDTO> addresses = addressDao.getByUserID(1L);
        assertFalse(addresses.isEmpty());
        
        AddressDTO addressToUpdate = addresses.get(0);
        Long addressId = addressToUpdate.getAddressId();
        
        // Update the address
        AddressDTO updatedAddress = new AddressDTO();
        updatedAddress.setAddressId(addressId);
        updatedAddress.setUserId(1L);
        updatedAddress.setCountry("USA");
        updatedAddress.setCity("Los Angeles");
        updatedAddress.setStreet("999 Updated Street");
        updatedAddress.setApartment("Suite 100");
        
        addressDao.updateAddress(updatedAddress);
        
        // Verify the update
        List<AddressDTO> updatedAddresses = addressDao.getByUserID(1L);
        AddressDTO foundAddress = updatedAddresses.stream()
            .filter(addr -> addressId.equals(addr.getAddressId()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(foundAddress);
        assertEquals("Los Angeles", foundAddress.getCity());
        assertEquals("999 Updated Street", foundAddress.getStreet());
        assertEquals("Suite 100", foundAddress.getApartment());
    }

    @Test
    void testDeleteAddress() {
        // Get an existing address
        List<AddressDTO> addresses = addressDao.getByUserID(1L);
        assertFalse(addresses.isEmpty());
        
        Long addressId = addresses.get(0).getAddressId();
        int initialCount = addresses.size();
        
        // Delete the address
        addressDao.deleteAddress(addressId);
        
        // Verify deletion
        List<AddressDTO> remainingAddresses = addressDao.getByUserID(1L);
        assertEquals(initialCount - 1, remainingAddresses.size());
        
        // Verify the address no longer exists
        boolean stillExists = remainingAddresses.stream()
            .anyMatch(addr -> addressId.equals(addr.getAddressId()));
        assertFalse(stillExists);
    }

    @Test
    void testGetAddressesForMultipleUsers() {
        // Test getting addresses for different users
        List<AddressDTO> user1Addresses = addressDao.getByUserID(1L);
        assertEquals(2, user1Addresses.size());
        
        List<AddressDTO> user2Addresses = addressDao.getByUserID(2L);
        assertEquals(1, user2Addresses.size());
        assertEquals("Los Angeles", user2Addresses.get(0).getCity());
        
        List<AddressDTO> user11Addresses = addressDao.getByUserID(11L);
        assertEquals(1, user11Addresses.size());
        assertEquals("Seattle", user11Addresses.get(0).getCity());
    }

    @Test
    void testAddAddressWithApartment() {
        AddressDTO newAddress = new AddressDTO();
        newAddress.setUserId(2L);
        newAddress.setCountry("USA");
        newAddress.setCity("Denver");
        newAddress.setStreet("753 Mountain View");
        newAddress.setApartment("Suite 15");
        
        addressDao.addAddress(newAddress);
        
        List<AddressDTO> addresses = addressDao.getByUserID(2L);
        assertEquals(2, addresses.size());
        
        AddressDTO addedAddress = addresses.stream()
            .filter(addr -> "Denver".equals(addr.getCity()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(addedAddress);
        assertEquals("Suite 15", addedAddress.getApartment());
    }

    @Test
    void testAddAddressWithoutApartment() {
        AddressDTO newAddress = new AddressDTO();
        newAddress.setUserId(3L);
        newAddress.setCountry("USA");
        newAddress.setCity("Portland");
        newAddress.setStreet("456 Forest Avenue");
        newAddress.setApartment(null);
        
        addressDao.addAddress(newAddress);
        
        List<AddressDTO> addresses = addressDao.getByUserID(3L);
        assertEquals(2, addresses.size());
        
        AddressDTO addedAddress = addresses.stream()
            .filter(addr -> "Portland".equals(addr.getCity()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(addedAddress);
        assertNull(addedAddress.getApartment());
    }
}

