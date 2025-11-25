package com.homemate.UserProfile.Mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.homemate.UserProfile.Models.Address;

@Component
public class AddressRowMapper implements RowMapper<Address> {
    @Override
    public Address mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        Address address = new Address();
        address.setAddressId(rs.getLong("addressID"));
        address.setUserId(rs.getLong("userID"));
        address.setCountry(rs.getString("country"));
        address.setCity(rs.getString("city"));
        address.setStreet(rs.getString("street"));
        address.setApartment(rs.getString("apartment"));
        return address;
    }
}
