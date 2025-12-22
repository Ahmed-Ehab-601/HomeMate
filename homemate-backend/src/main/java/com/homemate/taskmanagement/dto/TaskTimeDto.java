package com.homemate.taskmanagement.dto;

import lombok.*;

import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TaskTimeDto {
    private Long taskID;
    private LocalDateTime startDate;
    private Integer estimation;
}
