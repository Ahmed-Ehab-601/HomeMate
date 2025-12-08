package com.homemate.taskmanagement.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RescheduleRequestDto {
    @NotNull(message = "New start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime newStartDate;
}
