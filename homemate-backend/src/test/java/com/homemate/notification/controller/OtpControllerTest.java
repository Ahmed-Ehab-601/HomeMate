package com.homemate.notification.controller;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.OtpVerificationResult;
import com.homemate.notification.domains.dto.OtpVerifyRequest;
import com.homemate.notification.domains.model.EmailType;
import com.homemate.notification.service.OTPService;
import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.TaskerProfile.models.Tasker;
import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.UserProfile.Models.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpControllerTest {

    @Mock
    private OTPService otpService;

    @Mock
    private UserDao userDao;

    @Mock
    private TaskerDao taskerDao;

    @InjectMocks
    private OtpController otpController;

    private EmailRequest emailRequest;
    private OtpVerificationResult successResult;
    private OtpVerificationResult failureResult;
    private User mockUser;
    private Tasker mockTasker;
    private String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        emailRequest = EmailRequest.builder()
                .recipientEmail(testEmail)
                .emailType(EmailType.EMAIL_VERIFICATION)
                .build();

        successResult = OtpVerificationResult.builder()
                .success(true)
                .message("OTP sent successfully")
                .build();

        failureResult = OtpVerificationResult.builder()
                .success(false)
                .message("Failed to send OTP")
                .build();

        mockUser = new User();
        mockUser.setUserID(1L);
        mockUser.setEmail(testEmail);

        mockTasker = new Tasker();
        mockTasker.setTaskerID(1L);
        mockTasker.setEmail(testEmail);
    }

    @Test
    void testSendOtpSignupWithValidNewUser() throws ExecutionException, InterruptedException {
        when(userDao.getByEmail(testEmail)).thenReturn(null);
        when(taskerDao.getByEmail(testEmail)).thenReturn(null);
        when(otpService.sendOtp(any(EmailRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(successResult));

        ResponseEntity<OtpVerificationResult> response = otpController.sendOtp("signup", emailRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("OTP sent successfully", response.getBody().getMessage());
        verify(otpService, times(1)).sendOtp(any(EmailRequest.class));
    }

    @Test
    void testSendOtpSignupWithExistingUser() throws ExecutionException, InterruptedException {
        when(userDao.getByEmail(testEmail)).thenReturn(mockUser);

        ResponseEntity<OtpVerificationResult> response = otpController.sendOtp("signup", emailRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User already exists with this email address", response.getBody().getMessage());
        verify(otpService, never()).sendOtp(any(EmailRequest.class));
    }

    @Test
    void testSendOtpSignupWithExistingTasker() throws ExecutionException, InterruptedException {
        when(userDao.getByEmail(testEmail)).thenReturn(null);
        when(taskerDao.getByEmail(testEmail)).thenReturn(mockTasker);

        ResponseEntity<OtpVerificationResult> response = otpController.sendOtp("signup", emailRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User already exists with this email address", response.getBody().getMessage());
        verify(otpService, never()).sendOtp(any(EmailRequest.class));
    }

    @Test
    void testSendOtpForgotPasswordWithExistingUser() throws ExecutionException, InterruptedException {
        when(userDao.getByEmail(testEmail)).thenReturn(mockUser);
        when(otpService.sendOtp(any(EmailRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(successResult));

        ResponseEntity<OtpVerificationResult> response = otpController.sendOtp("forgetpassword", emailRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("OTP sent successfully", response.getBody().getMessage());
        verify(otpService, times(1)).sendOtp(any(EmailRequest.class));
    }

    @Test
    void testSendOtpForgotPasswordWithExistingTasker() throws ExecutionException, InterruptedException {
        when(userDao.getByEmail(testEmail)).thenReturn(null);
        when(taskerDao.getByEmail(testEmail)).thenReturn(mockTasker);
        when(otpService.sendOtp(any(EmailRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(successResult));

        ResponseEntity<OtpVerificationResult> response = otpController.sendOtp("forgetpassword", emailRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("OTP sent successfully", response.getBody().getMessage());
        verify(otpService, times(1)).sendOtp(any(EmailRequest.class));
    }

    @Test
    void testSendOtpForgotPasswordWithNonExistingUser() throws ExecutionException, InterruptedException {
        when(userDao.getByEmail(testEmail)).thenReturn(null);
        when(taskerDao.getByEmail(testEmail)).thenReturn(null);

        ResponseEntity<OtpVerificationResult> response = otpController.sendOtp("forgetpassword", emailRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User not found with this email address", response.getBody().getMessage());
        verify(otpService, never()).sendOtp(any(EmailRequest.class));
    }

    @Test
    void testSendOtpWithInvalidType() throws ExecutionException, InterruptedException {
        ResponseEntity<OtpVerificationResult> response = otpController.sendOtp("invalidType", emailRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid otp request type", response.getBody().getMessage());
        verify(otpService, never()).sendOtp(any(EmailRequest.class));
        verify(userDao, never()).getByEmail(any());
        verify(taskerDao, never()).getByEmail(any());
    }

    @Test
    void testSendOtpWithEmptyType() throws ExecutionException, InterruptedException {
        ResponseEntity<OtpVerificationResult> response = otpController.sendOtp("", emailRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid otp request type", response.getBody().getMessage());
        verify(otpService, never()).sendOtp(any(EmailRequest.class));
    }

    @Test
    void testSendOtpWithNullType() throws ExecutionException, InterruptedException {
        ResponseEntity<OtpVerificationResult> response = otpController.sendOtp("randomType", emailRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid otp request type", response.getBody().getMessage());
    }

    // ============ CASE INSENSITIVITY TESTS ============

    @Test
    void testSendOtpSignupCaseInsensitivity() throws ExecutionException, InterruptedException {
        when(userDao.getByEmail(testEmail)).thenReturn(null);
        when(taskerDao.getByEmail(testEmail)).thenReturn(null);
        when(otpService.sendOtp(any(EmailRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(successResult));

        ResponseEntity<OtpVerificationResult> response = otpController.sendOtp("SIGNUP", emailRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        verify(otpService, times(1)).sendOtp(any(EmailRequest.class));
    }

    @Test
    void testSendOtpForgotPasswordCaseInsensitivity() throws ExecutionException, InterruptedException {
        when(userDao.getByEmail(testEmail)).thenReturn(mockUser);
        when(otpService.sendOtp(any(EmailRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(successResult));

        ResponseEntity<OtpVerificationResult> response = otpController.sendOtp("FORGETPASSWORD", emailRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        verify(otpService, times(1)).sendOtp(any(EmailRequest.class));
    }

    @Test
    void testSendOtpWithServiceFailure() throws ExecutionException, InterruptedException {
        when(userDao.getByEmail(testEmail)).thenReturn(null);
        when(taskerDao.getByEmail(testEmail)).thenReturn(null);
        when(otpService.sendOtp(any(EmailRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(failureResult));

        ResponseEntity<OtpVerificationResult> response = otpController.sendOtp("signup", emailRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to send OTP", response.getBody().getMessage());
        verify(otpService, times(1)).sendOtp(any(EmailRequest.class));
    }

    @Test
    void testVerifyOtpSuccess() throws ExecutionException, InterruptedException {
        OtpVerifyRequest verifyRequest = OtpVerifyRequest.builder()
                .recipientEmail(testEmail)
                .code("123456")
                .emailType(EmailType.EMAIL_VERIFICATION)
                .build();

        OtpVerificationResult verifySuccessResult = OtpVerificationResult.builder()
                .success(true)
                .message("OTP verified successfully")
                .token("jwt-token-123")
                .build();

        when(otpService.validateCode(any(OtpVerifyRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(verifySuccessResult));

        ResponseEntity<OtpVerificationResult> response = otpController.verifyOtp(verifyRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("OTP verified successfully", response.getBody().getMessage());
        assertEquals("jwt-token-123", response.getBody().getToken());
        verify(otpService, times(1)).validateCode(any(OtpVerifyRequest.class));
    }

    @Test
    void testVerifyOtpFailure() throws ExecutionException, InterruptedException {
        OtpVerifyRequest verifyRequest = OtpVerifyRequest.builder()
                .recipientEmail(testEmail)
                .code("000000")
                .emailType(EmailType.EMAIL_VERIFICATION)
                .build();

        OtpVerificationResult verifyFailureResult = OtpVerificationResult.builder()
                .success(false)
                .message("Invalid OTP")
                .build();

        when(otpService.validateCode(any(OtpVerifyRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(verifyFailureResult));

        ResponseEntity<OtpVerificationResult> response = otpController.verifyOtp(verifyRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid OTP", response.getBody().getMessage());
        verify(otpService, times(1)).validateCode(any(OtpVerifyRequest.class));
    }
}
