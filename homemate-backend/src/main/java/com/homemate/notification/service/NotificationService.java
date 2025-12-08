package com.homemate.notification.service;

import com.homemate.notification.domains.dto.OtpSendRequest;

public interface NotificationService {
    void createNewAccount(OtpSendRequest otpSendRequest);
}
