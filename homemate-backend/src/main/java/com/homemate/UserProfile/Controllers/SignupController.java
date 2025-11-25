package com.homemate.UserProfile.Controllers;

import com.homemate.Authentication.dto.LoginRequestDto;
import com.homemate.UserProfile.DTO.UserProfileDTO;
import com.homemate.security.model.AppUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SignupController {

    @PostMapping("/signup")
    public ResponseEntity<UserProfileDTO> getProfile(
            @RequestBody LoginRequestDto loginRequestDto,
            @AuthenticationPrincipal AppUserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getId()));
    }

}
