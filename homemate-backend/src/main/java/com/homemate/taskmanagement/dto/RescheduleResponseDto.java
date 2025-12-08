package com.homemate.taskmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleResponseDto {
    private Long taskID;
    private LocalDateTime newStartDate;
    private StatusDto rescheduleStatus;
}
