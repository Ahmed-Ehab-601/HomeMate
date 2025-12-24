package com.homemate.UserProfile.DAO;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.homemate.UserProfile.DTO.AddressDTO;
import com.homemate.UserProfile.Mappers.AddressDTORowMapper;

@Repository
public class AddressDao {
    private final JdbcTemplate jdbcTemplate;
    private final AddressDTORowMapper addressDTORowMapper;

    public AddressDao(JdbcTemplate jdbcTemplate, AddressDTORowMapper addressDTORowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.addressDTORowMapper = addressDTORowMapper;
    }

    private static final String GET_ADDRESSES_SQL =
        "SELECT addressID, userID, country, city, street, apartment FROM Address WHERE userID = ?";

    @SuppressWarnings( "null" )
    public List<AddressDTO> getByUserID(Long userID) {
        return jdbcTemplate.query(
            GET_ADDRESSES_SQL,
            this.addressDTORowMapper,
            userID
        );
    }

    private static final String ADD_ADDRESS_SQL = "INSERT INTO Address (userID, country, city, street, apartment) VALUES (?, ?, ?, ?, ?)";

    public void addAddress(AddressDTO addressDTO) {
        jdbcTemplate.update(ADD_ADDRESS_SQL, addressDTO.getUserID(), addressDTO.getCountry(), addressDTO.getCity(), addressDTO.getStreet(), addressDTO.getApartment());
    }

    private static final String UPDATE_ADDRESS_SQL = "UPDATE Address SET country = ?, city = ?, street = ?, apartment = ? WHERE addressID = ?";

    public void updateAddress(AddressDTO addressDTO) {
        jdbcTemplate.update(UPDATE_ADDRESS_SQL, addressDTO.getCountry(), addressDTO.getCity(), addressDTO.getStreet(), addressDTO.getApartment(), addressDTO.getAddressId());
    }

    private static final String DELETE_ADDRESS_SQL = "DELETE FROM Address WHERE addressID = ?";

    public void deleteAddress(Long addressId) {
        try {
            jdbcTemplate.update(DELETE_ADDRESS_SQL, addressId);
        } 
        catch (Exception e) {
            throw new IllegalArgumentException("This address is used in a task and cannot be deleted.");
        }
    }
}
