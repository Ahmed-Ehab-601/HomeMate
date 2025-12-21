package com.homemate.notification.domains.strategy.pattern.impl;

import com.homemate.notification.domains.strategy.pattern.EmailBuilder;
import com.homemate.notification.service.utils.EmailTemplateValidator;
import com.homemate.taskmanagement.dto.TaskDto;
import java.time.format.DateTimeFormatter;

public class TaskRequestEmailBuilder implements EmailBuilder {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public String buildBody(TaskDto taskDto) {
        EmailTemplateValidator.validateTaskDto(taskDto, "TASK_REQUEST");
        String taskerName = EmailTemplateValidator.requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_REQUEST");
        String userName = EmailTemplateValidator.requireNonNull(taskDto.getUserName(), "userName", "TASK_REQUEST");
        String serviceName = EmailTemplateValidator.requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_REQUEST");
        String address = EmailTemplateValidator.requireNonNull(taskDto.getAddressDetails(), "addressDetails", "TASK_REQUEST");
        String date = formatDate(taskDto.getStartDate());

        return String.format(
                "Hi %s,\n\n" +
                        "You have a new task request!\n\n" +
                        "Task Details:\n" +
                        "• Customer: %s\n" +
                        "• Service: %s\n" +
                        "• Date & Time: %s\n" +
                        "• Location: %s\n" +
                        "• Description: %s\n\n" +
                        "Please review and respond to this request in the app.\n\n" +
                        "Best regards,\n" +
                        "The Homemate Team",
                taskerName,
                userName,
                serviceName,
                date,
                address,
                taskDto.getDescription() != null ? taskDto.getDescription() : "No description provided"
        );
    }

    @Override
    public String buildSubject(TaskDto taskDto) {
        EmailTemplateValidator.validateTaskDto(taskDto, "TASK_REQUEST");
        String serviceName = EmailTemplateValidator.requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_REQUEST");
        return "New Task Request - " + serviceName;
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
