package com.homemate.taskmanagement.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

