package com.homemate.notification.service.impl;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.TaskResponse;
import com.homemate.notification.domains.exception.EmailTemplateException;
import com.homemate.notification.service.EmailService;
import com.homemate.notification.service.utils.EmailTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class EmailServiceImpl implements EmailService {
    private final EmailTemplate emailTemplate;
    private final JavaMailSender javaMailSender;

    @Value("${homemate.email.from:homemateservice8@gmail.com}")
    private String fromEmail;

    public EmailServiceImpl(EmailTemplate emailTemplate, JavaMailSender javaMailSender) {
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
            simpleMailMessage.setFrom(fromEmail);
            javaMailSender.send(simpleMailMessage);

            log.info("Email sent successfully - Type: {}, RecipientType: {}, Recipient: {}",
                    emailRequest.getEmailType(), emailRequest.getRecipientType(), emailRequest.getRecipientEmail());

            return CompletableFuture.completedFuture(
                    TaskResponse.builder()
                            .success(true)
                            .message("Email sent successfully")
                            .build()
            );

        } catch (MailException e) {
            log.error("Failed to send email - Type: {}, RecipientType: {}, Recipient: {}, Error: {}",
                    emailRequest.getEmailType(), emailRequest.getRecipientType(), emailRequest.getRecipientEmail(), e.getMessage(), e);
            return CompletableFuture.completedFuture(
                    TaskResponse.builder()
                            .success(false)
                            .message("Failed to send email: " + e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            log.error("Unexpected error sending email - Type: {}, RecipientType: {}, Recipient: {}, Error: {}",
                    emailRequest.getEmailType(), emailRequest.getRecipientType(), emailRequest.getRecipientEmail(), e.getMessage(), e);
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
    public CompletableFuture<TaskResponse> sendEmail(EmailRequest emailRequest) {
        try {
            String subject = emailTemplate.buildEmailSubject(emailRequest);
            String body = emailTemplate.buildEmailBody(emailRequest);
            return sendNotification(subject, body, emailRequest);

        } catch (EmailTemplateException e) {
            return handleTemplateException(e);
        } catch (Exception e) {
            log.error("Unexpected error processing email - Type: {}, RecipientType: {}, Recipient: {}",
                    emailRequest.getEmailType(), emailRequest.getRecipientType(), emailRequest.getRecipientEmail(), e);
            return CompletableFuture.completedFuture(
                    TaskResponse.builder()
                            .success(false)
                            .message("Failed to process email request: " + e.getMessage())
                            .build()
            );
        }
    }

    private CompletableFuture<TaskResponse> handleTemplateException(EmailTemplateException e) {
        log.error("Email template validation failed - Template: {}, Missing Field: {}, Message: {}",
                e.getTemplateType(), e.getMissingField(), e.getMessage());

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
    @Async("taskExecutor")
    @Override
    public CompletableFuture<TaskResponse> sendDirectEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(body);
            simpleMailMessage.setTo(to);
            simpleMailMessage.setFrom(fromEmail);
            javaMailSender.send(simpleMailMessage);

            log.info("Direct email sent successfully to: {}", to);

            return CompletableFuture.completedFuture(
                    TaskResponse.builder()
                            .success(true)
                            .message("Email sent successfully")
                            .build()
            );

        } catch (MailException e) {
            log.error("Failed to send direct email to: {}, Error: {}", to, e.getMessage(), e);
            return CompletableFuture.completedFuture(
                    TaskResponse.builder()
                            .success(false)
                            .message("Failed to send email: " + e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            log.error("Unexpected error sending direct email to: {}, Error: {}", to, e.getMessage(), e);
            return CompletableFuture.completedFuture(
                    TaskResponse.builder()
                            .success(false)
                            .message("An unexpected error occurred")
                            .build()
            );
        }
    }
}