package com.homemate.notification.service;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.TaskResponse;
import org.springframework.stereotype.Service;

@Service
public interface EmailService {
    TaskResponse sendUserEmail(EmailRequest emailRequest);
    TaskResponse sendTaskerEmail(EmailRequest emailRequest);

}
