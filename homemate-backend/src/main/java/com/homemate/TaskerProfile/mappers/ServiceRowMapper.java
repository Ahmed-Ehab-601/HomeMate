package com.homemate.TaskerProfile.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.homemate.TaskerProfile.models.Services;

@Component
public class ServiceRowMapper implements RowMapper<Services> {

    @Override
    public Services mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        Services service = new Services();
        service.setServiceID(rs.getLong("serviceID"));
        service.setName(rs.getString("name"));
        service.setDescription(rs.getString("description"));
        service.setImagedata(rs.getBytes("imagedata"));
        service.setImageName(rs.getString("imageName"));
        service.setImageType(rs.getString("imageType"));
        return service;
    }
}
