package com.homemate.notification.domains.strategy.pattern.impl;

import com.homemate.notification.domains.strategy.pattern.EmailBuilder;
import com.homemate.notification.service.utils.EmailTemplateValidator;
import com.homemate.taskmanagement.dto.TaskDto;
import java.time.format.DateTimeFormatter;

public class TaskAcceptedEmailBuilder implements EmailBuilder {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public String buildBody(TaskDto taskDto) {
        EmailTemplateValidator.validateTaskDto(taskDto, "TASK_ACCEPTED");
        String userName = EmailTemplateValidator.requireNonNull(taskDto.getUserName(), "userName", "TASK_ACCEPTED");
        String taskerName = EmailTemplateValidator.requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_ACCEPTED");
        String serviceName = EmailTemplateValidator.requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_ACCEPTED");
        String address = EmailTemplateValidator.requireNonNull(taskDto.getAddressDetails(), "addressDetails", "TASK_ACCEPTED");
        String date = formatDate(taskDto.getStartDate());

        return String.format(
                """
                        Hi %s,
                        
                        Great news! Your task request has been accepted!
                        
                        Task Details:
                        • Tasker: %s
                        • Service: %s
                        • Date & Time: %s
                        • Location: %s
                        
                        Your task is now confirmed. The Tasker will contact you soon.
                        
                        Best regards,
                        The Homemate Team""",
                userName,
                taskerName,
                serviceName,
                date,
                address
        );
    }

    @Override
    public String buildSubject(TaskDto taskDto) {
        EmailTemplateValidator.validateTaskDto(taskDto, "TASK_STATUS_SUBJECT");
        String serviceName = EmailTemplateValidator.requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_STATUS_SUBJECT");
        return "Task Accepted - " + serviceName + " on " + formatDate(taskDto.getStartDate());
    }

    private String formatDate(java.time.LocalDateTime localDateTime) {
        return localDateTime != null
                ? localDateTime.format(dateTimeFormatter) + " " + getAmPmLabel(localDateTime)
                : "Not specified";
    }

    private String getAmPmLabel(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.getHour() >= 12 ? "PM" : "AM";
    }
}
