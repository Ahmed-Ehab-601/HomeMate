package com.homemate.notification.service.utils;
import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.exception.EmailTemplateException;
import com.homemate.taskmanagement.dto.TaskDto;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateValidator {
    public static void validateTaskDto(TaskDto taskDto, String operation) {
        if (taskDto == null) {
            throw new EmailTemplateException(
                    "TaskDto cannot be null for " + operation,
                    operation,
                    "taskDto"
            );
        }
    }

    static void validateEmailRequest(EmailRequest emailRequest) {
        if (emailRequest == null) {
            throw new EmailTemplateException(
                    "EmailRequest cannot be null",
                    "EMAIL_REQUEST",
                    "emailRequest"
            );
        }
        if (emailRequest.getEmailType() == null) {
            throw new EmailTemplateException(
                    "Email type cannot be null",
                    "EMAIL_REQUEST",
                    "emailType"
            );
        }
    }

    public static String requireNonNull(String value, String fieldName, String operation) {
        if (value == null||value.trim().isEmpty()) {
            throw new EmailTemplateException(
                    String.format("Required field '%s' is null or empty for %s", fieldName, operation),
                    operation,
                    fieldName
            );
        }
        return value;
    }

}