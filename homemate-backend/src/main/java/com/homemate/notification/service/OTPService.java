package com.homemate.notification.service;
import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.OtpVerificationResult;
import com.homemate.notification.domains.dto.OtpVerifyRequest;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public interface OTPService {
    CompletableFuture<OtpVerificationResult> sendOtp(EmailRequest emailRequest) throws ExecutionException, InterruptedException;
    CompletableFuture<OtpVerificationResult> validateCode(OtpVerifyRequest otpVerifyRequest);
}
