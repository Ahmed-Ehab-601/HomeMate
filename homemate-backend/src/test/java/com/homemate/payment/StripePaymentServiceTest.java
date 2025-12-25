package com.homemate.payment;

import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.TaskerProfile.models.Tasker;
import com.homemate.payment.dao.PaymentDao;
import com.homemate.payment.dto.PaymentRequestDTO;
import com.homemate.payment.dto.PaymentResponseDTO;
import com.homemate.payment.exceptions.PaymentException;
import com.homemate.payment.mapper.PaymentMapper;
import com.homemate.payment.model.Payment;
import com.homemate.payment.service.StripeAccountService;
import com.homemate.payment.service.StripePaymentService;
import com.homemate.taskmanagement.dao.GetTasksDao;
import com.homemate.taskmanagement.dao.TaskRequestDao;
import com.homemate.taskmanagement.dao.TaskStatusDao;
import com.homemate.taskmanagement.dto.TaskDto;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StripePaymentServiceTest {

    @Mock
    private PaymentDao paymentDao;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private TaskStatusDao taskStatusDao;
    @Mock
    private TaskRequestDao taskDao;
    @Mock
    private TaskerDao taskerDao;
    @Mock
    private StripeAccountService stripeAccountService;

    @InjectMocks
    private StripePaymentService stripePaymentService;

    @Test
    void testGenerateOnboardingLinkSuccess() {
        Long taskerId = 1L;
        Tasker tasker = new Tasker();
        tasker.setStripeAccountId("acct_123");

        when(taskerDao.getByID(taskerId)).thenReturn(tasker);
        when(stripeAccountService.generateOnboardingLink("acct_123"))
                .thenReturn("https://onboarding.url");

        String url = stripePaymentService.generateOnboardingLink(taskerId);

        assertEquals("https://onboarding.url", url);
    }

    @Test
    void testGenerateOnboardingLinkTaskerNotFound() {
        Long taskerId = 1L;

        when(taskerDao.getByID(taskerId)).thenReturn(null);

        assertThrows(PaymentException.class, () ->
                stripePaymentService.generateOnboardingLink(taskerId)
        );
    }
    @Test
    void testCreatePaymentSuccess() throws StripeException {
        PaymentRequestDTO requestDTO = new PaymentRequestDTO();
        requestDTO.setTaskId(1L);
        requestDTO.setTaskerId(2L);

        Payment payment = new Payment();
        payment.setTotalAmount(100.0);
        payment.setPlatformFee(10.0);

        PaymentIntent mockIntent = new PaymentIntent();
        mockIntent.setId("pi_123");

        when(paymentMapper.getPaymnetEntity(requestDTO)).thenReturn(payment);
        when(paymentDao.create(payment)).thenReturn(Optional.of(1L));
        when(paymentDao.getTaskerStripeAccountId(2L)).thenReturn("acct_123");
        doNothing().when(paymentDao).attachStripeIntent(1L, "pi_123");

        try (MockedStatic<PaymentIntent> mockedIntent = mockStatic(PaymentIntent.class)) {
            mockedIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenReturn(mockIntent);

            Payment result = stripePaymentService.createPayment(requestDTO);

            assertNotNull(result);
            assertEquals("pi_123", result.getStripePaymentIntentId());
            assertEquals("REQUIRES_PAYMENT", result.getStatus());
            verify(paymentDao).attachStripeIntent(1L, "pi_123");
        }
    }
    @Test
    void testCreatePaymentFailsWhenPaymentRecordNotCreated() {
        PaymentRequestDTO requestDTO = new PaymentRequestDTO();
        requestDTO.setTaskId(1L);
        requestDTO.setTaskerId(2L);

        Payment payment = new Payment();

        when(paymentMapper.getPaymnetEntity(requestDTO)).thenReturn(payment);
        when(paymentDao.create(payment)).thenReturn(Optional.empty());

        assertThrows(PaymentException.class, () ->
                stripePaymentService.createPayment(requestDTO)
        );
    }

    @Test
    void testCreatePaymentFailsWhenTaskerHasNoStripeAccount() {
        PaymentRequestDTO requestDTO = new PaymentRequestDTO();
        requestDTO.setTaskId(1L);
        requestDTO.setTaskerId(2L);

        Payment payment = new Payment();

        when(paymentMapper.getPaymnetEntity(requestDTO)).thenReturn(payment);
        when(paymentDao.create(payment)).thenReturn(Optional.of(1L));
        when(paymentDao.getTaskerStripeAccountId(2L)).thenReturn(null);

        assertThrows(PaymentException.class, () ->
                stripePaymentService.createPayment(requestDTO)
        );
    }

    @Test
    void testVerifyAndConfirmPaymentSuccess() throws StripeException {
        String intentId = "pi_123";
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setTaskId(10L);
        payment.setTaskerId(2L);
        payment.setTaskerAmount(90.0);

        try (MockedStatic<PaymentIntent> mockedIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = new PaymentIntent();
            mockIntent.setStatus("succeeded");

            when(paymentDao.getByStripeIntentId(intentId)).thenReturn(payment);
            mockedIntent.when(() -> PaymentIntent.retrieve(intentId)).thenReturn(mockIntent);

            boolean result = stripePaymentService.verifyAndConfirmPayment(intentId);

            assertTrue(result);
            verify(paymentDao).updateStatus(1L, "PAID");
            verify(paymentDao).setPaidAt(1L);
            verify(taskStatusDao).setTaskAsPaid(10L);
            verify(taskStatusDao).updateTaskerTotalEarning(2L, 90.0);
        }
    }

    @Test
    void testVerifyAndConfirmPaymentFailsWhenPaymentNotFound() throws StripeException {
        String intentId = "pi_123";

        when(paymentDao.getByStripeIntentId(intentId)).thenReturn(null);

        boolean result = stripePaymentService.verifyAndConfirmPayment(intentId);

        assertFalse(result);
    }

    @Test
    void testVerifyAndConfirmPaymentFailsWhenNotSucceeded() throws StripeException {
        String intentId = "pi_123";
        Payment payment = new Payment();

        try (MockedStatic<PaymentIntent> mockedIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = new PaymentIntent();
            mockIntent.setStatus("pending");

            when(paymentDao.getByStripeIntentId(intentId)).thenReturn(payment);
            mockedIntent.when(() -> PaymentIntent.retrieve(intentId)).thenReturn(mockIntent);

            boolean result = stripePaymentService.verifyAndConfirmPayment(intentId);

            assertFalse(result);
        }
    }

    @Test
    void testGetPaymentStatusSuccess() {
        Long paymentId = 1L;
        Payment payment = new Payment();
        payment.setStatus("PAID");

        when(paymentDao.getById(paymentId)).thenReturn(payment);

        String status = stripePaymentService.getPaymentStatus(paymentId);

        assertEquals("PAID", status);
    }

    @Test
    void testGetPaymentStatusNotFound() {
        Long paymentId = 1L;

        when(paymentDao.getById(paymentId)).thenReturn(null);

        assertThrows(PaymentException.class, () ->
                stripePaymentService.getPaymentStatus(paymentId)
        );
    }

    @Test
    void testMarkTaskAsPaidCashSuccess() {
        Long taskId = 1L;
        Long taskerId = 2L;

        TaskDto taskDto = new TaskDto();
        taskDto.setTaskerID(2L);
        taskDto.setHourRate(50.0);
        taskDto.setWorkedHours(4.0);

        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        stripePaymentService.markTaskAsPaidCash(taskId, taskerId);

        verify(taskStatusDao).setTaskAsPaid(taskId);
        verify(taskStatusDao).updateTaskerTotalEarning(taskerId, 200.0);
    }

    @Test
    void testMarkTaskAsPaidCashTaskNotFound() {
        Long taskId = 1L;
        Long taskerId = 2L;

        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.empty());

        assertThrows(PaymentException.class, () ->
                stripePaymentService.markTaskAsPaidCash(taskId, taskerId)
        );
    }

    @Test
    void testMarkTaskAsPaidCashUnauthorized() {
        Long taskId = 1L;
        Long taskerId = 2L;

        TaskDto taskDto = new TaskDto();
        taskDto.setTaskerID(999L);

        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        assertThrows(PaymentException.class, () ->
                stripePaymentService.markTaskAsPaidCash(taskId, taskerId)
        );
    }

    @Test
    void testConfirmPaymentSuccess() throws StripeException {
        String intentId = "pi_123";
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setTaskId(10L);
        payment.setTaskerId(2L);
        payment.setTaskerAmount(90.0);

        try (MockedStatic<PaymentIntent> mockedIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = new PaymentIntent();
            mockIntent.setStatus("succeeded");

            when(paymentDao.getByStripeIntentId(intentId)).thenReturn(payment);
            mockedIntent.when(() -> PaymentIntent.retrieve(intentId)).thenReturn(mockIntent);

            stripePaymentService.confirmPayment(intentId);

            verify(paymentDao).updateStatus(1L, "PAID");
        }
    }

    @Test
    void testConfirmPaymentVerificationFails() throws StripeException {
        String intentId = "pi_123";

        when(paymentDao.getByStripeIntentId(intentId)).thenReturn(null);

        assertThrows(PaymentException.class, () ->
                stripePaymentService.confirmPayment(intentId)
        );
    }
}
