package com.homemate.notification.service.imp;

import com.homemate.notification.config.RedisConfig;
import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.OtpSendRequest;
import com.homemate.notification.domains.dto.OtpVerificationResult;
import com.homemate.notification.domains.dto.OtpVerifyRequest;
import com.homemate.notification.service.OTPService;
import com.homemate.notification.service.utils.EmailTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import static com.homemate.notification.domains.dto.EmailRequest.EmailType.EMAIL_VERIFICATION;
import static com.homemate.notification.domains.dto.EmailRequest.EmailType.FORGOT_PASSWORD;

@Validated
@RequiredArgsConstructor
@Component
public class OTPServiceImp implements OTPService {
    private final EmailTemplate emailTemplate;
    private final JavaMailSender javaMailSender;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String OTP_PREFIX ="otp:";
    private static final String ATTEMPTS_PREFIX ="otp_attempts:";
    private static final String HOMEMATE_EMAIL ="homematesevice8@gmail.com";
    public String generateAndStoreOTP(String email) {
        String otp =generateCode();
        String otpCodeKey=OTP_PREFIX+email;
        redisTemplate.opsForValue().set(
                otpCodeKey,
            otp,
            RedisConfig.OTP_TTL_MINUTES,
            TimeUnit.MINUTES
        );

        String attemptsKey=ATTEMPTS_PREFIX+email;
        redisTemplate.opsForValue().set(
            attemptsKey,
            "0",
            RedisConfig.OTP_ATTEMPT_TTL_MINUTES,
            TimeUnit.MINUTES
        );

        return otp;
    }
    @Override
    public OtpVerificationResult validateCode( OtpVerifyRequest otpVerifyRequest) {
        String otpCodeKey =OTP_PREFIX+otpVerifyRequest.getEmail();
        String attemptsKey =ATTEMPTS_PREFIX+otpVerifyRequest.getEmail();

        String cachedOtp=(String)redisTemplate.opsForValue().get(otpCodeKey);
        if (cachedOtp==null) {
            return OtpVerificationResult.builder()
                .success(false)
                .message("OTP expired")
                .build();
        }

        Object attemptsObject=redisTemplate.opsForValue().get(attemptsKey);
        long attempts;
        if (attemptsObject==null) {
            redisTemplate.opsForValue().set(
                    attemptsKey,
                    "0",
                    RedisConfig.OTP_ATTEMPT_TTL_MINUTES,
                    TimeUnit.MINUTES
            );
            attempts =0;
        }
        else {
            attempts =Long.parseLong((String) attemptsObject);
        }

        if (attempts>= RedisConfig.OTP_MAX_ATTEMPTS) {
            redisTemplate.delete(otpCodeKey);
            redisTemplate.delete(attemptsKey);
            return OtpVerificationResult.builder()
                .success(false)
                .message("Maximum attempts exceeded")
                .build();
        }

        if (cachedOtp.equals(otpVerifyRequest.getCode())) {
            redisTemplate.delete(otpCodeKey);
            redisTemplate.delete(attemptsKey);
            return OtpVerificationResult.builder()
                .success(true)
                .message("OTP verified successfully")
                .build();
        }

        long currentAttempts=Long.parseLong((String) redisTemplate.opsForValue().get(attemptsKey));
        long updatedAttempts =currentAttempts+1;
        redisTemplate.opsForValue().set(
                attemptsKey,
                String.valueOf(updatedAttempts),
                RedisConfig.OTP_ATTEMPT_TTL_MINUTES,
                TimeUnit.MINUTES
        );

        return OtpVerificationResult.builder()
                .success(false)
                .message("Invalid OTP. Attempts remaining: " +
                        (RedisConfig.OTP_MAX_ATTEMPTS -updatedAttempts))
                .build();
    }


    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int otp = random.nextInt(1000000);
        return String.format("%06d", otp);
    }

   @Override
    public void sendOtp(EmailRequest emailRequest) {
         if (emailRequest.getEmailType()==EMAIL_VERIFICATION || emailRequest.getEmailType()==FORGOT_PASSWORD){
             String otpCode =generateAndStoreOTP(emailRequest.getRecipientEmail());
             String body =emailTemplate.buildVerificationCode(otpCode);
             try {
                 MimeMessage mimeMessage=javaMailSender.createMimeMessage();
                 MimeMessageHelper mimeMessageHelper= new MimeMessageHelper(mimeMessage, true, "UTF-8");
                 mimeMessageHelper.setFrom(HOMEMATE_EMAIL);
                 mimeMessageHelper.setTo(emailRequest.getRecipientEmail());
                 EmailRequest.EmailType emailType=emailRequest.getEmailType();

                 switch (emailType){
                     case EMAIL_VERIFICATION, FORGOT_PASSWORD ->mimeMessageHelper.setSubject(emailTemplate.buildEmailSubject(emailRequest));
                     default ->throw new IllegalArgumentException("Unsupported email type: " + emailType);
                 }

                 String htmlContent = "<html><body>" +
                         "<img src='cid:logo' style='width:200px; height:auto;' />" +
                         "<h2>Your Verification Code</h2>" +
                         "<p>" + body + "</p>" +
                         "</body></html>";

                 mimeMessageHelper.setText(htmlContent, true);
                 mimeMessageHelper.addInline("logo", new ClassPathResource("logo.png"));
                 javaMailSender.send(mimeMessage);
             } catch (MessagingException e) {
                 throw new RuntimeException("Failed to send OTP email", e);
             }
         }
    }
}
