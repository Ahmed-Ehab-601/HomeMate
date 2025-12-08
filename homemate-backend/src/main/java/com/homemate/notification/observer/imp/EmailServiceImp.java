package com.homemate.notification.observer.imp;
import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.observer.NotificationObserver;
import com.homemate.notification.observer.NotificationSubject;
import com.homemate.notification.observer.utils.EmailTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImp implements NotificationObserver {
    private EmailTemplate emailTemplate;
    private JavaMailSender javaMailSender;
    public EmailServiceImp(EmailTemplate emailTemplate,JavaMailSender javaMailSender) {
        this.emailTemplate = emailTemplate;
        this.javaMailSender=javaMailSender;
    }

    private void sendUserEmail(EmailRequest emailRequest){

    }
    private void sendTaskerEmail(EmailRequest emailRequest){

    }
    private void sendAuthEmail(String email, String code){

    }
    @Override
    public void update(NotificationSubjectImp notificationSubjectImp) {

    }

}
