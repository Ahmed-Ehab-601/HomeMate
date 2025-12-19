package com.homemate.notification.domains.strategy.pattern.impl;

import com.homemate.notification.domains.strategy.pattern.EmailBuilder;
import com.homemate.notification.service.utils.EmailTemplateValidator;
import com.homemate.taskmanagement.dto.TaskDto;
import java.time.format.DateTimeFormatter;

public class TaskSuspendedEmailBuilder implements EmailBuilder {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public String buildBody(TaskDto taskDto) {
        EmailTemplateValidator.validateTaskDto(taskDto, "TASK_SUSPENDED");
        String userName = EmailTemplateValidator.requireNonNull(taskDto.getUserName(), "userName", "TASK_SUSPENDED");
        String taskerName = EmailTemplateValidator.requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_SUSPENDED");
        String serviceName = EmailTemplateValidator.requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_SUSPENDED");

        String workedTime = formatWorkedHours(taskDto.getWorkedHours());
        return String.format(
                """
                        Hi %s,
                        
                        %s has temporarily suspended work on your task.
                        
                        Task Details:
                        • Service: %s
                        • Worked Time: %s
                        
                        Don't worry - your worked time has been saved. The tasker will resume when ready.
                        
                        Best regards,
                        The Homemate Team""",
                userName,
                taskerName,
                serviceName,
                workedTime
        );
    }

    @Override
    public String buildSubject(TaskDto taskDto) {
        EmailTemplateValidator.validateTaskDto(taskDto, "TASK_STATUS_SUBJECT");
        String serviceName = EmailTemplateValidator.requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_STATUS_SUBJECT");
        return "Task Suspended - " + serviceName;
    }

    private String formatWorkedHours(Double workedHours) {
        if (workedHours == null || workedHours == 0) {
            return "0 hours 0 minutes";
        }
        long hours = workedHours.longValue();
        long minutes = Math.round((workedHours - hours) * 60);
        return String.format("%d hours %d minutes", hours, minutes);
    }
}
