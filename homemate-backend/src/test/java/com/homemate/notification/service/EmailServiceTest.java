package com.homemate.notification.service;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.TaskResponse;
import com.homemate.notification.domains.exception.EmailTemplateException;
import com.homemate.notification.domains.model.EmailType;
import com.homemate.notification.domains.model.RecipientType;
import com.homemate.notification.service.impl.EmailServiceImpl;
import com.homemate.notification.service.utils.EmailTemplate;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Mock
    private EmailTemplate emailTemplate;

    @Mock
    private JavaMailSender javaMailSender;
    private EmailServiceImpl underTest;
    private static final String RECIPIENT_EMAIL ="someone@gmail.com";
        private static final String HOMEMATE_EMAIL ="homemateservice8@gmail.com";

    @BeforeEach
    void setUp() {
        underTest =new EmailServiceImpl(emailTemplate, javaMailSender);
                ReflectionTestUtils.setField(underTest, "fromEmail", HOMEMATE_EMAIL);
    }


    @Test
    void testSendUserEmailWithTaskAcceptedType() throws ExecutionException, InterruptedException {
        TaskDto task =TaskDto.builder().taskID(1L).status(Status.Accepted).build();
        EmailRequest emailRequest =EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Accepted");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Your task has been accepted");
        TaskResponse response = underTest.sendEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        assertEquals("Email sent successfully", response.getMessage());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(emailTemplate).buildEmailSubject(emailRequest);
        verify(emailTemplate).buildEmailBody(emailRequest);
    }

    @Test
    void testSendUserEmailWithTaskRejectedType() throws ExecutionException, InterruptedException {
        TaskDto task =TaskDto.builder().taskID(2L).status(Status.Rejected).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Rejected");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Your task has been rejected");
        TaskResponse response = underTest.sendEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(emailTemplate).buildEmailBody(emailRequest);

    }

    @Test
    void testSendUserEmailWithTaskStatusType() throws ExecutionException, InterruptedException {
        TaskDto task =TaskDto.builder().taskID(3L).build();
        EmailRequest emailRequest =EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Status Updated");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Task status has changed");
        TaskResponse response = underTest.sendEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(emailTemplate).buildEmailBody(emailRequest);
    }

    @Test
    void testSendUserEmailWithTaskRescheduleType() throws ExecutionException, InterruptedException {
        TaskDto task =TaskDto.builder().taskID(4L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_RESCHEDULE)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();
        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Rescheduled");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Your task has been rescheduled");
        TaskResponse response = underTest.sendEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(emailTemplate).buildEmailBody(emailRequest);
    }

    @Test
    void testSendUserEmailWithTaskResumedType() throws ExecutionException, InterruptedException {
        TaskDto task =TaskDto.builder().taskID(5L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_RESUMED)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();
        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Resumed");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Your task has been resumed");
        TaskResponse response = underTest.sendEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(emailTemplate).buildEmailBody(emailRequest);
    }

    @Test
        void testSendUserEmailWithSupportedVerificationType() throws ExecutionException, InterruptedException {
                TaskDto task =TaskDto.builder().taskID(6L).build();
                EmailRequest emailRequest =EmailRequest.builder()
                                .task(task)
                                .emailType(EmailType.EMAIL_VERIFICATION)
                                .recipientEmail(RECIPIENT_EMAIL)
                                .recipientType(RecipientType.USER)
                                .build();

                when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Verify your email");
                when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Verification body");

                TaskResponse response = underTest.sendEmail(emailRequest).get();
                assertTrue(response.isSuccess());
                verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendUserEmailContainsCorrectFromAddress() {
        TaskDto task = TaskDto.builder().taskID(7L).status(Status.Accepted). build();
        EmailRequest emailRequest =EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Accepted");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Your task has been accepted");
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        underTest.sendEmail(emailRequest);
        verify(javaMailSender).send(captor.capture());
        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals(HOMEMATE_EMAIL, sentMessage.getFrom());
    }

    @Test
    void testSendUserEmailContainsCorrectToAddress() {
        TaskDto task = TaskDto.builder().taskID(8L).status(Status.Accepted).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();
        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Accepted");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Your task has been accepted");
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        underTest.sendEmail(emailRequest);
        verify(javaMailSender).send(captor.capture());
        SimpleMailMessage sentMessage = captor.getValue();
        assertArrayEquals(new String[]{RECIPIENT_EMAIL}, sentMessage.getTo());
    }


    @Test
    void testSendTaskerEmailWithTaskRequestType() throws ExecutionException, InterruptedException {

        TaskDto task = TaskDto.builder().taskID(10L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_REQUEST)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.TASKER)
                .build();
        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("New Task Request");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("You have a new task request");

        TaskResponse response = underTest.sendEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(emailTemplate).buildEmailBody(emailRequest);
    }

    @Test
    void testSendTaskerEmailWithTaskRescheduleType() throws ExecutionException, InterruptedException {

        TaskDto task = TaskDto.builder().taskID(11L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_RESCHEDULE)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.TASKER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Rescheduled");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("A task has been rescheduled");
        TaskResponse response = underTest.sendEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(emailTemplate).buildEmailBody(emailRequest);
    }

    @Test
        void testSendTaskerEmailWithSupportedTaskStatusType() throws ExecutionException, InterruptedException {

                TaskDto task = TaskDto.builder().taskID(12L).status(Status.Accepted).build();
                EmailRequest emailRequest = EmailRequest.builder()
                                .task(task)
                                .emailType(EmailType.TASK_STATUS)
                                .recipientEmail(RECIPIENT_EMAIL)
                                .recipientType(RecipientType.TASKER)
                                .build();

                when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task status update");
                when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Status body");

                TaskResponse response = underTest.sendEmail(emailRequest).get();
                assertTrue(response.isSuccess());
                verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendTaskerEmailContainsCorrectSubject() {

        TaskDto task = TaskDto.builder().taskID(13L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_REQUEST)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.TASKER)
                .build();

        String expectedSubject = "New Task Request";
        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn(expectedSubject);
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Task details");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        underTest.sendEmail(emailRequest);

        verify(javaMailSender).send(captor.capture());
        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals(expectedSubject, sentMessage.getSubject());
    }

    @Test
    void testSendTaskerEmailContainsCorrectBody() {
        TaskDto task = TaskDto.builder().taskID(14L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_REQUEST)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.TASKER)
                .build();

        String expectedBody = "You have a new task request";
        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("New Task Request");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn(expectedBody);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        underTest.sendEmail(emailRequest);
        verify(javaMailSender).send(captor.capture());
        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals(expectedBody, sentMessage.getText());
    }

    @Test
    void testSendTaskerEmailWithMultipleCalls() {
        TaskDto task1 =TaskDto.builder().taskID(15L).build();
        EmailRequest emailRequest1 =EmailRequest.builder()
                .task(task1)
                .emailType(EmailType.TASK_REQUEST)
                .recipientEmail("user1@gmail.com")
                .recipientType(RecipientType.TASKER)
                .build();

        TaskDto task2 =TaskDto.builder().taskID(16L).build();
        EmailRequest emailRequest2 = EmailRequest.builder()
                .task(task2)
                .emailType(EmailType.TASK_REQUEST)
                .recipientEmail("user2@gmail.com")
                .recipientType(RecipientType.TASKER)
                .build();

        when(emailTemplate.buildEmailSubject(any())).thenReturn("Task Request");
        when(emailTemplate.buildEmailBody(any())).thenReturn("Task details");

        underTest.sendEmail(emailRequest1);
        underTest.sendEmail(emailRequest2);

        verify(javaMailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testEmailServiceHandlesNullTask() throws ExecutionException, InterruptedException {
        EmailRequest emailRequest =EmailRequest.builder()
                .task(null)
                .emailType(EmailType.TASK_REQUEST)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.TASKER)
                .build();
        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Request");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Task details");
        TaskResponse response = underTest.sendEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendUserEmailHandlesMailException() throws ExecutionException, InterruptedException {
        TaskDto task = TaskDto.builder().taskID(25L).status(Status.Accepted).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Accepted");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Your task has been accepted");
        doThrow(new MailSendException("SMTP connection failed"))
                .when(javaMailSender)
                .send(any(SimpleMailMessage.class));
        TaskResponse response = underTest.sendEmail(emailRequest).get();
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Failed to send email: " + "SMTP connection failed"));
    }

    @Test
    void testSendTaskerEmailHandlesTemplateException() throws ExecutionException, InterruptedException {
        TaskDto task =TaskDto.builder()
                .taskID(26L) // mising feids
                .build();

        EmailRequest emailRequest =EmailRequest.builder()
                .task(task)
                .emailType(EmailType.TASK_REQUEST)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(RecipientType.TASKER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest))
                .thenReturn("New Task Request");

        when(emailTemplate.buildEmailBody(emailRequest))
                .thenThrow(new EmailTemplateException(
                        "Required field 'taskerName' is null or empty for TASK_REQUEST",
                        "TASK_REQUEST",
                        "taskerName"
                ));

        TaskResponse response =underTest.sendEmail(emailRequest).get();
        assertFalse(response.isSuccess());
        String expectedMsg=String.format("Template: %s, Missing: %s, Reason: %s",
                "TASK_REQUEST",
                "taskerName",
               "Required field 'taskerName' is null or empty for TASK_REQUEST");

        assertEquals(expectedMsg, response.getMessage());

    }

    @Test
        void testSendUserEmailWithSupportedForgotPasswordType() throws ExecutionException, InterruptedException {
                TaskDto task =TaskDto.builder().taskID(20L).build();
                EmailRequest emailRequest = EmailRequest.builder()
                                .task(task)
                                .emailType(EmailType.FORGOT_PASSWORD)
                                .recipientEmail(RECIPIENT_EMAIL)
                                .recipientType(RecipientType.USER)
                                .build();

                when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Reset password");
                when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Reset body");

                TaskResponse response = underTest.sendEmail(emailRequest).get();
                assertTrue(response.isSuccess());
                verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
        void testSendTaskerEmailWithSupportedVerificationType() throws ExecutionException, InterruptedException {
                TaskDto task = TaskDto.builder().taskID(21L).build();
                EmailRequest emailRequest = EmailRequest.builder()
                                .task(task)
                                .emailType(EmailType.EMAIL_VERIFICATION)
                                .recipientEmail(RECIPIENT_EMAIL)
                                .recipientType(RecipientType.TASKER)
                                .build();

                when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Verify your email");
                when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Verification body");

                TaskResponse response = underTest.sendEmail(emailRequest).get();
                assertTrue(response.isSuccess());
                verify(javaMailSender).send(any(SimpleMailMessage.class));
    }
}
