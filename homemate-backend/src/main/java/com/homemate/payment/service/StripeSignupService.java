package com.homemate.payment.service;

import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.TaskerProfile.models.Tasker;
import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.UserProfile.Models.User;
import com.homemate.payment.exceptions.BadRequestException;
import com.homemate.payment.exceptions.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StripeSignupService {

    private final UserDao userDao;
    private final TaskerDao taskerDao;
    private final StripeCustomerService stripeCustomerService;
    private final StripeAccountService stripeAccountService;

    public void signupUser(Long userId) {
        User user = userDao.getByID(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        if (user.getStripeCustomerId() != null) {
            throw new BadRequestException("Stripe customer already exists");
        }

        String customerId = stripeCustomerService.createCustomer(
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName()
        );

        userDao.updateStripeCustomerId(userId, customerId);
    }

    public String signupTasker(Long taskerId) {
        Tasker tasker = taskerDao.getByID(taskerId);
        if (tasker == null) {
            throw new ResourceNotFoundException("Tasker not found");
        }

        String accountId = tasker.getStripeAccountId();
        if (accountId == null) {
            accountId = stripeAccountService.createConnectedAccount(
                    tasker.getEmail(),
                    tasker.getFirstName(),
                    tasker.getLastName()
            );
            taskerDao.updateStripeAccountId(taskerId, accountId);
        }

        return stripeAccountService.generateOnboardingLink(accountId);
    }
}
