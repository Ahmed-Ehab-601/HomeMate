package com.homemate.payment.service;

import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.TaskerProfile.models.Tasker;
import com.homemate.payment.dao.PaymentDao;
import com.homemate.payment.dto.PaymentRequestDTO;
import com.homemate.payment.dto.PaymentResponseDTO;
import com.homemate.payment.exceptions.PaymentException;
import com.homemate.payment.mapper.PaymentMapper;
import com.homemate.payment.model.Payment;
import com.homemate.taskmanagement.dao.GetTasksDao;
import com.homemate.taskmanagement.dao.TaskRequestDao;
import com.homemate.taskmanagement.dao.TaskStatusDao;
import com.homemate.taskmanagement.dto.TaskDto;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.stream.Task;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class StripePaymentService {

    private final PaymentDao paymentDao;
    private final PaymentMapper paymentMapper;
    private final TaskStatusDao taskStatusDao;
    private final TaskRequestDao taskDao;
    private final TaskerDao taskerDao;

    private final StripeAccountService stripeAccountService;


    public String generateOnboardingLink(Long taskerId) {
        Tasker tasker = taskerDao.getByID(taskerId);
        if (tasker == null) {
            throw new PaymentException("Tasker not found");
        }

        return stripeAccountService.generateOnboardingLink(
                tasker.getStripeAccountId()
        );
    }


    public PaymentResponseDTO createPaymentAndReturnClientSecret(
            PaymentRequestDTO request) {

        try {
            Payment payment = createPayment(request);

            PaymentIntent intent = PaymentIntent.retrieve(
                    payment.getStripePaymentIntentId()
            );

            return new PaymentResponseDTO(intent.getClientSecret());

        } catch (StripeException e) {
            log.error("Stripe error while creating payment", e);
            throw new PaymentException("Stripe payment creation failed", e);
        }
    }


    public Payment createPayment(PaymentRequestDTO paymentRequestDTO)
            throws StripeException {

        log.info("Creating payment for task {}", paymentRequestDTO.getTaskId());

        Payment payment = paymentMapper.getPaymnetEntity(paymentRequestDTO);
        Optional<Long> paymentIdOpt = paymentDao.create(payment);

        if (paymentIdOpt.isEmpty()) {
            throw new PaymentException("Failed to create payment record");
        }

        Long paymentId = paymentIdOpt.get();
        payment.setId(paymentId);

        String taskerStripeAccountId =
                paymentDao.getTaskerStripeAccountId(paymentRequestDTO.getTaskerId());

        if (taskerStripeAccountId == null || taskerStripeAccountId.isEmpty()) {
            throw new PaymentException("Tasker has not set up Stripe account");
        }

        PaymentIntent intent = createPaymentIntent(
                payment.getTotalAmount(),
                payment.getPlatformFee(),
                taskerStripeAccountId
        );

        paymentDao.attachStripeIntent(paymentId, intent.getId());
        payment.setStripePaymentIntentId(intent.getId());
        payment.setStatus("REQUIRES_PAYMENT");

        return payment;
    }


    private PaymentIntent createPaymentIntent(
            Double totalAmount,
            Double platformFee,
            String taskerStripeAccountId) throws StripeException {

        long totalAmountInCents = Math.round(totalAmount * 100);
        long platformFeeInCents = Math.round(platformFee * 100);

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(totalAmountInCents)
                        .setCurrency("usd")
                        .addPaymentMethodType("card")
                        .setTransferData(
                                PaymentIntentCreateParams.TransferData.builder()
                                        .setDestination(taskerStripeAccountId)
                                        .build()
                        )
                        .setApplicationFeeAmount(platformFeeInCents)
                        .build();

        return PaymentIntent.create(params);
    }


    public void confirmPayment(String paymentIntentId) {
        try {
            boolean verified = verifyAndConfirmPayment(paymentIntentId);

            if (!verified) {
                throw new PaymentException("Payment verification failed");
            }

        } catch (StripeException e) {
            log.error("Stripe verification error", e);
            throw new PaymentException("Stripe verification failed", e);
        }
    }

    public boolean verifyAndConfirmPayment(String paymentIntentId)
            throws StripeException {

        Payment payment = paymentDao.getByStripeIntentId(paymentIntentId);

        if (payment == null) {
            return false;
        }

        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

        if (!"succeeded".equals(intent.getStatus())) {
            return false;
        }

        paymentDao.updateStatus(payment.getId(), "PAID");
        paymentDao.setPaidAt(payment.getId());

        taskStatusDao.setTaskAsPaid(payment.getTaskId());
        taskStatusDao.updateTaskerTotalEarning(
                payment.getTaskerId(),
                payment.getTaskerAmount()
        );

        return true;
    }


    public String getPaymentStatus(Long paymentId) {
        Payment payment = paymentDao.getById(paymentId);
        if (payment == null) {
            throw new PaymentException("Payment not found");
        }
        return payment.getStatus();
    }

    public void markTaskAsPaidCash(Long taskId, Long taskerId) {

        TaskDto taskDto = taskDao.getTaskDetails(taskId)
                .orElseThrow(() -> new PaymentException("Task not found"));

        if (taskDto.getTaskerID() != taskerId) {
            throw new PaymentException("You are not authorized to mark this task as paid");
        }

        taskStatusDao.setTaskAsPaid(taskId);
        taskStatusDao.updateTaskerTotalEarning(taskerId, taskDto.getHourRate() * taskDto.getWorkedHours());

    }


}
