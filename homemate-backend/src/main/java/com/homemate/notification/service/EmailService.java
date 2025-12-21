package com.homemate.notification.service;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.TaskResponse;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public interface EmailService {
    CompletableFuture<TaskResponse> sendEmail(EmailRequest emailRequest);

}
