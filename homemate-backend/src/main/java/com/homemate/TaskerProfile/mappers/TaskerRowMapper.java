package com.homemate.TaskerProfile.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.homemate.TaskerProfile.models.Tasker;

@Component
public class TaskerRowMapper implements RowMapper<Tasker> {

    @Override
    public Tasker mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        Tasker tasker = new Tasker();
        tasker.setTaskerID(rs.getLong("taskerID"));
        tasker.setFirstName(rs.getString("firstName"));
        tasker.setLastName(rs.getString("lastName"));
        tasker.setUsername(rs.getString("username"));
        tasker.setPassword(rs.getString("password"));
        tasker.setEmail(rs.getString("email"));
        tasker.setBirthDate(rs.getTimestamp("birthDate"));
        tasker.setPhone(rs.getString("phone"));
        String gender = rs.getString("gender");
        tasker.setGender(gender != null && !gender.isEmpty() ? gender.charAt(0) : null);
        tasker.setImage(rs.getString("image"));
        tasker.setAvailability(rs.getString("availability"));
        tasker.setRating(rs.getObject("rating") != null ? rs.getDouble("rating") : null);
        tasker.setHourrate(rs.getObject("hourrate") != null ? rs.getDouble("hourrate") : null);
        tasker.setBio(rs.getString("bio"));
        tasker.setServiceID(rs.getLong("serviceID"));
        tasker.setServiceName(rs.getString("serviceName"));
        tasker.setTotalEarning(rs.getObject("totalEarning") != null ? rs.getDouble("totalEarning") : null);
        tasker.setWorkedHours(rs.getObject("WorkedHours") != null ? rs.getDouble("WorkedHours") : null);
        tasker.setAddressCity(rs.getString("addressCity"));
        tasker.setIsSuspended(rs.getBoolean("suspended"));
        return tasker;
    }
}
