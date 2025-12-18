package com.homemate.taskmanagement.dao;

import com.homemate.taskmanagement.dto.StatusDto;
import com.homemate.taskmanagement.dto.TaskCardDto;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class GetTasksDao {

    private final JdbcTemplate jdbcTemplate;
    private final TaskCardRowMapper taskCardRowMapper;

    public List<TaskCardDto> getListUserTasksByIDSortedByDate(Long userID, int page, int pageSize) {
        String sql = """
                SELECT
                    t.taskID,
                    t.startDate,
                    t.status,
                    CONCAT(u.firstName, ' ', u.lastName) AS userName,
                    CONCAT(tas.firstName, ' ', tas.lastName) AS taskerName,
                    s.name AS serviceName,
                    a.city
                FROM Task t
                    INNER JOIN Users u ON t.userID = u.userID
                    INNER JOIN Tasker tas ON t.taskerID = tas.taskerID
                    INNER JOIN Service s ON t.serviceID = s.serviceID
                    INNER JOIN Address a ON t.addressID = a.addressID
                WHERE u.userID = ?
                ORDER BY startDate DESC
                LIMIT ? OFFSET ?
                
                """;

        return jdbcTemplate.query(sql, taskCardRowMapper, userID, pageSize, page * pageSize);
    }


    public List<TaskCardDto> getListUserTasksByIDAndStatusSortedByDate(Long userID, StatusDto status,
                                                                       int page, int pageSize) {
        String sql = """
                SELECT
                    t.taskID,
                    t.startDate,
                    t.status,
                    CONCAT(u.firstName, ' ', u.lastName) AS userName,
                    CONCAT(tas.firstName, ' ', tas.lastName) AS taskerName,
                    s.name AS serviceName,
                    a.city
                FROM Task t
                    INNER JOIN Users u ON t.userID = u.userID
                    INNER JOIN Tasker tas ON t.taskerID = tas.taskerID
                    INNER JOIN Service s ON t.serviceID = s.serviceID
                    INNER JOIN Address a ON t.addressID = a.addressID
                WHERE u.userID = ? and status = ?
                ORDER BY startDate DESC
                LIMIT ? OFFSET ?
                
                """;

        return jdbcTemplate.query(sql, taskCardRowMapper, userID, status.toString(), pageSize, page * pageSize);
    }


    public Optional<Long> countTasksByUserID(Long userID) {
        String sql = "SELECT COUNT(*) FROM Task WHERE userID = ? ";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userID);
        return Optional.ofNullable(count);
    }


    public Optional<Long> countTasksByUserIDAndStatus(Long userID, StatusDto status) {
        String sql = "SELECT COUNT(*) FROM Task WHERE userID = ? and status = ? ";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userID, status.toString());
        return Optional.ofNullable(count);
    }


    public List<TaskCardDto> getListTaskerTasksByIDSortedByDate(Long taskerID, int page, int pageSize) {
        String sql = """
                SELECT
                    t.taskID,
                    t.startDate,
                    t.status,
                    CONCAT(u.firstName, ' ', u.lastName) AS userName,
                    CONCAT(tas.firstName, ' ', tas.lastName) AS taskerName,
                    s.name AS serviceName,
                    a.city
                FROM Task t
                    INNER JOIN Users u ON t.userID = u.userID
                    INNER JOIN Tasker tas ON t.taskerID = tas.taskerID
                    INNER JOIN Service s ON t.serviceID = s.serviceID
                    INNER JOIN Address a ON t.addressID = a.addressID
                WHERE tas.taskerID = ?
                ORDER BY startDate DESC
                LIMIT ? OFFSET ?
                
                """;

        return jdbcTemplate.query(sql, taskCardRowMapper, taskerID, pageSize, page * pageSize);
    }


    public List<TaskCardDto> getListTaskerTasksByIDAndStatusSortedByDate(Long taskerID, StatusDto status, int page, int pageSize) {
        String sql = """
                SELECT
                    t.taskID,
                    t.startDate,
                    t.status,
                    CONCAT(u.firstName, ' ', u.lastName) AS userName,
                    CONCAT(tas.firstName, ' ', tas.lastName) AS taskerName,
                    s.name AS serviceName,
                    a.city
                FROM Task t
                    INNER JOIN Users u ON t.userID = u.userID
                    INNER JOIN Tasker tas ON t.taskerID = tas.taskerID
                    INNER JOIN Service s ON t.serviceID = s.serviceID
                    INNER JOIN Address a ON t.addressID = a.addressID
                WHERE tas.taskerID = ? and status = ?
                ORDER BY startDate DESC
                LIMIT ? OFFSET ?
                
                """;

        return jdbcTemplate.query(sql, taskCardRowMapper, taskerID, status.toString(), pageSize, page * pageSize);
    }


    public Optional<Long> countTasksByTaskerID(Long taskerID) {
        String sql = "SELECT COUNT(*) FROM Task WHERE taskerID = ? ";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, taskerID);
        return Optional.ofNullable(count);
    }


    public Optional<Long> countTasksByTaskerIDAndStatus(Long taskerID, StatusDto status) {
        String sql = "SELECT COUNT(*) FROM Task WHERE taskerID = ? and status = ? ";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, taskerID, status.toString());
        return Optional.ofNullable(count);
    }

}
