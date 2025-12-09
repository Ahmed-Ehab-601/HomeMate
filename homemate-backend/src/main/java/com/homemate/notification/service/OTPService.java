package com.homemate.notification.service;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.OtpSendRequest;
import com.homemate.notification.domains.dto.OtpVerificationResult;
import com.homemate.notification.domains.dto.OtpVerifyRequest;
import org.springframework.stereotype.Service;

@Service
public interface OTPService {
    void sendOtp(EmailRequest emailRequest);
    OtpVerificationResult validateCode(OtpVerifyRequest otpVerifyRequest);
}
