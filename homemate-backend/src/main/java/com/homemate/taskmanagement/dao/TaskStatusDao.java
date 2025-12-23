package com.homemate.taskmanagement.dao;

import com.homemate.taskmanagement.exceptions.BadStateUpdateException;
import com.homemate.taskmanagement.exceptions.TaskNotFoundException;
import com.homemate.taskmanagement.model.Status;
import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Optional;

@Component
@AllArgsConstructor
public class TaskStatusDao {

    private JdbcTemplate jdbcTemplate;

    public Optional<Status> getStatus(Long taskID){
        String sql = "SELECT status FROM Task WHERE taskID = ? ";
        try {
            Status status = jdbcTemplate.queryForObject(sql, Status.class, taskID);
            return Optional.ofNullable(status);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();

        }
    }

    public boolean updateStatus(Long taskID,Status newStatus){
        String sql = "UPDATE Task SET status = ? WHERE taskID = ?";
        int rowsAffected = jdbcTemplate.update(sql,newStatus.toString(),taskID);
        return rowsAffected > 0;
    }

    public Optional<Long> getTaskerID(Long taskID){
        String sql = "SELECT taskerID FROM Task WHERE taskID = ? ";
        try {
            Long taskerID = jdbcTemplate.queryForObject(sql, Long.class, taskID);
            return Optional.ofNullable(taskerID);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();

        }
    }

    public boolean updateTaskWorkedHours(Long taskID,double workedHours){
        String sql = "UPDATE Task SET workedHours = workedHours + ? WHERE taskID = ?";
        int rowsAffected = jdbcTemplate.update(sql,workedHours,taskID);
        return rowsAffected > 0;
    }

    public boolean updateTaskStartInProgress(Long taskID, Timestamp time){
        String sql = "UPDATE Task SET startInProgress =  ? WHERE taskID = ?";
        int rowsAffected = jdbcTemplate.update(sql,time,taskID);
        return rowsAffected > 0;
    }

    public boolean updateTaskEndData(Long taskID, Timestamp time){
        String sql = "UPDATE Task SET endDate =  ? WHERE taskID = ?";
        int rowsAffected = jdbcTemplate.update(sql,time,taskID);
        return rowsAffected > 0;
    }

    public boolean updateTaskBill(Long taskID, double bill){
        String sql = "UPDATE Task SET bill =  ? WHERE taskID = ?";
        int rowsAffected = jdbcTemplate.update(sql,bill,taskID);
        return rowsAffected > 0;
    }


    public Timestamp getStartInProgress(Long taskID){
        String sql = "SELECT startInProgress FROM Task WHERE taskID = ? ";
        try {
            return jdbcTemplate.queryForObject(sql, Timestamp.class, taskID);

        } catch (EmptyResultDataAccessException e) {
            throw new TaskNotFoundException("id not correct");

        }
    }


    public Double getTaskWorkedHours(Long taskID){
        String sql = "SELECT workedHours FROM Task WHERE taskID = ? ";
        try {
            return jdbcTemplate.queryForObject(sql, Double.class, taskID);

        } catch (EmptyResultDataAccessException e) {
            throw new TaskNotFoundException("id not correct");

        }
    }


    public boolean updateTaskerWorkedHours(Long taskerID,double workedHours){
        String sql = "UPDATE Tasker SET WorkedHours = WorkedHours + ? WHERE taskerID = ?";
        int rowsAffected = jdbcTemplate.update(sql,workedHours,taskerID);
        return rowsAffected > 0;
    }

    public Double getTaskerHourRate(Long taskerID){
        String sql = "SELECT hourRate FROM Tasker WHERE taskerID = ?";
        try {
            return jdbcTemplate.queryForObject(sql,Double.class , taskerID);

        } catch (EmptyResultDataAccessException e) {
            throw new BadStateUpdateException("tasker Id  not correct");

        }
    }


    public boolean updateTaskerTotalEarning(Long taskerID,double bill){
        String sql = "UPDATE Tasker SET totalEarning = totalEarning + ? WHERE taskerID = ?";
        int rowsAffected = jdbcTemplate.update(sql,bill,taskerID);
        return rowsAffected > 0;
    }

    public double getTaskBill(Long taskID){
        String sql = "SELECT bill FROM Task WHERE taskID = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Double.class, taskID);

        } catch (EmptyResultDataAccessException e) {
            throw new TaskNotFoundException("id not correct");
        }
    }

    public void setTaskAsPaid(Long taskID) {
        String sql = "UPDATE Task SET paid = TRUE WHERE taskID = ?";
        int rowsAffected = jdbcTemplate.update(sql, taskID);

        if (rowsAffected == 0) {
            throw new TaskNotFoundException("Task not found with id: " + taskID);
        }
    }






}
