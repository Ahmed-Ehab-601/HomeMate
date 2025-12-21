package com.homemate.payment.controller;


import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.TaskerProfile.models.Tasker;
import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.UserProfile.Models.User;
import com.homemate.payment.service.StripeAccountService;
import com.homemate.payment.service.StripeCustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stripe/signup")
public class StripeSignUpController {

    private final StripeCustomerService stripeCustomerService;
    private final StripeAccountService stripeAccountService;
    private final UserDao userDao;  // Add this
    private final TaskerDao taskerDao; // Add this

    public StripeSignUpController(StripeCustomerService stripeCustomerService,
                                  StripeAccountService stripeAccountService,
                                  UserDao userDao,
                                  TaskerDao taskerDao) {
        this.stripeCustomerService = stripeCustomerService;
        this.stripeAccountService = stripeAccountService;
        this.userDao = userDao;
        this.taskerDao = taskerDao;
    }

    // Optional Stripe signup for user
    @PostMapping("/user/{userId}")
    public ResponseEntity<String> createStripeCustomer(@PathVariable Long userId) {
        User user = userDao.getByID(userId);; // get user
        if (user.getStripeCustomerId() != null) {
            return ResponseEntity.badRequest().body("Stripe account already exists");
        }

        String stripeCustomerId = stripeCustomerService.createCustomer(
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName()
        );

        userDao.updateStripeCustomerId(userId, stripeCustomerId);

        return ResponseEntity.ok("Stripe customer created successfully");
    }

    // Optional Stripe signup for tasker (connected account)
    @PostMapping("/tasker/{taskerId}")
    public ResponseEntity<String> createStripeConnectedAccount(@PathVariable Long taskerId) {

        Tasker tasker = taskerDao.getByID(taskerId);

        String accountId = tasker.getStripeAccountId();

        // 1️⃣ Create account if not exists
        if (accountId == null) {
            accountId = stripeAccountService.createConnectedAccount(
                    tasker.getEmail(),
                    tasker.getFirstName(),
                    tasker.getLastName()
            );
            taskerDao.updateStripeAccountId(taskerId, accountId);
        }

        // 2️⃣ Generate onboarding link
        String onboardingLink =
                stripeAccountService.generateOnboardingLink(accountId);

        // 3️⃣ RETURN THE LINK (IMPORTANT)
        return ResponseEntity.ok(onboardingLink);
    }

}

