package com.homemate.notification.service;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.TaskResponse;
import com.homemate.notification.domains.exception.EmailTemplateException;
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
                .emailType(EmailRequest.EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(EmailRequest.RecipientType.USER)
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
        EmailRequest emailRequest =EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(EmailRequest.RecipientType.USER)
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
                .emailType(EmailRequest.EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(EmailRequest.RecipientType.USER)
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
        EmailRequest emailRequest =EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.TASK_RESCHEDULE)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(EmailRequest.RecipientType.USER)
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
                .emailType(EmailRequest.EmailType.TASK_RESUMED)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(EmailRequest.RecipientType.USER)
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
                                .emailType(EmailRequest.EmailType.EMAIL_VERIFICATION)
                                .recipientEmail(RECIPIENT_EMAIL)
                                .recipientType(EmailRequest.RecipientType.USER)
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
                .emailType(EmailRequest.EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(EmailRequest.RecipientType.USER)
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
                .emailType(EmailRequest.EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(EmailRequest.RecipientType.USER)
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
                .emailType(EmailRequest.EmailType.TASK_REQUEST)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(EmailRequest.RecipientType.TASKER)
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
                .emailType(EmailRequest.EmailType.TASK_RESCHEDULE)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(EmailRequest.RecipientType.TASKER)
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
                                .emailType(EmailRequest.EmailType.TASK_STATUS)
                                .recipientEmail(RECIPIENT_EMAIL)
                                .recipientType(EmailRequest.RecipientType.TASKER)
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
                .emailType(EmailRequest.EmailType.TASK_REQUEST)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(EmailRequest.RecipientType.TASKER)
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
                .emailType(EmailRequest.EmailType.TASK_REQUEST)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(EmailRequest.RecipientType.TASKER)
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
                .emailType(EmailRequest.EmailType.TASK_REQUEST)
                .recipientEmail("user1@gmail.com")
                .recipientType(EmailRequest.RecipientType.TASKER)
                .build();

        TaskDto task2 =TaskDto.builder().taskID(16L).build();
        EmailRequest emailRequest2 = EmailRequest.builder()
                .task(task2)
                .emailType(EmailRequest.EmailType.TASK_REQUEST)
                .recipientEmail("user2@gmail.com")
                .recipientType(EmailRequest.RecipientType.TASKER)
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
                .emailType(EmailRequest.EmailType.TASK_REQUEST)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(EmailRequest.RecipientType.TASKER)
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
                .emailType(EmailRequest.EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(EmailRequest.RecipientType.USER)
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
                .emailType(EmailRequest.EmailType.TASK_REQUEST)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(EmailRequest.RecipientType.TASKER)
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
                                .emailType(EmailRequest.EmailType.FORGOT_PASSWORD)
                                .recipientEmail(RECIPIENT_EMAIL)
                                .recipientType(EmailRequest.RecipientType.USER)
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
                                .emailType(EmailRequest.EmailType.EMAIL_VERIFICATION)
                                .recipientEmail(RECIPIENT_EMAIL)
                                .recipientType(EmailRequest.RecipientType.TASKER)
                                .build();

                when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Verify your email");
                when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Verification body");

                TaskResponse response = underTest.sendEmail(emailRequest).get();
                assertTrue(response.isSuccess());
                verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailWithNullRecipientEmail() throws ExecutionException, InterruptedException {
        TaskDto task = TaskDto.builder().taskID(22L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.TASK_STATUS)
                .recipientEmail(null)
                .recipientType(EmailRequest.RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Status");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Status body");

        TaskResponse response = underTest.sendEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailWithEmptyRecipientEmail() throws ExecutionException, InterruptedException {
        TaskDto task = TaskDto.builder().taskID(23L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.TASK_STATUS)
                .recipientEmail("")
                .recipientType(EmailRequest.RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Status");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Status body");

        TaskResponse response = underTest.sendEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailMultipleRecipientsInSequence() throws ExecutionException, InterruptedException {
        TaskDto task1 = TaskDto.builder().taskID(24L).status(Status.Accepted).build();
        EmailRequest emailRequest1 = EmailRequest.builder()
                .task(task1)
                .emailType(EmailRequest.EmailType.TASK_STATUS)
                .recipientEmail("user1@gmail.com")
                .recipientType(EmailRequest.RecipientType.USER)
                .build();

        TaskDto task2 = TaskDto.builder().taskID(25L).status(Status.Rejected).build();
        EmailRequest emailRequest2 = EmailRequest.builder()
                .task(task2)
                .emailType(EmailRequest.EmailType.TASK_STATUS)
                .recipientEmail("user2@gmail.com")
                .recipientType(EmailRequest.RecipientType.USER)
                .build();

        when(emailTemplate.buildEmailSubject(any())).thenReturn("Task Status");
        when(emailTemplate.buildEmailBody(any())).thenReturn("Status body");

        TaskResponse response1 = underTest.sendEmail(emailRequest1).get();
        TaskResponse response2 = underTest.sendEmail(emailRequest2).get();

        assertTrue(response1.isSuccess());
        assertTrue(response2.isSuccess());
        verify(javaMailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmailVerifiesCorrectEmailContent() {
        TaskDto task = TaskDto.builder().taskID(26L).status(Status.Accepted).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(EmailRequest.RecipientType.USER)
                .build();

        String expectedSubject = "Task Accepted";
        String expectedBody = "Your task has been accepted";

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn(expectedSubject);
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn(expectedBody);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        underTest.sendEmail(emailRequest);
        verify(javaMailSender).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals(expectedSubject, sentMessage.getSubject());
        assertEquals(expectedBody, sentMessage.getText());
    }

    @Test
    void testSendEmailCallsTemplateBuilderMethods() throws ExecutionException, InterruptedException {
        TaskDto task = TaskDto.builder().taskID(27L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.TASK_REQUEST)
                .recipientEmail(RECIPIENT_EMAIL)
                .recipientType(EmailRequest.RecipientType.TASKER)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("New Task Request");
        when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Task details");

        underTest.sendEmail(emailRequest).get();

        verify(emailTemplate, times(1)).buildEmailSubject(emailRequest);
        verify(emailTemplate, times(1)).buildEmailBody(emailRequest);
    }

    @Test
    void testSendEmailWithAllEmailTypes() throws ExecutionException, InterruptedException {
        EmailRequest.EmailType[] emailTypes = {
            EmailRequest.EmailType.TASK_STATUS,
            EmailRequest.EmailType.TASK_REQUEST,
            EmailRequest.EmailType.TASK_RESCHEDULE,
            EmailRequest.EmailType.TASK_RESUMED,
            EmailRequest.EmailType.EMAIL_VERIFICATION,
            EmailRequest.EmailType.FORGOT_PASSWORD
        };

        for (EmailRequest.EmailType emailType : emailTypes) {
            TaskDto task = TaskDto.builder().taskID(System.nanoTime()).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(emailType)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.USER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
            when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

            TaskResponse response = underTest.sendEmail(emailRequest).get();
            assertTrue(response.isSuccess());
        }

        verify(javaMailSender, times(emailTypes.length)).send(any(SimpleMailMessage.class));
    }
    

        @Test
        void testSendEmailHandlesRuntimeException() throws ExecutionException, InterruptedException {
            TaskDto task = TaskDto.builder().taskID(100L).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.TASK_STATUS)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.USER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest))
                    .thenThrow(new RuntimeException("Unexpected error"));

            TaskResponse response = underTest.sendEmail(emailRequest).get();

            assertFalse(response.isSuccess());
            assertTrue(response.getMessage().contains("Failed to process email request"));
        }

        @Test
        void testSendEmailHandlesNullPointerException() throws ExecutionException, InterruptedException {
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(null)
                    .emailType(EmailRequest.EmailType.TASK_STATUS)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.USER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest))
                    .thenThrow(new NullPointerException("Null value"));

            TaskResponse response = underTest.sendEmail(emailRequest).get();

            assertFalse(response.isSuccess());
            assertTrue(response.getMessage().contains("Failed to process email request"));
        }

        @Test
        void testSendEmailWithMailAuthenticationException() throws ExecutionException, InterruptedException {
            TaskDto task = TaskDto.builder().taskID(101L).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.TASK_REQUEST)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.TASKER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
            when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");
            doThrow(new MailSendException("Authentication failed"))
                    .when(javaMailSender).send(any(SimpleMailMessage.class));

            TaskResponse response = underTest.sendEmail(emailRequest).get();

            assertFalse(response.isSuccess());
            assertTrue(response.getMessage().contains("Failed to send email"));
        }

        @Test
        void testSendEmailWithTemplateExceptionContainsAllDetails() throws ExecutionException, InterruptedException {
            TaskDto task = TaskDto.builder().taskID(102L).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.TASK_REQUEST)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.TASKER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
            when(emailTemplate.buildEmailBody(emailRequest))
                    .thenThrow(new EmailTemplateException(
                            "Field validation failed",
                            "TASK_REQUEST",
                            "userName"
                    ));

            TaskResponse response = underTest.sendEmail(emailRequest).get();

            assertFalse(response.isSuccess());
            assertTrue(response.getMessage().contains("TASK_REQUEST"));
            assertTrue(response.getMessage().contains("userName"));
            assertTrue(response.getMessage().contains("Field validation failed"));
        }

        // Boundary Tests

        @Test
        void testSendEmailWithVeryLongRecipientEmail() throws ExecutionException, InterruptedException {
            String longEmail = "a".repeat(50) + "@" + "b".repeat(50) + ".com";
            TaskDto task = TaskDto.builder().taskID(103L).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.TASK_STATUS)
                    .recipientEmail(longEmail)
                    .recipientType(EmailRequest.RecipientType.USER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
            when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

            TaskResponse response = underTest.sendEmail(emailRequest).get();

            assertTrue(response.isSuccess());
            verify(javaMailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        void testSendEmailWithVeryLongSubject() throws ExecutionException, InterruptedException {
            String longSubject = "Subject ".repeat(100);
            TaskDto task = TaskDto.builder().taskID(104L).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.TASK_STATUS)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.USER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn(longSubject);
            when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            underTest.sendEmail(emailRequest);

            verify(javaMailSender).send(captor.capture());
            assertEquals(longSubject, captor.getValue().getSubject());
        }

        @Test
        void testSendEmailWithVeryLongBody() throws ExecutionException, InterruptedException {
            String longBody = "Body content ".repeat(1000);
            TaskDto task = TaskDto.builder().taskID(105L).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.TASK_STATUS)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.USER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
            when(emailTemplate.buildEmailBody(emailRequest)).thenReturn(longBody);

            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            underTest.sendEmail(emailRequest);

            verify(javaMailSender).send(captor.capture());
            assertEquals(longBody, captor.getValue().getText());
        }



        @Test
        void testSendEmailConcurrentCallsWithDifferentEmailTypes() throws ExecutionException, InterruptedException {
            EmailRequest request1 = EmailRequest.builder()
                    .task(TaskDto.builder().taskID(106L).build())
                    .emailType(EmailRequest.EmailType.TASK_STATUS)
                    .recipientEmail("user1@test.com")
                    .recipientType(EmailRequest.RecipientType.USER)
                    .build();

            EmailRequest request2 = EmailRequest.builder()
                    .task(TaskDto.builder().taskID(107L).build())
                    .emailType(EmailRequest.EmailType.TASK_REQUEST)
                    .recipientEmail("user2@test.com")
                    .recipientType(EmailRequest.RecipientType.TASKER)
                    .build();

            when(emailTemplate.buildEmailSubject(any())).thenReturn("Subject");
            when(emailTemplate.buildEmailBody(any())).thenReturn("Body");

            TaskResponse response1 = underTest.sendEmail(request1).get();
            TaskResponse response2 = underTest.sendEmail(request2).get();

            assertTrue(response1.isSuccess());
            assertTrue(response2.isSuccess());
            verify(javaMailSender, times(2)).send(any(SimpleMailMessage.class));
        }


        @Test
        void testSendEmailWithSpecialCharactersInSubject() {
            TaskDto task = TaskDto.builder().taskID(108L).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.TASK_STATUS)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.USER)
                    .build();

            String specialSubject = "Task Update: €$£¥ & < > \" ' @";
            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn(specialSubject);
            when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            underTest.sendEmail(emailRequest);

            verify(javaMailSender).send(captor.capture());
            assertEquals(specialSubject, captor.getValue().getSubject());
        }

        @Test
        void testSendEmailWithUnicodeCharactersInBody() {
            TaskDto task = TaskDto.builder().taskID(109L).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.TASK_STATUS)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.USER)
                    .build();

            String unicodeBody = "Hello 你好 مرحبا שלום 안녕하세요 🎉";
            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
            when(emailTemplate.buildEmailBody(emailRequest)).thenReturn(unicodeBody);

            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            underTest.sendEmail(emailRequest);

            verify(javaMailSender).send(captor.capture());
            assertEquals(unicodeBody, captor.getValue().getText());
        }



        @Test
        void testSendEmailWithTaskResumedForTasker() throws ExecutionException, InterruptedException {
            TaskDto task = TaskDto.builder().taskID(110L).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.TASK_RESUMED)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.TASKER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Resumed");
            when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Task has resumed");

            TaskResponse response = underTest.sendEmail(emailRequest).get();

            assertTrue(response.isSuccess());
            verify(javaMailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        void testSendEmailWithForgotPasswordForTasker() throws ExecutionException, InterruptedException {
            TaskDto task = TaskDto.builder().taskID(111L).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.FORGOT_PASSWORD)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.TASKER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Reset Password");
            when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Reset your password");

            TaskResponse response = underTest.sendEmail(emailRequest).get();

            assertTrue(response.isSuccess());
            verify(javaMailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        void testSendEmailWithTaskStatusInReview() throws ExecutionException, InterruptedException {
            TaskDto task = TaskDto.builder().taskID(112L).status(Status.InReview).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.TASK_STATUS)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.USER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task In Review");
            when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Task is in review");

            TaskResponse response = underTest.sendEmail(emailRequest).get();

            assertTrue(response.isSuccess());
            verify(javaMailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        void testSendEmailWithTaskStatusDone() throws ExecutionException, InterruptedException {
            TaskDto task = TaskDto.builder().taskID(113L).status(Status.Done).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.TASK_STATUS)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.USER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Done");
            when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Task is done");

            TaskResponse response = underTest.sendEmail(emailRequest).get();

            assertTrue(response.isSuccess());
            verify(javaMailSender).send(any(SimpleMailMessage.class));
        }

        // Verification Tests for Method Calls

        @Test
        void testSendEmailInvokesTemplateMethodsInCorrectOrder() throws ExecutionException, InterruptedException {
            TaskDto task = TaskDto.builder().taskID(114L).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.TASK_REQUEST)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.TASKER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
            when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

            underTest.sendEmail(emailRequest).get();

            var inOrder = inOrder(emailTemplate, javaMailSender);
            inOrder.verify(emailTemplate).buildEmailSubject(emailRequest);
            inOrder.verify(emailTemplate).buildEmailBody(emailRequest);
            inOrder.verify(javaMailSender).send(any(SimpleMailMessage.class));
        }

        @Test
        void testSendEmailDoesNotCallMailSenderWhenSubjectBuildFails() throws ExecutionException, InterruptedException {
            TaskDto task = TaskDto.builder().taskID(115L).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.TASK_REQUEST)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.TASKER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest))
                    .thenThrow(new EmailTemplateException("Error", "TYPE", "field"));

            underTest.sendEmail(emailRequest).get();

            verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
        }

        @Test
        void testSendEmailDoesNotCallMailSenderWhenBodyBuildFails() throws ExecutionException, InterruptedException {
            TaskDto task = TaskDto.builder().taskID(116L).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.TASK_REQUEST)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.TASKER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
            when(emailTemplate.buildEmailBody(emailRequest))
                    .thenThrow(new EmailTemplateException("Error", "TYPE", "field"));

            underTest.sendEmail(emailRequest).get();

            verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
        }

        // Additional MailException Variants

        @Test
        void testSendEmailHandlesMailParseException() throws ExecutionException, InterruptedException {
            TaskDto task = TaskDto.builder().taskID(117L).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.TASK_STATUS)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.USER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
            when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");
            doThrow(new MailSendException("Invalid email address"))
                    .when(javaMailSender).send(any(SimpleMailMessage.class));

            TaskResponse response = underTest.sendEmail(emailRequest).get();

            assertFalse(response.isSuccess());
            assertTrue(response.getMessage().contains("Failed to send email"));
        }

        @Test
        void testSendEmailReturnsCorrectErrorMessageOnMailException() throws ExecutionException, InterruptedException {
            TaskDto task = TaskDto.builder().taskID(118L).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.TASK_STATUS)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.USER)
                    .build();

            String errorMessage = "Connection timeout";
            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
            when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");
            doThrow(new MailSendException(errorMessage))
                    .when(javaMailSender).send(any(SimpleMailMessage.class));

            TaskResponse response = underTest.sendEmail(emailRequest).get();

            assertFalse(response.isSuccess());
            assertTrue(response.getMessage().contains(errorMessage));
        }

        // Response Validation Tests

        @Test
        void testSendEmailResponseHasCorrectSuccessFlag() throws ExecutionException, InterruptedException {
            TaskDto task = TaskDto.builder().taskID(119L).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.TASK_STATUS)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.USER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
            when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");

            TaskResponse response = underTest.sendEmail(emailRequest).get();

            assertNotNull(response);
            assertTrue(response.isSuccess());
            assertNotNull(response.getMessage());
        }

        @Test
        void testSendEmailResponseHasCorrectFailureFlag() throws ExecutionException, InterruptedException {
            TaskDto task = TaskDto.builder().taskID(120L).build();
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(task)
                    .emailType(EmailRequest.EmailType.TASK_STATUS)
                    .recipientEmail(RECIPIENT_EMAIL)
                    .recipientType(EmailRequest.RecipientType.USER)
                    .build();

            when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Subject");
            when(emailTemplate.buildEmailBody(emailRequest)).thenReturn("Body");
            doThrow(new MailSendException("Error"))
                    .when(javaMailSender).send(any(SimpleMailMessage.class));

            TaskResponse response = underTest.sendEmail(emailRequest).get();

            assertNotNull(response);
            assertFalse(response.isSuccess());
            assertNotNull(response.getMessage());
        }
    }

