package com.homemate.taskmanagement.model;

import com.homemate.taskmanagement.dto.TaskDto;
import lombok.Data;

@Data
public class EmailRequest {
    private TaskDto task;
    private EmailType type;
    private String recipientEmail;
}
