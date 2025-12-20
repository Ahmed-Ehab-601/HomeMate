package com.homemate.payment;

import com.stripe.Stripe;
import com.stripe.model.Account;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripeAccountService {

    public String createConnectedAccount(String email, String firstName, String lastName) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("type", "express"); // Express account is simpler for platforms
            params.put("email", email);
            Map<String, Object> businessProfile = new HashMap<>();
            businessProfile.put("name", firstName + " " + lastName);
            params.put("business_profile", businessProfile);

            Account account = Account.create(params);
            return account.getId();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Stripe Connected Account", e);
        }
    }
}
