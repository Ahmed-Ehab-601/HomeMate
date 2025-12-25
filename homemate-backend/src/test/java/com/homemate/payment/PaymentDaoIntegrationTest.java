package com.homemate.payment;


import com.homemate.payment.dao.PaymentDao;
import com.homemate.payment.model.Payment;
import com.homemate.taskmanagement.exceptions.BadStateUpdateException;
import com.homemate.taskmanagement.exceptions.BadTaskRequestException;
import com.homemate.taskmanagement.exceptions.TaskNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("task")
public class PaymentDaoIntegrationTest {

    private final PaymentDao underTest;

    @Autowired
    public PaymentDaoIntegrationTest(PaymentDao underTest) {
        this.underTest = underTest;
    }

    @Test
    void testCreatePaymentSuccessfully() {
        Payment payment = new Payment();
        payment.setTaskId(1L);
        payment.setUserId(1L);
        payment.setTaskerId(1L);
        payment.setTotalAmount(100.0);
        payment.setPlatformFee(10.0);
        payment.setTaskerAmount(90.0);

        Optional<Long> paymentId = underTest.create(payment);

        assertThat(paymentId).isPresent();
        assertThat(paymentId.get()).isGreaterThan(0L);
    }


    @Test
    void testAttachStripeIntentSuccessfully() {
        Payment payment = new Payment();
        payment.setTaskId(1L);
        payment.setUserId(2L);
        payment.setTaskerId(3L);
        payment.setTotalAmount(100.0);
        payment.setPlatformFee(10.0);
        payment.setTaskerAmount(90.0);

        Optional<Long> paymentId = underTest.create(payment);
        assertThat(paymentId).isPresent();

        underTest.attachStripeIntent(paymentId.get(), "pi_123456");

        Payment retrieved = underTest.getById(paymentId.get());
        assertThat(retrieved.getStripePaymentIntentId()).isEqualTo("pi_123456");
        assertThat(retrieved.getStatus()).isEqualTo("REQUIRES_PAYMENT");
    }

    @Test
    void testUpdateStatusSuccessfully() {
        Payment payment = new Payment();
        payment.setTaskId(1L);
        payment.setUserId(2L);
        payment.setTaskerId(3L);
        payment.setTotalAmount(100.0);
        payment.setPlatformFee(10.0);
        payment.setTaskerAmount(90.0);

        Optional<Long> paymentId = underTest.create(payment);
        assertThat(paymentId).isPresent();

        underTest.updateStatus(paymentId.get(), "PAID");

        Payment retrieved = underTest.getById(paymentId.get());
        assertThat(retrieved.getStatus()).isEqualTo("PAID");
    }

    @Test
    void testSetPaidAtSuccessfully() {
        Payment payment = new Payment();
        payment.setTaskId(1L);
        payment.setUserId(2L);
        payment.setTaskerId(3L);
        payment.setTotalAmount(100.0);
        payment.setPlatformFee(10.0);
        payment.setTaskerAmount(90.0);

        Optional<Long> paymentId = underTest.create(payment);
        assertThat(paymentId).isPresent();

        underTest.setPaidAt(paymentId.get());

        Payment retrieved = underTest.getById(paymentId.get());
        assertNotNull(retrieved.getPaidAt());
    }

    @Test
    void testMarkPaidSuccessfully() {
        Payment payment = new Payment();
        payment.setTaskId(1L);
        payment.setUserId(2L);
        payment.setTaskerId(3L);
        payment.setTotalAmount(100.0);
        payment.setPlatformFee(10.0);
        payment.setTaskerAmount(90.0);

        Optional<Long> paymentId = underTest.create(payment);
        assertThat(paymentId).isPresent();

        underTest.markPaid(paymentId.get());

        Payment retrieved = underTest.getById(paymentId.get());
        assertThat(retrieved.getStatus()).isEqualTo("PAID");
        assertNotNull(retrieved.getPaidAt());
    }


    @Test
    void testGetByStripeIntentIdThrowsExceptionForNonExistent() {
        assertThrows(RuntimeException.class, () ->
                underTest.getByStripeIntentId("pi_nonexistent")
        );
    }

    @Test
    void testGetByIdSuccessfully() {
        Payment payment = new Payment();
        payment.setTaskId(1L);
        payment.setUserId(2L);
        payment.setTaskerId(3L);
        payment.setTotalAmount(100.0);
        payment.setPlatformFee(10.0);
        payment.setTaskerAmount(90.0);

        Optional<Long> paymentId = underTest.create(payment);
        assertThat(paymentId).isPresent();

        Payment retrieved = underTest.getById(paymentId.get());
        assertNotNull(retrieved);
        assertThat(retrieved.getId()).isEqualTo(paymentId.get());
        assertThat(retrieved.getTotalAmount()).isEqualTo(100.0);
    }

    @Test
    void testGetByIdThrowsExceptionForNonExistent() {
        assertThrows(RuntimeException.class, () ->
                underTest.getById(99999L)
        );
    }


    @Test
    void testGetTaskerStripeAccountIdThrowsExceptionForNonExistent() {
        assertThrows(BadStateUpdateException.class, () ->
                underTest.getTaskerStripeAccountId(99999L)
        );
    }

    @Test
    void testGetTaskerIDSuccessfully() {
        Long taskId = 1L;

        Long taskerId = underTest.getTaskerID(taskId);

        assertNotNull(taskerId);
        assertThat(taskerId).isGreaterThan(0L);
    }

    @Test
    void testGetTaskerIDThrowsExceptionForNonExistent() {
        assertThrows(TaskNotFoundException.class, () ->
                underTest.getTaskerID(99999L)
        );
    }

    @Test
    void testGetUserIDSuccessfully() {
        Long taskId = 1L;

        Long userId = underTest.getUserID(taskId);

        assertNotNull(userId);
        assertThat(userId).isGreaterThan(0L);
    }

    @Test
    void testGetUserIDThrowsExceptionForNonExistent() {
        assertThrows(TaskNotFoundException.class, () ->
                underTest.getUserID(99999L)
        );
    }

    @Test
    void testGetTaskBillSuccessfully() {
        Long taskId = 1L;

        Double bill = underTest.getTaskBill(taskId);

        assertNotNull(bill);
        assertThat(bill).isGreaterThan(0.0);
    }

    @Test
    void testGetTaskBillThrowsExceptionForNonExistent() {
        assertThrows(TaskNotFoundException.class, () ->
                underTest.getTaskBill(99999L)
        );
    }
}
