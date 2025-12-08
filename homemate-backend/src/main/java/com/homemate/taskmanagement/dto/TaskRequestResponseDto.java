package com.homemate.taskmanagement.dto;

import com.homemate.taskmanagement.model.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequestResponseDto {
    private Long taskID;
    private Status status;
}
