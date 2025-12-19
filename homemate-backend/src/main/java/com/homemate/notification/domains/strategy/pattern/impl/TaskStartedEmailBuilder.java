package com.homemate.notification.domains.strategy.pattern.impl;

import com.homemate.notification.domains.strategy.pattern.EmailBuilder;
import com.homemate.notification.service.utils.EmailTemplateValidator;
import com.homemate.taskmanagement.dto.TaskDto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskStartedEmailBuilder implements EmailBuilder {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public String buildBody(TaskDto taskDto) {
        EmailTemplateValidator.validateTaskDto(taskDto, "TASK_STARTED");
        String userName = EmailTemplateValidator.requireNonNull(taskDto.getUserName(), "userName", "TASK_STARTED");
        String taskerName = EmailTemplateValidator.requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_STARTED");
        String serviceName = EmailTemplateValidator.requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_STARTED");
        String address = EmailTemplateValidator.requireNonNull(taskDto.getAddressDetails(), "addressDetails", "TASK_STARTED");

        String startTime = taskDto.getStartInProgress() != null
                ? taskDto.getStartInProgress().format(dateTimeFormatter) + " " + getAmPmLabel(taskDto.getStartInProgress())
                : LocalDateTime.now().format(dateTimeFormatter) + " " + getAmPmLabel(LocalDateTime.now());

        return String.format(
                """
                        Hi %s,
                        
                        Good news! %s has started working on your task.
                        
                        Task Details:
                        • Service: %s
                        • Started: %s
                        • Location: %s
                        • Hourly Rate: $%.2f/hour
                        
                        The tasker is currently working on-site. You can track progress and communicate through the task chat.
                        
                        Best regards,
                        The Homemate Team""",
                userName,
                taskerName,
                serviceName,
                startTime,
                address,
                taskDto.getHourRate()
        );
    }

    @Override
    public String buildSubject(TaskDto taskDto) {
        EmailTemplateValidator.validateTaskDto(taskDto, "TASK_STATUS_SUBJECT");
        String serviceName = EmailTemplateValidator.requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_STATUS_SUBJECT");
        String taskerName = EmailTemplateValidator.requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_STATUS_SUBJECT");
        return "Task Started - " + serviceName + " with " + taskerName;
    }

    private String getAmPmLabel(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.getHour() >= 12 ? "PM" : "AM";
    }
}
