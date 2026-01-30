package com.homemate.taskmanagement.dao;

import com.homemate.taskmanagement.dto.StatusDto;
import com.homemate.taskmanagement.dto.TaskCardDto;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
                    t.estimation,
                    t.paid,
                    c.userUnreadMessages,
                    c.taskerUnreadMessages,
                    CONCAT(u.firstName, ' ', u.lastName) AS userName,
                    CONCAT(tas.firstName, ' ', tas.lastName) AS taskerName,
                    s.name AS serviceName,
                    a.city
                FROM Task t
                    INNER JOIN Users u ON t.userID = u.userID
                    INNER JOIN Tasker tas ON t.taskerID = tas.taskerID
                    INNER JOIN Service s ON t.serviceID = s.serviceID
                    INNER JOIN Address a ON t.addressID = a.addressID
                    INNER JOIN Chat c ON t.chatID = c.chatID
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
                    t.estimation,
                    t.paid,
                    c.userUnreadMessages,
                    c.taskerUnreadMessages,
                    CONCAT(u.firstName, ' ', u.lastName) AS userName,
                    CONCAT(tas.firstName, ' ', tas.lastName) AS taskerName,
                    s.name AS serviceName,
                    a.city
                FROM Task t
                    INNER JOIN Users u ON t.userID = u.userID
                    INNER JOIN Tasker tas ON t.taskerID = tas.taskerID
                    INNER JOIN Service s ON t.serviceID = s.serviceID
                    INNER JOIN Address a ON t.addressID = a.addressID
                    INNER JOIN Chat c ON t.chatID = c.chatID
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
                    t.estimation,
                    t.paid,
                    c.taskerUnreadMessages,
                    c.userUnreadMessages,
                    CONCAT(u.firstName, ' ', u.lastName) AS userName,
                    CONCAT(tas.firstName, ' ', tas.lastName) AS taskerName,
                    s.name AS serviceName,
                    a.city
                FROM Task t
                    INNER JOIN Users u ON t.userID = u.userID
                    INNER JOIN Tasker tas ON t.taskerID = tas.taskerID
                    INNER JOIN Service s ON t.serviceID = s.serviceID
                    INNER JOIN Address a ON t.addressID = a.addressID
                    INNER JOIN Chat c ON t.chatID = c.chatID
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
                    t.paid,
                    t.estimation,
                    c.taskerUnreadMessages,
                    c.userUnreadMessages,
                    CONCAT(u.firstName, ' ', u.lastName) AS userName,
                    CONCAT(tas.firstName, ' ', tas.lastName) AS taskerName,
                    s.name AS serviceName,
                    a.city
                FROM Task t
                    INNER JOIN Users u ON t.userID = u.userID
                    INNER JOIN Tasker tas ON t.taskerID = tas.taskerID
                    INNER JOIN Service s ON t.serviceID = s.serviceID
                    INNER JOIN Address a ON t.addressID = a.addressID
                    INNER JOIN Chat c ON t.chatID = c.chatID
                WHERE tas.taskerID = ? and status = ?
                ORDER BY startDate DESC
                LIMIT ? OFFSET ?
                
                """;

        return jdbcTemplate.query(sql, taskCardRowMapper, taskerID, status.toString(), pageSize, page * pageSize);
    }
    public List<TaskCardDto> getUserTasksByDateRange(Long userID, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT
                    t.taskID,
                    t.startDate,
                    t.status,
                    t.estimation,
                    t.paid,
                    c.userUnreadMessages,
                    c.taskerUnreadMessages,
                    CONCAT(u.firstName, ' ', u.lastName) AS userName,
                    CONCAT(tas.firstName, ' ', tas.lastName) AS taskerName,
                    s.name AS serviceName,
                    a.city
                FROM Task t
                    INNER JOIN Users u ON t.userID = u.userID
                    INNER JOIN Tasker tas ON t.taskerID = tas.taskerID
                    INNER JOIN Service s ON t.serviceID = s.serviceID
                    INNER JOIN Address a ON t.addressID = a.addressID
                    INNER JOIN Chat c ON t.chatID = c.chatID
                WHERE u.userID = ?
                    AND DATE(t.startDate) >= ?
                    AND DATE(t.startDate) <= ?
                ORDER BY startDate ASC
                """;
        return jdbcTemplate.query(sql, taskCardRowMapper, userID, startDate, endDate);
    }
    public List<TaskCardDto> getUserTasksByDateRangeAndStatus(Long userID, LocalDate startDate,
                                                              LocalDate endDate, StatusDto status) {
        String sql = """
                SELECT
                    t.taskID,
                    t.startDate,
                    t.status,
                    t.paid,
                    t.estimation,
                    c.userUnreadMessages,
                    c.taskerUnreadMessages,
                    CONCAT(u.firstName, ' ', u.lastName) AS userName,
                    CONCAT(tas.firstName, ' ', tas.lastName) AS taskerName,
                    s.name AS serviceName,
                    a.city
                FROM Task t
                    INNER JOIN Users u ON t.userID = u.userID
                    INNER JOIN Tasker tas ON t.taskerID = tas.taskerID
                    INNER JOIN Service s ON t.serviceID = s.serviceID
                    INNER JOIN Address a ON t.addressID = a.addressID
                    INNER JOIN Chat c ON t.chatID = c.chatID

                WHERE u.userID = ?
                    AND DATE(t.startDate) >= ?
                    AND DATE(t.startDate) <= ?
                    AND t.status = ?
                ORDER BY startDate ASC
                """;
        return jdbcTemplate.query(sql, taskCardRowMapper, userID, startDate, endDate, status.toString());
    }

    public List<TaskCardDto> getTaskerTasksByDateRange(Long taskerID, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT
                    t.taskID,
                    t.startDate,
                    t.status,
                    t.estimation,
                    t.paid,
                    c.taskerUnreadMessages,
                    c.userUnreadMessages,
                    CONCAT(u.firstName, ' ', u.lastName) AS userName,
                    CONCAT(tas.firstName, ' ', tas.lastName) AS taskerName,
                    s.name AS serviceName,
                    a.city
                FROM Task t
                    INNER JOIN Users u ON t.userID = u.userID
                    INNER JOIN Tasker tas ON t.taskerID = tas.taskerID
                    INNER JOIN Service s ON t.serviceID = s.serviceID
                    INNER JOIN Address a ON t.addressID = a.addressID
                    INNER JOIN Chat c ON t.chatID = c.chatID

                WHERE tas.taskerID = ?
                    AND DATE(t.startDate) >= ?
                    AND DATE(t.startDate) <= ?
                ORDER BY startDate ASC
                """;
        return jdbcTemplate.query(sql, taskCardRowMapper, taskerID, startDate, endDate);
    }
    public List<TaskCardDto> getTaskerTasksByDateRangeAndStatus(Long taskerID, LocalDate startDate,
                                                                LocalDate endDate, StatusDto status) {
        String sql = """
                SELECT
                    t.taskID,
                    t.startDate,
                    t.status,
                    t.estimation,
                    t.paid,
                    c.taskerUnreadMessages,
                    c.userUnreadMessages,
                    CONCAT(u.firstName, ' ', u.lastName) AS userName,
                    CONCAT(tas.firstName, ' ', tas.lastName) AS taskerName,
                    s.name AS serviceName,
                    a.city
                FROM Task t
                    INNER JOIN Users u ON t.userID = u.userID
                    INNER JOIN Tasker tas ON t.taskerID = tas.taskerID
                    INNER JOIN Service s ON t.serviceID = s.serviceID
                    INNER JOIN Address a ON t.addressID = a.addressID
                    INNER JOIN Chat c ON t.chatID = c.chatID
                WHERE tas.taskerID = ?
                    AND DATE(t.startDate) >= ?
                    AND DATE(t.startDate) <= ?
                    AND t.status = ?
                ORDER BY startDate ASC
                """;
        return jdbcTemplate.query(sql, taskCardRowMapper, taskerID, startDate, endDate, status.toString());
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
