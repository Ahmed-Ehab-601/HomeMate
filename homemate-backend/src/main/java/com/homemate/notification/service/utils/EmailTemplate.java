package com.homemate.notification.service.utils;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.exception.EmailTemplateException;
import com.homemate.notification.domains.strategy.pattern.EmailBuilder;
import com.homemate.notification.domains.strategy.pattern.EmailTemplateFactory;
import com.homemate.notification.domains.strategy.pattern.impl.*;
import com.homemate.taskmanagement.dto.TaskDto;

import org.springframework.stereotype.Component;

import static com.homemate.notification.service.utils.EmailTemplateValidator.*;

@Component
public class EmailTemplate {
    private final EmailTemplateFactory emailTemplateFactory= new EmailTemplateFactory();



    public String buildEmailSubject(EmailRequest emailRequest) {
        validateEmailRequest(emailRequest);
        EmailRequest.EmailType emailType = emailRequest.getEmailType();
        TaskDto taskDto = emailRequest.getTask();

        EmailBuilder builder = getEmailBuilder(emailType);
        return builder.buildSubject(taskDto);
    }


    public String buildEmailBody(EmailRequest emailRequest) {
        validateEmailRequest(emailRequest);
        EmailRequest.EmailType emailType = emailRequest.getEmailType();
        TaskDto taskDto = emailRequest.getTask();

        EmailBuilder builder = getEmailBuilder(emailType);
        return builder.buildBody(taskDto);
    }

    public OtpEmailContent buildOtpEmailContent(EmailRequest.EmailType emailType, String otpCode) {
        EmailBuilder builder = getEmailBuilder(emailType);
        String body;
        if (builder instanceof EmailVerificationEmailBuilder verificationBuilder) {
            body = verificationBuilder.buildBodyWithCode(otpCode);
        } else if (builder instanceof ForgotPasswordEmailBuilder passwordBuilder) {
            body = passwordBuilder.buildBodyWithCode(otpCode);
        } else {
            throw new EmailTemplateException(
                    "Email type " + emailType + " does not support OTP codes",
                    emailType.name(),
                    "emailType"
            );
        }

        String subject = builder.buildSubject(null);
        return new OtpEmailContent(subject, body);
    }

    private EmailBuilder getEmailBuilder(EmailRequest.EmailType emailType) {
        EmailBuilder builder = emailTemplateFactory.getBuilder(emailType);
        if (builder == null) {
            throw new EmailTemplateException(
                    "Unsupported email type: " + emailType,
                    emailType.name(),
                    "emailType"
            );
        }
        return builder;
    }



    public record OtpEmailContent(String subject, String body) {}
}