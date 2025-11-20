package com.homemate.Authentication;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

// will need to split this class into user and tasker dao's
@Repository
public class UserDetailsDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDetailsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AppUserDetails findUserDetailsById(Long id) {
        String sql = "SELECT id, username, email, password, admin FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new AppUserDetailsRowMapper("ROLE_USER"), id);
    }

    public AppUserDetails findTakserDetailsById(Long id) {
        String sql = "SELECT id, username, email, password FROM tasker WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new AppUserDetailsRowMapper("ROLE_TASKER"), id);
    }
}
