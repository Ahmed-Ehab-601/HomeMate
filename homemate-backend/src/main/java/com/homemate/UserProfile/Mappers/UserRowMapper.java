package com.homemate.UserProfile.Mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.homemate.UserProfile.Models.User;

@Component
public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setUserID(rs.getLong("userID"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setBirthDate(rs.getTimestamp("birthDate"));
        String gender = rs.getString("gender");
        user.setGender(gender != null && !gender.isEmpty() ? gender.charAt(0) : null);
        user.setPhone(rs.getString("phone"));
        user.setIsAdmin(rs.getObject("admin") != null ? rs.getBoolean("admin") : null);
        user.setIsSuspended(rs.getObject("suspended") != null ? rs.getBoolean("suspended") : null);
        user.setFirstName(rs.getString("firstName"));
        user.setLastName(rs.getString("lastName"));
        //user.setStripeCustomerId(rs.getString("stripe_customer_id"));
        return user;
    }
}

