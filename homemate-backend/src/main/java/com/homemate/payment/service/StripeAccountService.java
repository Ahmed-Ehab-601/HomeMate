package com.homemate.payment.service;
import com.google.api.client.util.Value;
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

    // Generate onboarding link for tasker
    public String generateOnboardingLink(String accountId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("account", accountId);
            params.put("refresh_url", "https://example.com/refresh"); // just a placeholder
            params.put("return_url", "https://example.com/return");   // just a placeholder
            params.put("type", "account_onboarding");

            AccountLink accountLink = AccountLink.create(params);
            return accountLink.getUrl();
        } catch (StripeException e) {
            throw new RuntimeException("Failed to generate onboarding link", e);
        }
    }


    // Check if account is ready for payouts
    public boolean isAccountEnabled(String accountId) {
        try {
            Account account = Account.retrieve(accountId);
            return account.getChargesEnabled() && account.getPayoutsEnabled();
        } catch (StripeException e) {
            throw new RuntimeException("Failed to retrieve account info", e);
        }
    }
}
