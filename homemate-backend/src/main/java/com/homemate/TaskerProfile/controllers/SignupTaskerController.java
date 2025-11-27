package com.homemate.TaskerProfile.controllers;

import com.homemate.TaskerProfile.DTO.TaskerSignupDTO;
import com.homemate.TaskerProfile.services.TaskerSignupService;
import com.homemate.security.service.ValidateSignupService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/tasker")
public class SignupTaskerController {

    private TaskerSignupService taskerSignupService;
    private ValidateSignupService validateSignup;

    SignupTaskerController(
        TaskerSignupService taskerSignupService, 
        ValidateSignupService validateSignup
    ) {
        this.taskerSignupService = taskerSignupService;
        this.validateSignup = validateSignup;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody TaskerSignupDTO dto) {
        String error = validateSignup.validateTaskerSignup(dto);
        if (error != null) 
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        String jwt = taskerSignupService.registerTasker(dto);
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user with the same username or email already exists");
        }
        return ResponseEntity.ok(jwt);
    }
}
