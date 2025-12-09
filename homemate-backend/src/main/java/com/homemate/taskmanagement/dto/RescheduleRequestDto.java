package com.homemate.taskmanagement.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleRequestDto {
    @NotNull(message = "New start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime newStartDate;

}
