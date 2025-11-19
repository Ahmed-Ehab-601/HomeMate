package com.homemate.UserProfile.Mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.homemate.UserProfile.DTO.UserProfileDTO;

@Component
public class UserProfileDTORowMapper implements RowMapper<UserProfileDTO> {

    @Override
    public UserProfileDTO mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUserId(rs.getLong("userID"));
        dto.setUsername(rs.getString("username"));
        dto.setFirstName(rs.getString("firstName"));
        dto.setLastName(rs.getString("lastName"));
        dto.setEmail(rs.getString("email"));
        dto.setBirthDate(rs.getTimestamp("birthDate"));
        String gender = rs.getString("gender");
        dto.setGender(gender != null && !gender.isEmpty() ? gender.charAt(0) : null);
        dto.setPhone(rs.getString("phone"));
        dto.setAdmin(rs.getObject("isAdmin") != null ? rs.getBoolean("isAdmin") : null);
        dto.setSuspended(rs.getObject("isSuspended") != null ? rs.getBoolean("isSuspended") : null);
        return dto;
    }
}

