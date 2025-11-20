package com.homemate.TaskManagement.Dao.impl;

import com.homemate.TaskManagement.Dao.TaskDao;
import com.homemate.TaskManagement.Dao.TaskRowMapper;
import com.homemate.TaskManagement.Dto.TaskDto;
import com.homemate.TaskManagement.exceptions.BadTaskRequestException;
import com.homemate.TaskManagement.exceptions.DuplicateChatException;
import com.homemate.TaskManagement.model.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

@Component
public class TaskDaoImpl implements TaskDao {

    private final JdbcTemplate jdbcTemplate;


    public TaskDaoImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

        TaskDto taskDto = jdbcTemplate.queryForObject(sql,new TaskRowMapper(),taskID);
        return Optional.ofNullable(taskDto);
    }

    @Override
    public boolean checkIfTaskExist(Long userID, Long taskerID,Long addressID) {
        String sql = "SELECT COUNT(*) FROM Task WHERE userID = ? and taskerID = ? and addressID = ? and status = 'InReview' ";
        Integer count = jdbcTemplate.queryForObject(sql,Integer.class,userID,taskerID,addressID);
        return count !=null && count == 1;
    }

    @Override
    public boolean checkIfTaskLimit(Long userID,Integer limit) {
        String sql = "SELECT COUNT(*) FROM Task WHERE userID = ? and status = 'InReview' ";
        Integer count = jdbcTemplate.queryForObject(sql,Integer.class,userID);
        return count !=null && count >= limit;
    }

}
