package com.homemate.Authentication.dao;

import com.homemate.Authentication.Entity.User;
import com.homemate.Authentication.rowmapper.UserRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoLogin {

    private final JdbcTemplate jdbcTemplate;

    public UserDaoLogin(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return jdbcTemplate.queryForObject(sql, new UserRowMapper(), email);
    }
}
