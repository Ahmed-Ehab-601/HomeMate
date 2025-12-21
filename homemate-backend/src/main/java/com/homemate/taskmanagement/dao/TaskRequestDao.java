package com.homemate.taskmanagement.dao;

import com.homemate.TaskerProfile.models.TaskerAvailability;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.exceptions.BadTaskRequestException;
import com.homemate.taskmanagement.exceptions.DuplicateChatException;
import com.homemate.taskmanagement.mappers.TaskerBusyTimeMapper;
import com.homemate.taskmanagement.model.TaskEntity;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

@Component
@AllArgsConstructor
public class TaskRequestDao {


    private final JdbcTemplate jdbcTemplate;
    private final TaskRowMapper taskRowMapper;

    private final TaskerBusyTimeMapper taskerBusyTimeMapper;
    public Optional<Long> insertTask(TaskEntity task) {
        String sql = "INSERT INTO Task (userID, taskerID, serviceID, addressId, startDate, description, chatID)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?) ";

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, task.getUserID());
                ps.setLong(2, task.getTaskerID());
                ps.setLong(3, task.getServiceID());
                ps.setLong(4, task.getAddressID());
                ps.setTimestamp(5, Timestamp.valueOf(task.getStartDate()));
                ps.setString(6, task.getDescription());
                ps.setLong(7, task.getChatID());
                return ps;

            }, keyHolder);

            Number key = keyHolder.getKey();
            return key != null ? Optional.of(key.longValue()) : Optional.empty();

        } catch (DataAccessException e) {
            throw new BadTaskRequestException();

        }

    }


    public Optional<Long> getChat(Long userID, Long taskerId) {
        String sql = "SELECT chatID FROM Chat WHERE userID = ? and taskerID = ?";
        try {
            Long chatID = jdbcTemplate.queryForObject(sql, Long.class, userID, taskerId);
            return Optional.ofNullable(chatID);

        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();

        } catch (DataAccessException e) {
            throw new BadTaskRequestException();
        }

    }


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

    public Optional<TaskDto> getTaskDetails(Long taskID) {
        String sql = """
            SELECT
                t.taskID,
                t.taskerID,
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
                tas.email AS taskerMail,tas.hourRate
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


    public boolean checkIfTaskExist(Long userID, Long taskerID,Long addressID) {
        String sql = "SELECT COUNT(*) FROM Task WHERE userID = ? and taskerID = ? and addressID = ? and status = 'InReview' ";
        Long count = jdbcTemplate.queryForObject(sql,Long.class,userID,taskerID,addressID);
        return count !=null && count == 1;
    }


    public boolean checkIfTaskLimit(Long userID,Integer limit) {
        String sql = "SELECT COUNT(*) FROM Task WHERE userID = ? and status = 'InReview' ";
        Long count = jdbcTemplate.queryForObject(sql,Long.class,userID);
        return count !=null && count >= limit;
    }

    public Map<LocalDateTime, Integer> getBusytime(Long taskerId, LocalDate day) {
        LocalDateTime startOfDay = day.atStartOfDay();
        LocalDateTime endOfDay = day.plusDays(1).atStartOfDay();
        String sql = "SELECT  startDate , estimation FROM Task WHERE taskerID = ? AND startDate >= ? AND startDate < ? ";
        return jdbcTemplate.query(sql,taskerBusyTimeMapper,
                taskerId,
                Timestamp.valueOf(startOfDay),
                Timestamp.valueOf(endOfDay));
    }

    public void add(Long taskId, int estimation) {
        String sql = "UPDATE Task " +
                "SET estimation = ? " +
                "WHERE taskID = ?";
        jdbcTemplate.update(sql,estimation,taskId);
    }

    public TaskerAvailability CheckAvailability(Long taskerId) {
        String sql = "SELECT availability FROM Tasker WHERE taskerID = ?";
        String availabilityStr = jdbcTemplate.queryForObject(sql, String.class, taskerId);

        if (availabilityStr == null) {
            return null;
        }
        return TaskerAvailability.valueOf(availabilityStr.toUpperCase());
    }
    //CREATE TABLE Task (
    //    taskID INT AUTO_INCREMENT PRIMARY KEY,
    //    startDate TIMESTAMP NOT NULL,
    //    workedHours FLOAT DEFAULT 0,
    //    userID INT NOT NULL,
    //    taskerID INT NOT NULL,
    //    serviceID INT NOT NULL,
    //    endDate TIMESTAMP NULL,
    //    chatID INT,
    //    bill FLOAT DEFAULT 0,
    //    status ENUM('InReview','Accepted','InProgress','Suspended','Done','Rejected') DEFAULT 'InReview' NOT NULL,
    //    startInProgress TIMESTAMP NULL,
    //    addressID INT NOT NULL,
    //    description VARCHAR(500),
    //    FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE RESTRICT ON UPDATE CASCADE,
    //    FOREIGN KEY (taskerID) REFERENCES Tasker(taskerID) ON DELETE RESTRICT ON UPDATE CASCADE,
    //    FOREIGN KEY (serviceID) REFERENCES Service(serviceID) ON DELETE RESTRICT ON UPDATE CASCADE,
    //    FOREIGN KEY (chatID) REFERENCES Chat(chatID) ON DELETE SET NULL ON UPDATE CASCADE,
    //    FOREIGN KEY (addressID) REFERENCES Address(addressID) ON DELETE RESTRICT ON UPDATE CASCADE,
    //    INDEX idx_task_user (userID),
    //    INDEX idx_task_tasker (taskerID),
    //    INDEX idx_task_service (serviceID),
    //    INDEX idx_task_status (status),
    //    INDEX idx_task_start_date (startDate),
    //    INDEX idx_task_finish_date (endDate),
    //    INDEX idx_task_chat (chatID)
    //);
}
