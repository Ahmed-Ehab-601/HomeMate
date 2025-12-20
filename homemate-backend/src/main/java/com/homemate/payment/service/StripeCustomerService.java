package com.homemate.payment.service;

import com.stripe.model.Customer;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StripeCustomerService {

    public String createCustomer(String email, String name) {
        try {
            Customer customer = Customer.create(
                    Map.of(
                            "email", email,
                            "name", name
                    )
            );
            return customer.getId();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Stripe customer", e);
        }
    }
}
