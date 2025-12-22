package com.homemate.taskmanagement.dto;


import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;


@Data
@Builder
public class TaskCardDto {
    @NotNull
    private Long taskID;
    @NotNull
    private LocalDateTime startDate;
    @NotNull
    private String userName;
    @NotNull
    private String taskerName;
    @NotNull
    private String serviceName;
    @NotNull
    private StatusDto status;
    @NotNull
    private String addressCity;
    private Integer timeEstimated;


}
