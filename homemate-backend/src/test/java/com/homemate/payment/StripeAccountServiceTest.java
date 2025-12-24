package com.homemate.payment;

import com.homemate.payment.exceptions.StripeOperationException;
import com.homemate.payment.service.StripeAccountService;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class StripeAccountServiceTest {

    @InjectMocks
    private StripeAccountService stripeAccountService;

    @Test
    void testCreateConnectedAccountSuccess() {
        try (MockedStatic<Account> mockedAccount = mockStatic(Account.class)) {
            Account mockAccount = new Account();
            mockAccount.setId("acct_123");

            mockedAccount.when(() -> Account.create(any(Map.class)))
                    .thenReturn(mockAccount);

            String accountId = stripeAccountService.createConnectedAccount(
                    "test@example.com",
                    "John",
                    "Doe"
            );

            assertEquals("acct_123", accountId);
        }
    }

    @Test
    void testCreateConnectedAccountFailure() {
        try (MockedStatic<Account> mockedAccount = mockStatic(Account.class)) {
            mockedAccount.when(() -> Account.create(any(Map.class)))
                    .thenThrow(new RuntimeException("Stripe error"));

            assertThrows(StripeOperationException.class, () ->
                    stripeAccountService.createConnectedAccount(
                            "test@example.com",
                            "John",
                            "Doe"
                    )
            );
        }
    }

    @Test
    void testGenerateOnboardingLinkSuccess() {
        try (MockedStatic<AccountLink> mockedLink = mockStatic(AccountLink.class)) {
            AccountLink mockLink = new AccountLink();
            mockLink.setUrl("https://connect.stripe.com/setup/123");

            mockedLink.when(() -> AccountLink.create(any(Map.class)))
                    .thenReturn(mockLink);

            String url = stripeAccountService.generateOnboardingLink("acct_123");

            assertEquals("https://connect.stripe.com/setup/123", url);
        }
    }

    @Test
    void testGenerateOnboardingLinkFailure() {
        try (MockedStatic<AccountLink> mockedLink = mockStatic(AccountLink.class)) {
            mockedLink.when(() -> AccountLink.create(any(Map.class)))
                    .thenThrow(new RuntimeException("Stripe error"));

            assertThrows(StripeOperationException.class, () ->
                    stripeAccountService.generateOnboardingLink("acct_123")
            );
        }
    }
}