package com.homemate.taskmanagement.mappers;

import com.homemate.taskmanagement.dto.TaskRequestDto;
import com.homemate.taskmanagement.model.TaskEntity;
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
