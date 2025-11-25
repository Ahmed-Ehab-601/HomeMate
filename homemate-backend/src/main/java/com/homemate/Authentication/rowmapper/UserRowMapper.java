package com.homemate.Authentication.rowmapper;

import com.homemate.Authentication.Entity.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User u = new User();

        u.setUserID(rs.getInt("userID"));
        u.setFirstName(rs.getString("firstName"));
        u.setLastName(rs.getString("lastName"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setEmail(rs.getString("email"));

        // birthDate can be null
        var ts = rs.getTimestamp("birthDate");
        u.setBirthDate(ts != null ? ts.toLocalDateTime() : null);

        u.setGender(rs.getString("gender"));
        u.setPhone(rs.getString("phone"));
        u.setAdmin(rs.getBoolean("admin"));
        u.setSuspended(rs.getBoolean("suspended"));

        return u;
    }
}
