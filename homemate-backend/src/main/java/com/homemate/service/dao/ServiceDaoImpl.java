package com.homemate.service.dao;

import java.sql.SQLException;
import java.util.List;

import com.homemate.service.mapRow.ServiceMapRow;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.homemate.service.Interface.IServiceDao;
import com.homemate.service.model.ServiceEntity;

@Component
public class ServiceDaoImpl implements IServiceDao<ServiceEntity> {

    private final JdbcTemplate jdbcTemplate;

    public ServiceDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(ServiceEntity service) throws SQLException {
        try {
            String sql = "INSERT INTO service(name, description, imageData, imageName, imageType) " +
                    "VALUES (?, ?, ?, ?, ?)";

            jdbcTemplate.update(
                    sql,
                    service.getName(),
                    service.getDescription(),
                    service.getImageData(),
                    service.getImageName(),
                    service.getImageType()
            );
        } catch (DataAccessException e) {
            throw new SQLException("Failed to create service: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(long id) throws SQLException {
        try {
            String sql = "DELETE FROM service WHERE serviceID = ?";
            int rowsAffected = jdbcTemplate.update(sql, id);

            if (rowsAffected == 0) {
                throw new SQLException("Service with ID " + id + " not found");
            }
        } catch (DataAccessException e) {
            if (e.getMessage() != null &&
                    (e.getMessage().contains("foreign key") ||
                            e.getMessage().contains("constraint"))) {
                throw new SQLException(
                        "Cannot delete service - it has related tasks or taskers", e);
            }
            throw new SQLException("Failed to delete service: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(long id, ServiceEntity service) throws SQLException {
        try {
            String sql = "UPDATE service SET name = ?, description = ?, " +
                    "imageData = ?, imageName = ?, imageType = ? WHERE serviceID = ?";

            int rowsAffected = jdbcTemplate.update(
                    sql,
                    service.getName(),
                    service.getDescription(),
                    service.getImageData(),
                    service.getImageName(),
                    service.getImageType(),
                    id
            );

            if (rowsAffected == 0) {
                throw new SQLException("Service with ID " + id + " not found");
            }
        } catch (DataAccessException e) {
            throw new SQLException("Failed to update service: " + e.getMessage(), e);
        }
    }

    @Override
    public ServiceEntity get(long serviceID) throws SQLException {
        try {
            String sql = "SELECT serviceID, name, description, imageData, imageName, imageType " +
                    "FROM service WHERE serviceID = ?";
            return jdbcTemplate.queryForObject(sql, new ServiceMapRow(), serviceID);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            throw new SQLException("Failed to fetch service: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ServiceEntity> getAll() throws SQLException {
        try {
            String sql = "SELECT serviceID, name, description, imageData, imageName, imageType " +
                    "FROM service";
            return jdbcTemplate.query(sql, new ServiceMapRow());
        } catch (DataAccessException e) {
            throw new SQLException("Failed to fetch services: " + e.getMessage(), e);
        }
    }

    @Override
    public int countCompletedTasksByServiceId(Long serviceID) throws Exception {
        try {
            String sql = "SELECT COUNT(*) FROM Task WHERE serviceID = ? AND status = 'done'";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, serviceID);
            return count != null ? count : 0;
        } catch (DataAccessException e) {
            throw new Exception("Failed to count completed tasks: " + e.getMessage(), e);
        }
    }

    @Override
    public int countTasker(long serviceID) throws Exception {
        try {
            String sql = "SELECT COUNT(*) FROM Tasker WHERE serviceID = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, serviceID);
            return count != null ? count : 0;
        } catch (DataAccessException e) {
            throw new Exception("Failed to count taskers: " + e.getMessage(), e);
        }
    }

    public Long findIdByName(String name) throws Exception {
        try {
            String sql = "SELECT serviceID FROM service WHERE name = ?";
            return jdbcTemplate.queryForObject(sql, Long.class, name);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            throw new Exception("Failed to find service by name: " + e.getMessage(), e);
        }
    }

    public ServiceEntity findById(long id) throws Exception {
        try {
            String sql = "SELECT serviceID, name, description, imageData, imageName, imageType " +
                    "FROM service WHERE serviceID = ?";
            return jdbcTemplate.queryForObject(sql, new ServiceMapRow(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            throw new Exception("Failed to find service by ID: " + e.getMessage(), e);
        }
    }

    public Integer isServiceInUse(long id) {
        try {
            String sql = "SELECT COUNT(*) FROM Task WHERE serviceID = ? AND status != 'done'";
            return jdbcTemplate.queryForObject(sql, Integer.class, id);
        } catch (DataAccessException e) {
            return null;
        }
    }
}