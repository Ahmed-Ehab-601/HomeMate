package com.homemate.notification.service.imp;

import com.homemate.notification.config.RedisConfig;
import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.OtpVerificationResult;
import com.homemate.notification.domains.dto.OtpVerifyRequest;
import com.homemate.notification.service.OTPService;
import com.homemate.notification.service.utils.EmailTemplate;
import com.homemate.security.service.JwtService;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
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

import static com.homemate.notification.domains.dto.EmailRequest.EmailType.EMAIL_VERIFICATION;
import static com.homemate.notification.domains.dto.EmailRequest.EmailType.FORGOT_PASSWORD;

@Validated
@RequiredArgsConstructor
@Component
public class OTPServiceImp implements OTPService {
    private final JwtService jwtService;
    private final EmailTemplate emailTemplate;
    private final JavaMailSender javaMailSender;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String OTP_PREFIX ="otp:";
    public static final String ATTEMPTS_PREFIX ="otp_attempts:";
    private static final String HOMEMATE_EMAIL ="homematesevice8@gmail.com";

    @Async("otpExecutor")
    public CompletableFuture<String> generateAndStoreOTP(String email, EmailRequest.EmailType emailType) {
        String attemptsKey=ATTEMPTS_PREFIX+email;
        int attemptTtl =emailType == FORGOT_PASSWORD ? RedisConfig.OTP_ATTEMPT_TTL_HOURS_RESET_PASSWORD : RedisConfig.OTP_ATTEMPT_TTL_MINUTES;
        TimeUnit attemptTtlUnit =emailType == FORGOT_PASSWORD ? TimeUnit.HOURS : TimeUnit.MINUTES;
         redisTemplate.opsForValue().set(
                    attemptsKey,
                    "0",
                    attemptTtl,
                    attemptTtlUnit);
        String otp =generateCode();
        String otpCodeKey=OTP_PREFIX+email;
        redisTemplate.opsForValue().set(
                otpCodeKey,
            otp,
            RedisConfig.OTP_TTL_SEC,
            TimeUnit.SECONDS
        );

        return CompletableFuture.completedFuture(otp);
    }
    @Async("otpExecutor")
    @Override
    public CompletableFuture<OtpVerificationResult> validateCode(OtpVerifyRequest otpVerifyRequest) {
        String otpCodeKey=OTP_PREFIX+otpVerifyRequest.getRecipientEmail();
        String attemptsKey=ATTEMPTS_PREFIX+otpVerifyRequest.getRecipientEmail();
        int attemptTtl= otpVerifyRequest.getEmailType() == FORGOT_PASSWORD ? RedisConfig.OTP_ATTEMPT_TTL_HOURS_RESET_PASSWORD : RedisConfig.OTP_ATTEMPT_TTL_MINUTES;
        TimeUnit attemptTtlUnit =otpVerifyRequest.getEmailType() == FORGOT_PASSWORD ? TimeUnit.HOURS : TimeUnit.MINUTES;
        String cachedOtp=(String)redisTemplate.opsForValue().get(otpCodeKey);
        if (cachedOtp==null) {
            return CompletableFuture.completedFuture(
                OtpVerificationResult.builder()
                    .success(false)
                    .message("OTP expired")
                    .token("")
                    .build()
            );
        }

        Object attemptsObject=redisTemplate.opsForValue().get(attemptsKey);
        long attempts;
        if (attemptsObject==null) {
            redisTemplate.opsForValue().set(
                    attemptsKey,
                    "0",
                    attemptTtl,
                    attemptTtlUnit
            );
            attempts =0;
        }
        else {
            attempts =Long.parseLong((String) attemptsObject);
        }

        if (attempts>=RedisConfig.OTP_MAX_ATTEMPTS) {
            redisTemplate.delete(otpCodeKey);
            String timeUnit =attemptTtlUnit == TimeUnit.HOURS ? "hour(s)" : "minute(s)";
                return CompletableFuture.completedFuture(
                OtpVerificationResult.builder()
                    .success(false)
                    .message(String.format(
                        "Maximum verification attempts exceeded. Please try again in %d %s.",
                        attemptTtl,
                        timeUnit
                    ))
                    .token("")
                    .build()
                );
        }

        if (cachedOtp.equals(otpVerifyRequest.getCode())) {
            redisTemplate.delete(otpCodeKey);
            redisTemplate.delete(attemptsKey);
                return CompletableFuture.completedFuture(
                OtpVerificationResult.builder()
                    .success(true)
                    .message("OTP verified successfully")
                    .token(jwtService.generateVerifyToken(otpVerifyRequest.getRecipientEmail()))
                    .build()
                );
        }

        long currentAttempts=Long.parseLong((String) redisTemplate.opsForValue().get(attemptsKey));
        long updatedAttempts =currentAttempts+1;
        redisTemplate.opsForValue().set(
                attemptsKey,
                String.valueOf(updatedAttempts),
                attemptTtl,
                attemptTtlUnit
        );

        return CompletableFuture.completedFuture(
            OtpVerificationResult.builder()
            .success(false)
            .message("Invalid OTP. Attempts remaining: " +
                (RedisConfig.OTP_MAX_ATTEMPTS -updatedAttempts))
            .token("")
            .build()
        );
    }


    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int otp = random.nextInt(1000000);
        return String.format("%06d", otp);
    }

    @Override
    @Async("otpExecutor")
    public CompletableFuture<OtpVerificationResult> sendOtp(EmailRequest emailRequest) throws ExecutionException, InterruptedException {
        if (emailRequest.getEmailType()!=null &&(emailRequest.getEmailType() == EMAIL_VERIFICATION || emailRequest.getEmailType() == FORGOT_PASSWORD)) {
            String attemptsKey =ATTEMPTS_PREFIX+emailRequest.getRecipientEmail();
            Object attemptsObject=redisTemplate.opsForValue().get(attemptsKey);
            int attemptTtl =emailRequest.getEmailType() == FORGOT_PASSWORD ? RedisConfig.OTP_ATTEMPT_TTL_HOURS_RESET_PASSWORD : RedisConfig.OTP_ATTEMPT_TTL_MINUTES;
            TimeUnit attemptTtlUnit =emailRequest.getEmailType() == FORGOT_PASSWORD ? TimeUnit.HOURS : TimeUnit.MINUTES;
            if (attemptsObject!=null) {
                long attempts=Long.parseLong((String) attemptsObject);
                if (attempts>=RedisConfig.OTP_MAX_ATTEMPTS) {
                    String timeUnit = attemptTtlUnit == TimeUnit.HOURS ? "hour(s)" : "minute(s)";
                        return CompletableFuture.completedFuture(
                        OtpVerificationResult.builder()
                            .success(false)
                            .message(String.format(
                                "Maximum verification attempts exceeded. Please try again in %d %s.",
                                attemptTtl,
                                timeUnit
                            ))
                            .token("")
                            .build()
                        );
                }
            }

                    String otpCode=generateAndStoreOTP(emailRequest.getRecipientEmail(), emailRequest.getEmailType()).get();
            String body="";
            String title = "";
            EmailRequest.EmailType emailType = emailRequest.getEmailType();
            switch(emailType) {
                case EMAIL_VERIFICATION -> {
                    body = emailTemplate.buildVerificationCode(otpCode);
                    title = "Verify Your Email";
                }
                case FORGOT_PASSWORD -> {
                    body = emailTemplate.buildResetPasswordCode(otpCode);
                    title = "Reset Your Password";
                }
            }

            try {
                MimeMessage mimeMessage =javaMailSender.createMimeMessage();
                MimeMessageHelper mimeMessageHelper =new MimeMessageHelper(mimeMessage, true, "UTF-8");
                mimeMessageHelper.setFrom(HOMEMATE_EMAIL);
                mimeMessageHelper.setTo(emailRequest.getRecipientEmail());
                mimeMessageHelper.setSubject(title);

                String htmlContent = "<html><body>" +
                        "<img src='cid:logo' style='width:200px; height:auto;' />" +
                        "<h2>" + title + "</h2>" +
                        "<p>" + body + "</p>" +
                        "</body></html>";

                mimeMessageHelper.setText(htmlContent, true);
                mimeMessageHelper.addInline("logo", new ClassPathResource("logo.png"));
                javaMailSender.send(mimeMessage);
                return CompletableFuture.completedFuture(
                    OtpVerificationResult.builder()
                        .success(true)
                        .message("OTP sent successfully")
                        .token("")
                        .build()
                );

            } catch (MessagingException e) {
                return CompletableFuture.completedFuture(
                    OtpVerificationResult.builder()
                        .success(false)
                        .message("Failed to send OTP email. Please try again later.")
                        .token("")
                        .build()
                );
            }
        }
        return CompletableFuture.completedFuture(
            OtpVerificationResult.builder()
                .success(false)
                .message("Unsupported Email Type")
                .token("")
                .build()
        );

    }
}
