package com.homemate.payment.controller;

import com.homemate.payment.service.StripeSignupService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Void> signupUser(@PathVariable Long userId) {
        stripeSignupService.signupUser(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tasker/{taskerId}")
    public ResponseEntity<String> signupTasker(@PathVariable Long taskerId) {
        return ResponseEntity.ok(
                stripeSignupService.signupTasker(taskerId)
        );
    }

}
