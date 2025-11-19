 package com.homemate.MapRow;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.homemate.Model.ServiceEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

 @Component

 public class ServiceMapRow implements RowMapper<ServiceEntity> {
  @Override
  public ServiceEntity mapRow(ResultSet rs,int rowNum) throws SQLException {
    ServiceEntity service = new ServiceEntity();

    service.setId(rs.getLong("serviceID"));
    service.setName(rs.getString("name"));
    service.setDescription(rs.getString("description"));
    service.setImageData(rs.getBytes("imageData"));
    service.setImageName(rs.getString("imageName"));
    service.setImageType(rs.getString("imageType"));

    return service;
  }

 }