package com.homemate.TaskManagement.Dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskRequestDto {
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    private String description;
    @NotNull(message = "userID is required")
    private Long userID;
    @NotNull(message = "taskerID is required")
    private Long taskerID;
    @NotNull(message = "serviceID is required")
    private Long serviceID;
    @NotNull(message = "addressID is required")
    private Long addressID;
}

