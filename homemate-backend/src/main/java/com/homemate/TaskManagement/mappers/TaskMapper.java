package com.homemate.TaskManagement.mappers;

import com.homemate.TaskManagement.Dto.TaskRequestDto;
import com.homemate.TaskManagement.model.TaskEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    public TaskEntity getTaskEntity(TaskRequestDto taskRequestDto){
        return TaskEntity.builder().
                addressID(taskRequestDto.getAddressID()).
                startDate(taskRequestDto.getStartDate()).
                description(taskRequestDto.getDescription()).
                userID(taskRequestDto.getUserID()).
                taskerID(taskRequestDto.getTaskerID()).
                serviceID(taskRequestDto.getServiceID())
                .build();
    }

}
