package com.homemate.taskmanagement.model;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TaskEntity {
    Long taskID;
    LocalDateTime startDate;
    LocalDateTime endDate;
    Status status;
    String description;
    Double workedHours;
    LocalDateTime startInProgress;
    Double bill;
    Long userID;
    Long taskerID;
    Long serviceID;
    Long chatID;
    Long addressID;
}

