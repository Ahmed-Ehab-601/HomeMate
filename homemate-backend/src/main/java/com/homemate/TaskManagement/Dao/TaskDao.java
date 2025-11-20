package com.homemate.TaskManagement.Dao;

import com.homemate.TaskManagement.Dto.TaskDto;
import com.homemate.TaskManagement.model.TaskEntity;

import java.util.Optional;

public interface TaskDao {
    Optional<Long> insertTask(TaskEntity task);
    Optional<Long> getChat (Long userID,Long TaskerId);
    Optional<Long> insertChat(Long userID,Long TaskerId);
    Optional<TaskDto> getTaskDetails (Long taskID);
    boolean checkIfTaskExist(Long userID,Long TaskerID,Long addressID);
    boolean checkIfTaskLimit(Long userID,Integer limit);
}
