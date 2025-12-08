package com.homemate.notification.service.imp;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.OtpSendRequest;
import com.homemate.notification.observer.imp.NotificationSubjectImp;
import com.homemate.notification.observer.imp.OTPServiceImp;
import com.homemate.notification.service.NotificationService;
import org.springframework.stereotype.Service;

import static com.homemate.notification.domains.dto.EmailRequest.EmailType.EMAIL_VERIFICATION;

@Service
public class NotificationServiceImp implements NotificationService {
    private OTPServiceImp otpServiceImp;
    public NotificationServiceImp(OTPServiceImp otpServiceImp) {
        this.otpServiceImp = otpServiceImp;
    }

    @Override
    public void createNewAccount(OtpSendRequest otpSendRequest) {
        NotificationSubjectImp notificationSubjectImp = new NotificationSubjectImp();
        notificationSubjectImp.setEmailRequest(
                EmailRequest.builder()
                        .emailType(EMAIL_VERIFICATION)
                        .recipientEmail(otpSendRequest.getEmail())
                        .build()
        );
        notificationSubjectImp.attach(otpServiceImp);
        notificationSubjectImp.notifyAllObservers();

    }
}
