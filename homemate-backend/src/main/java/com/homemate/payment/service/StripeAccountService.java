package com.homemate.payment.service;
import com.google.api.client.util.Value;
import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.TaskerProfile.models.Tasker;
import com.homemate.payment.exceptions.PaymentException;
import com.homemate.payment.exceptions.StripeOperationException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class StripeAccountService {

    private final TaskerDao taskerDao;

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
                    "refresh_url", "http://localhost:5173/tasker/profile",
                    "return_url", "http://localhost:5173/tasker/profile"
            ));
            return link.getUrl();
        } catch (Exception e) {
            throw new StripeOperationException("Failed to generate onboarding link", e);
        }
    }

    public boolean isAccountEnabled(Long taskerId) {
        log.info("tasker is {}",taskerId);

        Tasker tasker = taskerDao.getByID(taskerId);

        if (tasker == null) {
            throw new PaymentException("Tasker not found");
        }

        String accountId = taskerDao.getTaskerStripeAccount(taskerId);
        log.info("account {}",accountId);

        if (accountId == null) {
            log.info("heeeeeeee");
            return false;
        }

        try {
            Account account = Account.retrieve(accountId);
            return account.getChargesEnabled() && account.getPayoutsEnabled();
        } catch (StripeException e) {
            throw new PaymentException("Failed to check Stripe account status", e);
        }
    }


}
