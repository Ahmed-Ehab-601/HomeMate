package com.homemate.Dao;
import java.sql.SQLException;
import java.util.List;

import com.homemate.Dto.ServiceDto;
import com.homemate.MapRow.ServiceMapRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.homemate.Interface.IServiceDao;
import com.homemate.Model.ServiceEntity;
@Component
 public class ServiceDaoImpl implements IServiceDao<ServiceEntity> {

    private final JdbcTemplate jdbcTemplate;
    public ServiceDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public void save(ServiceEntity service) throws SQLException {
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
    public void delete(long id) throws SQLException {
        try {
            String sql = "DELETE FROM service WHERE serviceID = ?";
            jdbcTemplate.update(
                    sql,
                    id
            );
        } catch (Exception e) {
            throw new UnsupportedOperationException("Unimplemented method 'delete'");
        }
    }
    @Override
    public void update(long id , ServiceEntity service) throws SQLException {
        try {
            ServiceEntity s = (ServiceEntity) service;

            String sql = "UPDATE service SET name = ? ,description = ?, imageData = ?, imageName = ?, imageType = ? WHERE serviceID = ?" ;

            jdbcTemplate.update(
                    sql,
                    s.getName(),
                    s.getDescription(),
                    s.getImageData(),
                    s.getImageName(),
                    s.getImageType(),
                    id
            );

        } catch (Exception e) {
            throw new UnsupportedOperationException("Unimplemented method 'save'");}
    }
 @Override
 public ServiceEntity get(long serviceID) throws SQLException {
    try {
        String sql= "SELECT serviceID, name, description, imageData, imageName, imageType FROM service WHERE serviceID = ?";
        return jdbcTemplate.queryForObject(sql, new ServiceMapRow(),serviceID);
    } catch (Exception e) {
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }
}

@Override
   public List<ServiceEntity> getAll() throws SQLException {
        String sql = "SELECT serviceID, name, description, imageData, imageName, imageType FROM service";
        try {
            return jdbcTemplate.query(sql, new ServiceMapRow());
        } catch (Exception e) {
            throw new SQLException("Failed to fetch services", e);
        }
    }
@Override
public int countCompletedTasksByServiceId(Long serviceID) throws Exception{
    String sql = "SELECT COUNT(*) FROM Task WHERE serviceID = ? AND status = 'done'";
    return jdbcTemplate.queryForObject(sql, Integer.class, serviceID);
    }
@Override
public int countTasker(long serviceID) throws Exception{
    String sql = "SELECT COUNT(*) FROM Tasker WHERE serviceID = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, serviceID);
            }

    public Long findIdByName(String name) throws Exception {
        String sql = "SELECT serviceID FROM service WHERE name = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, name);
    }

}
