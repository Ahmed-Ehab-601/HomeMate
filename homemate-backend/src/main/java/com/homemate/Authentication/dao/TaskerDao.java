package com.homemate.Authentication.dao;

import com.homemate.Authentication.Entity.Tasker;
import com.homemate.Authentication.rowmapper.TaskerRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TaskerDao {

    private final JdbcTemplate jdbcTemplate;

    public TaskerDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Tasker getTaskerByEmail(String email) {
        String sql = "SELECT * FROM tasker WHERE email = ?";
        return jdbcTemplate.queryForObject(sql, new TaskerRowMapper(), email);
    }
}
