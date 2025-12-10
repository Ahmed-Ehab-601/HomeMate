package com.homemate.notification.service;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.dto.TaskResponse;
import com.homemate.notification.domains.exception.EmailTemplateException;
import com.homemate.notification.service.imp.EmailServiceImp;
import com.homemate.notification.service.utils.EmailTemplate;
import com.homemate.taskmanagement.dto.TaskDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

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
    private EmailServiceImp underTest;
    private static final String RECIPIENT_EMAIL ="someone@gmail.com";
    private static final String HOMEMATE_EMAIL ="homematesevice8@gmail.com";

    @BeforeEach
    void setUp() {
        underTest =new EmailServiceImp(emailTemplate, javaMailSender);
    }


    @Test
    void testSendUserEmailWithTaskAcceptedType() throws ExecutionException, InterruptedException {
        TaskDto task =TaskDto.builder().taskID(1L).build();
        EmailRequest emailRequest =EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.TASK_ACCEPTED)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Accepted");
        when(emailTemplate.buildTaskStatusChangedBody(task)).thenReturn("Your task has been accepted");
        TaskResponse response = underTest.sendUserEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        assertEquals("Email sent successfully", response.getMessage());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(emailTemplate).buildEmailSubject(emailRequest);
        verify(emailTemplate).buildTaskStatusChangedBody(task);
    }

    @Test
    void testSendUserEmailWithTaskRejectedType() throws ExecutionException, InterruptedException {
        TaskDto task =TaskDto.builder().taskID(2L).build();
        EmailRequest emailRequest =EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.TASK_REJECTED)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Rejected");
        when(emailTemplate.buildTaskStatusChangedBody(task)).thenReturn("Your task has been rejected");
        TaskResponse response = underTest.sendUserEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(emailTemplate).buildTaskStatusChangedBody(task);

    }

    @Test
    void testSendUserEmailWithTaskStatusType() throws ExecutionException, InterruptedException {
        TaskDto task =TaskDto.builder().taskID(3L).build();
        EmailRequest emailRequest =EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.TASK_STATUS)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Status Updated");
        when(emailTemplate.buildTaskStatusChangedBody(task)).thenReturn("Task status has changed");
        TaskResponse response = underTest.sendUserEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(emailTemplate).buildTaskStatusChangedBody(task);
    }

    @Test
    void testSendUserEmailWithTaskRescheduleType() throws ExecutionException, InterruptedException {
        TaskDto task =TaskDto.builder().taskID(4L).build();
        EmailRequest emailRequest =EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.TASK_RESCHEDULE)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();
        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Rescheduled");
        when(emailTemplate.buildUserTaskRescheduleBody(task)).thenReturn("Your task has been rescheduled");
        TaskResponse response = underTest.sendUserEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(emailTemplate).buildUserTaskRescheduleBody(task);
    }

    @Test
    void testSendUserEmailWithTaskResumedType() throws ExecutionException, InterruptedException {
        TaskDto task =TaskDto.builder().taskID(5L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.TASK_RESUMED)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();
        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Resumed");
        when(emailTemplate.BuildTaskResumedBody(task)).thenReturn("Your task has been resumed");
        TaskResponse response = underTest.sendUserEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(emailTemplate).BuildTaskResumedBody(task);
    }

    @Test
    void testSendUserEmailWithUnsupportedType() throws ExecutionException, InterruptedException {
        TaskDto task =TaskDto.builder().taskID(6L).build();
        EmailRequest emailRequest =EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.EMAIL_VERIFICATION)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();
        TaskResponse response = underTest.sendUserEmail(emailRequest).get();
        assertFalse(response.isSuccess());
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendUserEmailContainsCorrectFromAddress() {
        TaskDto task =TaskDto.builder().taskID(7L).build();
        EmailRequest emailRequest =EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.TASK_ACCEPTED)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Accepted");
        when(emailTemplate.buildTaskStatusChangedBody(task)).thenReturn("Your task has been accepted");
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        underTest.sendUserEmail(emailRequest);
        verify(javaMailSender).send(captor.capture());
        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals(HOMEMATE_EMAIL, sentMessage.getFrom());
    }

    @Test
    void testSendUserEmailContainsCorrectToAddress() {
        TaskDto task = TaskDto.builder().taskID(8L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.TASK_ACCEPTED)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();
        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Accepted");
        when(emailTemplate.buildTaskStatusChangedBody(task)).thenReturn("Your task has been accepted");
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        underTest.sendUserEmail(emailRequest);
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
                .build();
        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("New Task Request");
        when(emailTemplate.buildTaskRequestBody(task)).thenReturn("You have a new task request");

        TaskResponse response = underTest.sendTaskerEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(emailTemplate).buildTaskRequestBody(task);
    }

    @Test
    void testSendTaskerEmailWithTaskRescheduleType() throws ExecutionException, InterruptedException {

        TaskDto task = TaskDto.builder().taskID(11L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.TASK_RESCHEDULE)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Rescheduled");
        when(emailTemplate.buildTaskerTaskRescheduleBody(task)).thenReturn("A task has been rescheduled");
        TaskResponse response = underTest.sendTaskerEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(emailTemplate).buildTaskerTaskRescheduleBody(task);
    }

    @Test
    void testSendTaskerEmailWithUnsupportedType() throws ExecutionException, InterruptedException {

        TaskDto task = TaskDto.builder().taskID(12L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.TASK_ACCEPTED)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();

        TaskResponse response = underTest.sendTaskerEmail(emailRequest).get();
        assertFalse(response.isSuccess());
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendTaskerEmailContainsCorrectSubject() {

        TaskDto task = TaskDto.builder().taskID(13L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.TASK_REQUEST)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();

        String expectedSubject = "New Task Request";
        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn(expectedSubject);
        when(emailTemplate.buildTaskRequestBody(task)).thenReturn("Task details");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        underTest.sendTaskerEmail(emailRequest);

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
                .build();

        String expectedBody = "You have a new task request";
        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("New Task Request");
        when(emailTemplate.buildTaskRequestBody(task)).thenReturn(expectedBody);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        underTest.sendTaskerEmail(emailRequest);
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
                .build();

        TaskDto task2 =TaskDto.builder().taskID(16L).build();
        EmailRequest emailRequest2 = EmailRequest.builder()
                .task(task2)
                .emailType(EmailRequest.EmailType.TASK_REQUEST)
                .recipientEmail("user2@gmail.com")
                .build();

        when(emailTemplate.buildEmailSubject(any())).thenReturn("Task Request");
        when(emailTemplate.buildTaskRequestBody(any())).thenReturn("Task details");

        underTest.sendTaskerEmail(emailRequest1);
        underTest.sendTaskerEmail(emailRequest2);

        verify(javaMailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testEmailServiceHandlesNullTask() throws ExecutionException, InterruptedException {
        EmailRequest emailRequest =EmailRequest.builder()
                .task(null)
                .emailType(EmailRequest.EmailType.TASK_REQUEST)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Request");
        when(emailTemplate.buildTaskRequestBody(null)).thenReturn("Task details");
        TaskResponse response = underTest.sendTaskerEmail(emailRequest).get();
        assertTrue(response.isSuccess());
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendUserEmailHandlesMailException() throws ExecutionException, InterruptedException {
        TaskDto task = TaskDto.builder().taskID(25L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.TASK_ACCEPTED)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();

        when(emailTemplate.buildEmailSubject(emailRequest)).thenReturn("Task Accepted");
        when(emailTemplate.buildTaskStatusChangedBody(task)).thenReturn("Your task has been accepted");
        doThrow(new MailSendException("SMTP connection failed"))
                .when(javaMailSender)
                .send(any(SimpleMailMessage.class));
        TaskResponse response = underTest.sendUserEmail(emailRequest).get();
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
                .build();

      when(emailTemplate.buildEmailSubject(emailRequest))
                .thenReturn("New Task Request");

        when(emailTemplate.buildTaskRequestBody(task))
                .thenThrow(new EmailTemplateException(
                        "Required field 'taskerName' is null or empty for TASK_REQUEST",
                        "TASK_REQUEST",
                        "taskerName"
                ));

        TaskResponse response =underTest.sendTaskerEmail(emailRequest).get();
        assertFalse(response.isSuccess());
        String expectedMsg=String.format("Template: %s, Missing: %s, Reason: %s",
                "TASK_REQUEST",
                "taskerName",
               "Required field 'taskerName' is null or empty for TASK_REQUEST");

        assertEquals(expectedMsg, response.getMessage());

    }

    @Test
    void testSendUserEmailLogsErrorForUnsupportedType() throws ExecutionException, InterruptedException {
        TaskDto task =TaskDto.builder().taskID(20L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.FORGOT_PASSWORD)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();
        TaskResponse response = underTest.sendUserEmail(emailRequest).get();
        assertFalse(response.isSuccess());
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendTaskerEmailLogsErrorForUnsupportedType() throws ExecutionException, InterruptedException {
        TaskDto task = TaskDto.builder().taskID(21L).build();
        EmailRequest emailRequest = EmailRequest.builder()
                .task(task)
                .emailType(EmailRequest.EmailType.EMAIL_VERIFICATION)
                .recipientEmail(RECIPIENT_EMAIL)
                .build();

        TaskResponse response = underTest.sendTaskerEmail(emailRequest).get();
        assertFalse(response.isSuccess());
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }
}
