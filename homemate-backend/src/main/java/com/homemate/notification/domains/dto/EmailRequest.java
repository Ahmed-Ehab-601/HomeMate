package com.homemate.notification.domains.dto;

import com.homemate.taskmanagement.dto.TaskDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailRequest {
    private TaskDto task;
    private EmailType emailType;
    private String recipientEmail;
    public enum EmailType {
        TASK_ACCEPTED,
        TASK_REJECTED,
        TASK_RESCHEDULE,
        TASK_STATUS,
        TASK_REQUEST,
        EMAIL_VERIFICATION,
        FORGOT_PASSWORD,
        TASK_RESUMED
    }
    
    
}
