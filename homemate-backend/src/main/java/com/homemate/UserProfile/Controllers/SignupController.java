package com.homemate.UserProfile.Controllers;

import com.homemate.Authentication.dto.LoginRequestDto;
import com.homemate.UserProfile.DTO.SignupUserDTO;
import com.homemate.UserProfile.DTO.UserProfileDTO;
import com.homemate.UserProfile.Services.UserSignupService;
import com.homemate.security.model.AppUserDetails;
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

    UserSignupService userSignupService;

    SignupController(UserSignupService userSignupService) {
        this.userSignupService = userSignupService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signupUser(@RequestBody SignupUserDTO signupUserDTO) {
        String result = userSignupService.signup(signupUserDTO);
        if (result == null) {
            return ResponseEntity.status(403).body("Invalid Credentials");
        }
        return ResponseEntity.ok(result);
    }

}
