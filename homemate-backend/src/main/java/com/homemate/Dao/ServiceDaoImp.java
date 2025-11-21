package com.homemate.Dao;

import com.homemate.Model.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ServiceDaoImp implements ServiceDao {

    private final JdbcTemplate jdbcTemplate;
    private final ServiceRowMapper serviceRowMapper;

    @Autowired
    public ServiceDaoImp(JdbcTemplate jdbcTemplate, ServiceRowMapper serviceRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.serviceRowMapper = serviceRowMapper;
    }

    @Override
    public List<Service> getAllServices() {
        String sql = "SELECT serviceID, name, description, imagedata, imageName, imageType FROM Service";
        return jdbcTemplate.query(sql, serviceRowMapper);
    }

    @Override
    public int getTotalTasksForService(int serviceId) {
        String sql = "SELECT COUNT(*) FROM Task WHERE serviceID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, serviceId);
        return count;
    }
}
