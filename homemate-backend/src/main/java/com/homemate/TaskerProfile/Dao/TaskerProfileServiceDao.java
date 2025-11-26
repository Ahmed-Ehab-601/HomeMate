package com.homemate.TaskerProfile.Dao;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.homemate.TaskerProfile.mappers.TaskerProfileServiceRowMapper;
import com.homemate.TaskerProfile.models.Services;

@Repository("taskerProfileServiceDao")
public class TaskerProfileServiceDao {
    private final JdbcTemplate jdbcTemplate;
    private final TaskerProfileServiceRowMapper serviceRowMapper;

    public TaskerProfileServiceDao(JdbcTemplate jdbcTemplate, TaskerProfileServiceRowMapper serviceRowMapper){
        this.jdbcTemplate = jdbcTemplate;
        this.serviceRowMapper = serviceRowMapper;
    }

    public List<Services> getAll(){
        String sql = "SELECT * FROM Service";
        return jdbcTemplate.query(sql, serviceRowMapper);
    }
}

