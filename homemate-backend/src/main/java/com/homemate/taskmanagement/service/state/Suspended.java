package com.homemate.taskmanagement.service.state;

import com.homemate.taskmanagement.dao.TaskStatusDao;
import com.homemate.taskmanagement.exceptions.BadStateUpdateException;
import com.homemate.taskmanagement.model.Status;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

@AllArgsConstructor
public class Suspended implements TaskState{
    private Long taskID;
    private TaskStatusDao taskDao;


    @Override
    public void updateWorkedHours() {
        Timestamp startInProgress = taskDao.getStartInProgress(taskID);
        Duration duration = Duration.between(startInProgress.toInstant(), Instant.now());
        double workedHours = duration.toSeconds() / (60.0 * 60.0) ;
        taskDao.updateTaskWorkedHours(taskID,workedHours);
        taskDao.updateTaskStartInProgress(taskID,null);
    }

    @Override
    public void updateStatus() {
        taskDao.updateStatus(taskID, Status.Suspended);
    }

    @Override
    public void changeContext(TaskContext newContext) {
        if(!(newContext.state instanceof InProgress ) && !(newContext.state instanceof Done)){
            throw new BadStateUpdateException("Suspended can be changed to Done or InProgress only");
        }
    }
}
