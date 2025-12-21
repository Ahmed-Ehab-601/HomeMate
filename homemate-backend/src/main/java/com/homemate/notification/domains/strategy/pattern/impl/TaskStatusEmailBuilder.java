package com.homemate.notification.domains.strategy.pattern.impl;

import com.homemate.notification.domains.strategy.pattern.EmailBuilder;
import com.homemate.notification.service.utils.EmailTemplateValidator;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.model.Status;
import java.time.format.DateTimeFormatter;

public class TaskStatusEmailBuilder implements EmailBuilder {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public String buildBody(TaskDto taskDto) {
        EmailTemplateValidator.validateTaskDto(taskDto, "TASK_STATUS");
        Status status = taskDto.getStatus();
        if (status == null) {
            throw new IllegalArgumentException("Task status cannot be null");
        }

        return switch (status) {
            case InProgress -> new TaskStartedEmailBuilder().buildBody(taskDto);
            case Suspended -> new TaskSuspendedEmailBuilder().buildBody(taskDto);
            case Done -> new TaskCompletedEmailBuilder().buildBody(taskDto);
            case Accepted -> new TaskAcceptedEmailBuilder().buildBody(taskDto);
            case Rejected -> new TaskRejectedEmailBuilder().buildBody(taskDto);
            default -> throw new IllegalArgumentException("Unsupported task status: " + status);
        };
    }

    @Override
    public String buildSubject(TaskDto taskDto) {
        EmailTemplateValidator.validateTaskDto(taskDto, "TASK_STATUS");
        Status status = taskDto.getStatus();
        if (status == null) {
            throw new IllegalArgumentException("Task status cannot be null");
        }

        String serviceName = EmailTemplateValidator.requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_STATUS");

        return switch (status) {
            case InProgress -> {
                String taskerName = EmailTemplateValidator.requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_STATUS");
                yield "Task Started - " + serviceName + " with " + taskerName;
            }
            case Suspended -> "Task Suspended - " + serviceName;
            case Done -> "Task Completed - Invoice Ready";
            case Accepted -> "Task Accepted - " + serviceName + " on " + formatDate(taskDto.getStartDate());
            case Rejected -> "Task Request Declined - " + serviceName;
            default -> throw new IllegalArgumentException("Unsupported status for subject: " + status);
        };
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
