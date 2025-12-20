package com.homemate.notification.service.impl;

import com.homemate.notification.config.RedisConfig;
import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.OtpVerificationResult;
import com.homemate.notification.domains.dto.OtpVerifyRequest;
import com.homemate.notification.domains.model.EmailType;
import com.homemate.notification.service.OTPService;
import com.homemate.notification.service.OtpStorageService;
import com.homemate.notification.service.utils.EmailTemplate;
import com.homemate.security.service.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.homemate.notification.domains.model.EmailType.EMAIL_VERIFICATION;
import static com.homemate.notification.domains.model.EmailType.FORGOT_PASSWORD;

@Slf4j
@Validated
@RequiredArgsConstructor
@Component
public class OTPServiceImpl implements OTPService {

    private static final String HOUR_S = "hour(s)";
    private static final String MINUTE_S = "minute(s)";
    private static final String MAX_ATTEMPTS_MESSAGE = "Maximum verification attempts exceeded. Please try again in %d %s.";
    private static final String OTP_SENT_SUCCESSFULLY = "OTP sent successfully";
    private static final String OTP_SEND_FAILED = "Failed to send OTP email. Please try again later.";
    private static final String UNSUPPORTED_EMAIL_TYPE = "Unsupported Email Type";
    private static final String INVALID_OTP_ATTEMPTS_REMAINING = "Invalid OTP. Attempts remaining: ";
    private static final String VERIFICATION_SUCCESS_MESSAGE = "OTP verified successfully";
    private static final String OTP_EXPIRED_MESSAGE = "OTP expired";
    private static final String TASK_EMAIL_NO_OTP_MESSAGE = "Task-related emails do not require OTP verification";

    @Value("${homemate.email.from:homemateservice8@gmail.com}")
    private String fromEmail;

    private final JwtService jwtService;
    private final EmailTemplate emailTemplate;
    private final JavaMailSender javaMailSender;
    private final OtpStorageService otpStorageService;

    @Async("otpExecutor")
    public CompletableFuture<String> generateAndStoreOTP(String email,EmailType emailType) {
        int attemptTtl = getAttemptTtl(emailType);
        TimeUnit attemptTtlUnit = getTimeUnit(emailType);

        // Reset attempts counter when generating new OTP
        otpStorageService.resetAttempts(email, attemptTtl, attemptTtlUnit);

        // Generate and store OTP
        String otp = generateCode();
        otpStorageService.storeOtp(email, otp, RedisConfig.OTP_TTL_SEC, TimeUnit.SECONDS);

        log.info("Generated and stored OTP for email: {}", email);
        return CompletableFuture.completedFuture(otp);
    }

    @Async("otpExecutor")
    @Override
    public CompletableFuture<OtpVerificationResult> validateCode(OtpVerifyRequest otpVerifyRequest) {
        String email = otpVerifyRequest.getRecipientEmail();
        int attemptTtl = getAttemptTtl(otpVerifyRequest.getEmailType());
        TimeUnit attemptTtlUnit = getTimeUnit(otpVerifyRequest.getEmailType());

        // Check if OTP exists
        String cachedOtp = otpStorageService.getOtp(email);
        if (cachedOtp == null) {
            log.warn("OTP validation failed - OTP expired or not found for email: {}", email);
            return expiredOtpResult();
        }

        // Check if max attempts reached
        long attempts = otpStorageService.getAttempts(email);
        if (otpStorageService.hasReachedMaxAttempts(email, RedisConfig.OTP_MAX_ATTEMPTS)) {
            log.warn("OTP validation failed - Max attempts exceeded for email: {}", email);
            otpStorageService.deleteOtp(email);
            String timeUnit = formatTimeUnit(attemptTtlUnit);
            return maxAttemptsExceededResult(attemptTtl, timeUnit);
        }

        // Validate OTP code
        if (cachedOtp.equals(otpVerifyRequest.getCode())) {
            log.info("OTP validation successful for email: {}", email);
            otpStorageService.deleteOtp(email);
            otpStorageService.deleteAttempts(email);
            return successResult(otpVerifyRequest.getRecipientEmail());
        }

        // Increment attempts on failed validation
        otpStorageService.incrementAttempts(email, attemptTtl, attemptTtlUnit);
        long remainingAttempts = RedisConfig.OTP_MAX_ATTEMPTS - (attempts + 1);

        log.warn("OTP validation failed - Invalid code for email: {}. Remaining attempts: {}",
                email, remainingAttempts);

        return invalidOtpResult(remainingAttempts);
    }

    @Override
    @Async("otpExecutor")
    public CompletableFuture<OtpVerificationResult> sendOtp(EmailRequest emailRequest)
            throws ExecutionException, InterruptedException {

        EmailType emailType = emailRequest.getEmailType();

        if ((emailType != EMAIL_VERIFICATION && emailType != FORGOT_PASSWORD)) {
            return handleNonOtpEmailType(emailType, emailRequest.getRecipientEmail());
        }

        String email = emailRequest.getRecipientEmail();
        int attemptTtl = getAttemptTtl(emailType);
        TimeUnit attemptTtlUnit = getTimeUnit(emailType);

        // Check if max attempts already reached
        if (otpStorageService.hasReachedMaxAttempts(email, RedisConfig.OTP_MAX_ATTEMPTS)) {
            log.warn("OTP send blocked - Max attempts already reached for email: {}", email);
            String timeUnit = formatTimeUnit(attemptTtlUnit);
            return maxAttemptsExceededResult(attemptTtl, timeUnit);
        }

        String otpCode = generateAndStoreOTP(email, emailType).get();
        EmailTemplate.OtpEmailContent emailContent = emailTemplate.buildOtpEmailContent(emailType, otpCode);

        return sendOtpEmail(email, emailContent);
    }

