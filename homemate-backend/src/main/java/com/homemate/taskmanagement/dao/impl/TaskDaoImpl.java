package com.homemate.taskmanagement.dao.impl;

import com.homemate.taskmanagement.dao.TaskCardRowMapper;
import com.homemate.taskmanagement.dao.TaskDao;
import com.homemate.taskmanagement.dao.TaskRowMapper;
import com.homemate.taskmanagement.dto.StatusDto;
import com.homemate.taskmanagement.dto.TaskCardDto;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.exceptions.BadTaskRequestException;
import com.homemate.taskmanagement.exceptions.DuplicateChatException;
import com.homemate.taskmanagement.model.Status;
import com.homemate.taskmanagement.model.TaskEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Component
public class TaskDaoImpl implements TaskDao {

    private final JdbcTemplate jdbcTemplate;
    private final TaskCardRowMapper taskCardRowMapper;
    private final TaskRowMapper taskRowMapper;


    public TaskDaoImpl(final JdbcTemplate jdbcTemplate, TaskCardRowMapper taskCardRowMapper, TaskRowMapper taskRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.taskCardRowMapper = taskCardRowMapper;
        this.taskRowMapper = taskRowMapper;
    }


    @Override
    public Optional<Long> insertTask(TaskEntity task) {
        String sql = "INSERT INTO Task (userID, taskerID, serviceID, addressId, startDate, description, chatID)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?) ";

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1,task.getUserID());
                ps.setLong(2,task.getTaskerID());
                ps.setLong(3,task.getServiceID());
                ps.setLong(4,task.getAddressID());
                ps.setTimestamp(5, Timestamp.valueOf(task.getStartDate()));
                ps.setString(6,task.getDescription());
                ps.setLong(7,task.getChatID());
                return ps;

            },keyHolder );

            Number key = keyHolder.getKey();
            return key != null ? Optional.of(key.longValue()) : Optional.empty();
        } catch (DataAccessException e) {
            throw new BadTaskRequestException();
        }

    }

    @Override
    public Optional<Long> getChat(Long userID, Long taskerId) {
        String sql = "SELECT chatID FROM Chat WHERE userID = ? and taskerID = ?";
        try{
           Long chatID =  jdbcTemplate.queryForObject(sql,Long.class,userID,taskerId);
           return Optional.ofNullable(chatID);

        }catch (EmptyResultDataAccessException ignored){
            return Optional.empty();

        } catch (DataAccessException e) {
            throw new BadTaskRequestException();
        }

    }

    @Override
    public Optional<Long> insertChat(Long userID, Long taskerId) {
        String sql = "INSERT INTO Chat (userID, taskerID) VALUES(?, ?) ";

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1,userID);
                ps.setLong(2,taskerId);
                return ps;
            },keyHolder );

            Number key = keyHolder.getKey();
            if (key == null){
                throw new BadTaskRequestException();
            }
            return Optional.of(key.longValue());
        } catch (DataAccessException e) {
            throw new DuplicateChatException();
        }

    }

    @Override
    public Optional<TaskDto> getTaskDetails(Long taskID) {
        String sql = """
            SELECT
                t.taskID,
                t.startDate,
                t.endDate,
                t.status,
                t.description,
                t.workedHours,
                t.startInProgress,
                t.bill,
                t.chatID,
                CONCAT(u.firstName, ' ', u.lastName) AS userName,
                CONCAT(tas.firstName, ' ', tas.lastName) AS taskerName,
                s.name AS serviceName,
                a.apartment,
                a.street,
                a.city,
                a.country,
                u.email AS userMail,
                tas.email AS taskerMail
            FROM Task t
                INNER JOIN Users u ON t.userID = u.userID
                INNER JOIN Tasker tas ON t.taskerID = tas.taskerID
                INNER JOIN Service s ON t.serviceID = s.serviceID
                INNER JOIN Address a ON t.addressID = a.addressID
            WHERE t.taskID = ?
            """;

        TaskDto taskDto = jdbcTemplate.queryForObject(sql,taskRowMapper,taskID);
        return Optional.ofNullable(taskDto);
    }

    @Override
    public boolean checkIfTaskExist(Long userID, Long taskerID,Long addressID) {
        String sql = "SELECT COUNT(*) FROM Task WHERE userID = ? and taskerID = ? and addressID = ? and status = 'InReview' ";
        Long count = jdbcTemplate.queryForObject(sql,Long.class,userID,taskerID,addressID);
        return count !=null && count == 1;
    }

    @Override
    public boolean checkIfTaskLimit(Long userID,Integer limit) {
        String sql = "SELECT COUNT(*) FROM Task WHERE userID = ? and status = 'InReview' ";
        Long count = jdbcTemplate.queryForObject(sql,Long.class,userID);
        return count !=null && count >= limit;
    }

    @Override
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

        return jdbcTemplate.query(sql,taskCardRowMapper,userID,pageSize,page * pageSize);
    }

    @Override
    public List<TaskCardDto> getListUserTasksByIDAndStatusSortedByDate(Long userID, StatusDto status, int page, int pageSize) {
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

        return jdbcTemplate.query(sql,taskCardRowMapper,userID, status.toString(),pageSize,page * pageSize);
    }

    @Override
    public Optional<Long> countTasksByUserID(Long userID) {
        String sql = "SELECT COUNT(*) FROM Task WHERE userID = ? ";
        Long count = jdbcTemplate.queryForObject(sql,Long.class,userID);
        return Optional.ofNullable(count);
    }

    @Override
    public Optional<Long> countTasksByUserIDAndStatus(Long userID, StatusDto status) {
        String sql = "SELECT COUNT(*) FROM Task WHERE userID = ? and status = ? ";
        Long count = jdbcTemplate.queryForObject(sql,Long.class,userID,status.toString());
        return Optional.ofNullable(count);
    }

    @Override
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

        return jdbcTemplate.query(sql,taskCardRowMapper,taskerID,pageSize,page * pageSize);
    }

    @Override
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

        return jdbcTemplate.query(sql,taskCardRowMapper,taskerID, status.toString(),pageSize,page * pageSize);
    }

    @Override
    public Optional<Long> countTasksByTaskerID(Long taskerID) {
        String sql = "SELECT COUNT(*) FROM Task WHERE taskerID = ? ";
        Long count = jdbcTemplate.queryForObject(sql,Long.class,taskerID);
        return Optional.ofNullable(count);
    }

    @Override
    public Optional<Long> countTasksByTaskerIDAndStatus(Long taskerID, StatusDto status) {
        String sql = "SELECT COUNT(*) FROM Task WHERE taskerID = ? and status = ? ";
        Long count = jdbcTemplate.queryForObject(sql,Long.class,taskerID,status.toString());
        return Optional.ofNullable(count);
    }
    @Override
    public Optional<Status> getStatus(Long taskID){
        String sql = "SELECT status FROM Task WHERE taskID = ? ";
        try {
            Status status = jdbcTemplate.queryForObject(sql, Status.class, taskID);
            return Optional.ofNullable(status);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    @Override
    public boolean updateStatus(Long taskID,Status newStatus){
        String sql = "UPDATE Task SET status = ? WHERE taskID = ?";
        int rowsAffected = jdbcTemplate.update(sql,newStatus.toString(),taskID);
        return rowsAffected > 0;
    }
    @Override
    public Optional<Long> getTaskerID(Long taskID){
        String sql = "SELECT taskerID FROM Task WHERE taskID = ? ";
        try {
            Long taskerID = jdbcTemplate.queryForObject(sql, Long.class, taskID);
            return Optional.ofNullable(taskerID);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

}
