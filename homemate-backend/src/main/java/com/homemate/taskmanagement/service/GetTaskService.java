package com.homemate.taskmanagement.service;

import com.homemate.taskmanagement.dao.GetTasksDao;
import com.homemate.taskmanagement.dto.PaginatedResponse;
import com.homemate.taskmanagement.dto.StatusDto;
import com.homemate.taskmanagement.dto.TaskCardDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class GetTaskService {
    
    private final GetTasksDao getTasksDao;

    
    public Optional<PaginatedResponse> getUserTasks(Long userID, StatusDto statusDto, int page, int pageSize){

        if(statusDto == StatusDto.All){
            Optional<Long> totalCount =  getTasksDao.countTasksByUserID(userID);
            if (totalCount.isEmpty() || totalCount.get() == 0){
                return Optional.empty();
            }
            List<TaskCardDto> tasks = getTasksDao.getListUserTasksByIDSortedByDate(userID,page,pageSize);
            PaginatedResponse response = PaginatedResponse.builder().
                    tasks(tasks).
                    page(page).
                    pageSize(pageSize).
                    totalCount(totalCount.get()).
                    totalPages(Math.ceilDiv(totalCount.get(),pageSize))
                    .build();
            return Optional.ofNullable(response);

        }else{
            Optional<Long> totalCount =  getTasksDao.countTasksByUserIDAndStatus(userID,statusDto);
            if (totalCount.isEmpty()){
                return Optional.empty();
            }
            List<TaskCardDto> tasks = getTasksDao.getListUserTasksByIDAndStatusSortedByDate(userID,statusDto,page,pageSize);
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
            Optional<Long> totalCount = getTasksDao.countTasksByTaskerID(taskerID);
            if (totalCount.isEmpty() || totalCount.get() == 0) {
                return Optional.empty();
            }
            List<TaskCardDto> tasks = getTasksDao.getListTaskerTasksByIDSortedByDate(taskerID, page, pageSize);
            PaginatedResponse response = PaginatedResponse.builder().
                    tasks(tasks).
                    page(page).
                    pageSize(pageSize).
                    totalCount(totalCount.get()).
                    totalPages(Math.ceilDiv(totalCount.get(), pageSize))
                    .build();
            return Optional.ofNullable(response);

        } else {
            Optional<Long> totalCount = getTasksDao.countTasksByTaskerIDAndStatus(taskerID, statusDto);
            if (totalCount.isEmpty()) {
                return Optional.empty();
            }
            List<TaskCardDto> tasks = getTasksDao.getListTaskerTasksByIDAndStatusSortedByDate(taskerID, statusDto, page, pageSize);
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
    public List<TaskCardDto> getTaskerTasksForNewView(
            Long taskerID, LocalDate startDate,
            LocalDate endDate, StatusDto statusDto) {
        if(statusDto == StatusDto.All){
            return getTasksDao.getTaskerTasksByDateRange(taskerID,startDate,endDate);
        }

        return  getTasksDao.getTaskerTasksByDateRangeAndStatus(taskerID,startDate,endDate,statusDto);



    }
    public List<TaskCardDto> getUserTasksForNewView(
            Long userID, LocalDate startDate,
            LocalDate endDate, StatusDto statusDto) {
        if(statusDto == StatusDto.All){
            return getTasksDao.getUserTasksByDateRange(userID,startDate,endDate);
        }

        return  getTasksDao.getUserTasksByDateRangeAndStatus(userID
                ,startDate,endDate,statusDto);



    }
    
}
