package com.homemate.TaskerProfile.controllers;

import com.homemate.TaskerProfile.DTO.TaskerSignupDTO;
import com.homemate.TaskerProfile.services.TaskerSignupService;
import com.homemate.UserProfile.Controllers.SignupController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasker")
public class SignupTaskerController {

    private TaskerSignupService taskerSignupService;

    SignupTaskerController(TaskerSignupService taskerSignupService) {
        this.taskerSignupService = taskerSignupService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody TaskerSignupDTO dto) {
        String jwt = taskerSignupService.registerTasker(dto);
        if (jwt == null) {
            return ResponseEntity.status(403).body("Invalid Credentials");
        }
        return ResponseEntity.ok(jwt);
    }
}
