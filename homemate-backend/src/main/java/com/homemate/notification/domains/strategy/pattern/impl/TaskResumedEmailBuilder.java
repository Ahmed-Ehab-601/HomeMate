package com.homemate.notification.domains.strategy.pattern.impl;

import com.homemate.notification.domains.strategy.pattern.EmailBuilder;
import com.homemate.notification.service.utils.EmailTemplateValidator;
import com.homemate.taskmanagement.dto.TaskDto;
import java.time.format.DateTimeFormatter;

public class TaskResumedEmailBuilder implements EmailBuilder {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public String buildBody(TaskDto taskDto) {
        EmailTemplateValidator.validateTaskDto(taskDto, "TASK_RESUMED");
        String userName = EmailTemplateValidator.requireNonNull(taskDto.getUserName(), "userName", "TASK_RESUMED");
        String taskerName = EmailTemplateValidator.requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_RESUMED");
        String serviceName = EmailTemplateValidator.requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_RESUMED");
        String date = formatDate(taskDto.getStartDate());
        return String.format(
                "Hi %s,\n\n" +
                        "%s has resumed work on your task.\n\n" +
                        "Task Details:\n" +
                        "• Service: %s\n" +
                        "• Resumed: %s\n" +
                        "• Previous Worked Time: %s\n\n" +
                        "Work is continuing. You'll be notified when the task is completed.\n\n" +
                        "Best regards,\n" +
                        "The Homemate Team",
                userName,
                taskerName,
                serviceName,
                date,
                formatWorkedHours(taskDto.getWorkedHours())
        );
    }

    @Override
    public String buildSubject(TaskDto taskDto) {
        EmailTemplateValidator.validateTaskDto(taskDto, "TASK_RESUMED");
        String taskerName = EmailTemplateValidator.requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_RESUMED");
        return "Task Resumed - " + taskerName + " is Back on the Job";
    }

    private String formatWorkedHours(Double workedHours) {
        if (workedHours == null || workedHours == 0) {
            return "0 hours 0 minutes";
        }
        long hours = workedHours.longValue();
        long minutes = Math.round((workedHours - hours) * 60);
        return String.format("%d hours %d minutes", hours, minutes);
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
