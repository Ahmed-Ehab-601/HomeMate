package com.homemate.taskmanagement.service;

//import com.homemate.TaskManagement.Dao.impl.TaskDaoImpl;
import com.homemate.taskmanagement.dao.impl.TaskDaoImpl;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.dto.TaskRequestDto;
import com.homemate.taskmanagement.exceptions.BadTaskRequestException;
import com.homemate.taskmanagement.exceptions.DuplicateRequestException;
import com.homemate.taskmanagement.exceptions.RequestLimitExceededException;
import com.homemate.taskmanagement.mappers.TaskMapper;
import com.homemate.taskmanagement.model.TaskEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TaskManagementService {
    private final TaskMapper taskMapper;
    private final TaskDaoImpl taskDao;

    public TaskManagementService(TaskMapper taskMapper,TaskDaoImpl taskDao) {
        this.taskMapper = taskMapper;
        this.taskDao = taskDao;
    }


    public Optional<TaskDto> requestTask(TaskRequestDto requestDto){
        checkRequest(requestDto);
        TaskEntity newTask = taskMapper.getTaskEntity(requestDto);
        newTask.setChatID(handleChat(requestDto));
        Optional<Long> taskID = taskDao.insertTask(newTask);
        if(taskID.isEmpty()) throw new IllegalArgumentException("the task not created correctly");
        return getTaskDetails(taskID.get());

    }
    public Long handleChat(TaskRequestDto requestDto) {
        return getORCreateChat(requestDto.getUserID(),requestDto.getTaskerID());
    }
    private Long getORCreateChat(Long userID,Long taskerID){
        Optional<Long> existingChat = taskDao.getChat(userID, taskerID);
        if (existingChat.isPresent()) {
            return existingChat.get();
        }
        Optional<Long> newChat = taskDao.insertChat(userID, taskerID);
        return newChat.orElseGet(() -> taskDao.getChat(userID, taskerID)
                .orElseThrow(BadTaskRequestException::new));

    }
    public void checkRequest(TaskRequestDto requestDto){
        if(taskDao.checkIfTaskExist(requestDto.getUserID(),requestDto.getTaskerID(),requestDto.getAddressID())){
            throw new DuplicateRequestException();
        } else if (taskDao.checkIfTaskLimit(requestDto.getUserID(),10)) {
            throw new RequestLimitExceededException();
        }
    }
    public Optional<TaskDto> getTaskDetails(Long taskID){
        return taskDao.getTaskDetails(taskID);
    }




}
