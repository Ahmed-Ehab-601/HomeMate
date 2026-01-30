package com.homemate.UserProfile.Mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.homemate.UserProfile.DTO.AddressDTO;

@Component
public class AddressDTORowMapper implements RowMapper<AddressDTO> {
    @Override
    public AddressDTO mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressId(rs.getLong("addressID"));
        addressDTO.setUserID(rs.getLong("userID"));
        addressDTO.setCountry(rs.getString("country"));
        addressDTO.setCity(rs.getString("city"));
        addressDTO.setStreet(rs.getString("street"));
        addressDTO.setApartment(rs.getString("apartment"));
        return addressDTO;
    }   
}
