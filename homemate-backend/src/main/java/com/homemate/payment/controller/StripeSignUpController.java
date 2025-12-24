package com.homemate.payment.controller;

import com.homemate.payment.service.StripeSignupService;
import com.homemate.security.model.AppUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stripe/signup")
@AllArgsConstructor
public class StripeSignUpController {

    private final StripeSignupService stripeSignupService;

    @PostMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> signupUser(@PathVariable Long userId, @AuthenticationPrincipal AppUserDetails userDetails) {
        stripeSignupService.signupUser(userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tasker/{taskerId}")
    @PreAuthorize("hasAnyRole('TASKER')")
    public ResponseEntity<String> signupTasker(@PathVariable Long taskerId,@AuthenticationPrincipal AppUserDetails userDetails) {
        return ResponseEntity.ok(
                stripeSignupService.signupTasker(userDetails.getId())
        );
    }

}
