package com.homemate.notification.domains.dto;

import com.homemate.notification.domains.model.EmailType;
import com.homemate.notification.domains.model.RecipientType;
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
    private RecipientType recipientType;
}
