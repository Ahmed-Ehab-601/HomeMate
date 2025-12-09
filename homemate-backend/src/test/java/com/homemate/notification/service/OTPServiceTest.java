package com.homemate.notification.service;

import com.homemate.notification.config.RedisConfig;
import com.homemate.notification.domains.dto.OtpSendRequest;
import com.homemate.notification.domains.dto.OtpVerificationResult;
import com.homemate.notification.domains.dto.OtpVerifyRequest;
import com.homemate.notification.service.imp.OTPServiceImp;
import com.homemate.notification.service.utils.EmailTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OTPServiceTest {
    @Mock
    private EmailTemplate emailTemplate;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private OTPServiceImp underTest;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        underTest =new OTPServiceImp(emailTemplate, javaMailSender, redisTemplate);
    }

    @Test
    void testGenerateAndStoreOTPShouldReturnSixDigitCode() {
        String email ="homemate@gmail.com";
        String otp = underTest.generateAndStoreOTP(email);
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    void testGenerateAndStoreOTPShouldStoreInRedis() {
        String email= "homemate@gmail.com";
        underTest.generateAndStoreOTP(email);
        verify(valueOperations, atLeastOnce()).set(
                eq("otp:"+email),
                anyString(),
                anyLong(),
                any()
        );
    }

    @Test
    void testGenerateAndStoreOTPShouldInitializeAttemptsCounter() {
        String email="homemate@gmail.com";
        underTest.generateAndStoreOTP(email);
        verify(valueOperations,atLeastOnce()).set(
                eq("otp_attempts:" +email),
                eq("0"),
                anyLong(),
                any()
        );
    }


    @Test
    void testSendOtpShouldGenerateOTPAndSendEmail() {
        String email ="homemate@gmail.com";
        OtpSendRequest otpSendRequest =new OtpSendRequest();
        otpSendRequest.setEmail(email);
        String emailBody ="Your OTP is: 123456 ";
        when(emailTemplate.buildVerificationCode(anyString())).thenReturn(emailBody);
        when(emailTemplate.buildEmailSubject(any())).thenReturn("Email Verification");
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        underTest.sendOtp(otpSendRequest);
        verify(emailTemplate).buildVerificationCode(anyString());
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendOtpShouldCallEmailTemplate() {
        String email ="homemate@gmail.com";
        OtpSendRequest otpSendRequest =new OtpSendRequest();
        otpSendRequest.setEmail(email);
        when(emailTemplate.buildVerificationCode(anyString())).thenReturn("OTP Code");
        when(emailTemplate.buildEmailSubject(any())).thenReturn("Subject");
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        underTest.sendOtp(otpSendRequest);
        verify(emailTemplate).buildVerificationCode(anyString());
    }


    @Test
    void testValidateCodeWithValidOTPShouldReturnSuccess() {
        String email ="homemate@gmail.com";
        String correctOtp ="123456";
        OtpVerifyRequest otpVerifyRequest =new OtpVerifyRequest();
        otpVerifyRequest.setEmail(email);
        otpVerifyRequest.setCode(correctOtp);
        when(valueOperations.get("otp:" + email)).thenReturn(correctOtp);
        when(valueOperations.get("otp_attempts:" + email)).thenReturn("0");
        when(redisTemplate.delete(anyString())).thenReturn(true);
        OtpVerificationResult otpVerificationResult = underTest.validateCode(otpVerifyRequest);
        assertTrue(otpVerificationResult.isSuccess());
        assertEquals("OTP verified successfully", otpVerificationResult.getMessage());
    }

    @Test
    void testValidateCodeWithValidOTPShouldDeleteOTPFromRedis() {
        String email ="homemate@gmail.com";
        String correctOtp ="123456";
        OtpVerifyRequest request =new OtpVerifyRequest();
        request.setEmail(email);
        request.setCode(correctOtp);
        when(valueOperations.get("otp:"+email)).thenReturn(correctOtp);
        when(valueOperations.get("otp_attempts:" +email)).thenReturn("0");
        when(redisTemplate.delete(anyString())).thenReturn(true);
        underTest.validateCode(request);
        verify(redisTemplate, times(2)).delete(anyString());
    }

    @Test
    void testValidateCodeWithExpiredOTPShouldReturnFailure() {
        String email = "homemate@gmail.com";
        
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setEmail(email);
        request.setCode("123456");
        
        when(valueOperations.get("otp:" + email)).thenReturn(null);
        
        OtpVerificationResult result = underTest.validateCode(request);
        
        assertFalse(result.isSuccess());
        assertEquals("OTP expired", result.getMessage());
    }

    @Test
    void testValidateCodeWithInvalidOTPShouldReturnFailure() {
        String email ="homemate@gmail.com";
        String storedOtp ="123456";
        String providedOtp ="654321";
        OtpVerifyRequest request =new OtpVerifyRequest();
        request.setEmail(email);
        request.setCode(providedOtp);
        when(valueOperations.get("otp:" + email)).thenReturn(storedOtp);
        when(valueOperations.get("otp_attempts:" + email)).thenReturn("0");
        OtpVerificationResult otpVerificationResult = underTest.validateCode(request);
        assertFalse(otpVerificationResult.isSuccess());
        assertTrue(otpVerificationResult.getMessage().contains("Invalid OTP"));
    }

    @Test
    void testValidateCodeWithInvalidOTPShouldIncrementAttempts() {
        String email ="homemate@gmail.com";
        String storedOtp ="123456";
        String providedOtp ="654321";
        OtpVerifyRequest otpVerifyRequest =new OtpVerifyRequest();
        otpVerifyRequest.setEmail(email);
        otpVerifyRequest.setCode(providedOtp);
        when(valueOperations.get("otp:" + email)).thenReturn(storedOtp);
        when(valueOperations.get("otp_attempts:" + email)).thenReturn("0");
        underTest.validateCode(otpVerifyRequest);
        verify(valueOperations).set(
                eq("otp_attempts:" + email),
                eq("1"),
                anyLong(),
                any()
        );
    }

    @Test
    void testValidateCodeWithMaxAttemptsExceededShouldReturnFailure() {
        String email = "homemate@gmail.com";
        OtpVerifyRequest otpVerifyRequest = new OtpVerifyRequest();
        otpVerifyRequest.setEmail(email);
        otpVerifyRequest.setCode("123456");
        
        when(valueOperations.get("otp:" + email)).thenReturn("654321");
        when(valueOperations.get("otp_attempts:" + email)).thenReturn(String.valueOf(RedisConfig.OTP_MAX_ATTEMPTS));
        when(redisTemplate.delete(anyString())).thenReturn(true);
        
        OtpVerificationResult result = underTest.validateCode(otpVerifyRequest);
        
        assertFalse(result.isSuccess());
        assertEquals("Maximum attempts exceeded", result.getMessage());
    }

    @Test
    void testValidateCodeWithMaxAttemptsExceededShouldDeleteOTPAndAttempts() {
        String email ="homemate@gmail.com";
        OtpVerifyRequest otpVerifyRequest =new OtpVerifyRequest();
        otpVerifyRequest.setEmail(email);
        otpVerifyRequest.setCode("123456");
        when(valueOperations.get("otp:" + email)).thenReturn("654321");
        when(valueOperations.get("otp_attempts:" + email)).thenReturn(String.valueOf(RedisConfig.OTP_MAX_ATTEMPTS));
        when(redisTemplate.delete(anyString())).thenReturn(true);
        underTest.validateCode(otpVerifyRequest);
        
        verify(redisTemplate, times(2)).delete(anyString());
    }

    @Test
    void testValidateCodeWithMissingAttemptsCounterShouldInitializeIt() {
        String email ="homemate@gmail.com";
        String storedOtp ="123456";
        OtpVerifyRequest otpVerifyRequest =new OtpVerifyRequest();
        otpVerifyRequest.setEmail(email);
        otpVerifyRequest.setCode("654321");
        when(valueOperations.get("otp:" + email)).thenReturn(storedOtp);
        when(valueOperations.get("otp_attempts:"+email)).thenReturn(null).thenReturn("0");
        underTest.validateCode(otpVerifyRequest);
        verify(valueOperations).set(
                eq("otp_attempts:"+email),
                eq("0"),
                anyLong(),
                any()
        );
    }
}
