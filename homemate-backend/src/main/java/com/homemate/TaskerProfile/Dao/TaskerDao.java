package com.homemate.TaskerProfile.Dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.homemate.TaskerProfile.DTO.TaskerProfileDTO;
import com.homemate.TaskerProfile.mappers.TaskerProfileDTORowMapper;
import com.homemate.TaskerProfile.mappers.TaskerRowMapper;
import com.homemate.TaskerProfile.models.Tasker;

@Repository
public class TaskerDao {
    private final JdbcTemplate jdbcTemplate;
    private final TaskerProfileDTORowMapper taskerProfileDTORowMapper;
    private final TaskerRowMapper taskerRowMapper;

    public TaskerDao(JdbcTemplate jdbcTemplate, TaskerProfileDTORowMapper taskerProfileDTORowMapper, TaskerRowMapper taskerRowMapper){
        this.jdbcTemplate = jdbcTemplate;
        this.taskerProfileDTORowMapper = taskerProfileDTORowMapper;
        this.taskerRowMapper = taskerRowMapper;
    }

    public Tasker getByID(Long ID){
        String sql =  "SELECT t.*, s.name AS serviceName FROM Tasker t LEFT JOIN Service s ON t.serviceID = s.serviceID WHERE t.taskerID = ?";
        return jdbcTemplate.queryForObject(sql, taskerRowMapper,ID);
    }

    public Tasker getByEmail(String email){
        String sql = "SELECT t.*, s.name AS serviceName FROM Tasker t LEFT JOIN Service s ON t.serviceID = s.serviceID WHERE t.email = ?";
        return jdbcTemplate.queryForObject(sql, taskerRowMapper, email);
    }

    public void update(Tasker tasker){
        String sql = "UPDATE Tasker SET firstName = ?, lastName = ?, username = ?, password = ?, email = ?, " +
                     "birthDate = ?, phone = ?, gender = ?, image = ?, availability = ?, rating = ?, " +
                     "hourrate = ?, bio = ?, serviceID = ?, totalEarning = ?, WorkedHours = ?, addressCity = ? " +
                     "WHERE taskerID = ?";
        jdbcTemplate.update(sql, 
            tasker.getFirstName(),
            tasker.getLastName(),
            tasker.getUsername(),
            tasker.getPassword(),
            tasker.getEmail(),
            tasker.getBirthDate(),
            tasker.getPhone(),
            tasker.getGender() != null ? tasker.getGender().toString() : null,
            tasker.getImage(),
            tasker.getAvailability(),
            tasker.getRating(),
            tasker.getHourrate(),
            tasker.getBio(),
            tasker.getServiceID(),
            tasker.getTotalEarning(),
            tasker.getWorkedHours(),
            tasker.getAddressCity(),
            tasker.getTaskerID()
        );
    }

    public void delete(Long id){
        String sql = "DELETE FROM Tasker WHERE taskerID = ?";
        jdbcTemplate.update(sql, id);
    }

    public TaskerProfileDTO getProfile(Long id){
        String sql = "SELECT * FROM Tasker WHERE taskerID = ?";
        return jdbcTemplate.queryForObject(sql, taskerProfileDTORowMapper, id);
    }
    
}
