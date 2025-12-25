package com.homemate.payment;

import com.homemate.payment.service.StripeCustomerService;
import com.stripe.model.Customer;
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
public class StripeCustomerServiceTest {

    @InjectMocks
    private StripeCustomerService stripeCustomerService;

    @Test
    void testCreateCustomerSuccess() {
        try (MockedStatic<Customer> mockedCustomer = mockStatic(Customer.class)) {
            Customer mockCustomer = new Customer();
            mockCustomer.setId("cus_123");

            mockedCustomer.when(() -> Customer.create(any(Map.class)))
                    .thenReturn(mockCustomer);

            String customerId = stripeCustomerService.createCustomer(
                    "test@example.com",
                    "John Doe"
            );

            assertEquals("cus_123", customerId);
        }
    }

    @Test
    void testCreateCustomerFailure() {
        try (MockedStatic<Customer> mockedCustomer = mockStatic(Customer.class)) {
            mockedCustomer.when(() -> Customer.create(any(Map.class)))
                    .thenThrow(new RuntimeException("Stripe error"));

            assertThrows(RuntimeException.class, () ->
                    stripeCustomerService.createCustomer(
                            "test@example.com",
                            "John Doe"
                    )
            );
        }
    }
}