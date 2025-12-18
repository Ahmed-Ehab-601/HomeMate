package com.homemate.notification.controller;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.TaskResponse;
import com.homemate.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
	private final EmailService emailService;
	@PostMapping("/user")
	public ResponseEntity<TaskResponse> sendUserEmail(
			 @RequestBody EmailRequest emailRequest
	) {
		try {
			TaskResponse taskResponse= emailService.sendUserEmail(emailRequest).get();
			if (taskResponse.isSuccess()) {
				return ResponseEntity.accepted().body(taskResponse);
			} else {
				return ResponseEntity.badRequest().body(taskResponse);
			}
		} catch (Exception e) {
			log.error("Error sending user email notification", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(TaskResponse.builder()
							.success(false)
							.message("Failed to send notification: " + e.getMessage())
							.build());
		}
	}

	@PostMapping("/tasker")
	public ResponseEntity<TaskResponse> sendTaskerEmail(
			@RequestBody EmailRequest emailRequest
	) {
		try {
			TaskResponse taskResponse= emailService.sendTaskerEmail(emailRequest).get();
			if (taskResponse.isSuccess()) {
				return ResponseEntity.accepted().body(taskResponse);
			} else {
				return ResponseEntity.badRequest().body(taskResponse);
			}
		} catch (Exception e) {
			log.error("Error sending tasker email notification", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(TaskResponse.builder()
							.success(false)
							.message("Failed to send notification: " + e.getMessage())
							.build());
		}
	}
}
