package com.homemate.notification.service.imp;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.TaskResponse;
import com.homemate.notification.domains.exception.EmailTemplateException;
import com.homemate.notification.service.EmailService;
import com.homemate.notification.service.utils.EmailTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class EmailServiceImp implements EmailService {
    private EmailTemplate emailTemplate;
    private JavaMailSender javaMailSender;
    private static final String HOMEMATE_EMAIL = "homematesevice8@gmail.com";

    public EmailServiceImp(EmailTemplate emailTemplate, JavaMailSender javaMailSender) {
        this.emailTemplate = emailTemplate;
        this.javaMailSender = javaMailSender;
    }

    @Async("taskExecutor")
    private CompletableFuture<TaskResponse> sendNotification(String subject, String body, EmailRequest emailRequest) {
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(body);
            simpleMailMessage.setTo(emailRequest.getRecipientEmail());
            simpleMailMessage.setFrom(HOMEMATE_EMAIL);
            javaMailSender.send(simpleMailMessage);

            log.info("Email sent successfully to: {}", emailRequest.getRecipientEmail());
            return CompletableFuture.completedFuture(
                    TaskResponse.builder()
                            .success(true)
                            .message("Email sent successfully")
                            .build()
            );

        } catch (MailException e) {
            log.error("Failed to send email to: {}. Error: {}",
                    emailRequest.getRecipientEmail(), e.getMessage(), e);
            return CompletableFuture.completedFuture(
                    TaskResponse.builder()
                            .success(false)
                            .message("Failed to send email: " + e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            log.error("Unexpected error while sending email: {}", e.getMessage(), e);
            return CompletableFuture.completedFuture(
                    TaskResponse.builder()
                            .success(false)
                            .message("An unexpected error occurred")
                            .build()
            );
        }
    }

    @Async("taskExecutor")
    @Override
    public CompletableFuture<TaskResponse> sendUserEmail(EmailRequest emailRequest) {
        try {
            EmailRequest.EmailType emailType = emailRequest.getEmailType();

            return switch (emailType) {
                case  TASK_STATUS ->
                        sendNotification(
                                emailTemplate.buildEmailSubject(emailRequest),
                                emailTemplate.buildTaskStatusChangedBody(emailRequest.getTask()),
                                emailRequest
                        );
                case TASK_RESCHEDULE ->
                        sendNotification(
                                emailTemplate.buildEmailSubject(emailRequest),
                                emailTemplate.buildUserTaskRescheduleBody(emailRequest.getTask()),
                                emailRequest
                        );
                case TASK_RESUMED ->
                        sendNotification(
                                emailTemplate.buildEmailSubject(emailRequest),
                                emailTemplate.BuildTaskResumedBody(emailRequest.getTask()),
                                emailRequest
                        );
                default -> {
                    log.error("Unsupported email type: {}", emailType);
                    yield CompletableFuture.completedFuture(
                            TaskResponse.builder()
                                    .success(false)
                                    .message("Unsupported email type: " + emailType)
                                    .build()
                    );
                }
            };
        } catch (EmailTemplateException e) {
            log.error("Email template validation failed - Template: {}, Missing Field: {}, Message: {}",
                    e.getTemplateType(),
                    e.getMissingField(),
                    e.getMessage());
            return CompletableFuture.completedFuture(
                    TaskResponse.builder()
                            .success(false)
                            .message(String.format("Template: %s, Missing: %s, Reason: %s",
                                    e.getTemplateType(),
                                    e.getMissingField(),
                                    e.getMessage()))
                            .build()
            );
        }
    }

    @Async("taskExecutor")
    @Override
    public CompletableFuture<TaskResponse> sendTaskerEmail(EmailRequest emailRequest) {
        try {
            EmailRequest.EmailType emailType = emailRequest.getEmailType();
            return switch (emailType) {
                case TASK_REQUEST ->
                        sendNotification(
                                emailTemplate.buildEmailSubject(emailRequest),
                                emailTemplate.buildTaskRequestBody(emailRequest.getTask()),
                                emailRequest
                        );
                case TASK_RESCHEDULE ->
                        sendNotification(
                                emailTemplate.buildEmailSubject(emailRequest),
                                emailTemplate.buildTaskerTaskRescheduleBody(emailRequest.getTask()),
                                emailRequest
                        );
                default -> {
                    log.error("Unsupported email type: {}", emailType);
                    yield CompletableFuture.completedFuture(
                            TaskResponse.builder()
                                    .success(false)
                                    .message("Unsupported email type: " + emailType)
                                    .build()
                    );
                }
            };
        } catch (EmailTemplateException e) {
            log.error("Email template validation failed - Template: {}, Missing Field: {}, Message: {}",
                    e.getTemplateType(),
                    e.getMissingField(),
                    e.getMessage());

            return CompletableFuture.completedFuture(
                    TaskResponse.builder()
                            .success(false)
                            .message(String.format("Template: %s, Missing: %s, Reason: %s",
                                    e.getTemplateType(),
                                    e.getMissingField(),
                                    e.getMessage()))
                            .build()
            );
        }
    }
}