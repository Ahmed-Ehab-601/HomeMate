package com.homemate.Dao;
import java.sql.SQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.homemate.Interface.IServiceDao;
import com.homemate.Model.ServiceEntity;
@Component
 public class ServiceDaoImpl implements IServiceDao<Object> {

    private final JdbcTemplate jdbcTemplate;
    public ServiceDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public void save(Object service) throws SQLException {
        try {
            ServiceEntity s = (ServiceEntity) service;

            String sql = "INSERT INTO service(name, description, imageData, imageName, imageType) " +
                         "VALUES (?, ?, ?, ?, ?)";

            jdbcTemplate.update(
                    sql,
                    s.getName(),
                    s.getDescription(),
                    s.getImageData(),
                    s.getImageName(),
                    s.getImageType()
            );

        } catch (Exception e) {
            throw new UnsupportedOperationException("Unimplemented method 'save'");}
    }


    @Override
    public void delete(Object service) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public Object update(Object service) throws SQLException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public Object get(Object service) throws SQLException {
    try {
        return null;
    } catch (Exception e) {
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }
}

    @Override
    public Iterable<Object> getAll() throws SQLException {
        try {
            return null;
        } catch (Exception e) {
        throw new UnsupportedOperationException("Unimplemented method 'getAll'");
    }
}

    
}
