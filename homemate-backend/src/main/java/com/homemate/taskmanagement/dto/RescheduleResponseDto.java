package com.homemate.taskmanagement.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class RescheduleResponseDto {
    private Long taskID;
    private LocalDateTime newStartDate;
    private StatusDto rescheduleStatus;
}
