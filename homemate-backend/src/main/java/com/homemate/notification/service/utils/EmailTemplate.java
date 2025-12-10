package com.homemate.notification.service.utils;

import com.homemate.notification.config.RedisConfig;
import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.exception.EmailTemplateException;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.model.Status;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class EmailTemplate {
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private void validateTaskDto(TaskDto taskDto, String operation) {
        if (taskDto == null) {
            throw new EmailTemplateException(
                    "TaskDto cannot be null for " + operation,
                    operation,
                    "taskDto"
            );
        }
    }

    private void validateEmailRequest(EmailRequest emailRequest) {
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

    private String requireNonNull(String value, String fieldName, String operation) {
        if (value == null || value.trim().isEmpty()) {
            throw new EmailTemplateException(
                    String.format("Required field '%s' is null or empty for %s", fieldName, operation),
                    operation,
                    fieldName
            );
        }
        return value;
    }

    public String buildTaskStatusChangedBody(TaskDto taskDto) {
        validateTaskDto(taskDto, "TASK_STATUS_CHANGED");
        Status status = taskDto.getStatus();
        if (status==null) {
            throw new EmailTemplateException(
                    "Task status cannot be null",
                    "TASK_STATUS_CHANGED",
                    "status"
            );
        }

        return switch (status) {
            case InProgress -> buildTaskStartedBody(taskDto);
            case Suspended -> buildTaskSuspendedBody(taskDto);
            case Done -> buildTaskCompletedBody(taskDto);
            case Accepted -> buildTaskAcceptedBody(taskDto);
            case Rejected -> buildTaskRejectedBody(taskDto);
            default -> throw new EmailTemplateException(
                    "Unsupported task status: " + status,
                    "TASK_STATUS_CHANGED",
                    "status"
            );
        };
    }

    private String buildTaskStartedBody(TaskDto taskDto) {
        validateTaskDto(taskDto, "TASK_STARTED");
        String userName = requireNonNull(taskDto.getUserName(), "userName", "TASK_STARTED");
        String taskerName = requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_STARTED");
        String serviceName = requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_STARTED");
        String address = requireNonNull(taskDto.getAddressDetails(), "addressDetails", "TASK_STARTED");

        String startTime = taskDto.getStartInProgress() != null
                ? taskDto.getStartInProgress().format(dateTimeFormatter) + " " + getAmPmLabel(taskDto.getStartInProgress())
                : LocalDateTime.now().format(dateTimeFormatter) + " " + getAmPmLabel(LocalDateTime.now());

        return String.format(
                "Hi %s,\n\n" +
                        "Good news! %s has started working on your task.\n\n" +
                        "Task Details:\n" +
                        "• Service: %s\n" +
                        "• Started: %s\n" +
                        "• Location: %s\n" +
                        "• Hourly Rate: $%.2f/hour\n\n" +
                        "The tasker is currently working on-site. You can track progress and communicate through the task chat.\n\n" +
                        "Best regards,\n" +
                        "The Homemate Team",
                userName,
                taskerName,
                serviceName,
                startTime,
                address,
                taskDto.getRate() != null ? taskDto.getRate() : 0.0
        );
    }

    private String buildTaskAcceptedBody(TaskDto taskDto) {
        validateTaskDto(taskDto, "TASK_ACCEPTED");
        String userName = requireNonNull(taskDto.getUserName(), "userName", "TASK_ACCEPTED");
        String taskerName = requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_ACCEPTED");
        String serviceName = requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_ACCEPTED");
        String address = requireNonNull(taskDto.getAddressDetails(), "addressDetails", "TASK_ACCEPTED");
        String date = formatDate(taskDto.getStartDate());

        return String.format(
                "Hi %s,\n\n" +
                        "Great news! Your task request has been accepted!\n\n" +
                        "Task Details:\n" +
                        "• Tasker: %s\n" +
                        "• Service: %s\n" +
                        "• Date & Time: %s\n" +
                        "• Location: %s\n\n" +
                        "Your task is now confirmed. The Tasker will contact you soon.\n\n" +
                        "Best regards,\n" +
                        "The Homemate Team",
                userName,
                taskerName,
                serviceName,
                date,
                address
        );
    }

    private String buildTaskRejectedBody(TaskDto taskDto) {
        validateTaskDto(taskDto, "TASK_REJECTED");

        String userName = requireNonNull(taskDto.getUserName(), "userName", "TASK_REJECTED");
        String taskerName = requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_REJECTED");
        String serviceName = requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_REJECTED");

        String date = formatDate(taskDto.getStartDate());

        return String.format(
                "Hi %s,\n\n" +
                        "Unfortunately, %s is unable to accept your request at this time.\n\n" +
                        "Task Details:\n" +
                        "• Service: %s\n" +
                        "• Requested Date: %s\n\n" +
                        "Don't worry! You can request another Tasker.\n" +
                        "Note: You can still chat with the Tasker for more information.\n\n" +
                        "Best regards,\n" +
                        "The Homemate Team",
                userName,
                taskerName,
                serviceName,
                date
        );
    }

    public String buildTaskRequestBody(TaskDto taskDto) {
        validateTaskDto(taskDto, "TASK_REQUEST");
        String taskerName = requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_REQUEST");
        String userName = requireNonNull(taskDto.getUserName(), "userName", "TASK_REQUEST");
        String serviceName = requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_REQUEST");
        String address = requireNonNull(taskDto.getAddressDetails(), "addressDetails", "TASK_REQUEST");
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

    public String buildEmailSubject(EmailRequest emailRequest) {
        validateEmailRequest(emailRequest);

        EmailRequest.EmailType emailType = emailRequest.getEmailType();

        if (emailType == EmailRequest.EmailType.EMAIL_VERIFICATION) {
            return "Verify Your Email Address";
        }
        if (emailType == EmailRequest.EmailType.FORGOT_PASSWORD) {
            return "Reset Your Password";
        }

        TaskDto taskDto = emailRequest.getTask();
        if (taskDto == null) {
            throw new EmailTemplateException(
                    "TaskDto is required for email type: " + emailType,
                    emailType.name(),
                    "task"
            );
        }

        String serviceName = requireNonNull(taskDto.getServiceName(), "serviceName", emailType.name());

        return switch (emailType) {
            case TASK_RESCHEDULE -> "Task Rescheduled - " + serviceName;
            case TASK_STATUS -> buildTaskStatusSubject(taskDto);
            case TASK_ACCEPTED -> {
                String date = formatDate(taskDto.getStartDate());
                yield "Task Accepted - " + serviceName + " on " + date;
            }
            case TASK_REJECTED -> "Task Request Declined - " + serviceName;
            case TASK_REQUEST -> "New Task Request - " + serviceName;
            case TASK_RESUMED -> {
                String taskerName = requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_RESUMED");
                yield "Task Resumed - " + taskerName + " is Back on the Job";
            }
            default -> throw new EmailTemplateException(
                    "Unsupported email type: " + emailType,
                    emailType.name(),
                    "emailType"
            );
        };
    }

    public String BuildTaskResumedBody(TaskDto taskDto) {
        validateTaskDto(taskDto, "TASK_RESUMED");
        String userName = requireNonNull(taskDto.getUserName(), "userName", "TASK_RESUMED");
        String taskerName = requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_RESUMED");
        String serviceName = requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_RESUMED");
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

    private String buildTaskStatusSubject(TaskDto taskDto) {
        validateTaskDto(taskDto, "TASK_STATUS_SUBJECT");

        Status status = taskDto.getStatus();
        if (status == null) {
            throw new EmailTemplateException(
                    "Task status cannot be null",
                    "TASK_STATUS_SUBJECT",
                    "status"
            );
        }

        String serviceName = requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_STATUS_SUBJECT");

        return switch (status) {
            case InProgress -> {
                String taskerName = requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_STATUS_SUBJECT");
                yield "Task Started - " + serviceName + " with " + taskerName;
            }
            case Suspended -> "Task Suspended - " + serviceName;
            case Done -> "Task Completed - Invoice Ready";
            case Accepted -> "Task Accepted - " + serviceName + " on " + formatDate(taskDto.getStartDate());
            case Rejected -> "Task Request Declined - " + serviceName;
            default -> throw new EmailTemplateException(
                    "Unsupported status for subject: " + status,
                    "TASK_STATUS_SUBJECT",
                    "status"
            );
        };
    }

    public String buildUserTaskRescheduleBody(TaskDto taskDto) {
        validateTaskDto(taskDto, "USER_TASK_RESCHEDULE");

        String userName = requireNonNull(taskDto.getUserName(), "userName", "USER_TASK_RESCHEDULE");
        String taskerName = requireNonNull(taskDto.getTaskerName(), "taskerName", "USER_TASK_RESCHEDULE");
        String serviceName = requireNonNull(taskDto.getServiceName(), "serviceName", "USER_TASK_RESCHEDULE");
        String address = requireNonNull(taskDto.getAddressDetails(), "addressDetails", "USER_TASK_RESCHEDULE");

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

    public String buildTaskerTaskRescheduleBody(TaskDto taskDto) {
        validateTaskDto(taskDto, "TASKER_TASK_RESCHEDULE");

        String taskerName = requireNonNull(taskDto.getTaskerName(), "taskerName", "TASKER_TASK_RESCHEDULE");
        String userName = requireNonNull(taskDto.getUserName(), "userName", "TASKER_TASK_RESCHEDULE");
        String serviceName = requireNonNull(taskDto.getServiceName(), "serviceName", "TASKER_TASK_RESCHEDULE");
        String address = requireNonNull(taskDto.getAddressDetails(), "addressDetails", "TASKER_TASK_RESCHEDULE");

        String newDate = formatDate(taskDto.getStartDate());

        return String.format(
                "Hi %s,\n\n" +
                        "A task with %s has been rescheduled.\n\n" +
                        "Updated Task Details:\n" +
                        "• Customer: %s\n" +
                        "• Service: %s\n" +
                        "• New Date & Time: %s\n" +
                        "• Location: %s\n\n" +
                        "Please confirm your availability for the new schedule. If you have any conflicts, contact the customer immediately through the app.\n\n" +
                        "Best regards,\n" +
                        "The Homemate Team",
                taskerName,
                userName,
                userName,
                serviceName,
                newDate,
                address
        );
    }

    private String buildTaskCompletedBody(TaskDto taskDto) {
        validateTaskDto(taskDto, "TASK_COMPLETED");

        String userName = requireNonNull(taskDto.getUserName(), "userName", "TASK_COMPLETED");
        String taskerName = requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_COMPLETED");
        String serviceName = requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_COMPLETED");

        String completedTime = taskDto.getEndDate() != null
                ? taskDto.getEndDate().format(dateTimeFormatter) + " " + getAmPmLabel(taskDto.getEndDate())
                : LocalDateTime.now().format(dateTimeFormatter) + " " + getAmPmLabel(LocalDateTime.now());
        String workedTime = formatWorkedHours(taskDto.getWorkedHours());

        return String.format(
                "Hi %s,\n\n" +
                        "Great news! %s has completed your task.\n\n" +
                        "Task Summary:\n" +
                        "• Service: %s\n" +
                        "• Completed: %s\n" +
                        "• Total Worked Time: %s\n" +
                        "• Hourly Rate: $%.2f/hour\n" +
                        "• Total Bill: $%.2f\n\n" +
                        "Payment Instructions:\n" +
                        "Please review the work and proceed with payment through your preferred method.\n\n" +
                        "Thank you for using Homemate!\n\n" +
                        "Best regards,\n" +
                        "The Homemate Team",
                userName,
                taskerName,
                serviceName,
                completedTime,
                workedTime,
                taskDto.getRate() != null ? taskDto.getRate() : 0.0,
                taskDto.getBill() != null ? taskDto.getBill() : 0.0
        );
    }

    private String buildTaskSuspendedBody(TaskDto taskDto) {
        validateTaskDto(taskDto, "TASK_SUSPENDED");

        String userName = requireNonNull(taskDto.getUserName(), "userName", "TASK_SUSPENDED");
        String taskerName = requireNonNull(taskDto.getTaskerName(), "taskerName", "TASK_SUSPENDED");
        String serviceName = requireNonNull(taskDto.getServiceName(), "serviceName", "TASK_SUSPENDED");

        String workedTime = formatWorkedHours(taskDto.getWorkedHours());
        return String.format(
                "Hi %s,\n\n" +
                        "%s has temporarily suspended work on your task.\n\n" +
                        "Task Details:\n" +
                        "• Service: %s\n" +
                        "• Worked Time: %s\n\n" +
                        "Don't worry - your worked time has been saved. The tasker will resume when ready.\n\n" +
                        "Best regards,\n" +
                        "The Homemate Team",
                userName,
                taskerName,
                serviceName,
                workedTime
        );
    }

    public String buildVerificationCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new EmailTemplateException(
                    "Verification code cannot be null or empty",
                    "EMAIL_VERIFICATION",
                    "code"
            );
        }
        return "Your HomeMate verification code is " + code + "\n" +
                "This code expires in " + RedisConfig.OTP_TTL_SEC + " seconds.";
    }

    public String buildResetPasswordCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new EmailTemplateException(
                    "Reset password code cannot be null or empty",
                    "FORGOT_PASSWORD",
                    "code"
            );
        }
        return "Use this code to reset your HomeMate password: " + code + "\n" +
                "This code expires in " + RedisConfig.OTP_TTL_SEC + " seconds.";
    }

    private String formatWorkedHours(Double workedHours) {
        if (workedHours == null || workedHours == 0) {
            return "0 hours 0 minutes";
        }

        long hours = workedHours.longValue();
        long minutes = Math.round((workedHours - hours) * 60);

        return String.format("%d hours %d minutes", hours, minutes);
    }

    private String formatDate(LocalDateTime localDateTime) {
        return localDateTime != null
                ? localDateTime.format(dateTimeFormatter) + " " + getAmPmLabel(localDateTime)
                : "Not specified";
    }

    private String getAmPmLabel(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.getHour() >= 12 ? "PM" : "AM";
    }
}