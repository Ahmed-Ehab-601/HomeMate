package com.homemate.payment.controller;
import com.stripe.model.Customer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StripeController {

    @GetMapping("/stripe/test")
    public String testStripe() throws Exception {
        Customer customer = Customer.create(
                Map.of("email", "test@example.com")
        );
        return "Stripe connected. Customer ID: " + customer.getId();
    }
}