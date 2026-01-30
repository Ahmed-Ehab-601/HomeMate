package com.homemate.notification.service.impl;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.TaskResponse;
import com.homemate.notification.domains.exception.EmailTemplateException;
import com.homemate.notification.service.EmailService;
import com.homemate.notification.service.utils.EmailTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class EmailServiceImpl implements EmailService {
    private final EmailTemplate emailTemplate;
    private final JavaMailSender javaMailSender;
    private final RestTemplate restTemplate;

    @Value("${homemate.email.from:homemateservice8@gmail.com}")
    private String fromEmail;

    @Value("${homemate.email.from.name:HomeMate}")
    private String fromName;

    @Value("${brevo.key:}")
    private String brevoApiKey;

    @Value("${brevo.enabled:false}")
    private boolean brevoEnabled;

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    public EmailServiceImpl(EmailTemplate emailTemplate, JavaMailSender javaMailSender) {
        this.emailTemplate = emailTemplate;
        this.javaMailSender = javaMailSender;
        this.restTemplate = new RestTemplate();
    }

    @Async("taskExecutor")
    private CompletableFuture<TaskResponse> sendNotification(String subject, String body, EmailRequest emailRequest) {
        // Try Brevo API first if enabled
        if (brevoEnabled && brevoApiKey != null && !brevoApiKey.isEmpty()) {
            log.info("Attempting to send email via Brevo API");
            CompletableFuture<TaskResponse> brevoResult = sendViaBrevoAPI(subject, body, emailRequest);

            // If Brevo fails, fallback to SMTP
            if (!brevoResult.join().isSuccess()) {
                log.warn("Brevo API failed, falling back to SMTP");
                return sendViaSMTP(subject, body, emailRequest);
            }
            return brevoResult;
        }

        // Use SMTP if Brevo not enabled
        log.info("Using SMTP to send email");
        return sendViaSMTP(subject, body, emailRequest);
    }

    private CompletableFuture<TaskResponse> sendViaSMTP(String subject, String body, EmailRequest emailRequest) {
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(body);
            simpleMailMessage.setTo(emailRequest.getRecipientEmail());
            simpleMailMessage.setFrom(fromEmail);
            javaMailSender.send(simpleMailMessage);

            log.info("✅ Email sent via SMTP - Type: {}, Recipient: {}",
                    emailRequest.getEmailType(), emailRequest.getRecipientEmail());

            return CompletableFuture.completedFuture(
                    TaskResponse.builder()
                            .success(true)
                            .message("Email sent successfully via SMTP")
                            .build()
            );

        } catch (MailException e) {
            log.error("❌ Failed to send email via SMTP - Type: {}, Recipient: {}, Error: {}",
                    emailRequest.getEmailType(), emailRequest.getRecipientEmail(), e.getMessage());
            return CompletableFuture.completedFuture(
                    TaskResponse.builder()
                            .success(false)
                            .message("Failed to send email: " + e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            log.error("❌ Unexpected error sending email via SMTP - Type: {}, Recipient: {}, Error: {}",
                    emailRequest.getEmailType(), emailRequest.getRecipientEmail(), e.getMessage());
            return CompletableFuture.completedFuture(
                    TaskResponse.builder()
                            .success(false)
                            .message("An unexpected error occurred")
                            .build()
            );
        }
    }

    private CompletableFuture<TaskResponse> sendViaBrevoAPI(String subject, String body, EmailRequest emailRequest) {
        try {
            // Build sender object
            Map<String, String> sender = new HashMap<>();
            sender.put("name", fromName);
            sender.put("email", fromEmail);

            // Build recipient object
            Map<String, String> recipient = new HashMap<>();
            recipient.put("email", emailRequest.getRecipientEmail());

            // Build request body according to Brevo API docs
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sender", sender);
            requestBody.put("to", new Object[]{recipient});
            requestBody.put("subject", subject);
            requestBody.put("textContent", body);

            // Build headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);
            headers.set("accept", "application/json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Send request
            ResponseEntity<String> response = restTemplate.exchange(
                    BREVO_API_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Email sent via Brevo API - Type: {}, Recipient: {}",
                        emailRequest.getEmailType(), emailRequest.getRecipientEmail());

                return CompletableFuture.completedFuture(
                        TaskResponse.builder()
                                .success(true)
                                .message("Email sent successfully via Brevo")
                                .build()
                );
            } else {
                log.error("❌ Brevo API error: {} - {}", response.getStatusCode(), response.getBody());
                return CompletableFuture.completedFuture(
                        TaskResponse.builder()
                                .success(false)
                                .message("Failed to send email via Brevo: " + response.getStatusCode())
                                .build()
                );
            }

        } catch (Exception e) {
            log.error("❌ Failed to send email via Brevo API - Recipient: {}, Error: {}",
                    emailRequest.getRecipientEmail(), e.getMessage());

            return CompletableFuture.completedFuture(
                    TaskResponse.builder()
                            .success(false)
                            .message("Failed to send email via Brevo: " + e.getMessage())
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