package com.homemate.notification.service;
import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.OtpVerificationResult;
import com.homemate.notification.domains.dto.OtpVerifyRequest;
import com.homemate.notification.service.imp.OTPServiceImp;
import com.homemate.notification.service.utils.EmailTemplate;
import com.homemate.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.homemate.notification.service.imp.OTPServiceImp.ATTEMPTS_PREFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OTPServiceTest {

    @Mock
    private JwtService jwtService;

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
        underTest = new OTPServiceImp(
            jwtService,
            emailTemplate,
            javaMailSender,
            redisTemplate
        );
    }

    @Test
    void testGenerateAndStoreOTPShouldReturnSixDigitCode() throws ExecutionException, InterruptedException {
        String email ="homemate@gmail.com";
        String otp = underTest.generateAndStoreOTP(email, EmailRequest.EmailType.EMAIL_VERIFICATION).join();
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    void testGenerateAndStoreOTPShouldStoreInRedis() {
        String email= "homemate@gmail.com";
        underTest.generateAndStoreOTP(email, EmailRequest.EmailType.EMAIL_VERIFICATION).join();
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
        underTest.generateAndStoreOTP(email, EmailRequest.EmailType.EMAIL_VERIFICATION).join();
        verify(valueOperations,atLeastOnce()).set(
                eq("otp_attempts:" +email),
                eq("0"),
                anyLong(),
                any()
        );
    }


    @Test
    void testSendOtpShouldGenerateOTPAndSendEmail() throws ExecutionException, InterruptedException {
        String email ="homemate@gmail.com";
        EmailRequest emailRequest =new EmailRequest();
        emailRequest.setRecipientEmail(email);
        emailRequest.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        String emailBody ="Your HomeMate verification code is 123456";
        when(emailTemplate.buildVerificationCode(anyString())).thenReturn(emailBody);
        when(valueOperations.get(anyString())).thenReturn(null);
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        OtpVerificationResult result = underTest.sendOtp(emailRequest).join();
        assertTrue(result.isSuccess());
        assertEquals("OTP sent successfully", result.getMessage());
        verify(emailTemplate).buildVerificationCode(anyString());
        verify(javaMailSender).send(any(MimeMessage.class));
    }


    @Test
    void testSendOtpShouldCallEmailTemplate() throws ExecutionException, InterruptedException {
        String email ="homemate@gmail.com";
        EmailRequest emailRequest =new EmailRequest();
        emailRequest.setRecipientEmail(email);
        emailRequest.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        when(emailTemplate.buildVerificationCode(anyString())).thenReturn("OTP Code");
        when(valueOperations.get(anyString())).thenReturn(null);
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        underTest.sendOtp(emailRequest).get();
        verify(emailTemplate).buildVerificationCode(anyString());
    }


    @Test
    void testValidateCodeWithValidOTPShouldReturnSuccess() throws ExecutionException, InterruptedException {
        String email ="homemate@gmail.com";
        String correctOtp ="123456";
        OtpVerifyRequest otpVerifyRequest =new OtpVerifyRequest();
        otpVerifyRequest.setRecipientEmail(email);
        otpVerifyRequest.setCode(correctOtp);
        otpVerifyRequest.setEmailType( EmailRequest.EmailType.EMAIL_VERIFICATION);
        when(valueOperations.get("otp:" + email)).thenReturn(correctOtp);
        when(valueOperations.get("otp_attempts:" + email)).thenReturn("0");
        when(redisTemplate.delete(anyString())).thenReturn(true);
            OtpVerificationResult otpVerificationResult = underTest.validateCode(otpVerifyRequest).join();
        assertTrue(otpVerificationResult.isSuccess());
        assertEquals("OTP verified successfully", otpVerificationResult.getMessage());
    }

    @Test
    void testValidateCodeWithValidOTPShouldDeleteOTPFromRedis() {
        String email ="homemate@gmail.com";
        String correctOtp ="123456";
        OtpVerifyRequest request =new OtpVerifyRequest();
        request.setRecipientEmail(email);
        request.setCode(correctOtp);
        request.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        when(valueOperations.get("otp:"+email)).thenReturn(correctOtp);
        when(valueOperations.get("otp_attempts:" +email)).thenReturn("0");
        when(redisTemplate.delete(anyString())).thenReturn(true);
        underTest.validateCode(request );
        verify(redisTemplate, times(2)).delete(anyString());
    }

    @Test
    void testValidateCodeWithExpiredOTPShouldReturnFailure() {
        String email = "homemate@gmail.com";
        
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setRecipientEmail(email);
        request.setCode("123456");
        request.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        when(valueOperations.get("otp:" + email)).thenReturn(null);
        
        OtpVerificationResult result = underTest.validateCode(request).join();
        
        assertFalse(result.isSuccess());
        assertEquals("OTP expired", result.getMessage());
    }

    @Test
    void testValidateCodeWithInvalidOTPShouldReturnFailure() throws ExecutionException, InterruptedException {
        String email ="homemate@gmail.com";
        String storedOtp ="123456";
        String providedOtp ="654321";
        OtpVerifyRequest request =new OtpVerifyRequest();
        request.setRecipientEmail(email);
        request.setCode(providedOtp);
        request.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        when(valueOperations.get("otp:" + email)).thenReturn(storedOtp);
        when(valueOperations.get("otp_attempts:" + email)).thenReturn("0");
            OtpVerificationResult otpVerificationResult = underTest.validateCode(request).join();
        assertFalse(otpVerificationResult.isSuccess());
        assertTrue(otpVerificationResult.getMessage().contains("Invalid OTP"));
    }

    @Test
    void testValidateCodeWithInvalidOTPShouldIncrementAttempts() {
        String email ="homemate@gmail.com";
        String storedOtp ="123456";
        String providedOtp ="654321";
        OtpVerifyRequest otpVerifyRequest =new OtpVerifyRequest();
        otpVerifyRequest.setRecipientEmail(email);
        otpVerifyRequest.setCode(providedOtp);
        otpVerifyRequest.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        when(valueOperations.get("otp:" + email)).thenReturn(storedOtp);
        when(valueOperations.get("otp_attempts:" + email)).thenReturn("0");
        underTest.validateCode(otpVerifyRequest).join();
        verify(valueOperations).set(
                eq("otp_attempts:" + email),
                eq("1"),
                anyLong(),
                any()
        );
    }

    @Test
    void testValidateCodeWithMaxAttemptsExceededShouldReturnFailure() throws ExecutionException, InterruptedException {
        String email ="homemate@gmail.com";
        OtpVerifyRequest otpVerifyRequest = new OtpVerifyRequest();
        otpVerifyRequest.setRecipientEmail(email);
        otpVerifyRequest.setCode("123456");
        otpVerifyRequest.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        when(valueOperations.get("otp:" + email)).thenReturn("654321");
        when(valueOperations.get("otp_attempts:" + email)).thenReturn(String.valueOf(3));
        when(redisTemplate.delete(anyString())).thenReturn(true);
        
        OtpVerificationResult result = underTest.validateCode(otpVerifyRequest).join();
      String expected=  String.format(
                "Maximum verification attempts exceeded. Please try again in %d %s.",
                15,
                "minute(s)"
        );
        assertFalse(result.isSuccess());
        assertEquals(expected, result.getMessage());
    }

    @Test
    void testValidateCodeWithMaxAttemptsExceededShouldDeleteOTPAndAttempts() {
        String email ="homemate@gmail.com";
        OtpVerifyRequest otpVerifyRequest =new OtpVerifyRequest();
        otpVerifyRequest.setRecipientEmail(email);
        otpVerifyRequest.setCode("123456");
        otpVerifyRequest.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        when(valueOperations.get("otp:" + email)).thenReturn("654321");
        when(valueOperations.get("otp_attempts:" + email)).thenReturn(String.valueOf(3));
        when(redisTemplate.delete(anyString())).thenReturn(true);
        underTest.validateCode(otpVerifyRequest).join();
        verify(redisTemplate, times(1)).delete(anyString());
    }

    @Test
    void testValidateCodeWithMissingAttemptsCounterShouldInitializeIt() {
        String email ="homemate@gmail.com";
        String storedOtp ="123456";
        OtpVerifyRequest otpVerifyRequest =new OtpVerifyRequest();
        otpVerifyRequest.setRecipientEmail(email);
        otpVerifyRequest.setCode("654321");
        otpVerifyRequest.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        when(valueOperations.get("otp:" + email)).thenReturn(storedOtp);
        when(valueOperations.get("otp_attempts:"+email)).thenReturn(null).thenReturn("0");
        underTest.validateCode(otpVerifyRequest).join();
        verify(valueOperations).set(
                eq("otp_attempts:"+email),
                eq("0"),
                anyLong(),
                any()
        );
    }

    @Test
    void testGenerateAndStoreOTPForResetPasswordShouldReturnSixDigitCode() {
        String email ="resetpassword@gmail.com";
        String otp = underTest.generateAndStoreOTP(email, EmailRequest.EmailType.FORGOT_PASSWORD).join();
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    void testGenerateAndStoreOTPForResetPasswordShouldStoreWithSeconds() {
        String email= "resetpassword@gmail.com";
        underTest.generateAndStoreOTP(email, EmailRequest.EmailType.FORGOT_PASSWORD).join();
        verify(valueOperations, atLeastOnce()).set(
                eq("otp:"+email),
                anyString(),
                eq(120L),
                eq(TimeUnit.SECONDS)
        );
    }

    @Test
    void testGenerateAndStoreOTPForResetPasswordShouldInitializeAttemptsWithHour() {
        String email="resetpassword@gmail.com";
        underTest.generateAndStoreOTP(email, EmailRequest.EmailType.FORGOT_PASSWORD).join();
        verify(valueOperations, atLeastOnce()).set(
                eq("otp_attempts:" +email),
                eq("0"),
                eq(1L),
                eq(TimeUnit.HOURS)
        );
    }

    @Test
    void testSendOtpForResetPasswordShouldGenerateOTPAndSendEmail() throws ExecutionException, InterruptedException {
        String email ="resetpassword@gmail.com";
        EmailRequest emailRequest =new EmailRequest();
        emailRequest.setRecipientEmail(email);
        emailRequest.setEmailType(EmailRequest.EmailType.FORGOT_PASSWORD);
            String emailBody ="Use this code to reset your HomeMate password: 123456";
            when(emailTemplate.buildResetPasswordCode(anyString())).thenReturn(emailBody);
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
            underTest.sendOtp(emailRequest).join();
        verify(emailTemplate).buildResetPasswordCode(anyString());
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testValidateCodeForResetPasswordWithValidOTPShouldReturnSuccess() {
        String email ="resetpassword@gmail.com";
        String correctOtp ="654321";
        OtpVerifyRequest otpVerifyRequest =new OtpVerifyRequest();
        otpVerifyRequest.setRecipientEmail(email);
        otpVerifyRequest.setCode(correctOtp);
        otpVerifyRequest.setEmailType(EmailRequest.EmailType.FORGOT_PASSWORD);

        when(valueOperations.get("otp:" + email)).thenReturn(correctOtp);
        when(redisTemplate.delete(anyString())).thenReturn(true);
        OtpVerificationResult otpVerificationResult = underTest.validateCode(otpVerifyRequest).join();
        assertTrue(otpVerificationResult.isSuccess());
        assertEquals("OTP verified successfully", otpVerificationResult.getMessage());
    }

    @Test
    void testValidateCodeForResetPasswordWithHourBasedAttempts() {
        String email ="resetpassword@gmail.com";
        String storedOtp ="654321";
        String providedOtp ="123456";
        OtpVerifyRequest otpVerifyRequest =new OtpVerifyRequest();
        otpVerifyRequest.setRecipientEmail(email);
        otpVerifyRequest.setCode(providedOtp);
        otpVerifyRequest.setEmailType(EmailRequest.EmailType.FORGOT_PASSWORD);
        when(valueOperations.get("otp:" + email)).thenReturn(storedOtp);
        when(valueOperations.get("otp_attempts:" + email)).thenReturn("0");
        underTest.validateCode(otpVerifyRequest).join();
        verify(valueOperations).set(
                eq("otp_attempts:" + email),
                eq("1"),
                eq(1L),
                eq(TimeUnit.HOURS)
        );
    }

    @Test
    void testValidateCodeForResetPasswordWithMaxAttemptsExceededShouldDeleteOTP() {
        String email = "resetpassword@gmail.com";
        OtpVerifyRequest otpVerifyRequest = new OtpVerifyRequest();
        otpVerifyRequest.setRecipientEmail(email);
        otpVerifyRequest.setCode("123456");
        otpVerifyRequest.setEmailType(EmailRequest.EmailType.FORGOT_PASSWORD);
        when(valueOperations.get("otp:" + email)).thenReturn("654321");
        when(valueOperations.get("otp_attempts:" + email)).thenReturn(String.valueOf(3));
        when(redisTemplate.delete(anyString())).thenReturn(true);
        
        OtpVerificationResult result = underTest.validateCode(otpVerifyRequest).join();
        assertFalse(result.isSuccess());
        String expected=  String.format(
                "Maximum verification attempts exceeded. Please try again in %d %s.",
                1,
                "hour(s)"
        );
        assertEquals(expected, result.getMessage());
        verify(redisTemplate, times(1)).delete(anyString());
    }

    @Test
    void testValidateCodeWithValidOTPShouldGenerateJwtToken() {
        String email ="user@gmail.com";
        String correctOtp = "123456";
        String expectedToken = "jwt.token.here";
        
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setRecipientEmail(email);
        request.setCode(correctOtp);
        request.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        
        when(valueOperations.get("otp:" + email)).thenReturn(correctOtp);
        when(valueOperations.get("otp_attempts:" + email)).thenReturn("0");
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(jwtService.generateVerifyToken(email)).thenReturn(expectedToken);
        
        OtpVerificationResult result = underTest.validateCode(request).join();
        
        assertTrue(result.isSuccess());
        assertEquals(expectedToken, result.getToken());
        verify(jwtService).generateVerifyToken(email);
    }

    @Test
    void testValidateCodeShouldReturnRemainingAttempts() {
        String email = "user@gmail.com";
        String storedOtp = "123456";
        String providedOtp = "654321";
        
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setRecipientEmail(email);
        request.setCode(providedOtp);
        request.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        
        when(valueOperations.get("otp:" + email)).thenReturn(storedOtp);
        when(valueOperations.get("otp_attempts:" + email)).thenReturn("1");
        
        OtpVerificationResult result = underTest.validateCode(request).join();
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Attempts remaining: 1"));
    }

  
    @Test
    void testSendOtpShouldSetCorrectFromAddress() throws ExecutionException, InterruptedException {
        String email = "user@gmail.com";
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setRecipientEmail(email);
        emailRequest.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        
        when(emailTemplate.buildVerificationCode(anyString())).thenReturn("Code: 123456");
        when(valueOperations.get(anyString())).thenReturn(null);
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        
        OtpVerificationResult result = underTest.sendOtp(emailRequest).join();
        
        assertTrue(result.isSuccess());
        verify(javaMailSender).send(captor.capture());
        MimeMessage sentMessage = captor.getValue();
        assertNotNull(sentMessage);
    }

    @Test
    void testSendOtpShouldIncludeLogo() throws ExecutionException, InterruptedException {
        String email = "user@gmail.com";
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setRecipientEmail(email);
        emailRequest.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        
        when(emailTemplate.buildVerificationCode(anyString())).thenReturn("Code: 123456");
        when(valueOperations.get(anyString())).thenReturn(null);
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        OtpVerificationResult result = underTest.sendOtp(emailRequest).join();
        
        assertTrue(result.isSuccess());
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testGenerateCodeShouldGenerateUniqueCodes() throws ExecutionException, InterruptedException {
        String email1 ="user1@gmail.com";
        String email2 ="user2@gmail.com";
        String otp1 =underTest.generateAndStoreOTP(email1, EmailRequest.EmailType.EMAIL_VERIFICATION).join();
        String otp2 =underTest.generateAndStoreOTP(email2, EmailRequest.EmailType.EMAIL_VERIFICATION).join();
        assertNotNull(otp1);
        assertNotNull(otp2);
        assertEquals(6, otp1.length());
        assertEquals(6, otp2.length());
    }

    @Test
    void testValidateCodeWithSecondAttemptShouldShowCorrectRemainingAttempts() {
        String email ="user@gmail.com";
        String storedOtp = "123456";
        String providedOtp = "999999";
        
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setRecipientEmail(email);
        request.setCode(providedOtp);
        request.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        
        when(valueOperations.get("otp:" + email)).thenReturn(storedOtp);
        when(valueOperations.get("otp_attempts:" + email)).thenReturn("2");
        
        OtpVerificationResult result = underTest.validateCode(request).join();
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Attempts remaining: 0"));
    }

    @Test
    void testSendOtpForForgotPasswordShouldUseCorrectSubject() throws ExecutionException, InterruptedException {
        String email = "user@gmail.com";
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setRecipientEmail(email);
        emailRequest.setEmailType(EmailRequest.EmailType.FORGOT_PASSWORD);
        
        when(emailTemplate.buildResetPasswordCode(anyString())).thenReturn("Reset code: 123456");
        when(valueOperations.get(anyString())).thenReturn(null);
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        OtpVerificationResult result = underTest.sendOtp(emailRequest).join();
        
        assertTrue(result.isSuccess());
        verify(emailTemplate).buildResetPasswordCode(anyString());
    }

    @Test
    void testValidateCodeEmptyTokenWhenFailed() {
        String email ="user@gmail.com";
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setRecipientEmail(email);
        request.setCode("123456");
        request.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        when(valueOperations.get("otp:" + email)).thenReturn(null);
        OtpVerificationResult result = underTest.validateCode(request).join();
        assertFalse(result.isSuccess());
        assertEquals("", result.getToken());
    }

    @Test
    void testGenerateAndStoreOTPForEmailVerificationShouldUseMinutesTTL() {
        String email="user@gmail.com";
        underTest.generateAndStoreOTP(email, EmailRequest.EmailType.EMAIL_VERIFICATION).join();
        verify(valueOperations).set(
            eq("otp_attempts:" + email),
            eq("0"),
            anyLong(),
            eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void testSendOtpShouldCreateHtmlEmailWithCorrectStructure() throws ExecutionException, InterruptedException {
        String email ="user@gmail.com";
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setRecipientEmail(email);
        emailRequest.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        String expectedBody = "Your verification code is 123456";
        when(emailTemplate.buildVerificationCode(anyString())).thenReturn(expectedBody);
        when(valueOperations.get(anyString())).thenReturn(null);
        MimeMessage mimeMessage =new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        OtpVerificationResult result = underTest.sendOtp(emailRequest).join();
        assertTrue(result.isSuccess());
        verify(javaMailSender).send(any(MimeMessage.class));
        verify(emailTemplate).buildVerificationCode(anyString());
    }

    @Test
    void testValidateCodeWithExactlyMaxAttemptsShouldReturnMaxExceededMessage() {
        String email ="user@gmail.com";
        OtpVerifyRequest request =new OtpVerifyRequest();
        request.setRecipientEmail(email);
        request.setCode("123456");
        request.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        when(valueOperations.get("otp:" + email)).thenReturn("654321");
        when(valueOperations.get("otp_attempts:" + email)).thenReturn("3");
        when(redisTemplate.delete(anyString())).thenReturn(true);
        OtpVerificationResult result = underTest.validateCode(request).join();
        String expected =  String.format(
                "Maximum verification attempts exceeded. Please try again in %d %s.",
                15,
                "minute(s)"
        );
        assertFalse(result.isSuccess());
        assertEquals(expected, result.getMessage());
        verify(redisTemplate, times(1)).delete(anyString());
    }

    @Test
    void testMultipleSuccessfulOTPGenerationForDifferentUsers() throws ExecutionException, InterruptedException {
        String email1= "user1@gmail.com";
        String email2= "user2@gmail.com";
        String email3= "user3@gmail.com";
        String otp1= underTest.generateAndStoreOTP(email1, EmailRequest.EmailType.EMAIL_VERIFICATION).join();
        String otp2= underTest.generateAndStoreOTP(email2, EmailRequest.EmailType.FORGOT_PASSWORD).join();
        String otp3= underTest.generateAndStoreOTP(email3, EmailRequest.EmailType.EMAIL_VERIFICATION).join();
        assertNotNull(otp1);
        assertNotNull(otp2);
        assertNotNull(otp3);
        verify(valueOperations, times(6)).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testValidateCodeAfterOTPRegenerationShouldUseNewCode() {
        String email = "user@gmail.com";
        String newOtp = "222222";
        
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setRecipientEmail(email);
        request.setCode(newOtp);
        request.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        
        when(valueOperations.get("otp:" + email)).thenReturn(newOtp);
        when(valueOperations.get("otp_attempts:" + email)).thenReturn("0");
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(jwtService.generateVerifyToken(email)).thenReturn("token");
        
        OtpVerificationResult result = underTest.validateCode(request).join();
        
        assertTrue(result.isSuccess());
        assertEquals("OTP verified successfully", result.getMessage());
    }

    @Test
    void testSendOtpWithMaxAttemptsExceededShouldReturnErrorMessage() throws ExecutionException, InterruptedException {
        String email = "user@gmail.com";
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setRecipientEmail(email);
        emailRequest.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        
        when(valueOperations.get("otp_attempts:" + email)).thenReturn("3");
        
        OtpVerificationResult result = underTest.sendOtp(emailRequest).join();
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Maximum verification attempts exceeded"));
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendOtpWithMaxAttemptsForResetPasswordShouldShowHourMessage() throws ExecutionException, InterruptedException {
        String email = "user@gmail.com";
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setRecipientEmail(email);
        emailRequest.setEmailType(EmailRequest.EmailType.FORGOT_PASSWORD);
        
        when(valueOperations.get("otp_attempts:" + email)).thenReturn("3");
        
        OtpVerificationResult result = underTest.sendOtp(emailRequest).get();
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("hour(s)"));
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendOtpWithMaxAttemptsForEmailVerificationShouldShowMinuteMessage() throws ExecutionException, InterruptedException {
        String email = "user@gmail.com";
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setRecipientEmail(email);
        emailRequest.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        
        when(valueOperations.get("otp_attempts:" + email)).thenReturn("3");
        
        OtpVerificationResult result = underTest.sendOtp(emailRequest).get();
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("minute(s)"));
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }


    @Test
    void testSendOtpShouldReturnSuccessMessageWhenEmailSentSuccessfully() throws ExecutionException, InterruptedException {
        String email = "user@gmail.com";
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setRecipientEmail(email);
        emailRequest.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        
        when(emailTemplate.buildVerificationCode(anyString())).thenReturn("Code: 123456");
        when(valueOperations.get(anyString())).thenReturn(null);
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        
            OtpVerificationResult result = underTest.sendOtp(emailRequest).get();
        
        assertTrue(result.isSuccess());
        assertEquals("OTP sent successfully", result.getMessage());
    }

    @Test
    void testSendOtpForEmailVerificationWhenAttemptsNotSetShouldProceed() throws ExecutionException, InterruptedException {
        String email = "user@gmail.com";
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setRecipientEmail(email);
        emailRequest.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        
        when(emailTemplate.buildVerificationCode(anyString())).thenReturn("Code: 123456");
        when(valueOperations.get(ATTEMPTS_PREFIX + email)).thenReturn(null);
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        OtpVerificationResult result = underTest.sendOtp(emailRequest).get();
        
        assertTrue(result.isSuccess());
        assertEquals("OTP sent successfully", result.getMessage());
    }

    @Test
    void testValidateCodeWithMaxAttemptsExceededShouldDeleteOTPWithTimeMessage() {
        String email = "user@gmail.com";
        
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setRecipientEmail(email);
        request.setCode("123456");
        request.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        
        when(valueOperations.get("otp:" + email)).thenReturn("654321");
        when(valueOperations.get("otp_attempts:" + email)).thenReturn("3");
        when(redisTemplate.delete(anyString())).thenReturn(true);
        
        OtpVerificationResult result = underTest.validateCode(request).join();
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Please try again in"));
        verify(redisTemplate, times(1)).delete("otp:" + email);
    }

    @Test 
    void testSendOtpShouldStoreAttemptsBeforeGeneratingOTP() throws ExecutionException, InterruptedException {
        String email = "user@gmail.com";
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setRecipientEmail(email);
        emailRequest.setEmailType(EmailRequest.EmailType.EMAIL_VERIFICATION);
        
        when(emailTemplate.buildVerificationCode(anyString())).thenReturn("Code: 123456");
        when(valueOperations.get(ATTEMPTS_PREFIX + email)).thenReturn(null);
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        underTest.sendOtp(emailRequest).join();
        verify(valueOperations, atLeastOnce()).set(
            eq(ATTEMPTS_PREFIX + email),
            eq("0"),
            anyLong(),
            any(TimeUnit.class)
        );
    }
}