    private CompletableFuture<OtpVerificationResult> sendOtpEmail(String email, EmailTemplate.OtpEmailContent content) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject(content.subject());

            String htmlContent = buildHtmlContent(content.subject(), content.body());
            helper.setText(htmlContent, true);
            helper.addInline("logo", new ClassPathResource("logo.png"));

            javaMailSender.send(mimeMessage);

            log.info("OTP email sent successfully to: {}", email);
            return otpSentSuccessResult();

        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", email, e);
            return otpSendFailedResult();
        }
    }

    private String buildHtmlContent(String title, String body) {
        return "<html><body>" +
                "<img src='cid:logo' style='width:200px; height:auto;' />" +
                "<h2>" + title + "</h2>" +
                "<p>" + body + "</p>" +
                "</body></html>";
    }

    private CompletableFuture<OtpVerificationResult> handleNonOtpEmailType(
            EmailType emailType, String email) {

        if (emailType != null && isTaskRelatedEmail(emailType)) {
            log.warn("Task-related email type {} should not use OTP verification for email: {}",
                    emailType, email);
            return taskEmailNoOtpResult();
        }

        log.error("Unsupported email type: {} for email: {}", emailType, email);
        return unsupportedEmailTypeResult();
    }

    private boolean isTaskRelatedEmail(EmailType emailType) {
        return emailType == EmailType.TASK_REQUEST ||
                emailType == EmailType.TASK_STATUS ||
                emailType == EmailType.TASK_RESCHEDULE ||
                emailType == EmailType.TASK_RESUMED;
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int otp = random.nextInt(1000000);
        return String.format("%06d", otp);
    }

    private static int getAttemptTtl(EmailType emailType) {
        return emailType == FORGOT_PASSWORD
                ? RedisConfig.OTP_ATTEMPT_TTL_HOURS_RESET_PASSWORD
                : RedisConfig.OTP_ATTEMPT_TTL_MINUTES;
    }

    private static TimeUnit getTimeUnit(EmailType emailType) {
        return emailType == FORGOT_PASSWORD ? TimeUnit.HOURS : TimeUnit.MINUTES;
    }

    private static String formatTimeUnit(TimeUnit timeUnit) {
        return timeUnit == TimeUnit.HOURS ? HOUR_S : MINUTE_S;
    }

    private CompletableFuture<OtpVerificationResult> successResult(String email) {
        return CompletableFuture.completedFuture(
                OtpVerificationResult.builder()
                        .success(true)
                        .message(VERIFICATION_SUCCESS_MESSAGE)
                        .token(jwtService.generateVerifyToken(email))
                        .build()
        );
    }

    private CompletableFuture<OtpVerificationResult> invalidOtpResult(long remainingAttempts) {
        return CompletableFuture.completedFuture(
                OtpVerificationResult.builder()
                        .success(false)
                        .message(INVALID_OTP_ATTEMPTS_REMAINING + remainingAttempts)
                        .token("")
                        .build()
        );
    }

    private CompletableFuture<OtpVerificationResult> expiredOtpResult() {
        return CompletableFuture.completedFuture(
                OtpVerificationResult.builder()
                        .success(false)
                        .message(OTP_EXPIRED_MESSAGE)
                        .token("")
                        .build()
        );
    }

    private CompletableFuture<OtpVerificationResult> maxAttemptsExceededResult(int ttl, String timeUnit) {
        return CompletableFuture.completedFuture(
                OtpVerificationResult.builder()
                        .success(false)
                        .message(String.format(MAX_ATTEMPTS_MESSAGE, ttl, timeUnit))
                        .token("")
                        .build()
        );
    }

    private CompletableFuture<OtpVerificationResult> otpSentSuccessResult() {
        return CompletableFuture.completedFuture(
                OtpVerificationResult.builder()
                        .success(true)
                        .message(OTP_SENT_SUCCESSFULLY)
                        .token("")
                        .build()
        );
    }

    private CompletableFuture<OtpVerificationResult> otpSendFailedResult() {
        return CompletableFuture.completedFuture(
                OtpVerificationResult.builder()
                        .success(false)
                        .message(OTP_SEND_FAILED)
                        .token("")
                        .build()
        );
    }

    private CompletableFuture<OtpVerificationResult> unsupportedEmailTypeResult() {
        return CompletableFuture.completedFuture(
                OtpVerificationResult.builder()
                        .success(false)
                        .message(UNSUPPORTED_EMAIL_TYPE)
                        .token("")
                        .build()
        );
    }

    private CompletableFuture<OtpVerificationResult> taskEmailNoOtpResult() {
        return CompletableFuture.completedFuture(
                OtpVerificationResult.builder()
                        .success(false)
                        .message(TASK_EMAIL_NO_OTP_MESSAGE)
                        .token("")
                        .build()
        );
    }
}