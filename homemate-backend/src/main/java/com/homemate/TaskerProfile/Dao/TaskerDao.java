package com.homemate.TaskerProfile.Dao;

import java.sql.PreparedStatement;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.homemate.TaskerProfile.DTO.TaskerProfileDTO;
import com.homemate.TaskerProfile.DTO.TaskerSignupDTO;
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

    public Long saveTasker(TaskerSignupDTO dto) {

        String sql = """
                INSERT INTO Tasker 
                (firstName, lastName, username, password, email, birthDate, phone, gender, 
                 image, availability, rating, hourrate, bio, serviceID, totalEarning, WorkedHours, addressCity)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(con -> {
                PreparedStatement ps =
                        con.prepareStatement(sql, new String[]{"taskerID"});

                ps.setString(1, dto.getFirstName());
                ps.setString(2, dto.getLastName());
                ps.setString(3, dto.getUsername());
                ps.setString(4, dto.getPassword());
                ps.setString(5, dto.getEmail());
                ps.setTimestamp(6, dto.getDateOfBirth());
                ps.setString(7, dto.getPhoneNumber());
                ps.setString(8, "M");
                ps.setBytes(9, dto.getProfileImage());
                ps.setString(10, "AVAILABLE");
                ps.setDouble(11, 0.0);
                ps.setDouble(12, dto.getHourRate());
                ps.setString(13, dto.getBio());
                ps.setLong(14, dto.getServiceID());
                ps.setDouble(15, 0.0);
                ps.setDouble(16, 0.0);
                ps.setString(17, dto.getCity());
                return ps;
            }, keyHolder);

        } catch (DataIntegrityViolationException ex) {
            System.out.println(ex.getMessage());
            return -1L;
        }

        return keyHolder.getKey().longValue();
    }

    public void updatePassword(String email, String newPassword) {
        String sql = "UPDATE Tasker SET password = ? WHERE email = ?";
        jdbcTemplate.update(sql, newPassword, email);
    }
    
}
