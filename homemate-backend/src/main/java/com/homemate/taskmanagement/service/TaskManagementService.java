package com.homemate.taskmanagement.service;

//import com.homemate.TaskManagement.Dao.impl.TaskDaoImpl;
import com.homemate.taskmanagement.dao.impl.TaskDaoImpl;
import com.homemate.taskmanagement.dto.*;
import com.homemate.taskmanagement.exceptions.BadTaskRequestException;
import com.homemate.taskmanagement.exceptions.ConflictException;
import com.homemate.taskmanagement.exceptions.DuplicateRequestException;
import com.homemate.taskmanagement.exceptions.RequestLimitExceededException;
import com.homemate.taskmanagement.mappers.TaskMapper;
import com.homemate.taskmanagement.model.Status;
import com.homemate.taskmanagement.model.TaskEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    public Optional<PaginatedResponse> getUserTasks(Long userID, StatusDto statusDto, int page, int pageSize){

        if(statusDto == StatusDto.All){
            Optional<Long> totalCount =  taskDao.countTasksByUserID(userID);
            if (totalCount.isEmpty() || totalCount.get() == 0){
                return Optional.empty();
            }
            List<TaskCardDto> tasks = taskDao.getListUserTasksByIDSortedByDate(userID,page,pageSize);
            PaginatedResponse response = PaginatedResponse.builder().
                    tasks(tasks).
                    page(page).
                    pageSize(pageSize).
                    totalCount(totalCount.get()).
                    totalPages(Math.ceilDiv(totalCount.get(),pageSize))
                    .build();
            return Optional.ofNullable(response);

        }else{
            Optional<Long> totalCount =  taskDao.countTasksByUserIDAndStatus(userID,statusDto);
            if (totalCount.isEmpty()){
                return Optional.empty();
            }
            List<TaskCardDto> tasks = taskDao.getListUserTasksByIDAndStatusSortedByDate(userID,statusDto,page,pageSize);
            PaginatedResponse response = PaginatedResponse.builder().
                    tasks(tasks).
                    page(page).
                    pageSize(pageSize).
                    totalCount(totalCount.get()).
                    totalPages(Math.ceilDiv(totalCount.get(),pageSize))
                    .build();
            return Optional.ofNullable(response);

        }
    }

    public Optional<PaginatedResponse> getTaskerTasks(Long taskerID, StatusDto statusDto, int page, int pageSize) {

        if (statusDto == StatusDto.All) {
            Optional<Long> totalCount = taskDao.countTasksByTaskerID(taskerID);
            if (totalCount.isEmpty() || totalCount.get() == 0) {
                return Optional.empty();
            }
            List<TaskCardDto> tasks = taskDao.getListTaskerTasksByIDSortedByDate(taskerID, page, pageSize);
            PaginatedResponse response = PaginatedResponse.builder().
                    tasks(tasks).
                    page(page).
                    pageSize(pageSize).
                    totalCount(totalCount.get()).
                    totalPages(Math.ceilDiv(totalCount.get(), pageSize))
                    .build();
            return Optional.ofNullable(response);

        } else {
            Optional<Long> totalCount = taskDao.countTasksByTaskerIDAndStatus(taskerID, statusDto);
            if (totalCount.isEmpty()) {
                return Optional.empty();
            }
            List<TaskCardDto> tasks = taskDao.getListTaskerTasksByIDAndStatusSortedByDate(taskerID, statusDto, page, pageSize);
            PaginatedResponse response = PaginatedResponse.builder().
                    tasks(tasks).
                    page(page).
                    pageSize(pageSize).
                    totalCount(totalCount.get()).
                    totalPages(Math.ceilDiv(totalCount.get(), pageSize))
                    .build();
            return Optional.ofNullable(response);

        }
    }

    public RescheduleResponseDto rescheduleTask(Long taskId, RescheduleRequestDto dto ,long p) {

        TaskDto task = taskDao.getTaskDetails(taskId)
                .orElseThrow(BadTaskRequestException::new);

        Long taskerId = task.getTaskerID();
        LocalDateTime newStart = dto.getNewStartDate();
        if(task.getStatus()!= Status.InReview){
            throw new IllegalStateException("Task could not be rescheduled");
        }

        // role check
        String role = SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().iterator().next().getAuthority();

        boolean isTasker = role.equals("ROLE_TASKER");

//        if (isTasker) {
//            boolean conflict = taskDao.taskerHasConflict(taskerId, newStart, taskId);
//            if (conflict) {
//                throw new ConflictException("This new start time conflicts with an existing task.");
//            }
//        }


        boolean updated = taskDao.updateTaskStartDate(taskId, newStart);
        if (!updated) {
            throw new IllegalStateException("Task could not be rescheduled");
        }

        return RescheduleResponseDto.builder()
                .taskID(taskId)
                .newStartDate(newStart)
                .rescheduleStatus(StatusDto.Accepted)
                .build();
    }


}
