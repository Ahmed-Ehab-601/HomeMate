package com.homemate.taskmanagement.service;

import com.homemate.taskmanagement.dao.TaskDao;
import com.homemate.taskmanagement.exceptions.BadStateUpdateException;
import com.homemate.taskmanagement.model.Status;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@AllArgsConstructor
public class Done implements TaskState{
    private Long taskID;
    private Long taskerID;
    TaskDao taskDao;

    @Override
    public void sendEmail() {
        System.out.println("send done mail");
    }

    @Override
    public void updateWorkedHours() {
        Timestamp startInProgress = taskDao.getStartInProgress(taskID);
        if(startInProgress != null){
            Duration duration = Duration.between(startInProgress.toInstant(), Instant.now());
            double workedHours = duration.toMinutes() / 60.0 ;
            taskDao.updateTaskWorkedHours(taskID,workedHours);
            taskDao.updateTaskStartInProgress(taskID,null);
        }

        Double hourRate = taskDao.getTaskerHourRate(taskerID);
        Double workedHours = taskDao.getTaskWorkedHours(taskID);
        taskDao.updateTaskerWorkedHours(taskerID,workedHours);

        double bill = hourRate * workedHours;
        taskDao.updateTaskBill(taskID,bill);
        taskDao.updateTaskerTotalEarning(taskerID,bill);

        taskDao.updateTaskEndData(taskID,Timestamp.valueOf(LocalDateTime.now()));


    }

    @Override
    public void updateStatus() {
        taskDao.updateStatus(taskID,Status.Done);
    }

    @Override
    public void changeContext(TaskContext newContext) {
        throw new BadStateUpdateException("can't change done status");

    }
}
