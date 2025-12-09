package com.homemate.taskmanagement.service;

import com.homemate.taskmanagement.dao.TaskDao;
import com.homemate.taskmanagement.exceptions.BadStateUpdateException;
import com.homemate.taskmanagement.model.Status;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StatusFactory {
    private TaskDao taskDao;

    public TaskState createTaskState(Status status,Long taskID,Long taskerID){
        return switch (status) {
            case Status.Accepted -> new Accepted();
            case Status.Suspended  -> new Suspended(taskID,taskDao);
            case Status.InProgress -> new InProgress(taskID,taskDao);
            case Status.Done ->  new Done(taskID,taskerID,taskDao);
            default -> throw new BadStateUpdateException("can't update state of inReview or Rejected task");
        };
    }
}
