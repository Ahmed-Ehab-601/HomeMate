package com.homemate.payment.service;

import com.homemate.UserProfile.DAO.UserDao;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
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
