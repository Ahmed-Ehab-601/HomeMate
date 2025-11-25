package com.homemate.UserProfile.Mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.homemate.UserProfile.DTO.UserRequestTaskerDTO;

@Component
public class UserRequestTaskerDTORowMapper implements RowMapper<UserRequestTaskerDTO> {

    @Override
    public UserRequestTaskerDTO mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        UserRequestTaskerDTO dto = new UserRequestTaskerDTO();
        dto.setTaskerId(rs.getLong("taskerID"));
        dto.setFirstName(rs.getString("firstName"));
        dto.setLastName(rs.getString("lastName"));
        dto.setEmail(rs.getString("email"));
        dto.setPhone(rs.getString("phone"));
        dto.setRating(rs.getObject("rating") != null ? rs.getDouble("rating") : null);
        return dto;
    }
}

