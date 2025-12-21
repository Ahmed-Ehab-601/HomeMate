package com.homemate.notification.domains.strategy.pattern.impl;

import com.homemate.notification.domains.strategy.pattern.EmailBuilder;
import com.homemate.notification.config.RedisConfig;
import com.homemate.taskmanagement.dto.TaskDto;

public class ForgotPasswordEmailBuilder implements EmailBuilder {

    @Override
    public String buildBody(TaskDto taskDto) {
        // Password reset doesn't require TaskDto for body
        throw new UnsupportedOperationException("Password reset body is built separately with code parameter");
    }

    @Override
    public String buildSubject(TaskDto taskDto) {
        return "Reset Your Password";
    }

    public String buildBodyWithCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Reset password code cannot be null or empty");
        }
        return "Use this code to reset your HomeMate password: " + code + "\n" +
                "This code expires in " + RedisConfig.OTP_TTL_SEC + " seconds.";
    }
}
