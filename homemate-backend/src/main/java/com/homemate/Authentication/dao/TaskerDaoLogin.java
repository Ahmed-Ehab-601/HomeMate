package com.homemate.Authentication.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.homemate.Authentication.Entity.Tasker;
import com.homemate.Authentication.rowmapper.TaskerRowMapper;

@Repository
public class TaskerDaoLogin {

    private final JdbcTemplate jdbcTemplate;

    public TaskerDaoLogin(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Tasker getTaskerByEmail(String email) {
        String sql = "SELECT * FROM tasker WHERE email = ?";
        return jdbcTemplate.queryForObject(sql, new TaskerRowMapper(), email);
    }
}
