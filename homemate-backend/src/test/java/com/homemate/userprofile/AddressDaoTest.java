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

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AddressDTORowMapper.class)
@ActiveProfiles("userprofile")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AddressDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AddressDTORowMapper addressDTORowMapper;

    private AddressDao addressDao;

    @BeforeEach
    void setUp() {
        addressDao = new AddressDao(jdbcTemplate, addressDTORowMapper);
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

