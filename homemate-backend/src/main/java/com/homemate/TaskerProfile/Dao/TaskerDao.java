package com.homemate.TaskerProfile.Dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.homemate.TaskerProfile.mappers.TaskerRowMapper;
import com.homemate.TaskerProfile.models.Tasker;

@Repository
public class TaskerDao {
    private final JdbcTemplate jdbcTemplate;
    private final TaskerRowMapper taskerRowMapper;

    public TaskerDao(JdbcTemplate jdbcTemplate, TaskerRowMapper taskerRowMapper){
        this.jdbcTemplate = jdbcTemplate;
        this.taskerRowMapper = taskerRowMapper;
    }

    public Tasker getByID(Long ID){
        String sql =  "SELECT * FROM Tasker Where TaskerID = ?";
        return jdbcTemplate.queryForObject(sql, taskerRowMapper,ID);
    }

    public Tasker getByEmail(String email){
        return null;
    }
    
}
