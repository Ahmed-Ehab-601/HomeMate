package com.homemate.taskmanagement.dao;

import com.homemate.taskmanagement.dto.StatusDto;
import com.homemate.taskmanagement.dto.TaskCardDto;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.model.TaskEntity;

import java.util.List;
import java.util.Optional;

public interface TaskDao {
    Optional<Long> insertTask(TaskEntity task);

    Optional<Long> getChat(Long userID, Long TaskerId);

    Optional<Long> insertChat(Long userID, Long TaskerId);

    Optional<TaskDto> getTaskDetails(Long taskID);

    boolean checkIfTaskExist(Long userID, Long TaskerID, Long addressID);

    boolean checkIfTaskLimit(Long userID, Integer limit);

    List<TaskCardDto> getListUserTasksByIDSortedByDate(Long userID, int page, int pageSize);

    List<TaskCardDto> getListUserTasksByIDAndStatusSortedByDate(Long userID, StatusDto status, int page, int pageSize);

    Optional<Long> countTasksByUserID(Long userID);

    Optional<Long> countTasksByUserIDAndStatus(Long userID, StatusDto status);

    List<TaskCardDto> getListTaskerTasksByIDSortedByDate(Long taskerID, int page, int pageSize);

    List<TaskCardDto> getListTaskerTasksByIDAndStatusSortedByDate(Long taskerID, StatusDto status, int page, int pageSize);

    Optional<Long> countTasksByTaskerID(Long taskerID);

    Optional<Long> countTasksByTaskerIDAndStatus(Long taskerID, StatusDto status);

}