package com.homemate.taskmanagement.dto;

import com.homemate.taskmanagement.model.Status;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private Long taskID;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Status status;
    private String description;
    private Double workedHours;
    private LocalDateTime startInProgress;
    private Double bill;
    private String userName;
    private String taskerName;
    private String serviceName;
    private Long chatID;
    private String addressDetails;
    private Double rate;
    private String review;
    private List<ReviewImageDto> reviewImageDtoList;
    private String userMail;
    private String taskerMail;

}
