package com.homemate.notification.service.impl;
import com.homemate.notification.service.OtpStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpStorageServiceRedisImpl implements OtpStorageService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String OTP_PREFIX = "otp:";
    private static final String ATTEMPTS_PREFIX = "otp_attempts:";
    @Override
    public void storeOtp(String email, String otp, long ttl, TimeUnit timeUnit) {
        String key = buildOtpKey(email);
        redisTemplate.opsForValue().set(key, otp, ttl, timeUnit);
        log.debug("Stored OTP for email: {} with TTL: {} {}", email, ttl, timeUnit);
    }

    @Override
    public String getOtp(String email) {
        String key = buildOtpKey(email);
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            log.debug("No OTP found for email: {}", email);
            return null;
        }

        return (String) value;
    }

    @Override
    public void deleteOtp(String email) {
        String key = buildOtpKey(email);
        Boolean deleted = redisTemplate.delete(key);
        log.debug("Deleted OTP for email: {} - Success: {}", email, deleted);
    }

    @Override
    public long getAttempts(String email) {
        String key = buildAttemptsKey(email);
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            log.debug("No attempts found for email: {}, returning 0", email);
            return 0;
        }

        try {
            return Long.parseLong((String) value);
        } catch (NumberFormatException e) {
            log.error("Invalid attempts value for email: {} - Value: {}", email, value, e);
            return 0;
        }
    }

    @Override
    public void incrementAttempts(String email, long ttl, TimeUnit timeUnit) {
        String key = buildAttemptsKey(email);
        long currentAttempts = getAttempts(email);
        long newAttempts = currentAttempts + 1;

        redisTemplate.opsForValue().set(
                key,
                String.valueOf(newAttempts),
                ttl,
                timeUnit
        );

        log.debug("Incremented attempts for email: {} - New count: {}", email, newAttempts);
    }

    @Override
    public void resetAttempts(String email, long ttl, TimeUnit timeUnit) {
        String key = buildAttemptsKey(email);
        redisTemplate.opsForValue().set(key, "0", ttl, timeUnit);
        log.debug("Reset attempts for email: {} with TTL: {} {}", email, ttl, timeUnit);
    }

    @Override
    public void deleteAttempts(String email) {
        String key = buildAttemptsKey(email);
        Boolean deleted = redisTemplate.delete(key);
        log.debug("Deleted attempts for email: {} - Success: {}", email, deleted);
    }

    @Override
    public boolean otpExists(String email) {
        String key = buildOtpKey(email);
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean hasReachedMaxAttempts(String email, int maxAttempts) {
        long attempts = getAttempts(email);
        boolean reached = attempts >= maxAttempts;

        if (reached) {
            log.warn("Max attempts reached for email: {} - Attempts: {}/{}",
                    email, attempts, maxAttempts);
        }

        return reached;
    }
    private String buildOtpKey(String email) {
        return OTP_PREFIX + email;
    }
    private String buildAttemptsKey(String email) {
        return ATTEMPTS_PREFIX + email;
    }
}