package com.homemate.UserProfile.Mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.homemate.UserProfile.DTO.UserRequestTaskerDTO;
import com.homemate.UserProfile.Models.UserTaskerAvailability;

@Component
public class UserRequestTaskerDTORowMapper implements RowMapper<UserRequestTaskerDTO> {

   @Override
    public UserRequestTaskerDTO mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        UserRequestTaskerDTO dto = new UserRequestTaskerDTO();
        dto.setTaskerId(rs.getLong("taskerID"));
        dto.setFirstName(rs.getString("firstName"));
        dto.setLastName(rs.getString("lastName"));
        dto.setUsername(rs.getString("username"));
        dto.setEmail(rs.getString("email"));
        dto.setPhone(rs.getString("phone"));
        dto.setRating(rs.getObject("rating") != null ? rs.getDouble("rating") : null);
        dto.setAvailability(
            rs.getString("availability") != null
                ? UserTaskerAvailability.valueOf(rs.getString("availability").toUpperCase())
                : null
            );
        dto.setHourRate(rs.getObject("hourRate") != null ? rs.getDouble("hourRate") : null);
        dto.setBio(rs.getString("bio"));
        dto.setAddressCity(rs.getString("addressCity"));
        dto.setWorkedHours(rs.getObject("WorkedHours") != null ? rs.getDouble("WorkedHours") : null);
        dto.setImage(rs.getBytes("image"));
        dto.setServiceName(rs.getString("serviceName"));
        return dto;
    }

}
