package com.homemate.notification.domains.strategy.pattern.impl;

import com.homemate.notification.domains.strategy.pattern.EmailBuilder;
import com.homemate.notification.config.RedisConfig;
import com.homemate.taskmanagement.dto.TaskDto;

public class EmailVerificationEmailBuilder implements EmailBuilder {

    @Override
    public String buildBody(TaskDto taskDto) {
        // Email verification doesn't require TaskDto for body
        throw new UnsupportedOperationException("Email verification body is built separately with code parameter");
    }

    @Override
    public String buildSubject(TaskDto taskDto) {
        return "Verify Your Email Address";
    }

    public String buildBodyWithCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Verification code cannot be null or empty");
        }
        return "Your HomeMate verification code is " + code + "\n" +
                "This code expires in " + RedisConfig.OTP_TTL_SEC + " seconds.";
    }
}
