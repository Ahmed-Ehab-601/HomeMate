package com.homemate.Authentication;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AppUserDetailsRowMapper implements RowMapper<AppUserDetails> {

    String role;

    AppUserDetailsRowMapper(String role) {
        this.role = role;
    }

    @Override
    public AppUserDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
        boolean isAdmin = false;

        // Check if the column exists using metadata (can be slightly slower)
        if (hasColumn(rs, "admin")) {
            isAdmin = rs.getBoolean("admin");
        }

        String determinedRole = isAdmin ? "ROLE_ADMIN" : role;

        return new AppUserDetails(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("password"),
                determinedRole
        );
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
