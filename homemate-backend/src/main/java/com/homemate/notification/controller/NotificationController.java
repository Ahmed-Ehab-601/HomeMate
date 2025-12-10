package com.homemate.notification.controller;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
	private final EmailService emailService;

	@PostMapping("/user")
	public ResponseEntity<Map<String, String>> sendUserEmail(
			@RequestBody EmailRequest emailRequest
	) {
		emailService.sendUserEmail(emailRequest);
		return ResponseEntity.accepted().body(Map.of("message", "Notification sent to user"));
	}

	@PostMapping("/tasker")
	public ResponseEntity<Map<String, String>> sendTaskerEmail(
			 @RequestBody EmailRequest emailRequest
	) {
		emailService.sendTaskerEmail(emailRequest);
		return ResponseEntity.accepted().body(Map.of("message", "Notification sent to tasker"));
	}
}
