package com.homemate.taskmanagement.service.state;

import com.homemate.taskmanagement.dao.TaskStatusDao;
import com.homemate.taskmanagement.exceptions.BadStateUpdateException;
import com.homemate.taskmanagement.model.Status;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@AllArgsConstructor
public class Done implements TaskState{
    private Long taskID;
    private Long taskerID;
    private TaskStatusDao taskDao;



    @Override
    public void updateWorkedHours() {
        Timestamp startInProgress = taskDao.getStartInProgress(taskID);
        ZoneId zoneId = ZoneId.of("Africa/Cairo");
        if(startInProgress != null){
            LocalDateTime start = LocalDateTime.ofInstant(startInProgress.toInstant(), zoneId);
            LocalDateTime now = LocalDateTime.now(zoneId);
            Duration duration = Duration.between(start, now);
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

        taskDao.updateTaskEndData(taskID,Timestamp.valueOf(LocalDateTime.now(zoneId)));


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
