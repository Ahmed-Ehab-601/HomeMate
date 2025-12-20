package com.homemate.notification.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OtpStorageServiceRedisImpl Tests")
class OtpStorageServiceRedisImplTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_OTP = "123456";
    private static final String OTP_PREFIX = "otp:";
    private static final String ATTEMPTS_PREFIX = "otp_attempts:";
    private static final long TTL = 15L;
    private static final TimeUnit TIME_UNIT = TimeUnit.MINUTES;
    private static final int MAX_ATTEMPTS = 3;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private OtpStorageServiceRedisImpl otpStorageService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("Store OTP Tests")
    class StoreOtpTests {

        @Test
        @DisplayName("Should store OTP with correct key and TTL")
        void shouldStoreOtpSuccessfully() {
            // When
            otpStorageService.storeOtp(TEST_EMAIL, TEST_OTP, TTL, TIME_UNIT);

            // Then
            verify(valueOperations).set(
                    eq(OTP_PREFIX + TEST_EMAIL),
                    eq(TEST_OTP),
                    eq(TTL),
                    eq(TIME_UNIT)
            );
        }

        @Test
        @DisplayName("Should store OTP with different time units")
        void shouldStoreOtpWithDifferentTimeUnits() {
            // Given
            long ttlSeconds = 30L;
            TimeUnit seconds = TimeUnit.SECONDS;

            // When
            otpStorageService.storeOtp(TEST_EMAIL, TEST_OTP, ttlSeconds, seconds);

            // Then
            verify(valueOperations).set(
                    eq(OTP_PREFIX + TEST_EMAIL),
                    eq(TEST_OTP),
                    eq(ttlSeconds),
                    eq(seconds)
            );
        }
    }

    @Nested
    @DisplayName("Get OTP Tests")
    class GetOtpTests {

        @Test
        @DisplayName("Should return OTP when it exists")
        void shouldReturnOtpWhenExists() {
            // Given
            when(valueOperations.get(OTP_PREFIX + TEST_EMAIL)).thenReturn(TEST_OTP);

            // When
            String result = otpStorageService.getOtp(TEST_EMAIL);

            // Then
            assertEquals(TEST_OTP, result);
            verify(valueOperations).get(OTP_PREFIX + TEST_EMAIL);
        }

        @Test
        @DisplayName("Should return null when OTP does not exist")
        void shouldReturnNullWhenOtpDoesNotExist() {
            // Given
            when(valueOperations.get(OTP_PREFIX + TEST_EMAIL)).thenReturn(null);

            // When
            String result = otpStorageService.getOtp(TEST_EMAIL);

            // Then
            assertNull(result);
            verify(valueOperations).get(OTP_PREFIX + TEST_EMAIL);
        }
    }



    @Nested
    @DisplayName("Get Attempts Tests")
    class GetAttemptsTests {

        @Test
        @DisplayName("Should return attempts count when it exists")
        void shouldReturnAttemptsWhenExists() {
            // Given
            when(valueOperations.get(ATTEMPTS_PREFIX + TEST_EMAIL)).thenReturn("2");

            // When
            long result = otpStorageService.getAttempts(TEST_EMAIL);

            // Then
            assertEquals(2L, result);
            verify(valueOperations).get(ATTEMPTS_PREFIX + TEST_EMAIL);
        }

        @Test
        @DisplayName("Should return 0 when attempts do not exist")
        void shouldReturnZeroWhenAttemptsDoNotExist() {
            // Given
            when(valueOperations.get(ATTEMPTS_PREFIX + TEST_EMAIL)).thenReturn(null);

            // When
            long result = otpStorageService.getAttempts(TEST_EMAIL);

            // Then
            assertEquals(0L, result);
            verify(valueOperations).get(ATTEMPTS_PREFIX + TEST_EMAIL);
        }

        @Test
        @DisplayName("Should return 0 when attempts value is invalid")
        void shouldReturnZeroWhenAttemptsValueIsInvalid() {
            // Given
            when(valueOperations.get(ATTEMPTS_PREFIX + TEST_EMAIL)).thenReturn("invalid");

            // When
            long result = otpStorageService.getAttempts(TEST_EMAIL);

            // Then
            assertEquals(0L, result);
            verify(valueOperations).get(ATTEMPTS_PREFIX + TEST_EMAIL);
        }
    }

    @Nested
    @DisplayName("Increment Attempts Tests")
    class IncrementAttemptsTests {

        @Test
        @DisplayName("Should increment attempts from 0 to 1")
        void shouldIncrementAttemptsFromZero() {
            // Given
            when(valueOperations.get(ATTEMPTS_PREFIX + TEST_EMAIL)).thenReturn(null);

            // When
            otpStorageService.incrementAttempts(TEST_EMAIL, TTL, TIME_UNIT);

            // Then
            verify(valueOperations).set(
                    eq(ATTEMPTS_PREFIX + TEST_EMAIL),
                    eq("1"),
                    eq(TTL),
                    eq(TIME_UNIT)
            );
        }

        @Test
        @DisplayName("Should increment attempts from existing count")
        void shouldIncrementAttemptsFromExistingCount() {
            // Given
            when(valueOperations.get(ATTEMPTS_PREFIX + TEST_EMAIL)).thenReturn("2");

            // When
            otpStorageService.incrementAttempts(TEST_EMAIL, TTL, TIME_UNIT);

            // Then
            verify(valueOperations).set(
                    eq(ATTEMPTS_PREFIX + TEST_EMAIL),
                    eq("3"),
                    eq(TTL),
                    eq(TIME_UNIT)
            );
        }

        @Test
        @DisplayName("Should handle invalid attempts during increment")
        void shouldHandleInvalidAttemptsDuringIncrement() {
            // Given
            when(valueOperations.get(ATTEMPTS_PREFIX + TEST_EMAIL)).thenReturn("invalid");

            // When
            otpStorageService.incrementAttempts(TEST_EMAIL, TTL, TIME_UNIT);

            // Then
            verify(valueOperations).set(
                    eq(ATTEMPTS_PREFIX + TEST_EMAIL),
                    eq("1"),
                    eq(TTL),
                    eq(TIME_UNIT)
            );
        }
    }

    @Nested
    @DisplayName("Reset Attempts Tests")
    class ResetAttemptsTests {

        @Test
        @DisplayName("Should reset attempts to 0")
        void shouldResetAttempts() {
            // When
            otpStorageService.resetAttempts(TEST_EMAIL, TTL, TIME_UNIT);

            // Then
            verify(valueOperations).set(
                    eq(ATTEMPTS_PREFIX + TEST_EMAIL),
                    eq("0"),
                    eq(TTL),
                    eq(TIME_UNIT)
            );
        }
    }





    @Nested
    @DisplayName("Has Reached Max Attempts Tests")
    class HasReachedMaxAttemptsTests {

        @Test
        @DisplayName("Should return false when attempts are below max")
        void shouldReturnFalseWhenAttemptsBelowMax() {
            // Given
            when(valueOperations.get(ATTEMPTS_PREFIX + TEST_EMAIL)).thenReturn("2");

            // When
            boolean result = otpStorageService.hasReachedMaxAttempts(TEST_EMAIL, MAX_ATTEMPTS);

            // Then
            assertFalse(result);
            verify(valueOperations).get(ATTEMPTS_PREFIX + TEST_EMAIL);
        }

        @Test
        @DisplayName("Should return true when attempts equal max")
        void shouldReturnTrueWhenAttemptsEqualMax() {
            // Given
            when(valueOperations.get(ATTEMPTS_PREFIX + TEST_EMAIL)).thenReturn("3");

            // When
            boolean result = otpStorageService.hasReachedMaxAttempts(TEST_EMAIL, MAX_ATTEMPTS);

            // Then
            assertTrue(result);
            verify(valueOperations).get(ATTEMPTS_PREFIX + TEST_EMAIL);
        }

        @Test
        @DisplayName("Should return true when attempts exceed max")
        void shouldReturnTrueWhenAttemptsExceedMax() {
            // Given
            when(valueOperations.get(ATTEMPTS_PREFIX + TEST_EMAIL)).thenReturn("5");

            // When
            boolean result = otpStorageService.hasReachedMaxAttempts(TEST_EMAIL, MAX_ATTEMPTS);

            // Then
            assertTrue(result);
            verify(valueOperations).get(ATTEMPTS_PREFIX + TEST_EMAIL);
        }

        @Test
        @DisplayName("Should return false when no attempts recorded")
        void shouldReturnFalseWhenNoAttemptsRecorded() {
            // Given
            when(valueOperations.get(ATTEMPTS_PREFIX + TEST_EMAIL)).thenReturn(null);

            // When
            boolean result = otpStorageService.hasReachedMaxAttempts(TEST_EMAIL, MAX_ATTEMPTS);

            // Then
            assertFalse(result);
            verify(valueOperations).get(ATTEMPTS_PREFIX + TEST_EMAIL);
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("Should handle complete OTP lifecycle")
        void shouldHandleCompleteOtpLifecycle() {
            // Store OTP
            otpStorageService.storeOtp(TEST_EMAIL, TEST_OTP, TTL, TIME_UNIT);
            verify(valueOperations).set(
                    eq(OTP_PREFIX + TEST_EMAIL),
                    eq(TEST_OTP),
                    eq(TTL),
                    eq(TIME_UNIT)
            );

            // Check OTP exists
            when(redisTemplate.hasKey(OTP_PREFIX + TEST_EMAIL)).thenReturn(true);
            assertTrue(otpStorageService.otpExists(TEST_EMAIL));

            // Get OTP
            when(valueOperations.get(OTP_PREFIX + TEST_EMAIL)).thenReturn(TEST_OTP);
            assertEquals(TEST_OTP, otpStorageService.getOtp(TEST_EMAIL));

            // Delete OTP
            when(redisTemplate.delete(OTP_PREFIX + TEST_EMAIL)).thenReturn(true);
            otpStorageService.deleteOtp(TEST_EMAIL);
            verify(redisTemplate).delete(OTP_PREFIX + TEST_EMAIL);
        }

        @Test
        @DisplayName("Should handle complete attempts lifecycle")
        void shouldHandleCompleteAttemptsLifecycle() {
            // Initial state - no attempts
            when(valueOperations.get(ATTEMPTS_PREFIX + TEST_EMAIL)).thenReturn(null);
            assertEquals(0L, otpStorageService.getAttempts(TEST_EMAIL));

            // Increment attempts
            otpStorageService.incrementAttempts(TEST_EMAIL, TTL, TIME_UNIT);
            verify(valueOperations).set(
                    eq(ATTEMPTS_PREFIX + TEST_EMAIL),
                    eq("1"),
                    eq(TTL),
                    eq(TIME_UNIT)
            );

            // Check not reached max
            when(valueOperations.get(ATTEMPTS_PREFIX + TEST_EMAIL)).thenReturn("1");
            assertFalse(otpStorageService.hasReachedMaxAttempts(TEST_EMAIL, MAX_ATTEMPTS));

            // Reset attempts
            otpStorageService.resetAttempts(TEST_EMAIL, TTL, TIME_UNIT);
            verify(valueOperations).set(
                    eq(ATTEMPTS_PREFIX + TEST_EMAIL),
                    eq("0"),
                    eq(TTL),
                    eq(TIME_UNIT)
            );

            // Delete attempts
            when(redisTemplate.delete(ATTEMPTS_PREFIX + TEST_EMAIL)).thenReturn(true);
            otpStorageService.deleteAttempts(TEST_EMAIL);
            verify(redisTemplate).delete(ATTEMPTS_PREFIX + TEST_EMAIL);
        }

        @Test
        @DisplayName("Should handle multiple failed attempts scenario")
        void shouldHandleMultipleFailedAttemptsScenario() {
            // First attempt
            when(valueOperations.get(ATTEMPTS_PREFIX + TEST_EMAIL)).thenReturn(null);
            otpStorageService.incrementAttempts(TEST_EMAIL, TTL, TIME_UNIT);

            // Second attempt
            when(valueOperations.get(ATTEMPTS_PREFIX + TEST_EMAIL)).thenReturn("1");
            otpStorageService.incrementAttempts(TEST_EMAIL, TTL, TIME_UNIT);

            // Third attempt
            when(valueOperations.get(ATTEMPTS_PREFIX + TEST_EMAIL)).thenReturn("2");
            otpStorageService.incrementAttempts(TEST_EMAIL, TTL, TIME_UNIT);

            // Check max attempts reached
            when(valueOperations.get(ATTEMPTS_PREFIX + TEST_EMAIL)).thenReturn("3");
            assertTrue(otpStorageService.hasReachedMaxAttempts(TEST_EMAIL, MAX_ATTEMPTS));

            // Verify all increments were called
            verify(valueOperations, times(3)).set(
                    eq(ATTEMPTS_PREFIX + TEST_EMAIL),
                    anyString(),
                    eq(TTL),
                    eq(TIME_UNIT)
            );
        }
    }
}
