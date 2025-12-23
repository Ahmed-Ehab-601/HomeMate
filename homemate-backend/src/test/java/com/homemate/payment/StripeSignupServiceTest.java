package com.homemate.payment;

import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.TaskerProfile.models.Tasker;
import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.UserProfile.Models.User;
import com.homemate.payment.exceptions.BadRequestException;
import com.homemate.payment.exceptions.ResourceNotFoundException;
import com.homemate.payment.service.StripeAccountService;
import com.homemate.payment.service.StripeCustomerService;
import com.homemate.payment.service.StripeSignupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StripeSignupServiceTest {

    @Mock
    private UserDao userDao;
    @Mock
    private TaskerDao taskerDao;
    @Mock
    private StripeCustomerService stripeCustomerService;
    @Mock
    private StripeAccountService stripeAccountService;

    @InjectMocks
    private StripeSignupService stripeSignupService;

    @Test
    void testSignupUserSuccess() {
        Long userId = 1L;
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setStripeCustomerId(null);

        when(userDao.getByID(userId)).thenReturn(user);
        when(stripeCustomerService.createCustomer("test@example.com", "John Doe"))
                .thenReturn("cus_123");

        stripeSignupService.signupUser(userId);

        verify(userDao).updateStripeCustomerId(userId, "cus_123");
    }

    @Test
    void testSignupUserNotFound() {
        Long userId = 1L;

        when(userDao.getByID(userId)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () ->
                stripeSignupService.signupUser(userId)
        );
    }

    @Test
    void testSignupUserAlreadyExists() {
        Long userId = 1L;
        User user = new User();
        user.setStripeCustomerId("cus_existing");

        when(userDao.getByID(userId)).thenReturn(user);

        assertThrows(BadRequestException.class, () ->
                stripeSignupService.signupUser(userId)
        );
    }

    @Test
    void testSignupTaskerNewAccount() {
        Long taskerId = 1L;
        Tasker tasker = new Tasker();
        tasker.setEmail("tasker@example.com");
        tasker.setFirstName("Jane");
        tasker.setLastName("Smith");
        tasker.setStripeAccountId(null);

        when(taskerDao.getByID(taskerId)).thenReturn(tasker);
        when(stripeAccountService.createConnectedAccount(
                "tasker@example.com", "Jane", "Smith"))
                .thenReturn("acct_123");
        when(stripeAccountService.generateOnboardingLink("acct_123"))
                .thenReturn("https://onboarding.url");

        String url = stripeSignupService.signupTasker(taskerId);

        assertEquals("https://onboarding.url", url);
        verify(taskerDao).updateStripeAccountId(taskerId, "acct_123");
    }

    @Test
    void testSignupTaskerExistingAccount() {
        Long taskerId = 1L;
        Tasker tasker = new Tasker();
        tasker.setStripeAccountId("acct_existing");

        when(taskerDao.getByID(taskerId)).thenReturn(tasker);
        when(stripeAccountService.generateOnboardingLink("acct_existing"))
                .thenReturn("https://onboarding.url");

        String url = stripeSignupService.signupTasker(taskerId);

        assertEquals("https://onboarding.url", url);
        verify(stripeAccountService, never()).createConnectedAccount(any(), any(), any());
        verify(taskerDao, never()).updateStripeAccountId(any(), any());
    }

    @Test
    void testSignupTaskerNotFound() {
        Long taskerId = 1L;

        when(taskerDao.getByID(taskerId)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () ->
                stripeSignupService.signupTasker(taskerId)
        );
    }
}