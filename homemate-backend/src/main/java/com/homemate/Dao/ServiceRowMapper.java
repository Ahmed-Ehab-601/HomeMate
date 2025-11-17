package com.homemate.Dao;

import com.homemate.Model.Service;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class ServiceRowMapper implements RowMapper<Service> {

    @Override
    public Service mapRow(ResultSet rs, int rowNum) throws SQLException {
        Service service = new Service();

        service.setServiceid(rs.getInt("serviceID"));
        service.setServicename(rs.getString("name"));
        service.setDescription(rs.getString("description"));

        return service;
    }

}
