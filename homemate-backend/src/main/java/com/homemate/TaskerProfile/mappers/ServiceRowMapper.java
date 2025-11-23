package com.homemate.TaskerProfile.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.homemate.TaskerProfile.models.Service;

@Component
public class ServiceRowMapper implements RowMapper<Service> {

    @Override
    public Service mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        Service service = new Service();
        service.setServiceID(rs.getLong("serviceID"));
        service.setName(rs.getString("name"));
        service.setDescription(rs.getString("description"));
        service.setImagedata(rs.getBytes("imagedata"));
        service.setImageName(rs.getString("imageName"));
        service.setImageType(rs.getString("imageType"));
        return service;
    }
}
