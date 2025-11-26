package com.homemate.UserProfile.Controllers;

import com.homemate.Authentication.dto.LoginRequestDto;
import com.homemate.UserProfile.DTO.SignupUserDTO;
import com.homemate.UserProfile.DTO.UserProfileDTO;
import com.homemate.UserProfile.Services.UserUserSignupService;
import com.homemate.security.model.AppUserDetails;
import com.homemate.security.service.ValidateSignupService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class SignupController {

    UserUserSignupService userSignupService;
    ValidateSignupService validateSignup;

    SignupController(
        UserUserSignupService userSignupService,
        ValidateSignupService validateSignup
    ) {
        this.userSignupService = userSignupService;
        this.validateSignup = validateSignup;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signupUser(@RequestBody SignupUserDTO signupUserDTO) {
        String error = validateSignup.validateUserSignup(signupUserDTO);
        if (error != null) 
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        String result = userSignupService.signup(signupUserDTO);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user with the same username or email already exists");
        }

        return ResponseEntity.ok(result);
    }

}
