package com.homemate.taskerdiscovery.dao;

import com.homemate.taskerdiscovery.model.Service;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component("serviceRowMapper")
public class ServiceRowMapper implements RowMapper<Service> {

    @Override
    public Service mapRow(ResultSet rs, int rowNum) throws SQLException {
        Service service = new Service();

        service.setServiceId(rs.getInt("serviceID"));
        service.setServiceName(rs.getString("name"));
        service.setDescription(rs.getString("description"));
//        service.setImageData(rs.getBytes("imagedata"));
        service.setImageName(rs.getString("imageName"));
        service.setImageType(rs.getString("imageType"));

        return service;
    }

}