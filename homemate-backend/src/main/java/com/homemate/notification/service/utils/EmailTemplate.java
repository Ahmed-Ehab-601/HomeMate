package com.homemate.notification.service.utils;
import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.model.Status;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Component
public class EmailTemplate {
   private final DateTimeFormatter dateTimeFormatter =DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String buildTaskStatusChangedBody(TaskDto taskDto){
        Status status = taskDto.getStatus();
        return switch (status) {
            case InProgress -> buildTaskStartedBody(taskDto);
            case Suspended -> buildTaskSuspendedBody(taskDto);
            case Done -> buildTaskCompletedBody(taskDto);
            case Accepted -> buildTaskAcceptedBody(taskDto);
            case Rejected -> buildTaskRejectedBody(taskDto);
            default -> "";
        };
    }
    private String buildTaskStartedBody(TaskDto taskDto) {
        String startTime=taskDto.getStartInProgress()!=null
                ? taskDto.getStartInProgress().format(dateTimeFormatter)+" "+getAmPmLabel(taskDto.getStartInProgress())
                : LocalDateTime.now().format(dateTimeFormatter)+" "+getAmPmLabel(LocalDateTime.now());
        return String.format(
                "Hi %s,\n\n" +
                        "Good news! %s has started working on your task.\n\n" +
                        "Task Details:\n" +
                        "• Service: %s\n" +
                        "• Started: %s\n" +
                        "• Location: %s\n" +
                        "• Hourly Rate: $%.2f/hour\n\n" +
                        "The tasker is currently working on-site.started You can track progress and communicate through the task chat.\n\n" +
                        "Best regards,\n" +
                        "The Homiso Team",
                taskDto.getUserName(),
                taskDto.getTaskerName(),
                taskDto.getServiceName(),
                startTime,
                taskDto.getAddressDetails(),
                taskDto.getRate()!=null?taskDto.getRate():0.0
        );
    }
    private String buildTaskAcceptedBody(TaskDto taskDto) {
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
                        "The Homiso Team",
                taskDto.getUserName(),
                taskDto.getTaskerName(),
                taskDto.getServiceName(),
                date,
                taskDto.getAddressDetails()

        );
    }

    private String buildTaskRejectedBody(TaskDto taskDto) {
        String date = formatDate(taskDto.getStartDate());

        return String.format(
                "Hi %s,\n\n" +
                        "Unfortunately, %s is unable to accept your request at this time.\n\n" +
                        "Task Details:\n" +
                        "• Service: %s\n" +
                        "• Requested Date: %s\n\n" +
                        "Don't worry! You can request another Tasker:\n" +
                        "Note: You can still chat with the Tasker for more information.\n" +
                        "Best regards,\n" +
                        "The Homiso Team",
                taskDto.getUserName(),
                taskDto.getTaskerName(),
                taskDto.getServiceName(),
                date

        );
    }
    public String buildTaskRequestBody(TaskDto taskDto){
        String date =formatDate(taskDto.getStartDate());
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
                        "The Homiso Team",
                taskDto.getTaskerName(),
                taskDto.getUserName(),
                taskDto.getServiceName(),
                date,
                taskDto.getAddressDetails(),
                taskDto.getDescription() != null ? taskDto.getDescription() : "No description provided"
        );
    }


    public String buildEmailSubject(EmailRequest emailRequest) {
        EmailRequest.EmailType emailType = emailRequest.getEmailType();
        TaskDto taskDto = emailRequest.getTask();

        if (emailType == EmailRequest.EmailType.EMAIL_VERIFICATION) {
            return "Verify Your Email Address";
        }
        if (emailType == EmailRequest.EmailType.FORGOT_PASSWORD) {
            return "Reset Your Password";
        }
        if (taskDto == null) {
            return "Notification from Homiso";
        }
        
        String serviceName = taskDto.getServiceName();
        return switch (emailType) {
            case TASK_RESCHEDULE->"Task Rescheduled - "+serviceName;
            case TASK_STATUS -> buildTaskStatusSubject(taskDto);
            case TASK_REQUEST -> "New Task Request - " +serviceName;
            case TASK_RESUMED -> "Task Resumed - "+taskDto.getTaskerName() +" is Back on the Job";
            default -> "Notification from Homiso";
        };
    }
    public String BuildTaskResumedBody(TaskDto taskDto){
        String date =formatDate(taskDto.getStartDate());
        return String.format(
                "Hi %s,\n\n" +
                        "%s has resumed work on your task.\n\n\n" +
                        "Task Details:\n" +
                        "• Service: %s\n" +
                        "• Resumed: %s\n" +
                        "• Previous Worked Time: %s\n\n" +
                        "Work is continuing.  You'll be notified when the task is completed.\n\n" +
                        "Best regards,\n" +
                        "The Homiso Team",
                taskDto.getUserName(),
                taskDto.getTaskerName(),
                taskDto.getServiceName(),
                date,
                formatWorkedHours(taskDto.getWorkedHours()),
                 taskDto.getDescription() != null ? taskDto.getDescription() : "No description provided"
        );

    }
    private String buildTaskStatusSubject(TaskDto taskDto){
        Status status =taskDto.getStatus();
        String serviceName=taskDto.getServiceName();
        return switch (status) {
            case InProgress -> "Task Started - " + serviceName + " with " + taskDto.getTaskerName();
            case Suspended -> "Task Suspended - " + serviceName;
            case Done -> "Task Completed - Invoice Ready";
            case Accepted->"Task Accepted - " + serviceName +" on "+formatDate(taskDto.getStartDate());
            case Rejected -> "Task Task Request Declined - " + serviceName;
            default -> "Task Update - " + serviceName;
        };
    }
    public String buildUserTaskRescheduleBody(TaskDto taskDto){
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
                        "The Homiso Team",
                taskDto.getUserName(),
                taskDto.getTaskerName(),
                taskDto.getServiceName(),
                newDate,
                taskDto.getTaskerName(),
                taskDto.getAddressDetails()

        );
    }

    public String buildTaskerTaskRescheduleBody(TaskDto taskDto){
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
                        "The Homiso Team",
                taskDto.getTaskerName(),
                taskDto.getUserName(),
                taskDto.getUserName(),
                taskDto.getServiceName(),
                newDate,
                taskDto.getAddressDetails()
        );
    }

    private String buildTaskCompletedBody(TaskDto taskDto) {
        String completedTime = taskDto.getEndDate() != null
                ? taskDto.getEndDate().format(dateTimeFormatter)+" "+getAmPmLabel(taskDto.getEndDate())
                : LocalDateTime.now().format(dateTimeFormatter)+" "+getAmPmLabel(LocalDateTime.now());
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
                        "Thank you for using Homiso!\n\n" +
                        "Best regards,\n" +
                        "The Homiso Team",
                taskDto.getUserName(),
                taskDto.getTaskerName(),
                taskDto.getServiceName(),
                completedTime,
                workedTime,
                taskDto.getRate()!=null?taskDto.getRate():0.0,
                taskDto.getBill()!=null?taskDto.getBill():0.0
        );
    }


    private String buildTaskSuspendedBody(TaskDto taskDto) {
        String workedTime=formatWorkedHours(taskDto.getWorkedHours());
        return String.format(
                "Hi %s,\n\n" +
                        "%s has temporarily suspended work on your task.\n\n" +
                        "Task Details:\n" +
                        "• Service: %s\n" +
                        "• Worked Time: %s\n\n" +
                        "Don't worry - your worked time has been saved. The tasker will resume when ready.\n\n" +
                        "Best regards,\n" +
                        "The Homiso Team",
                taskDto.getUserName(),
                taskDto.getTaskerName(),
                taskDto.getServiceName(),
                workedTime
        );
    }


    public String buildVerificationCode(String code){
         return "Your HomeMate verification code is " + code + "\n" +
                "This code expires in " + com.homemate.notification.config.RedisConfig.OTP_TTL_MINUTES + " minutes.";
    }
    private String formatWorkedHours(Double workedHours) {
        if (workedHours ==null|| workedHours== 0) {
            return "0 hours 0 minutes";
        }

        long hours=workedHours.longValue();
        long minutes=Math.round((workedHours-hours) * 60);

        return String.format("%d hours %d minutes",hours,minutes);
    }

    private String  formatDate(LocalDateTime localDateTime){
      return  localDateTime != null ?localDateTime.format(dateTimeFormatter) +" "+getAmPmLabel(localDateTime) : "Not specified";
    }
    private String getAmPmLabel(LocalDateTime dateTime) {
        if (dateTime==null)return "";
        return dateTime.getHour()>=12?"PM":"AM";
    }
}
