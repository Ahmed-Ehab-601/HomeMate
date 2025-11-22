package com.homemate.Authentication.rowmapper;

import com.homemate.Authentication.Entity.Tasker;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TaskerRowMapper implements RowMapper<Tasker> {

    @Override
    public Tasker mapRow(ResultSet rs, int rowNum) throws SQLException {
        Tasker t = new Tasker();

        t.setTaskerID(rs.getInt("taskerID"));
        t.setFirstName(rs.getString("firstName"));
        t.setLastName(rs.getString("lastName"));
        t.setUsername(rs.getString("username"));
        t.setPassword(rs.getString("password"));
        t.setEmail(rs.getString("email"));

        var ts = rs.getTimestamp("birthDate");
        t.setBirthDate(ts != null ? ts.toLocalDateTime() : null);

        t.setPhone(rs.getString("phone"));
        t.setGender(rs.getString("gender"));
        t.setImage(rs.getBytes("image"));

        t.setAvailability(rs.getString("availability"));
        t.setRating(rs.getDouble("rating"));
        t.setHourRate(rs.getDouble("hourrate"));
        t.setBio(rs.getString("bio"));
        t.setServiceID(rs.getInt("serviceID"));
        t.setTotalEarning(rs.getDouble("totalEarning"));
        t.setWorkedHours(rs.getDouble("WorkedHours")); // note capital W in DB
        t.setAddressCity(rs.getString("addressCity"));

        return t;
    }
}

