package com.homemate.taskmanagement.dao;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@AllArgsConstructor
public class TaskRescheduleDao {
    private JdbcTemplate jdbcTemplate;


    public Optional<Long> getUserID(Long taskID) {
        String sql = "SELECT userID FROM Task WHERE taskID = ? ";
        try {
            Long taskerID = jdbcTemplate.queryForObject(sql, Long.class, taskID);
            return Optional.ofNullable(taskerID);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();

        }
    }


    public boolean updateTaskStartDate(Long taskId, LocalDateTime newStartDate) {
        String sql = "UPDATE Task SET startDate = ? WHERE taskID = ?";
        int rows = jdbcTemplate.update(sql, Timestamp.valueOf(newStartDate), taskId);
        return rows > 0;
    }


    public Optional<LocalDateTime> getStartDate(Long taskID) {
        String sql = "SELECT startDate FROM Task WHERE taskID = ?";
        try {
            Timestamp timestamp = jdbcTemplate.queryForObject(sql, Timestamp.class, taskID);
            return Optional.ofNullable(timestamp != null ? timestamp.toLocalDateTime() : null);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();

        }
    }
}
