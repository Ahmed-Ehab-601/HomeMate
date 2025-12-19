package com.homemate.notification.service;

import java.util.concurrent.TimeUnit;

public interface OtpStorageService {
    void storeOtp(String email, String otp, long ttl, TimeUnit timeUnit);
    String getOtp(String email);
    void deleteOtp(String email);
    long getAttempts(String email);
    void incrementAttempts(String email, long ttl, TimeUnit timeUnit);
    void resetAttempts(String email, long ttl, TimeUnit timeUnit);
    void deleteAttempts(String email);
    boolean otpExists(String email);
    boolean hasReachedMaxAttempts(String email, int maxAttempts);
}