package com.homemate.TaskManagement.model;

import com.homemate.TaskManagement.Dto.TaskDto;
import lombok.Data;

@Data
public class EmailRequest {
    private TaskDto task;
    private EmailType type;
    private String recipientEmail;
}
