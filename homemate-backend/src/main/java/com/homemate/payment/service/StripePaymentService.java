package com.homemate.payment.service;

import com.homemate.payment.dao.PaymentDao;
import com.homemate.payment.dto.PaymentRequestDTO;
import com.homemate.payment.mapper.PaymentMapper;
import com.homemate.payment.model.Payment;
import com.homemate.taskmanagement.dao.TaskStatusDao;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class StripePaymentService {

    private final PaymentDao paymentDao;
    private final PaymentMapper paymentMapper;
    private final TaskStatusDao taskStatusDao;

    /**
     * STEP 2: Create PaymentIntent
     * Called when user clicks "Pay Online"
     *
     * Flow:
     * 1. Save payment in database (status = CREATED)
     * 2. Create PaymentIntent in Stripe
     * 3. Update database with PaymentIntent ID
     * 4. Return clientSecret to frontend
     */
    public Payment createPayment(PaymentRequestDTO paymentRequestDTO) throws StripeException {
        log.info("Creating payment for task {}", paymentRequestDTO.getTaskId());

        // Step 1: Create payment record in database
        Payment payment = paymentMapper.getPaymnetEntity(paymentRequestDTO);
        Optional<Long> paymentIdOpt = paymentDao.create(payment);

        if (paymentIdOpt.isEmpty()) {
            throw new RuntimeException("Failed to create payment record");
        }

        Long paymentId = paymentIdOpt.get();
        payment.setId(paymentId);
        log.info("Payment record created with ID: {}", paymentId);

        // Step 2: Get tasker's Stripe account
        String taskerStripeAccountId = paymentDao.getTaskerStripeAccountId(
                paymentRequestDTO.getTaskerId()
        );

        if (taskerStripeAccountId == null || taskerStripeAccountId.isEmpty()) {
            throw new RuntimeException("Tasker has not set up Stripe account");
        }

        // Step 3: Create Stripe PaymentIntent
        PaymentIntent intent = createPaymentIntent(
                payment.getTotalAmount(),
                payment.getPlatformFee(),
                taskerStripeAccountId
        );

        log.info("PaymentIntent created: {}", intent.getId());

        // Step 4: Save PaymentIntent ID to database
        paymentDao.attachStripeIntent(paymentId, intent.getId());
        payment.setStripePaymentIntentId(intent.getId());
        payment.setStatus("REQUIRES_PAYMENT");

        return payment;
    }

    /**
     * Creates PaymentIntent with platform fee split
     *
     * How it works:
     * - Total amount charged to customer: $100
     * - Platform keeps application_fee_amount: $10
     * - Tasker automatically receives the rest: $90
     */
    private PaymentIntent createPaymentIntent(
            Double totalAmount,
            Double platformFee,
            String taskerStripeAccountId) throws StripeException {

        long totalAmountInCents = Math.round(totalAmount * 100);
        long platformFeeInCents = Math.round(platformFee * 100);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(totalAmountInCents)
                .setCurrency("usd")
                .addPaymentMethodType("card")
                // Transfer to tasker (Stripe calculates amount automatically)
                .setTransferData(
                        PaymentIntentCreateParams.TransferData.builder()
                                .setDestination(taskerStripeAccountId)
                                .build()
                )
                // Platform keeps this fee
                .setApplicationFeeAmount(platformFeeInCents)
                .build();

        return PaymentIntent.create(params);
    }

    /**
     * STEP 8: Verify and confirm payment
     * Frontend says "payment succeeded", but we DON'T trust it!
     * We verify with Stripe directly.
     *
     * @param paymentIntentId - The PaymentIntent ID from Stripe
     * @return true if payment is verified and confirmed
     */
    public boolean verifyAndConfirmPayment(String paymentIntentId) throws StripeException {
        log.info("Verifying payment with Stripe: {}", paymentIntentId);

        // Step 1: Get payment from database
        Payment payment = paymentDao.getByStripeIntentId(paymentIntentId);

        if (payment == null) {
            log.error("Payment not found for intent: {}", paymentIntentId);
            return false;
        }

        // Step 2: Verify with Stripe (CRITICAL - Don't trust frontend!)
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

        if (!"succeeded".equals(intent.getStatus())) {
            log.warn("Payment intent status is not succeeded: {}", intent.getStatus());
            return false;
        }

        log.info("Payment verified with Stripe. Status: succeeded");

        // Step 3: Update database - mark as PAID
        paymentDao.updateStatus(payment.getId(), "PAID");

        paymentDao.setPaidAt(payment.getId());
        log.info("mark task as paid ");
        taskStatusDao.setTaskAsPaid(payment.getTaskId());

        log.info("Payment {} marked as PAID", payment.getId());

        return true;
    }
}