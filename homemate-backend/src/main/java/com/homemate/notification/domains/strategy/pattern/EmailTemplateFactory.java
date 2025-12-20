package com.homemate.notification.domains.strategy.pattern;
import com.homemate.notification.domains.model.EmailType;
import com.homemate.notification.domains.strategy.pattern.impl.EmailVerificationEmailBuilder;
import com.homemate.notification.domains.strategy.pattern.impl.ForgotPasswordEmailBuilder;
import com.homemate.notification.domains.strategy.pattern.impl.TaskRescheduleEmailBuilder;
import com.homemate.notification.domains.strategy.pattern.impl.TaskRequestEmailBuilder;
import com.homemate.notification.domains.strategy.pattern.impl.TaskResumedEmailBuilder;
import com.homemate.notification.domains.strategy.pattern.impl.TaskStatusEmailBuilder;
import java.util.HashMap;
import java.util.Map;

public class EmailTemplateFactory {
    private final Map<EmailType, EmailBuilder> emailTypeBuilders;

    public EmailTemplateFactory() {
        this.emailTypeBuilders = new HashMap<>();
        this.emailTypeBuilders.put(EmailType.EMAIL_VERIFICATION, new EmailVerificationEmailBuilder());
        this.emailTypeBuilders.put(EmailType.FORGOT_PASSWORD, new ForgotPasswordEmailBuilder());
        this.emailTypeBuilders.put(EmailType.TASK_RESCHEDULE, new TaskRescheduleEmailBuilder());
        this.emailTypeBuilders.put(EmailType.TASK_REQUEST, new TaskRequestEmailBuilder());
        this.emailTypeBuilders.put(EmailType.TASK_RESUMED, new TaskResumedEmailBuilder());
        this.emailTypeBuilders.put(EmailType.TASK_STATUS, new TaskStatusEmailBuilder());
    }

    public EmailBuilder getBuilder(EmailType type) {
        return emailTypeBuilders.get(type);
    }


    public void addEmailType(EmailType type, EmailBuilder emailBuilder) {
        this.emailTypeBuilders.put(type, emailBuilder);
    }
}
