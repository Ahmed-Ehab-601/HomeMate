package com.homemate.Authentication.rowmapper;

import com.homemate.Authentication.dto.LoginResponseDto;

import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginResponseDtoRowMapper implements RowMapper<LoginResponseDto> {

    String role;

    public LoginResponseDtoRowMapper(String role) {
        this.role = role;
    }

    @Override
    public LoginResponseDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        boolean isAdmin = false;

        // Check if the column exists using metadata (can be slightly slower)
        if (hasColumn(rs, "admin")) {
            isAdmin = rs.getBoolean("admin");
        }

        String determinedRole = isAdmin ? "ROLE_ADMIN" : role;

        return new LoginResponseDto(
                null,
                determinedRole,
                rs.getString("username"),
                rs.getString("firstname"),
                rs.getString("lastname")
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
