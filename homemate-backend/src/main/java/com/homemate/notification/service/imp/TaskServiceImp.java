package com.homemate.notification.service.imp;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.service.EmailService;
import com.homemate.notification.service.utils.EmailTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class EmailServiceImp implements EmailService {
    private EmailTemplate emailTemplate;
    private JavaMailSender javaMailSender;
    private static final String HOMEMATE_EMAIL ="homematesevice8@gmail.com";
    public EmailServiceImp(EmailTemplate emailTemplate,JavaMailSender javaMailSender) {
        this.emailTemplate = emailTemplate;
        this.javaMailSender=javaMailSender;
    }
    private void sendNotification(String subject , String body , EmailRequest emailRequest) {
        SimpleMailMessage simpleMailMessage=new SimpleMailMessage();
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(body);
        simpleMailMessage.setTo(emailRequest.getRecipientEmail());
        simpleMailMessage.setFrom(HOMEMATE_EMAIL);
        javaMailSender.send(simpleMailMessage);
    }
    @Override
    public void sendUserEmail(EmailRequest emailRequest) {
        EmailRequest.EmailType emailType=emailRequest.getEmailType();
        switch (emailType){
            case TASK_ACCEPTED,TASK_STATUS,TASK_REJECTED -> sendNotification(emailTemplate.buildEmailSubject(emailRequest),emailTemplate.buildTaskStatusChangedBody(emailRequest.getTask()),emailRequest);
            case TASK_RESCHEDULE -> sendNotification(emailTemplate.buildEmailSubject(emailRequest),emailTemplate.buildUserTaskRescheduleBody(emailRequest.getTask()),emailRequest);
            case TASK_RESUMED -> sendNotification(emailTemplate.buildEmailSubject(emailRequest),emailTemplate.BuildTaskResumedBody(emailRequest.getTask()),emailRequest);
            default -> {
                log.error("Unsupported email type: {}", emailType);
                return;
            }

        }
    }

    @Override
    public void sendTaskerEmail(EmailRequest emailRequest) {
        EmailRequest.EmailType emailType=emailRequest.getEmailType();
        switch (emailType){
            case TASK_REQUEST -> sendNotification(emailTemplate.buildEmailSubject(emailRequest), emailTemplate.buildTaskRequestBody(emailRequest.getTask()), emailRequest);
            case TASK_RESCHEDULE -> sendNotification(emailTemplate.buildEmailSubject(emailRequest), emailTemplate.buildTaskerTaskRescheduleBody(emailRequest.getTask()), emailRequest);
            default -> {
                log.error("Unsupported email type: {}", emailType);
                return;
            }

        }
    }


}
