package com.homemate.notification.service;

import com.homemate.notification.config.RedisConfig;
import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.OtpVerificationResult;
import com.homemate.notification.domains.dto.OtpVerifyRequest;
import com.homemate.notification.domains.model.EmailType;
import com.homemate.notification.service.impl.OTPServiceImpl;
import com.homemate.notification.service.utils.EmailTemplate;
import com.homemate.security.service.JwtService;
import com.homemate.notification.service.utils.EmailTemplate.OtpEmailContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OTPServiceTest {

    private static final long EXPIRE_TIME_IN_MINS_FOR_EMAIL_VERIFICATION = 15L;
    private static final long EXPIRE_TIME_IN_MINS_RESET_PASSWORD = 120L;
    private static final long EXPIRE_TIME_IN_HOURS_RESET_PASSWORD = 1L;
    private static final String FROM_EMAIL = "homemateservice8@gmail.com";
    private static final String TEST_EMAIL = "homemate@gmail.com";
    private static final String RESET_PASSWORD_EMAIL = "resetpassword@gmail.com";
    private static final String VALID_OTP = "123456";
    private static final String INVALID_OTP = "654321";
    private static final String EXPECTED_TOKEN = "jwt.token.here";
    private static final int OTP_LENGTH = 6;
    private static final String OTP_PATTERN = "\\d{6}";

    private static final String SUCCESS_MESSAGE = "OTP verified successfully";
    private static final String EXPIRED_MESSAGE = "OTP expired";
    private static final String SENT_SUCCESS_MESSAGE = "OTP sent successfully";
    private static final String MAX_ATTEMPTS_FORMAT = "Maximum verification attempts exceeded. Please try again in %d %s.";
    private static final String INVALID_OTP_PREFIX = "Invalid OTP";
    private static final String ATTEMPTS_REMAINING_PREFIX = "Attempts remaining: ";

    // Email content
    private static final String VERIFY_EMAIL_TITLE = "Verify your email";
    private static final String VERIFY_EMAIL_BODY = "Your verification code is 123456";
    private static final String RESET_PASSWORD_TITLE = "Reset your password";
    private static final String RESET_PASSWORD_BODY = "Reset code: 123456";

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailTemplate emailTemplate;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private OtpStorageService otpStorageService;

    private OTPServiceImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new OTPServiceImpl(jwtService, emailTemplate, javaMailSender, otpStorageService);
        ReflectionTestUtils.setField(underTest, "fromEmail", FROM_EMAIL);
    }


    @Test
    void testGenerateAndStoreOTPShouldReturnSixDigitCode() {
        String otp = underTest.generateAndStoreOTP(TEST_EMAIL,EmailType.EMAIL_VERIFICATION).join();

        assertNotNull(otp);
        assertEquals(OTP_LENGTH, otp.length());
        assertTrue(otp.matches(OTP_PATTERN));
    }

    @Test
    void testGenerateAndStoreOTPShouldStoreInRedis() {
        underTest.generateAndStoreOTP(TEST_EMAIL,EmailType.EMAIL_VERIFICATION).join();

        verify(otpStorageService).storeOtp(
                eq(TEST_EMAIL),
                anyString(),
                eq(EXPIRE_TIME_IN_MINS_RESET_PASSWORD),
                eq(TimeUnit.SECONDS)
        );
    }

    @Test
    void testGenerateAndStoreOTPShouldInitializeAttemptsCounter() {
        underTest.generateAndStoreOTP(TEST_EMAIL,EmailType.EMAIL_VERIFICATION).join();

        verify(otpStorageService).resetAttempts(
                eq(TEST_EMAIL),
                eq(EXPIRE_TIME_IN_MINS_FOR_EMAIL_VERIFICATION),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void testGenerateAndStoreOTPForResetPasswordShouldReturnSixDigitCode() {
        String otp = underTest.generateAndStoreOTP(RESET_PASSWORD_EMAIL,EmailType.FORGOT_PASSWORD).join();

        assertNotNull(otp);
        assertEquals(OTP_LENGTH, otp.length());
        assertTrue(otp.matches(OTP_PATTERN));
    }

    @Test
    void testGenerateAndStoreOTPForResetPasswordShouldStoreWithSeconds() {
        underTest.generateAndStoreOTP(RESET_PASSWORD_EMAIL, EmailType.FORGOT_PASSWORD).join();

        verify(otpStorageService).storeOtp(
                eq(RESET_PASSWORD_EMAIL),
                anyString(),
                eq(EXPIRE_TIME_IN_MINS_RESET_PASSWORD),
                eq(TimeUnit.SECONDS)
        );
    }

    @Test
    void testGenerateAndStoreOTPForResetPasswordShouldInitializeAttemptsWithHour() {
        underTest.generateAndStoreOTP(RESET_PASSWORD_EMAIL, EmailType.FORGOT_PASSWORD).join();

        verify(otpStorageService).resetAttempts(
                eq(RESET_PASSWORD_EMAIL),
                eq(EXPIRE_TIME_IN_HOURS_RESET_PASSWORD),
                eq(TimeUnit.HOURS)
        );
    }

    @Test
    void testGenerateCodeShouldGenerateUniqueCodes() {
        String email1 = "user1@gmail.com";
        String email2 = "user2@gmail.com";

        String otp1 = underTest.generateAndStoreOTP(email1,EmailType.EMAIL_VERIFICATION).join();
        String otp2 = underTest.generateAndStoreOTP(email2,EmailType.EMAIL_VERIFICATION).join();

        assertNotNull(otp1);
        assertNotNull(otp2);
        assertEquals(OTP_LENGTH, otp1.length());
        assertEquals(OTP_LENGTH, otp2.length());
    }

    @Test
    void testMultipleSuccessfulOTPGenerationForDifferentUsers() {
        String email1 = "user1@gmail.com";
        String email2 = "user2@gmail.com";
        String email3 = "user3@gmail.com";
        int expectedCalls = 3;

        String otp1 = underTest.generateAndStoreOTP(email1,EmailType.EMAIL_VERIFICATION).join();
        String otp2 = underTest.generateAndStoreOTP(email2,EmailType.FORGOT_PASSWORD).join();
        String otp3 = underTest.generateAndStoreOTP(email3,EmailType.EMAIL_VERIFICATION).join();

        assertNotNull(otp1);
        assertNotNull(otp2);
        assertNotNull(otp3);
        verify(otpStorageService, times(expectedCalls)).storeOtp(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        verify(otpStorageService, times(expectedCalls)).resetAttempts(anyString(), anyLong(), any(TimeUnit.class));
    }



    @Test
    void testSendOtpShouldGenerateOTPAndSendEmail() throws Exception {
        EmailRequest emailRequest = createEmailRequest(TEST_EMAIL,EmailType.EMAIL_VERIFICATION);
        OtpEmailContent emailContent = new OtpEmailContent(VERIFY_EMAIL_TITLE, VERIFY_EMAIL_BODY);

        setupSendOtpMocks(TEST_EMAIL,EmailType.EMAIL_VERIFICATION, emailContent);

        OtpVerificationResult result = underTest.sendOtp(emailRequest).join();

        assertTrue(result.isSuccess());
        assertEquals(SENT_SUCCESS_MESSAGE, result.getMessage());
    }

    @Test
    void testSendOtpForResetPasswordShouldGenerateOTPAndSendEmail() throws Exception {
        EmailRequest emailRequest = createEmailRequest(RESET_PASSWORD_EMAIL,EmailType.FORGOT_PASSWORD);
        OtpEmailContent emailContent = new OtpEmailContent(RESET_PASSWORD_TITLE, RESET_PASSWORD_BODY);

        setupSendOtpMocks(RESET_PASSWORD_EMAIL,EmailType.FORGOT_PASSWORD, emailContent);

        underTest.sendOtp(emailRequest).join();

        verify(emailTemplate).buildOtpEmailContent(eq(EmailType.FORGOT_PASSWORD), anyString());
    }

    @Test
    void testSendOtpWithMaxAttemptsExceededShouldReturnErrorMessage() throws Exception {
        EmailRequest emailRequest = createEmailRequest(TEST_EMAIL,EmailType.EMAIL_VERIFICATION);

        when(otpStorageService.hasReachedMaxAttempts(TEST_EMAIL, RedisConfig.OTP_MAX_ATTEMPTS)).thenReturn(true);

        OtpVerificationResult result = underTest.sendOtp(emailRequest).join();

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Maximum verification attempts exceeded"));
    }


    @Test
    void testValidateCodeWithValidOTPShouldReturnSuccess() {
        OtpVerifyRequest request = createOtpVerifyRequest(TEST_EMAIL, VALID_OTP,EmailType.EMAIL_VERIFICATION);

        setupValidOtpMocks(TEST_EMAIL, VALID_OTP);

        OtpVerificationResult result = underTest.validateCode(request).join();

        assertTrue(result.isSuccess());
        assertEquals(SUCCESS_MESSAGE, result.getMessage());
    }

    @Test
    void testValidateCodeWithValidOTPShouldDeleteOTPFromRedis() {
        OtpVerifyRequest request = createOtpVerifyRequest(TEST_EMAIL, VALID_OTP,EmailType.EMAIL_VERIFICATION);

        setupValidOtpMocks(TEST_EMAIL, VALID_OTP);

        underTest.validateCode(request).join();

        verify(otpStorageService).deleteOtp(TEST_EMAIL);
        verify(otpStorageService).deleteAttempts(TEST_EMAIL);
    }

    @Test
    void testValidateCodeWithExpiredOTPShouldReturnFailure() {
        OtpVerifyRequest request = createOtpVerifyRequest(TEST_EMAIL, VALID_OTP,EmailType.EMAIL_VERIFICATION);

        when(otpStorageService.getOtp(TEST_EMAIL)).thenReturn(null);

        OtpVerificationResult result = underTest.validateCode(request).join();

        assertFalse(result.isSuccess());
        assertEquals(EXPIRED_MESSAGE, result.getMessage());
    }

    @Test
    void testValidateCodeWithInvalidOTPShouldReturnFailure() {
        OtpVerifyRequest request = createOtpVerifyRequest(TEST_EMAIL, INVALID_OTP,EmailType.EMAIL_VERIFICATION);

        setupInvalidOtpMocks(TEST_EMAIL, VALID_OTP);

        OtpVerificationResult result = underTest.validateCode(request).join();

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains(INVALID_OTP_PREFIX));
    }

    @Test
    void testValidateCodeWithInvalidOTPShouldIncrementAttempts() {
        OtpVerifyRequest request = createOtpVerifyRequest(TEST_EMAIL, INVALID_OTP,EmailType.EMAIL_VERIFICATION);

        setupInvalidOtpMocks(TEST_EMAIL, VALID_OTP);

        underTest.validateCode(request).join();

        verify(otpStorageService).incrementAttempts(
                eq(TEST_EMAIL),
                eq(EXPIRE_TIME_IN_MINS_FOR_EMAIL_VERIFICATION),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void testValidateCodeWithMaxAttemptsExceededShouldReturnFailure() {
        OtpVerifyRequest request = createOtpVerifyRequest(TEST_EMAIL, VALID_OTP, EmailType.EMAIL_VERIFICATION);
        long maxAttempts = 3L;

        setupMaxAttemptsExceededMocks(TEST_EMAIL, maxAttempts);

        OtpVerificationResult result = underTest.validateCode(request).join();
        String expectedMessage = String.format(MAX_ATTEMPTS_FORMAT,
                EXPIRE_TIME_IN_MINS_FOR_EMAIL_VERIFICATION, "minute(s)");

        assertFalse(result.isSuccess());
        assertEquals(expectedMessage, result.getMessage());
    }

    @Test
    void testValidateCodeWithMaxAttemptsExceededShouldDeleteOTPAndAttempts() {
        OtpVerifyRequest request = createOtpVerifyRequest(TEST_EMAIL, VALID_OTP,EmailType.EMAIL_VERIFICATION);
        long maxAttempts = 3L;

        setupMaxAttemptsExceededMocks(TEST_EMAIL, maxAttempts);

        underTest.validateCode(request).join();

        verify(otpStorageService).deleteOtp(TEST_EMAIL);
    }

    @Test
    void testValidateCodeForResetPasswordWithValidOTPShouldReturnSuccess() {
        OtpVerifyRequest request = createOtpVerifyRequest(RESET_PASSWORD_EMAIL, INVALID_OTP,EmailType.FORGOT_PASSWORD);

        setupValidOtpMocks(RESET_PASSWORD_EMAIL, INVALID_OTP);

        OtpVerificationResult result = underTest.validateCode(request).join();

        assertTrue(result.isSuccess());
        assertEquals(SUCCESS_MESSAGE, result.getMessage());
    }

    @Test
    void testValidateCodeForResetPasswordWithHourBasedAttempts() {
        OtpVerifyRequest request = createOtpVerifyRequest(RESET_PASSWORD_EMAIL, VALID_OTP,EmailType.FORGOT_PASSWORD);

        setupInvalidOtpMocks(RESET_PASSWORD_EMAIL, INVALID_OTP);

        underTest.validateCode(request).join();

        verify(otpStorageService).incrementAttempts(
                eq(RESET_PASSWORD_EMAIL),
                eq(EXPIRE_TIME_IN_HOURS_RESET_PASSWORD),
                eq(TimeUnit.HOURS)
        );
    }

    @Test
    void testValidateCodeForResetPasswordWithMaxAttemptsExceededShouldDeleteOTP() {
        OtpVerifyRequest request = createOtpVerifyRequest(RESET_PASSWORD_EMAIL, VALID_OTP,EmailType.FORGOT_PASSWORD);
        long maxAttempts = 3L;

        setupMaxAttemptsExceededMocks(RESET_PASSWORD_EMAIL, maxAttempts);

        OtpVerificationResult result = underTest.validateCode(request).join();
        String expectedMessage = String.format(MAX_ATTEMPTS_FORMAT,
                EXPIRE_TIME_IN_HOURS_RESET_PASSWORD, "hour(s)");

        assertFalse(result.isSuccess());
        assertEquals(expectedMessage, result.getMessage());
        verify(otpStorageService).deleteOtp(RESET_PASSWORD_EMAIL);
    }

    @Test
    void testValidateCodeWithValidOTPShouldGenerateJwtToken() {
        String email = "user@gmail.com";
        OtpVerifyRequest request = createOtpVerifyRequest(email, VALID_OTP,EmailType.EMAIL_VERIFICATION);

        setupValidOtpMocks(email, VALID_OTP);

        OtpVerificationResult result = underTest.validateCode(request).join();

        assertTrue(result.isSuccess());
        assertEquals(EXPECTED_TOKEN, result.getToken());
        verify(jwtService).generateVerifyToken(email);
    }

    @Test
    void testValidateCodeShouldReturnRemainingAttempts() {
        String email = "user@gmail.com";
        long currentAttempts = 1L;
        long remainingAttempts = 1L;
        OtpVerifyRequest request = createOtpVerifyRequest(email, INVALID_OTP,EmailType.EMAIL_VERIFICATION);

        when(otpStorageService.getOtp(email)).thenReturn(VALID_OTP);
        when(otpStorageService.getAttempts(email)).thenReturn(currentAttempts);
        when(otpStorageService.hasReachedMaxAttempts(email, RedisConfig.OTP_MAX_ATTEMPTS)).thenReturn(false);

        OtpVerificationResult result = underTest.validateCode(request).join();

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains(ATTEMPTS_REMAINING_PREFIX + remainingAttempts));
    }

    @Test
    void testValidateCodeEmptyTokenWhenFailed() {
        String email = "user@gmail.com";
        OtpVerifyRequest request = createOtpVerifyRequest(email, VALID_OTP,EmailType.EMAIL_VERIFICATION);

        when(otpStorageService.getOtp(email)).thenReturn(null);

        OtpVerificationResult result = underTest.validateCode(request).join();

        assertFalse(result.isSuccess());
        assertEquals("", result.getToken());
    }


    private EmailRequest createEmailRequest(String email, EmailType emailType) {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setRecipientEmail(email);
        emailRequest.setEmailType(emailType);
        return emailRequest;
    }

    private OtpVerifyRequest createOtpVerifyRequest(String email, String code,EmailType emailType) {
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setRecipientEmail(email);
        request.setCode(code);
        request.setEmailType(emailType);
        return request;
    }

    private void setupSendOtpMocks(String email,EmailType emailType , OtpEmailContent emailContent) {
        when(emailTemplate.buildOtpEmailContent(eq(emailType), anyString())).thenReturn(emailContent);
        when(otpStorageService.hasReachedMaxAttempts(email, RedisConfig.OTP_MAX_ATTEMPTS)).thenReturn(false);
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    private void setupValidOtpMocks(String email, String otp) {
        when(otpStorageService.getOtp(email)).thenReturn(otp);
        when(otpStorageService.getAttempts(email)).thenReturn(0L);
        when(otpStorageService.hasReachedMaxAttempts(email, RedisConfig.OTP_MAX_ATTEMPTS)).thenReturn(false);
        when(jwtService.generateVerifyToken(email)).thenReturn(OTPServiceTest.EXPECTED_TOKEN);
    }

    private void setupInvalidOtpMocks(String email, String storedOtp) {
        when(otpStorageService.getOtp(email)).thenReturn(storedOtp);
        when(otpStorageService.getAttempts(email)).thenReturn(0L);
        when(otpStorageService.hasReachedMaxAttempts(email, RedisConfig.OTP_MAX_ATTEMPTS)).thenReturn(false);
    }

    private void setupMaxAttemptsExceededMocks(String email, long attempts) {
        when(otpStorageService.getOtp(email)).thenReturn(OTPServiceTest.INVALID_OTP);
        when(otpStorageService.getAttempts(email)).thenReturn(attempts);
        when(otpStorageService.hasReachedMaxAttempts(email, RedisConfig.OTP_MAX_ATTEMPTS)).thenReturn(true);
    }

    @Test
    void testGenerateAndStoreOTPShouldReturnDifferentCodesForSameEmail() {
        String otp1 = underTest.generateAndStoreOTP(TEST_EMAIL,EmailType.EMAIL_VERIFICATION).join();
        String otp2 = underTest.generateAndStoreOTP(TEST_EMAIL,EmailType.EMAIL_VERIFICATION).join();

        assertNotNull(otp1);
        assertNotNull(otp2);
        assertEquals(OTP_LENGTH, otp1.length());
        assertEquals(OTP_LENGTH, otp2.length());
        assertTrue(otp1.matches(OTP_PATTERN));
        assertTrue(otp2.matches(OTP_PATTERN));
    }

    @Test
    void testSendOtpShouldReturnSuccessWithCorrectMessage() throws Exception {
        EmailRequest emailRequest = createEmailRequest(TEST_EMAIL,EmailType.EMAIL_VERIFICATION);
        OtpEmailContent emailContent = new OtpEmailContent(VERIFY_EMAIL_TITLE, VERIFY_EMAIL_BODY);

        setupSendOtpMocks(TEST_EMAIL,EmailType.EMAIL_VERIFICATION, emailContent);

        OtpVerificationResult result = underTest.sendOtp(emailRequest).join();

        assertTrue(result.isSuccess());
        assertEquals(SENT_SUCCESS_MESSAGE, result.getMessage());
        assertNotNull(result.getToken());
    }

    @Test
    void testValidateCodeShouldReturnCorrectTokenForValidOTP() {
        String email = "testuser@gmail.com";
        OtpVerifyRequest request = createOtpVerifyRequest(email, VALID_OTP,EmailType.EMAIL_VERIFICATION);

        setupValidOtpMocks(email, VALID_OTP);

        OtpVerificationResult result = underTest.validateCode(request).join();

        assertTrue(result.isSuccess());
        assertEquals(EXPECTED_TOKEN, result.getToken());
        verify(jwtService).generateVerifyToken(email);
    }

    @Test
    void testValidateCodeWithInvalidOTPShouldNotGenerateToken() {
        OtpVerifyRequest request = createOtpVerifyRequest(TEST_EMAIL, INVALID_OTP,EmailType.EMAIL_VERIFICATION);

        setupInvalidOtpMocks(TEST_EMAIL, VALID_OTP);

        OtpVerificationResult result = underTest.validateCode(request).join();

        assertFalse(result.isSuccess());
        assertTrue(result.getToken().isEmpty());
        verify(jwtService, never()).generateVerifyToken(any());
    }

    @Test
    void testValidateCodeShouldIncrementAttemptsMultipleTimes() {
        OtpVerifyRequest request1 = createOtpVerifyRequest(TEST_EMAIL, INVALID_OTP,EmailType.EMAIL_VERIFICATION);
        OtpVerifyRequest request2 = createOtpVerifyRequest(TEST_EMAIL, INVALID_OTP,EmailType.EMAIL_VERIFICATION);

        setupInvalidOtpMocks(TEST_EMAIL, VALID_OTP);

        underTest.validateCode(request1).join();
        underTest.validateCode(request2).join();

        verify(otpStorageService, times(2)).incrementAttempts(
                eq(TEST_EMAIL),
                eq(EXPIRE_TIME_IN_MINS_FOR_EMAIL_VERIFICATION),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void testValidateCodeShouldDeleteBothOtpAndAttemptsOnSuccess() {
        OtpVerifyRequest request = createOtpVerifyRequest(TEST_EMAIL, VALID_OTP,EmailType.EMAIL_VERIFICATION);

        setupValidOtpMocks(TEST_EMAIL, VALID_OTP);

        underTest.validateCode(request).join();

        verify(otpStorageService).deleteOtp(TEST_EMAIL);
        verify(otpStorageService).deleteAttempts(TEST_EMAIL);
    }

    @Test
    void testValidateCodeForResetPasswordShouldUseHourTimeUnit() {
        OtpVerifyRequest request = createOtpVerifyRequest(RESET_PASSWORD_EMAIL, INVALID_OTP,EmailType.FORGOT_PASSWORD);

        setupInvalidOtpMocks(RESET_PASSWORD_EMAIL, VALID_OTP);

        underTest.validateCode(request).join();

        verify(otpStorageService).incrementAttempts(
                eq(RESET_PASSWORD_EMAIL),
                eq(EXPIRE_TIME_IN_HOURS_RESET_PASSWORD),
                eq(TimeUnit.HOURS)
        );
    }

    @Test
    void testValidateCodeWithMaxAttemptsExceededShouldDeleteOtpOnly() {
        OtpVerifyRequest request = createOtpVerifyRequest(TEST_EMAIL, VALID_OTP,EmailType.EMAIL_VERIFICATION);
        long maxAttempts = 3L;

        setupMaxAttemptsExceededMocks(TEST_EMAIL, maxAttempts);

        underTest.validateCode(request).join();

        verify(otpStorageService).deleteOtp(TEST_EMAIL);
    }

    @Test
    void testSendOtpShouldCheckMaxAttemptsBeforeSending() throws Exception {
        EmailRequest emailRequest = createEmailRequest(TEST_EMAIL,EmailType.EMAIL_VERIFICATION);

        when(otpStorageService.hasReachedMaxAttempts(TEST_EMAIL, RedisConfig.OTP_MAX_ATTEMPTS)).thenReturn(true);

        OtpVerificationResult result = underTest.sendOtp(emailRequest).join();

        assertFalse(result.isSuccess());
        verify(otpStorageService).hasReachedMaxAttempts(TEST_EMAIL, RedisConfig.OTP_MAX_ATTEMPTS);
    }

    @Test
    void testValidateCodeWithNullOtpShouldReturnExpiredMessage() {
        OtpVerifyRequest request = createOtpVerifyRequest(TEST_EMAIL, VALID_OTP,EmailType.EMAIL_VERIFICATION);

        when(otpStorageService.getOtp(TEST_EMAIL)).thenReturn(null);

        OtpVerificationResult result = underTest.validateCode(request).join();

        assertFalse(result.isSuccess());
        assertEquals(EXPIRED_MESSAGE, result.getMessage());
    }

    @Test
    void testValidateCodeShouldVerifyCorrectOtpFormat() {
        OtpVerifyRequest request = createOtpVerifyRequest(TEST_EMAIL, VALID_OTP,EmailType.EMAIL_VERIFICATION);

        setupValidOtpMocks(TEST_EMAIL, VALID_OTP);

        OtpVerificationResult result = underTest.validateCode(request).join();

        assertTrue(result.isSuccess());
        // Verify that the OTP stored in Redis was exactly what we passed
        verify(otpStorageService).getOtp(TEST_EMAIL);
    }

    @Test
    void testMultipleEmailTypesForOTPGeneration() {
        EmailType[] emailTypes = {
           EmailType.EMAIL_VERIFICATION,
           EmailType.FORGOT_PASSWORD
        };

        for (EmailType emailType : emailTypes) {
            String otp = underTest.generateAndStoreOTP(TEST_EMAIL + emailType.toString(), emailType).join();
            assertNotNull(otp);
            assertEquals(OTP_LENGTH, otp.length());
            assertTrue(otp.matches(OTP_PATTERN));
        }

        verify(otpStorageService, times(2)).storeOtp(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testValidateCodeShouldCheckAttemptsBeforeReturningError() {
        String email = "check@gmail.com";
        long remainingAttempts = 2L;
        OtpVerifyRequest request = createOtpVerifyRequest(email, INVALID_OTP,EmailType.EMAIL_VERIFICATION);

        when(otpStorageService.getOtp(email)).thenReturn(VALID_OTP);
        when(otpStorageService.getAttempts(email)).thenReturn(remainingAttempts);
        when(otpStorageService.hasReachedMaxAttempts(email, RedisConfig.OTP_MAX_ATTEMPTS)).thenReturn(false);

        OtpVerificationResult result = underTest.validateCode(request).join();

        assertFalse(result.isSuccess());
        verify(otpStorageService).getAttempts(email);
    }
}