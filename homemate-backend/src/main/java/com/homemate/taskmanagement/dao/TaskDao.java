package com.homemate.taskmanagement.dao;

import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.model.TaskEntity;

import java.util.Optional;

public interface TaskDao {
    Optional<Long> insertTask(TaskEntity task);
    Optional<Long> getChat (Long userID,Long TaskerId);
    Optional<Long> insertChat(Long userID,Long TaskerId);
    Optional<TaskDto> getTaskDetails (Long taskID);
    boolean checkIfTaskExist(Long userID,Long TaskerID,Long addressID);
    boolean checkIfTaskLimit(Long userID,Integer limit);
}
