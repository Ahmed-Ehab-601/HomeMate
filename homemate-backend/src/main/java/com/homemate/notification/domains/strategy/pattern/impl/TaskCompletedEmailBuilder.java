package com.homemate.notification.domains.strategy.pattern.impl;

import com.homemate.notification.domains.strategy.pattern.EmailBuilder;
import com.homemate.notification.service.utils.EmailTemplateValidator;
import com.homemate.taskmanagement.dto.TaskDto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskCompletedEmailBuilder implements EmailBuilder {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public String buildBody(TaskDto taskDto) {
        EmailTemplateValidator.validateTaskDto(taskDto, "TASK_COMPLETED");
        String userName = EmailTemplateValidator.requireNonNull(taskDto.getUserName(), "userName", "TASK_COMPLETED");
        String taskerName = EmailTemplateValidator.requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_COMPLETED");
        String serviceName = EmailTemplateValidator.requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_COMPLETED");

        String completedTime = taskDto.getEndDate() != null
                ? taskDto.getEndDate().format(dateTimeFormatter) + " " + getAmPmLabel(taskDto.getEndDate())
                : LocalDateTime.now().format(dateTimeFormatter) + " " + getAmPmLabel(LocalDateTime.now());
        String workedTime = formatWorkedHours(taskDto.getWorkedHours());

        return String.format(
                """
                        Hi %s,
                        
                        Great news! %s has completed your task.
                        
                        Task Summary:
                        • Service: %s
                        • Completed: %s
                        • Total Worked Time: %s
                        • Hourly Rate: $%.2f/hour
                        • Total Bill: $%.2f
                        
                        Payment Instructions:
                        Please review the work and proceed with payment through your preferred method.
                        
                        Thank you for using Homemate!
                        
                        Best regards,
                        The Homemate Team""",
                userName,
                taskerName,
                serviceName,
                completedTime,
                workedTime,
                taskDto.getHourRate(),
                taskDto.getBill() != null ? taskDto.getBill() : 0.0
        );
    }

    @Override
    public String buildSubject(TaskDto taskDto) {
        return "Task Completed - Invoice Ready";
    }

    private String formatWorkedHours(Double workedHours) {
        if (workedHours == null || workedHours == 0) {
            return "0 hours 0 minutes";
        }
        long hours = workedHours.longValue();
        long minutes = Math.round((workedHours - hours) * 60);
        return String.format("%d hours %d minutes", hours, minutes);
    }

    private String getAmPmLabel(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.getHour() >= 12 ? "PM" : "AM";
    }
}
