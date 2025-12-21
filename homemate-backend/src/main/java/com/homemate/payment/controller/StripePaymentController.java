package com.homemate.payment.controller;

import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.TaskerProfile.models.Tasker;
import com.homemate.payment.dao.PaymentDao;
import com.homemate.payment.dto.PaymentConfirmDTO;
import com.homemate.payment.dto.PaymentRequestDTO;
import com.homemate.payment.dto.PaymentResponseDTO;
import com.homemate.payment.model.Payment;
import com.homemate.payment.service.StripeAccountService;
import com.homemate.payment.service.StripePaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/tasker")
public class StripePaymentController {

    private final TaskerDao taskerDao;
    private final StripeAccountService stripeAccountService;
    private final StripePaymentService stripePaymentService;
    private final PaymentDao paymentDao;

    public StripePaymentController(
            TaskerDao taskerDao,
            StripeAccountService stripeAccountService,
            StripePaymentService stripePaymentService,
            PaymentDao paymentDao) {
        this.taskerDao = taskerDao;
        this.stripeAccountService = stripeAccountService;
        this.stripePaymentService = stripePaymentService;
        this.paymentDao = paymentDao;
    }

    // ==================== TASKER ACCOUNT SETUP ====================

    @PostMapping("/create-connected-account/{taskerId}")
    public ResponseEntity<String> createConnectedAccount(@PathVariable Long taskerId) {
        Tasker tasker = taskerDao.getByID(taskerId);
        String onboardingUrl = stripeAccountService.generateOnboardingLink(
                tasker.getStripeAccountId()
        );
        return ResponseEntity.ok(onboardingUrl);
    }

    @GetMapping("/check-stripe-status/{taskerId}")
    public ResponseEntity<Boolean> checkStripeStatus(@PathVariable Long taskerId) {
        Tasker tasker = taskerDao.getByID(taskerId);
        boolean enabled = stripeAccountService.isAccountEnabled(
                tasker.getStripeAccountId()
        );
        return ResponseEntity.ok(enabled);
    }

    // ==================== SIMPLIFIED PAYMENT FLOW ====================

    /**
     * STEP 1 & 2 & 3 & 4: Create Payment
     *
     * Frontend sends: POST /api/tasker/payments/create
     * With: taskId, userId, taskerId, bill
     *
     * Backend:
     * - Saves payment in DB
     * - Creates PaymentIntent in Stripe
     * - Returns clientSecret to frontend
     *
     * Frontend will use clientSecret to collect card payment
     */
    @PostMapping("/payments/create")
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @Valid @RequestBody PaymentRequestDTO request) {

        log.info("Received payment request for task {}", request.getTaskId());

        try {
            // Create payment and PaymentIntent
            Payment payment = stripePaymentService.createPayment(request);

            // Get clientSecret from PaymentIntent
            PaymentIntent intent = PaymentIntent.retrieve(
                    payment.getStripePaymentIntentId()
            );

            // Return clientSecret to frontend
            PaymentResponseDTO response = new PaymentResponseDTO(
                    intent.getClientSecret()
            );

            log.info("Returning clientSecret to frontend for payment {}");
            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            log.error("Stripe error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error creating payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * STEP 7 & 8 & 9: Confirm Payment
     *
     * Frontend sends: POST /api/tasker/payments/confirm
     * With: paymentIntentId
     *
     * After user entered card and Stripe charged them,
     * frontend tells us "payment succeeded"
     *
     * But we DON'T trust frontend!
     * We verify with Stripe API directly.
     *
     * If verified -> Mark payment as PAID in database
     */
    @PostMapping("/payments/confirm")
    public ResponseEntity<String> confirmPayment(
            @Valid @RequestBody PaymentConfirmDTO request) {

        log.info("Received payment confirmation for intent: {}", request.getPaymentIntentId());

        try {
            // Verify payment with Stripe (don't trust frontend!)
            boolean verified = stripePaymentService.verifyAndConfirmPayment(
                    request.getPaymentIntentId()
            );

            if (verified) {
                log.info("Payment verified and confirmed");
                return ResponseEntity.ok("Payment confirmed successfully");
            } else {
                log.warn("Payment verification failed");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Payment verification failed");
            }

        } catch (StripeException e) {
            log.error("Stripe error during verification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Stripe verification error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error confirming payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error confirming payment");
        }
    }

    /**
     * Optional: Check payment status
     * Frontend can poll this to check if payment is confirmed
     */
    @GetMapping("/payments/{paymentId}/status")
    public ResponseEntity<String> getPaymentStatus(@PathVariable Long paymentId) {
        try {
            // Get payment from database
            Payment payment = paymentDao.getById(paymentId);
            return ResponseEntity.ok(payment.getStatus());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Payment not found");
        }
    }


}