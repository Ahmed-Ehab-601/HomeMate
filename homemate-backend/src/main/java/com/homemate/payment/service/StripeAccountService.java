package com.homemate.payment.service;
import com.google.api.client.util.Value;
import com.homemate.payment.exceptions.StripeOperationException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripeAccountService {

    public String createConnectedAccount(String email, String firstName, String lastName) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("type", "express");
            params.put("email", email);
            params.put("business_profile",
                    Map.of("name", firstName + " " + lastName));

            Account account = Account.create(params);
            return account.getId();
        } catch (Exception e) {
            throw new StripeOperationException("Failed to create connected account", e);
        }
    }

    public String generateOnboardingLink(String accountId) {
        try {
            AccountLink link = AccountLink.create(Map.of(
                    "account", accountId,
                    "type", "account_onboarding",
                    "refresh_url", "https://example.com/refresh",
                    "return_url", "https://example.com/return"
            ));
            return link.getUrl();
        } catch (Exception e) {
            throw new StripeOperationException("Failed to generate onboarding link", e);
        }
    }
}
