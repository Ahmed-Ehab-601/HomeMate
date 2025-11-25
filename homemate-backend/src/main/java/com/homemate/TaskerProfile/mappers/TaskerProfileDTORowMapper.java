package com.homemate.TaskerProfile.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.homemate.TaskerProfile.DTO.TaskerProfileDTO;

@Component
public class TaskerProfileDTORowMapper implements RowMapper<TaskerProfileDTO> {

    @Override
    public TaskerProfileDTO mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        TaskerProfileDTO tasker = new TaskerProfileDTO();
        tasker.setTaskerID(rs.getLong("taskerID"));
        tasker.setFirstName(rs.getString("firstName"));
        tasker.setLastName(rs.getString("lastName"));
        tasker.setUsername(rs.getString("username"));
        tasker.setEmail(rs.getString("email"));
        tasker.setBirthDate(rs.getTimestamp("birthDate"));
        tasker.setPhone(rs.getString("phone"));
        String gender = rs.getString("gender");
        tasker.setGender(gender != null && !gender.isEmpty() ? gender.charAt(0) : null);
        tasker.setAvailability(rs.getString("availability"));
        tasker.setRating(rs.getObject("rating") != null ? rs.getDouble("rating") : null);
        tasker.setHourrate(rs.getObject("hourrate") != null ? rs.getDouble("hourrate") : null);
        tasker.setBio(rs.getString("bio"));
        tasker.setServiceID(rs.getLong("serviceID"));
        tasker.setTotalEarning(rs.getObject("totalEarning") != null ? rs.getDouble("totalEarning") : null);
        tasker.setWorkedHours(rs.getObject("WorkedHours") != null ? rs.getDouble("WorkedHours") : null);
        tasker.setAddressCity(rs.getString("addressCity"));
        return tasker;
    }
}
