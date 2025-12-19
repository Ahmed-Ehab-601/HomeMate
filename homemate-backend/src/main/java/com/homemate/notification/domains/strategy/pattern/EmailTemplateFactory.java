package com.homemate.notification.domains.strategy.pattern;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.strategy.pattern.impl.*;

import java.util.HashMap;
import java.util.Map;

public class EmailTemplateFactory {
    private final Map<EmailRequest.EmailType, EmailBuilder> emailTypeBuilders;

    public EmailTemplateFactory() {
        this.emailTypeBuilders = new HashMap<>();
        this.emailTypeBuilders.put(EmailRequest.EmailType.EMAIL_VERIFICATION, new EmailVerificationEmailBuilder());
        this.emailTypeBuilders.put(EmailRequest.EmailType.FORGOT_PASSWORD, new ForgotPasswordEmailBuilder());
        this.emailTypeBuilders.put(EmailRequest.EmailType.TASK_RESCHEDULE, new TaskRescheduleEmailBuilder());
        this.emailTypeBuilders.put(EmailRequest.EmailType.TASK_REQUEST, new TaskRequestEmailBuilder());
        this.emailTypeBuilders.put(EmailRequest.EmailType.TASK_RESUMED, new TaskResumedEmailBuilder());
        this.emailTypeBuilders.put(EmailRequest.EmailType.TASK_STATUS, new TaskStatusEmailBuilder());
    }

    public EmailBuilder getBuilder(EmailRequest.EmailType type) {
        return emailTypeBuilders.get(type);
    }


    public void addEmailType(EmailRequest.EmailType type, EmailBuilder emailBuilder) {
        this.emailTypeBuilders.put(type, emailBuilder);
    }
}
