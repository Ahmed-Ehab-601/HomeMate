package com.homemate.notification.service;

import com.homemate.notification.domains.dto.EmailRequest;
import org.springframework.stereotype.Service;

@Service
public interface EmailService {
     void sendUserEmail(EmailRequest emailRequest);
     void sendTaskerEmail(EmailRequest emailRequest);

}
