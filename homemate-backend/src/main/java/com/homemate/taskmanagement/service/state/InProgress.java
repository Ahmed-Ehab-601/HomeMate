package com.homemate.taskmanagement.service.state;


import com.homemate.taskmanagement.dao.TaskStatusDao;
import com.homemate.taskmanagement.exceptions.BadStateUpdateException;
import com.homemate.taskmanagement.model.Status;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@AllArgsConstructor
public class InProgress implements TaskState{
    private Long taskID;
    TaskStatusDao taskDao;

    @Override
    public void updateWorkedHours() {
        LocalDateTime ldt = LocalDateTime.now(ZoneId.of("Africa/Cairo"));
        Timestamp timestamp = Timestamp.valueOf(ldt);
        taskDao.updateTaskStartInProgress(taskID, timestamp);

    }

    @Override
    public void updateStatus() {
        taskDao.updateStatus(taskID, Status.InProgress);
    }

    @Override
    public void changeContext(TaskContext newContext) {
        if(!(newContext.state instanceof Suspended ) && !(newContext.state instanceof Done)){
            throw new BadStateUpdateException("In Progress can be changed to in done or suspended only");
        }

    }
}
