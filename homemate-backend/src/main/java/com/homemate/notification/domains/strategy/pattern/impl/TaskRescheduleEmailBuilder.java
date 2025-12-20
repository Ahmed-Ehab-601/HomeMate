package com.homemate.notification.domains.strategy.pattern.impl;

import com.homemate.notification.domains.strategy.pattern.EmailBuilder;
import com.homemate.notification.service.utils.EmailTemplateValidator;
import com.homemate.taskmanagement.dto.TaskDto;
import java.time.format.DateTimeFormatter;

public class TaskRescheduleEmailBuilder implements EmailBuilder {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public String buildBody(TaskDto taskDto) {
        return buildBodyForUser(taskDto);
    }

    @Override
    public String buildSubject(TaskDto taskDto) {
        EmailTemplateValidator.validateTaskDto(taskDto, "TASK_RESCHEDULE");
        String serviceName = EmailTemplateValidator.requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_RESCHEDULE");
        return "Task Rescheduled - " + serviceName;
    }

    public String buildBodyForUser(TaskDto taskDto) {
        EmailTemplateValidator.validateTaskDto(taskDto, "TASK_RESCHEDULE");
        String userName = EmailTemplateValidator.requireNonNull(taskDto.getUserName(), "userName", "TASK_RESCHEDULE");
        String taskerName = EmailTemplateValidator.requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_RESCHEDULE");
        String serviceName = EmailTemplateValidator.requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_RESCHEDULE");
        String address = EmailTemplateValidator.requireNonNull(taskDto.getAddressDetails(), "addressDetails", "TASK_RESCHEDULE");
        String newDate = formatDate(taskDto.getStartDate());

        return String.format(
                "Hi %s,\n\n" +
                        "Your task has been rescheduled by %s.\n\n" +
                        "Updated Task Details:\n" +
                        "• Service: %s\n" +
                        "• New Date & Time: %s\n" +
                        "• Tasker: %s\n" +
                        "• Location: %s\n\n" +
                        "If you have any questions or concerns about this change, please contact the tasker through the app.\n\n" +
                        "Best regards,\n" +
                        "The Homemate Team",
                userName,
                taskerName,
                serviceName,
                newDate,
                taskerName,
                address
        );
    }

    public String buildBodyForTasker(TaskDto taskDto) {
        EmailTemplateValidator.validateTaskDto(taskDto, "TASK_RESCHEDULE");
        String userName = EmailTemplateValidator.requireNonNull(taskDto.getUserName(), "userName", "TASK_RESCHEDULE");
        String taskerName = EmailTemplateValidator.requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_RESCHEDULE");
        String serviceName = EmailTemplateValidator.requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_RESCHEDULE");
        String address = EmailTemplateValidator.requireNonNull(taskDto.getAddressDetails(), "addressDetails", "TASK_RESCHEDULE");
        String newDate = formatDate(taskDto.getStartDate());

        return String.format(
                "Hi %s,\n\n" +
                        "The task has been rescheduled by the customer.\n\n" +
                        "Updated Task Details:\n" +
                        "• Service: %s\n" +
                        "• New Date & Time: %s\n" +
                        "• Customer: %s\n" +
                        "• Location: %s\n\n" +
                        "Please update your schedule accordingly. If you have any questions, contact the customer through the app.\n\n" +
                        "Best regards,\n" +
                        "The Homemate Team",
                taskerName,
                serviceName,
                newDate,
                userName,
                address
        );
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