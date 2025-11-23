package com.homemate.TaskerProfile.Dao;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.homemate.TaskerProfile.mappers.ServiceRowMapper;
import com.homemate.TaskerProfile.models.Services;

@Repository
public class ServiceDao {
    private final JdbcTemplate jdbcTemplate;
    private final ServiceRowMapper serviceRowMapper;

    public ServiceDao(JdbcTemplate jdbcTemplate, ServiceRowMapper serviceRowMapper){
        this.jdbcTemplate = jdbcTemplate;
        this.serviceRowMapper = serviceRowMapper;
    }

    public List<Services> getAll(){
        String sql = "SELECT * FROM Service";
        return jdbcTemplate.query(sql, serviceRowMapper);
    }
}

